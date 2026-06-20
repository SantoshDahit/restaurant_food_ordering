<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tableApi } from '@/api/table'
import { ordersApi } from '@/api/orders'
import { orderItemApi } from '@/api/orderItem'
import { toast } from 'vue-sonner'
import api from '@/api/axios'
import { fileApi } from '@/api/file'
import {
  UtensilsCrossed, ShoppingBag, Trash2, X, ArrowRight,
  AlertTriangle, ShoppingCart, Info,
} from 'lucide-vue-next'
import {
  Button, ChipTabs, MenuItemCard, QuantityStepper, EmptyState, Spinner,
} from '@/components/ui'
import { formatNpr } from '@/utils/cn'
import type { RestaurantTableResponse, MenuItemResponse, MenuCategoryResponse, PageResponse, OrderDetailResponse } from '@/types'

const route = useRoute()
const router = useRouter()

// Mobile-only: the cart opens as a bottom-sheet drawer (it's an always-visible
// sidebar on desktop).
const cartOpen = ref(false)

// Persist the cart per table/QR token (or order, when adding to an existing
// tab) so it survives a round-trip. Cleared once the order is placed/appended.
const cartStorageKey = computed(() => {
  const id = (route.params.token as string | undefined)
    ?? (route.params.tableCode as string | undefined)
    ?? (route.params.orderCode as string | undefined)
  return id ? `qr_cart_${id}` : null
})

const table = ref<RestaurantTableResponse | null>(null)
// When set, we're adding items to an existing open order (the table's tab)
// rather than creating a new one.
const appendOrder = ref<OrderDetailResponse | null>(null)
const categories = ref<MenuCategoryResponse[]>([])
const items = ref<MenuItemResponse[]>([])
const cart = ref<Record<string, number>>({})
const activeCategory = ref('All')
const loading = ref(true)
const ordering = ref(false)
const fileUrlCache = ref<Record<string, string>>({})

const categoryOptions = computed(() => [
  { value: 'All', label: 'All items' },
  ...categories.value.map(c => ({ value: c.code, label: c.name })),
])

const filteredItems = computed(() => {
  if (activeCategory.value === 'All') return items.value
  return items.value.filter(i => i.categoryCode === activeCategory.value)
})

const cartItems = computed(() =>
  Object.entries(cart.value)
    .map(([code, quantity]) => ({ ...items.value.find(i => i.code === code)!, quantity }))
    .filter(i => i.name)
)

const cartTotal = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
)

const cartCount = computed(() =>
  Object.values(cart.value).reduce((a, b) => a + b, 0)
)

const isAppendMode = computed(() => appendOrder.value != null)
const tableLabel = computed(() => appendOrder.value?.tableNumber ?? table.value?.tableNumber ?? '')

async function loadMenu(rCode: string) {
  const [cats, its] = await Promise.all([
    api.get<PageResponse<MenuCategoryResponse>>('/menu-categories/search', { params: { restaurantCode: rCode, size: 50 } }).then(r => r.data),
    api.get<PageResponse<MenuItemResponse>>('/menu-items/search', { params: { restaurantCode: rCode, availability: 'AVAILABLE', size: 200 } }).then(r => r.data),
  ])
  categories.value = cats.content
  items.value = its.content
  const codes = its.content.filter(i => i.fileCode).map(i => i.fileCode!)
  await Promise.all(codes.map(async (code) => {
    try {
      const f = await fileApi.get(code)
      fileUrlCache.value[code] = f.url
    } catch { /* silent */ }
  }))
}

async function load() {
  try {
    loading.value = true
    const token = route.params.token as string | undefined
    const tableCode = route.params.tableCode as string | undefined
    const orderCode = route.params.orderCode as string | undefined

    // Entry from the tracking page: add to a specific existing order.
    if (orderCode) {
      const detail = await ordersApi.getDetail(orderCode)
      if (detail.status === 'COMPLETED' || detail.status === 'CANCELLED') {
        toast.error('This order is already closed.')
        router.replace(`/track/${detail.orderNumber}`)
        return
      }
      appendOrder.value = detail
      await loadMenu(detail.restaurantCode)
      restoreCart()
      return
    }

    // Scan/visit a table.
    table.value = token
      ? await tableApi.getByToken(token)
      : await tableApi.getByTableCode(tableCode!)
    await loadMenu(table.value.restaurantCode)

    // Mark this tab as a shared restaurant tablet ONLY when launched with
    // ?shared=1 (the dashboard "Launch table mode" button). A customer who scans
    // the table QR (or a QR sticker) has no such flag, so they're treated as
    // being on their own phone. sessionStorage is per-tab, so it never leaks to
    // a customer's device. The payment screen reads this to pick methods.
    if (route.query.shared) {
      sessionStorage.setItem('sharedDevice', 'true')
    } else {
      sessionStorage.removeItem('sharedDevice')
    }

    // Occupied table → the QR is a shared tab: add to its open order instead of
    // creating a new one.
    if (table.value.status === 'OCCUPIED') {
      try {
        const active = await ordersApi.getActiveByRestaurant(table.value.restaurantCode)
        const mine = active.find(o => o.tableCode === table.value!.code)
        if (mine) appendOrder.value = await ordersApi.getDetail(mine.code)
      } catch { /* fall back to a new order */ }
    }
    restoreCart()
  } catch {
    toast.error('Invalid QR code or table not found')
  } finally {
    loading.value = false
  }
}

