<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tableApi } from '@/api/table'
import { ordersApi } from '@/api/orders'
import { orderItemApi } from '@/api/orderItem'
import { toast } from 'vue-sonner'
import api from '@/api/axios'
import { fileApi } from '@/api/file'
import {
  UtensilsCrossed, ShoppingBag, Sparkles, Trash2, Minus, Plus,
  X, ArrowRight, Loader2, AlertTriangle,
} from 'lucide-vue-next'
import type { RestaurantTableResponse, MenuItemResponse, MenuCategoryResponse, PageResponse } from '@/types'

const route = useRoute()
const router = useRouter()

const table = ref<RestaurantTableResponse | null>(null)
const categories = ref<MenuCategoryResponse[]>([])
const items = ref<MenuItemResponse[]>([])
const cart = ref<Record<string, number>>({})
const activeCategory = ref('All')
const loading = ref(true)
const ordering = ref(false)
const fileUrlCache = ref<Record<string, string>>({})

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
    const token = route.params.token as string | undefined
    const tableCode = route.params.tableCode as string | undefined
    table.value = token
      ? await tableApi.getByToken(token)
      : await tableApi.getByTableCode(tableCode!)
    const rCode = table.value.restaurantCode
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
  } catch {
    toast.error('Invalid QR code or table not found')
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

function clearCart() {
  cart.value = {}
}

async function checkout() {
  if (!cartItems.value.length) {
    toast.error('Please add items to your order'); return
  }
  if (!table.value) {
    toast.error('Table not loaded'); return
  }
  ordering.value = true
  try {
    const order = await ordersApi.create({
      restaurantCode: table.value.restaurantCode,
      tableCode: table.value.code,
      orderType: 'QR_ORDER',
      deviceType: 'MOBILE',
    })
    for (const item of cartItems.value) {
      await orderItemApi.add(order.code, { menuItemCode: item.code, quantity: item.quantity })
    }
    const token = route.params.token as string | undefined
    router.push({
      path: '/payment',
      query: token
        ? { orderCode: order.code, restaurantCode: table.value.restaurantCode, source: 'qr', token }
        : { orderCode: order.code, restaurantCode: table.value.restaurantCode, source: 'table', tableCode: route.params.tableCode as string },
    })
  } catch {
    toast.error('Failed to place order. Please try again.')
    ordering.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="min-h-screen md:h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/40 flex flex-col md:overflow-hidden">

    <!-- Invalid QR / table -->
    <div v-if="!loading && !table" class="flex-1 flex items-center justify-center px-4">
      <div class="text-center max-w-sm">
        <div class="w-16 h-16 rounded-2xl bg-rose-50 ring-1 ring-rose-200 flex items-center justify-center mx-auto mb-3">
          <AlertTriangle class="w-7 h-7 text-rose-500" />
        </div>
        <p class="text-slate-900 font-semibold">Invalid QR code or table</p>
        <p class="text-sm text-slate-500 mt-1">Please scan the QR code on your table again.</p>
      </div>
    </div>

    <template v-else>
      <!-- Header -->
      <header class="relative overflow-hidden bg-gradient-to-r from-violet-500 via-violet-500 to-fuchsia-500 text-white px-4 sm:px-8 py-4 sm:py-5 flex-shrink-0">
        <!-- decorative -->
        <div aria-hidden="true" class="absolute -top-12 -right-12 w-48 h-48 rounded-full bg-white/10 blur-2xl" />
        <div aria-hidden="true" class="absolute -bottom-16 left-1/3 w-56 h-56 rounded-full bg-fuchsia-300/20 blur-3xl" />

        <div class="relative flex items-center justify-between gap-3">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-11 h-11 sm:w-12 sm:h-12 rounded-2xl bg-white/15 backdrop-blur ring-1 ring-white/30 flex items-center justify-center flex-shrink-0">
              <UtensilsCrossed class="w-6 h-6 text-white" />
            </div>
            <div class="min-w-0">
              <h1 class="text-xl sm:text-2xl font-bold tracking-tight truncate">
                Table {{ table?.tableNumber ?? '' }}
              </h1>
              <p class="text-violet-50/90 text-xs sm:text-sm flex items-center gap-1.5">
                <Sparkles class="w-3.5 h-3.5" />
                Tap any item to add it to your order
              </p>
            </div>
          </div>
          <div class="text-right flex-shrink-0 bg-white/10 backdrop-blur ring-1 ring-white/20 rounded-2xl px-3 sm:px-4 py-1.5 sm:py-2">
            <p class="text-xl sm:text-2xl font-bold tabular-nums">NPR {{ cartTotal.toFixed(0) }}</p>
            <p class="text-violet-50/90 text-[11px] sm:text-xs">{{ cartCount }} item{{ cartCount === 1 ? '' : 's' }}</p>
          </div>
        </div>
      </header>

      <!-- Loading -->
      <div v-if="loading" class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <Loader2 class="w-12 h-12 text-violet-500 mx-auto mb-3 animate-spin" />
          <p class="text-slate-500">Loading menu…</p>
        </div>
      </div>

      <div v-else class="flex-1 flex flex-col md:flex-row md:overflow-hidden">
        <!-- Left: Menu -->
        <div class="flex-1 flex flex-col md:overflow-hidden min-w-0">
          <!-- Category Tabs (right fade hints there's more to scroll) -->
          <div class="relative bg-white/70 backdrop-blur border-b border-slate-200/60 flex-shrink-0">
            <div class="flex gap-2 px-3 sm:px-6 py-3 overflow-x-auto category-scroller">
            <button @click="activeCategory = 'All'"
              :class="activeCategory === 'All'
                ? 'bg-gradient-to-r from-violet-500 to-fuchsia-500 text-white shadow-md shadow-violet-500/30'
                : 'bg-white text-slate-700 ring-1 ring-slate-200 hover:ring-slate-300'"
              class="px-4 sm:px-5 py-2 sm:py-2.5 rounded-xl font-semibold whitespace-nowrap text-sm transition-all">
              All Items
            </button>
            <button v-for="cat in categories" :key="cat.code" @click="activeCategory = cat.code"
              :class="activeCategory === cat.code
                ? 'bg-gradient-to-r from-violet-500 to-fuchsia-500 text-white shadow-md shadow-violet-500/30'
                : 'bg-white text-slate-700 ring-1 ring-slate-200 hover:ring-slate-300'"
              class="px-4 sm:px-5 py-2 sm:py-2.5 rounded-xl font-semibold whitespace-nowrap text-sm transition-all">
              {{ cat.name }}
            </button>
            </div>
            <!-- Right-edge fade hint that more categories are scrollable -->
            <div aria-hidden="true" class="pointer-events-none absolute top-0 right-0 bottom-0 w-8 bg-gradient-to-l from-white via-white/70 to-transparent" />
          </div>

          <!-- Items Grid -->
          <div class="flex-1 md:overflow-y-auto p-3 sm:p-5">
            <div v-if="filteredItems.length === 0" class="flex flex-col items-center justify-center h-full text-slate-400 py-12">
              <UtensilsCrossed class="w-16 h-16 mb-3 opacity-40" />
              <p class="text-lg">No items in this category yet</p>
            </div>
            <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 sm:gap-4">
              <button v-for="item in filteredItems" :key="item.code" @click="addToCart(item.code)"
                :class="cart[item.code]
                  ? 'bg-white ring-2 ring-violet-500 shadow-md shadow-violet-500/10'
                  : 'bg-white ring-1 ring-slate-200/60 hover:ring-violet-300 hover:shadow-md'"
                class="rounded-2xl p-3 sm:p-4 text-left relative transition-all active:scale-[0.97]">
                <!-- quantity badge -->
                <div v-if="cart[item.code]"
                  class="absolute -top-2 -right-2 bg-gradient-to-br from-violet-500 to-fuchsia-500 text-white rounded-full w-8 h-8 flex items-center justify-center font-bold text-sm shadow-lg shadow-violet-500/40 ring-2 ring-white">
                  {{ cart[item.code] }}
                </div>
                <!-- image -->
                <div class="relative rounded-xl h-28 sm:h-32 mb-3 overflow-hidden bg-gradient-to-br from-violet-50 to-fuchsia-50">
                  <img v-if="item.fileCode && fileUrlCache[item.fileCode]"
                    :src="fileUrlCache[item.fileCode]" :alt="item.name"
                    class="w-full h-full object-cover" loading="lazy" />
                  <div v-else class="w-full h-full flex items-center justify-center">
                    <UtensilsCrossed class="w-10 h-10 text-violet-300" />
                  </div>
                  <div v-if="item.isVeg" class="absolute top-1.5 left-1.5 bg-emerald-500/90 backdrop-blur text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">VEG</div>
                </div>
                <h3 class="font-semibold text-slate-900 text-sm leading-tight line-clamp-1">{{ item.name }}</h3>
                <p v-if="item.description" class="text-[11px] text-slate-400 mt-0.5 line-clamp-2">{{ item.description }}</p>
                <div class="mt-2 flex items-center justify-between">
                  <p class="text-base sm:text-lg font-bold text-violet-600 tabular-nums">NPR {{ item.price.toFixed(0) }}</p>
                  <div :class="cart[item.code] ? 'bg-violet-500 text-white' : 'bg-violet-100 text-violet-600'"
                    class="w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 transition-colors">
                    <Plus class="w-4 h-4" />
                  </div>
                </div>
              </button>
            </div>
          </div>
        </div>

        <!-- Right: Cart -->
        <div class="w-full md:w-[22rem] bg-white border-t md:border-t-0 md:border-l border-slate-200/60 flex flex-col md:flex-shrink-0">
          <div class="px-5 py-4 border-b border-slate-100 flex items-center gap-2.5">
            <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center shadow-md shadow-violet-500/30">
              <ShoppingBag class="w-5 h-5 text-white" />
            </div>
            <div>
              <h2 class="text-base font-bold text-slate-900">Your Order</h2>
              <p class="text-xs text-slate-500">{{ cartCount }} item{{ cartCount === 1 ? '' : 's' }}</p>
            </div>
          </div>

          <!-- Cart items -->
          <div class="md:flex-1 md:overflow-y-auto">
            <div v-if="cartItems.length === 0" class="flex flex-col items-center justify-center h-full text-slate-400 py-16 px-6 text-center">
              <div class="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center mb-3">
                <ShoppingBag class="w-7 h-7 text-slate-300" />
              </div>
              <p class="font-medium text-slate-500">Your cart is empty</p>
              <p class="text-xs mt-1">Tap items on the left to add them.</p>
            </div>
            <div v-else class="p-3 space-y-2">
              <div v-for="item in cartItems" :key="item.code"
                class="bg-slate-50 ring-1 ring-slate-200/60 rounded-2xl p-3">
                <div class="flex justify-between items-start mb-2.5 gap-2">
                  <div class="min-w-0 flex-1">
                    <p class="font-semibold text-slate-900 text-sm truncate">{{ item.name }}</p>
                    <p class="text-[11px] text-slate-500 tabular-nums">NPR {{ item.price.toFixed(0) }} each</p>
                  </div>
                  <button @click="deleteFromCart(item.code)"
                    class="w-7 h-7 -mr-1 -mt-1 rounded-full text-slate-400 hover:text-rose-600 hover:bg-rose-50 flex items-center justify-center transition-colors flex-shrink-0">
                    <X class="w-3.5 h-3.5" />
                  </button>
                </div>
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-1 bg-white ring-1 ring-slate-200 rounded-full p-0.5">
                    <button @click="removeFromCart(item.code)"
                      class="w-7 h-7 rounded-full flex items-center justify-center text-slate-600 hover:bg-slate-100 transition-colors">
                      <Minus class="w-3.5 h-3.5" />
                    </button>
                    <span class="w-7 text-center font-bold text-slate-900 text-sm tabular-nums">{{ item.quantity }}</span>
                    <button @click="addToCart(item.code)"
                      class="w-7 h-7 rounded-full flex items-center justify-center bg-gradient-to-br from-violet-500 to-fuchsia-500 text-white shadow-sm hover:shadow-md transition-shadow">
                      <Plus class="w-3.5 h-3.5" />
                    </button>
                  </div>
                  <span class="font-bold text-slate-900 text-sm tabular-nums">NPR {{ (item.price * item.quantity).toFixed(0) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div v-if="cartItems.length > 0" class="p-4 border-t border-slate-100 space-y-3 bg-white">
            <div class="bg-gradient-to-br from-violet-50 to-fuchsia-50 rounded-2xl p-3.5 ring-1 ring-violet-100/60">
              <div class="flex justify-between text-sm mb-1">
                <span class="text-slate-600">Subtotal</span>
                <span class="font-medium tabular-nums">NPR {{ cartTotal.toFixed(0) }}</span>
              </div>
              <div class="flex justify-between items-end pt-1.5 border-t border-violet-100/60 mt-1.5">
                <span class="font-bold text-slate-900">Total</span>
                <span class="font-bold text-violet-600 text-xl tabular-nums">NPR {{ cartTotal.toFixed(0) }}</span>
              </div>
            </div>
            <button @click="checkout" :disabled="ordering"
              class="w-full py-3.5 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white font-bold rounded-2xl shadow-lg shadow-violet-500/30 disabled:opacity-60 transition-all text-base flex items-center justify-center gap-2">
              <span>{{ ordering ? 'Processing…' : 'Proceed to Payment' }}</span>
              <ArrowRight v-if="!ordering" class="w-4 h-4" />
              <Loader2 v-else class="w-4 h-4 animate-spin" />
            </button>
            <button @click="clearCart"
              class="w-full py-2.5 bg-slate-50 hover:bg-slate-100 text-slate-600 font-medium rounded-xl ring-1 ring-slate-200/60 transition-colors text-sm inline-flex items-center justify-center gap-1.5">
              <Trash2 class="w-3.5 h-3.5" />
              Clear order
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
