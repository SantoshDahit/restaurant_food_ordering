<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ordersApi } from '@/api/orders'
import { orderItemApi } from '@/api/orderItem'
import { restaurantApi } from '@/api/restaurant'
import { toast } from 'vue-sonner'
import api from '@/api/axios'
import { fileApi } from '@/api/file'
import { UtensilsCrossed, ShoppingBag, Sparkles, Trash2, X, ArrowRight } from 'lucide-vue-next'
import {
  Button, ChipTabs, MenuItemCard, QuantityStepper, EmptyState, Spinner,
} from '@/components/ui'
import { formatNpr } from '@/utils/cn'
import type { MenuItemResponse, MenuCategoryResponse, PageResponse, RestaurantResponse } from '@/types'

const route = useRoute()
const router = useRouter()

const kioskCode = computed(() => route.params.kioskCode as string)
const restaurant = ref<RestaurantResponse | null>(null)
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

async function load() {
  try {
    loading.value = true
    const rest = await restaurantApi.getByKioskCode(kioskCode.value)
    const rCode = rest.code
    const [cats, its] = await Promise.all([
      api.get<PageResponse<MenuCategoryResponse>>('/menu-categories/search', { params: { restaurantCode: rCode, size: 50 } }).then(r => r.data),
      api.get<PageResponse<MenuItemResponse>>('/menu-items/search', { params: { restaurantCode: rCode, availability: 'AVAILABLE', size: 200 } }).then(r => r.data),
    ])
    restaurant.value = rest
    categories.value = cats.content
    items.value = its.content
    const codes = its.content.filter(i => i.fileCode).map(i => i.fileCode!)
    await Promise.all(codes.map(async (code) => {
      try {
        const f = await fileApi.get(code)
        fileUrlCache.value[code] = f.url
      } catch { /* silent */ }
    }))
  } catch {
    toast.error('Failed to load menu')
  } finally {
    loading.value = false
  }
}

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

async function checkout() {
  if (!cartItems.value.length) {
    toast.error('Please add items to your order'); return
  }
  if (!restaurant.value) {
    toast.error('Restaurant not loaded'); return
  }
  ordering.value = true
  try {
    const order = await ordersApi.create({
      restaurantCode: restaurant.value.code,
      orderType: 'KIOSK',
      deviceType: 'KIOSK',
    })
    for (const item of cartItems.value) {
      await orderItemApi.add(order.code, { menuItemCode: item.code, quantity: item.quantity })
    }
    router.push({ path: '/payment', query: { orderCode: order.code, restaurantCode: restaurant.value.code, source: 'kiosk', kioskCode: kioskCode.value } })
  } catch {
    toast.error('Checkout failed. Please try again.')
    ordering.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="min-h-screen md:h-screen bg-background flex flex-col md:overflow-hidden">
    <!-- Header — bold brand band for the self-serve kiosk -->
    <header class="relative overflow-hidden bg-gradient-brand text-primary-foreground px-4 sm:px-8 py-5 sm:py-6 flex-shrink-0">
      <div aria-hidden="true" class="absolute -top-12 -right-12 w-48 h-48 rounded-full bg-white/10 blur-2xl" />
      <div aria-hidden="true" class="absolute -bottom-16 left-1/3 w-56 h-56 rounded-full bg-white/10 blur-3xl" />

      <div class="relative flex items-center justify-between gap-3">
        <div class="flex items-center gap-3 min-w-0">
          <div class="w-12 h-12 rounded-2xl bg-white/15 backdrop-blur ring-1 ring-white/25 flex items-center justify-center flex-shrink-0">
            <UtensilsCrossed class="w-6 h-6 text-primary-foreground" />
          </div>
          <div class="min-w-0">
            <h1 class="text-xl sm:text-3xl font-semibold tracking-tight truncate">{{ restaurant?.name ?? 'Self Order Kiosk' }}</h1>
            <p class="text-primary-foreground/90 text-xs sm:text-sm flex items-center gap-1.5">
              <Sparkles class="w-3.5 h-3.5" />
              Touch any dish to add it to your order
            </p>
          </div>
        </div>
        <div class="text-right flex-shrink-0 bg-white/12 backdrop-blur ring-1 ring-white/20 rounded-2xl px-4 py-2">
          <p class="font-serif text-2xl sm:text-3xl font-semibold tabular-nums leading-none">{{ formatNpr(cartTotal) }}</p>
          <p class="text-primary-foreground/90 text-[11px] sm:text-xs mt-1">{{ cartCount }} item{{ cartCount === 1 ? '' : 's' }}</p>
        </div>
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

      <!-- Right: Cart (always visible on the kiosk) -->
      <aside class="w-full md:w-[23rem] bg-card border-t md:border-t-0 md:border-l border-border/70 flex flex-col md:flex-shrink-0">
        <header class="px-5 py-4 border-b border-border/70 flex items-center gap-3">
          <div class="w-9 h-9 rounded-lg bg-primary/10 flex items-center justify-center">
            <ShoppingBag class="w-4 h-4 text-primary" />
          </div>
          <div>
            <h2 class="text-base font-semibold text-foreground">Your order</h2>
            <p class="text-xs text-muted-foreground">{{ cartCount }} item{{ cartCount === 1 ? '' : 's' }}</p>
          </div>
        </header>

        <!-- Cart items -->
        <div class="md:flex-1 md:overflow-y-auto scrollbar-fine">
          <EmptyState
            v-if="cartItems.length === 0"
            :icon="ShoppingBag"
            title="Your cart is empty"
            description="Touch a dish to start your order."
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
        <div v-if="cartItems.length > 0" class="p-4 border-t border-border/70 space-y-3 bg-card">
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
          <Button block size="lg" :loading="ordering" @click="checkout">
            <span>{{ ordering ? 'Processing…' : 'Proceed to payment' }}</span>
            <ArrowRight v-if="!ordering" class="w-4 h-4" />
          </Button>
          <Button block variant="ghost" size="sm" @click="clearCart">
            <template #icon><Trash2 class="w-3.5 h-3.5" /></template>
            Clear order
          </Button>
        </div>
      </aside>
    </div>
  </div>
</template>