function restoreCart() {
  if (!cartStorageKey.value) return
  try {
    const saved = sessionStorage.getItem(cartStorageKey.value)
    if (saved) cart.value = JSON.parse(saved)
  } catch { /* ignore corrupt cart */ }
}

// Mirror every cart change into sessionStorage so it survives the payment round-trip.
watch(cart, (value) => {
  if (!cartStorageKey.value) return
  if (Object.keys(value).length) sessionStorage.setItem(cartStorageKey.value, JSON.stringify(value))
  else sessionStorage.removeItem(cartStorageKey.value)
}, { deep: true })

function addToCart(code: string) {
  cart.value = { ...cart.value, [code]: (cart.value[code] || 0) + 1 }
}

function removeFromCart(code: string) {
  const updated = { ...cart.value }
  if (updated[code] > 1) updated[code]--
  else delete updated[code]
  cart.value = updated
}

function deleteFromCart(code: string) {
  const updated = { ...cart.value }
  delete updated[code]
  cart.value = updated
}

// Bridge the QuantityStepper's numeric model back onto the cart map.
function setQty(code: string, value: number) {
  const current = cart.value[code] || 0
  if (value <= 0) deleteFromCart(code)
  else if (value > current) addToCart(code)
  else removeFromCart(code)
}

function clearCart() {
  cart.value = {}
}

function clearStoredCart() {
  if (cartStorageKey.value) sessionStorage.removeItem(cartStorageKey.value)
}

async function checkout() {
  if (!cartItems.value.length) {
    toast.error('Please add items to your order'); return
  }
  if (!isAppendMode.value && !table.value) {
    toast.error('Table not loaded'); return
  }
  ordering.value = true
  try {
    let orderNumber: string
    if (appendOrder.value) {
      // Add this round to the existing open tab.
      for (const item of cartItems.value) {
        await orderItemApi.add(appendOrder.value.code, { menuItemCode: item.code, quantity: item.quantity })
      }
      orderNumber = appendOrder.value.orderNumber
    } else {
      const order = await ordersApi.create({
        restaurantCode: table.value!.restaurantCode,
        tableCode: table.value!.code,
        orderType: 'QR_ORDER',
        deviceType: 'MOBILE',
      })
      for (const item of cartItems.value) {
        await orderItemApi.add(order.code, { menuItemCode: item.code, quantity: item.quantity })
      }
      orderNumber = order.orderNumber
    }
    // Dine-in pays at the end — go straight to live tracking, not payment.
    clearStoredCart()
    router.push(`/track/${orderNumber}`)
  } catch (e: any) {
    toast.error(e?.response?.data?.message ?? 'Failed to place order. Please try again.')
    ordering.value = false
  }
}

// Keep table availability live: when staff complete the active order the table
// frees (and vice-versa), so the ordering UI enables/blocks without a reload.
let statusTimer: ReturnType<typeof setInterval> | null = null

async function refreshTableStatus() {
  if (!table.value) return
  const token = route.params.token as string | undefined
  const tableCode = route.params.tableCode as string | undefined
  try {
    table.value = token
      ? await tableApi.getByToken(token)
      : await tableApi.getByTableCode(tableCode!)
  } catch { /* keep last good status on transient errors */ }
}

onMounted(() => {
  load()
  statusTimer = setInterval(refreshTableStatus, 5000)
})

onBeforeUnmount(() => {
  if (statusTimer) clearInterval(statusTimer)
})
</script>

<template>
  <div class="min-h-screen md:h-screen bg-background flex flex-col md:overflow-hidden">

    <!-- Invalid QR / table -->
    <div v-if="!loading && !table && !appendOrder" class="flex-1 flex items-center justify-center px-4">
      <div class="text-center max-w-sm">
        <div class="w-16 h-16 rounded-2xl bg-destructive/10 ring-1 ring-destructive/20 flex items-center justify-center mx-auto mb-4">
          <AlertTriangle class="w-7 h-7 text-destructive" />
        </div>
        <h2 class="text-foreground text-xl">Table not found</h2>
        <p class="text-sm text-muted-foreground mt-1.5">This QR code is invalid or expired. Please scan the code on your table again.</p>
      </div>
    </div>

    <template v-else>
      <!-- Header -->
      <header class="bg-card/90 backdrop-blur border-b border-border/70 px-4 sm:px-8 py-3.5 flex-shrink-0 sticky top-0 z-30">
        <div class="flex items-center justify-between gap-3">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-11 h-11 rounded-xl bg-primary/10 ring-1 ring-primary/15 flex items-center justify-center flex-shrink-0">
              <UtensilsCrossed class="w-5 h-5 text-primary" />
            </div>
            <div class="min-w-0">
              <h1 class="text-lg sm:text-xl font-semibold tracking-tight truncate text-foreground">
                {{ isAppendMode ? 'Add to your order' : tableLabel ? `Table ${tableLabel}` : 'Menu' }}
              </h1>
              <p class="text-xs sm:text-sm text-muted-foreground truncate">
                {{ isAppendMode ? `Adding to Table ${tableLabel}'s running tab` : 'Browse the menu and tap to add' }}
              </p>
            </div>
          </div>

          <!-- Mobile: opens the cart sheet. Desktop: passive summary (sidebar is visible). -->
          <button
            type="button"
            class="relative flex items-center gap-2.5 rounded-full border border-border bg-card pl-3 pr-4 py-1.5 transition-colors hover:bg-accent md:pointer-events-none md:hover:bg-card"
            @click="cartOpen = true"
          >
            <span class="relative md:hidden">
              <ShoppingCart class="w-5 h-5 text-foreground" />
              <span
                v-if="cartCount > 0"
                class="absolute -top-2 -right-2 bg-primary text-primary-foreground text-[10px] font-semibold rounded-full min-w-4 h-4 px-1 flex items-center justify-center"
              >{{ cartCount }}</span>
            </span>
            <span class="text-right leading-tight">
              <span class="block font-serif text-base font-semibold text-foreground tabular-nums">{{ formatNpr(cartTotal) }}</span>
              <span class="block text-[11px] text-muted-foreground">{{ cartCount }} item{{ cartCount === 1 ? '' : 's' }}</span>
            </span>
          </button>
        </div>
      </header>

      <!-- Loading -->
      <div v-if="loading" class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <Spinner size="lg" class="mx-auto mb-3" />
          <p class="text-muted-foreground text-sm">Loading menu…</p>
        </div>
      </div>

      <div v-else class="flex-1 flex flex-col md:flex-row md:overflow-hidden">
        <!-- Left: Menu -->
        <div class="flex-1 flex flex-col md:overflow-hidden min-w-0">
          <!-- Category chips -->
          <div class="bg-background/80 backdrop-blur border-b border-border/60 flex-shrink-0 px-3 sm:px-6 py-3">
            <ChipTabs v-model="activeCategory" :options="categoryOptions" />
          </div>

          <!-- Items grid -->
          <div class="flex-1 md:overflow-y-auto scrollbar-fine p-3 sm:p-6">
            <EmptyState
              v-if="filteredItems.length === 0"
              :icon="UtensilsCrossed"
              title="Nothing here yet"
              description="There are no items in this category right now."
            />
            <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 sm:gap-4">
              <MenuItemCard
                v-for="item in filteredItems"
                :key="item.code"
                :item="item"
                :image-url="item.fileCode ? fileUrlCache[item.fileCode] : undefined"
                :quantity="cart[item.code] || 0"
                @add="addToCart(item.code)"
              />
            </div>
          </div>
        </div>

        <!-- Mobile backdrop for the cart sheet -->
        <Transition
          enter-active-class="transition-opacity duration-200"
          leave-active-class="transition-opacity duration-200"
          enter-from-class="opacity-0"
          leave-to-class="opacity-0"
        >
          <div
            v-if="cartOpen"
            class="md:hidden fixed inset-0 bg-foreground/30 backdrop-blur-sm z-40"
            aria-hidden="true"
            @click="cartOpen = false"
          />
        </Transition>

        <!-- Cart: desktop sidebar / mobile bottom-sheet -->
        <aside
          :class="cartOpen ? 'flex' : 'hidden'"
          class="md:flex flex-col bg-card md:flex-shrink-0
                 fixed inset-x-0 bottom-0 z-50 max-h-[86vh] rounded-t-2xl shadow-sheet animate-slide-up
                 md:static md:inset-auto md:z-auto md:max-h-none md:h-auto md:w-[23rem] md:rounded-none md:shadow-none md:animate-none
                 border-t md:border-t-0 md:border-l border-border/70"
        >
          <!-- Grab handle (mobile) -->
          <div class="md:hidden flex justify-center pt-2.5 pb-1">
            <div class="h-1.5 w-10 rounded-full bg-border" />
          </div>

          <header class="px-5 py-4 border-b border-border/70 flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-primary/10 flex items-center justify-center">
              <ShoppingBag class="w-4 h-4 text-primary" />
            </div>
            <div class="flex-1 min-w-0">
              <h2 class="text-base font-semibold text-foreground">Your order</h2>
              <p class="text-xs text-muted-foreground">{{ cartCount }} item{{ cartCount === 1 ? '' : 's' }}</p>
            </div>
            <button
              type="button"
              class="md:hidden w-9 h-9 -mr-1.5 rounded-full text-muted-foreground hover:bg-accent flex items-center justify-center flex-shrink-0 transition-colors"
              aria-label="Close"
              @click="cartOpen = false"
            >
              <X class="w-5 h-5" />
            </button>
          </header>

          <!-- Items (scrolls within the panel) -->
          <div class="flex-1 overflow-y-auto scrollbar-fine min-h-0">
            <EmptyState
              v-if="cartItems.length === 0"
              :icon="ShoppingBag"
              title="Your cart is empty"
              description="Tap a dish to start your order."
              compact
            />
            <ul v-else class="p-3 space-y-2">
              <li
                v-for="item in cartItems"
                :key="item.code"
                class="rounded-xl border border-border/70 bg-background/60 p-3"
              >
                <div class="flex justify-between items-start gap-2 mb-3">
                  <div class="min-w-0 flex-1">
                    <p class="font-medium text-foreground text-sm leading-snug line-clamp-1">{{ item.name }}</p>
                    <p class="text-[11px] text-muted-foreground tabular-nums mt-0.5">{{ formatNpr(item.price) }} each</p>
                  </div>
                  <button
                    type="button"
                    class="w-7 h-7 -mr-1 -mt-1 rounded-full text-muted-foreground hover:text-destructive hover:bg-destructive/10 flex items-center justify-center transition-colors flex-shrink-0"
                    aria-label="Remove item"
                    @click="deleteFromCart(item.code)"
                  >
                    <X class="w-3.5 h-3.5" />
                  </button>
                </div>
                <div class="flex items-center justify-between">
                  <QuantityStepper
                    :model-value="item.quantity"
                    :min="0"
                    size="sm"
                    @update:model-value="(v) => setQty(item.code, v)"
                  />
                  <span class="font-semibold text-foreground text-sm tabular-nums">
                    {{ formatNpr(item.price * item.quantity) }}
                  </span>
                </div>
              </li>
            </ul>
          </div>

          <!-- Footer -->
          <div
            v-if="cartItems.length > 0"
            class="flex-shrink-0 p-4 pb-[max(1rem,env(safe-area-inset-bottom))] border-t border-border/70 space-y-3 bg-card"
          >
            <div class="rounded-xl bg-accent/60 p-3.5">
              <div class="flex justify-between text-sm text-muted-foreground">
                <span>Subtotal</span>
                <span class="tabular-nums">{{ formatNpr(cartTotal) }}</span>
              </div>
              <div class="flex justify-between items-end pt-2.5 mt-2.5 border-t border-border/70">
                <span class="font-medium text-foreground">Total</span>
                <span class="font-serif font-semibold text-foreground text-xl tabular-nums">{{ formatNpr(cartTotal) }}</span>
              </div>
            </div>

            <div
              v-if="isAppendMode"
              class="flex items-start gap-2 rounded-xl bg-info/10 ring-1 ring-info/20 px-3.5 py-3 text-info"
            >
              <Info class="w-4 h-4 mt-0.5 flex-shrink-0" />
              <p class="text-xs leading-snug">These items join your running tab and are paid together at the end.</p>
            </div>

            <Button block size="lg" :loading="ordering" @click="checkout">
              <span>{{ ordering ? 'Sending…' : isAppendMode ? 'Add to order' : 'Place order' }}</span>
              <ArrowRight v-if="!ordering" class="w-4 h-4" />
            </Button>
            <Button block variant="ghost" size="sm" @click="clearCart">
              <template #icon><Trash2 class="w-3.5 h-3.5" /></template>
              Clear order
            </Button>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>
