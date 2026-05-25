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
  ShoppingBag, Search, Plus, Minus, X, UtensilsCrossed,
  ArrowRight, Loader2, AlertTriangle, Trash2,
} from 'lucide-vue-next'
import type { RestaurantTableResponse, MenuItemResponse, MenuCategoryResponse, PageResponse } from '@/types'

const route = useRoute()
const router = useRouter()

const table = ref<RestaurantTableResponse | null>(null)
const categories = ref<MenuCategoryResponse[]>([])
const items = ref<MenuItemResponse[]>([])
const cart = ref<Record<string, number>>({})
const activeCategory = ref('All')
const search = ref('')
const loading = ref(true)
const ordering = ref(false)
const showCart = ref(false)
const fileUrlCache = ref<Record<string, string>>({})

const filteredItems = computed(() => {
  let result = items.value
  if (search.value) {
    result = result.filter(i => i.name.toLowerCase().includes(search.value.toLowerCase()))
  }
  if (activeCategory.value !== 'All') {
    result = result.filter(i => i.categoryCode === activeCategory.value)
  }
  return result
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
    const token = route.params.token as string
    table.value = await tableApi.getByToken(token)
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

async function placeOrder() {
  if (!cartItems.value.length) {
    toast.error('Please add items to your order'); return
  }
  ordering.value = true
  try {
    const order = await ordersApi.create({
      restaurantCode: table.value!.restaurantCode,
      tableCode: table.value!.code,
      orderType: 'QR_ORDER',
      deviceType: 'MOBILE',
    })
    for (const item of cartItems.value) {
      await orderItemApi.add(order.code, { menuItemCode: item.code, quantity: item.quantity })
    }
    router.push({ path: '/payment', query: { orderCode: order.code, restaurantCode: table.value!.restaurantCode, source: 'qr', token: route.params.token as string } })
  } catch {
    toast.error('Failed to place order. Please try again.')
    ordering.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/30">
    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <div class="text-center">
        <Loader2 class="w-12 h-12 text-violet-500 mx-auto mb-3 animate-spin" />
        <p class="text-slate-500">Loading menu…</p>
      </div>
    </div>

    <template v-else-if="table">
      <!-- Header -->
      <header class="bg-white/80 backdrop-blur border-b border-slate-200/60 sticky top-0 z-10">
        <div class="px-4 py-3">
          <div class="flex items-center justify-between gap-3">
            <div class="min-w-0">
              <h1 class="text-base font-bold text-slate-900">Order Menu</h1>
              <div class="flex items-center gap-1.5 mt-0.5">
                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[11px] font-semibold bg-gradient-to-r from-violet-500 to-fuchsia-500 text-white">
                  Table {{ table.tableNumber }}
                </span>
              </div>
            </div>
            <button
              @click="showCart = !showCart"
              class="relative w-11 h-11 rounded-full bg-gradient-to-br from-violet-500 to-fuchsia-500 shadow-md shadow-violet-500/30 flex items-center justify-center text-white active:scale-95 transition-transform"
            >
              <ShoppingBag class="w-5 h-5" />
              <span v-if="cartCount > 0"
                class="absolute -top-1 -right-1 bg-rose-500 text-white text-[10px] rounded-full min-w-[18px] h-[18px] px-1 flex items-center justify-center font-bold ring-2 ring-white">
                {{ cartCount }}
              </span>
            </button>
          </div>

          <!-- Search -->
          <div class="mt-3 relative">
            <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input v-model="search" type="text" placeholder="Search the menu…"
              class="w-full pl-9 pr-3 py-2 bg-slate-100 border border-transparent rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:bg-white focus:border-violet-300 transition-all" />
          </div>

          <!-- Category Pills -->
          <div class="flex gap-2 mt-3 overflow-x-auto pb-1 -mx-4 px-4">
            <button @click="activeCategory = 'All'"
              :class="activeCategory === 'All'
                ? 'bg-gradient-to-r from-violet-500 to-fuchsia-500 text-white shadow-sm'
                : 'bg-slate-100 text-slate-600'"
              class="px-4 py-1.5 rounded-full text-sm font-semibold whitespace-nowrap flex-shrink-0 transition-all">
              All
            </button>
            <button v-for="cat in categories" :key="cat.code" @click="activeCategory = cat.code"
              :class="activeCategory === cat.code
                ? 'bg-gradient-to-r from-violet-500 to-fuchsia-500 text-white shadow-sm'
                : 'bg-slate-100 text-slate-600'"
              class="px-4 py-1.5 rounded-full text-sm font-semibold whitespace-nowrap flex-shrink-0 transition-all">
              {{ cat.name }}
            </button>
          </div>
        </div>
      </header>

      <!-- Menu Grid -->
      <div class="p-3 sm:p-4 pb-28">
        <div v-if="filteredItems.length === 0" class="flex flex-col items-center justify-center py-16 text-slate-400">
          <UtensilsCrossed class="w-14 h-14 mb-3 opacity-40" />
          <p>No items found</p>
        </div>
        <div v-else class="grid grid-cols-2 gap-3">
          <div v-for="item in filteredItems" :key="item.code"
            class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm overflow-hidden flex flex-col">
            <div class="relative bg-gradient-to-br from-violet-50 to-fuchsia-50 h-28 sm:h-32 flex items-center justify-center overflow-hidden">
              <img v-if="item.fileCode && fileUrlCache[item.fileCode]"
                :src="fileUrlCache[item.fileCode]" :alt="item.name"
                class="w-full h-full object-cover" />
              <UtensilsCrossed v-else class="w-10 h-10 text-violet-300" />
              <div v-if="item.isVeg" class="absolute top-1.5 left-1.5 bg-emerald-500/90 backdrop-blur text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">VEG</div>
            </div>
            <div class="p-3 flex-1 flex flex-col">
              <h3 class="font-semibold text-slate-900 text-sm leading-tight line-clamp-2">{{ item.name }}</h3>
              <p v-if="item.description" class="text-[11px] text-slate-400 mt-1 line-clamp-1">{{ item.description }}</p>
              <div class="flex items-center justify-between mt-auto pt-2">
                <span class="text-violet-600 font-bold text-sm tabular-nums">NPR {{ item.price.toFixed(0) }}</span>
                <div class="flex items-center gap-1">
                  <button v-if="cart[item.code]" @click="removeFromCart(item.code)"
                    class="w-7 h-7 bg-slate-100 rounded-full flex items-center justify-center text-slate-700 hover:bg-slate-200 transition-colors">
                    <Minus class="w-3.5 h-3.5" />
                  </button>
                  <span v-if="cart[item.code]"
                    class="text-sm font-bold text-slate-900 min-w-[18px] text-center tabular-nums">{{ cart[item.code] }}</span>
                  <button @click="addToCart(item.code)"
                    class="w-7 h-7 bg-gradient-to-br from-violet-500 to-fuchsia-500 rounded-full flex items-center justify-center text-white shadow-sm hover:shadow-md transition-all active:scale-95">
                    <Plus class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sticky bottom order bar -->
      <div v-if="cartCount > 0" class="fixed bottom-0 left-0 right-0 z-20 p-3 bg-white/95 backdrop-blur border-t border-slate-200/60 shadow-[0_-8px_24px_-12px_rgba(0,0,0,0.15)]">
        <button @click="placeOrder" :disabled="ordering"
          class="w-full py-3.5 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white font-bold rounded-2xl shadow-lg shadow-violet-500/30 transition-all disabled:opacity-60 flex items-center justify-between px-4">
          <span class="bg-white/20 rounded-lg px-2 py-1 text-xs tabular-nums">{{ cartCount }} {{ cartCount === 1 ? 'item' : 'items' }}</span>
          <span class="text-base flex items-center gap-1.5">{{ ordering ? 'Placing…' : 'Place order' }}<ArrowRight v-if="!ordering" class="w-4 h-4" /></span>
          <span class="font-bold tabular-nums">NPR {{ cartTotal.toFixed(0) }}</span>
        </button>
      </div>

      <!-- Cart slide-in -->
      <Teleport to="body">
        <div v-if="showCart" class="fixed inset-0 z-50">
          <div @click="showCart = false" class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm" />
          <div class="absolute right-0 top-0 bottom-0 w-full max-w-sm bg-white shadow-2xl flex flex-col rounded-l-3xl">
            <div class="p-4 border-b border-slate-100 flex items-center justify-between">
              <div class="flex items-center gap-2.5">
                <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center shadow-md shadow-violet-500/30">
                  <ShoppingBag class="w-5 h-5 text-white" />
                </div>
                <div>
                  <h2 class="text-base font-bold text-slate-900">Your cart</h2>
                  <p class="text-xs text-slate-500">{{ cartCount }} item{{ cartCount === 1 ? '' : 's' }}</p>
                </div>
              </div>
              <button @click="showCart = false" class="w-9 h-9 rounded-full text-slate-500 hover:bg-slate-100 flex items-center justify-center">
                <X class="w-5 h-5" />
              </button>
            </div>
            <div class="flex-1 overflow-y-auto p-3 space-y-2">
              <div v-if="cartItems.length === 0" class="flex flex-col items-center justify-center py-16 text-slate-400 text-center">
                <div class="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center mb-3">
                  <ShoppingBag class="w-7 h-7 text-slate-300" />
                </div>
                <p>Your cart is empty</p>
              </div>
              <div v-else v-for="item in cartItems" :key="item.code"
                class="bg-slate-50 ring-1 ring-slate-200/60 rounded-2xl p-3">
                <div class="flex items-start justify-between gap-2 mb-2.5">
                  <div class="min-w-0">
                    <p class="font-semibold text-slate-900 text-sm truncate">{{ item.name }}</p>
                    <p class="text-[11px] text-slate-500 tabular-nums">NPR {{ item.price.toFixed(0) }} each</p>
                  </div>
                  <button @click="deleteFromCart(item.code)"
                    class="w-7 h-7 rounded-full text-slate-400 hover:text-rose-600 hover:bg-rose-50 flex items-center justify-center transition-colors flex-shrink-0">
                    <X class="w-3.5 h-3.5" />
                  </button>
                </div>
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-1 bg-white ring-1 ring-slate-200 rounded-full p-0.5">
                    <button @click="removeFromCart(item.code)"
                      class="w-7 h-7 rounded-full hover:bg-slate-100 flex items-center justify-center text-slate-600">
                      <Minus class="w-3.5 h-3.5" />
                    </button>
                    <span class="w-6 text-center font-bold text-slate-900 text-sm tabular-nums">{{ item.quantity }}</span>
                    <button @click="addToCart(item.code)"
                      class="w-7 h-7 rounded-full bg-gradient-to-br from-violet-500 to-fuchsia-500 text-white flex items-center justify-center shadow-sm">
                      <Plus class="w-3.5 h-3.5" />
                    </button>
                  </div>
                  <span class="font-bold text-slate-900 text-sm tabular-nums">NPR {{ (item.price * item.quantity).toFixed(0) }}</span>
                </div>
              </div>
            </div>
            <div v-if="cartItems.length > 0" class="p-4 border-t border-slate-100 space-y-3">
              <div class="bg-gradient-to-br from-violet-50 to-fuchsia-50 rounded-2xl p-3.5 ring-1 ring-violet-100/60 flex items-center justify-between">
                <span class="font-bold text-slate-900">Total</span>
                <span class="font-bold text-violet-600 text-xl tabular-nums">NPR {{ cartTotal.toFixed(0) }}</span>
              </div>
              <button @click="() => { showCart = false; placeOrder() }" :disabled="ordering"
                class="w-full py-3 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white font-bold rounded-2xl shadow-md shadow-violet-500/30 disabled:opacity-60 transition-all flex items-center justify-center gap-1.5">
                {{ ordering ? 'Placing…' : 'Place order' }}
                <ArrowRight v-if="!ordering" class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </Teleport>
    </template>

    <!-- Invalid QR -->
    <div v-else class="flex items-center justify-center min-h-screen px-4">
      <div class="text-center max-w-sm">
        <div class="w-16 h-16 rounded-2xl bg-rose-50 ring-1 ring-rose-200 flex items-center justify-center mx-auto mb-3">
          <AlertTriangle class="w-7 h-7 text-rose-500" />
        </div>
        <p class="text-slate-900 font-semibold">Invalid QR code</p>
        <p class="text-sm text-slate-500 mt-1">Please scan the QR code on your table again.</p>
      </div>
    </div>
  </div>
</template>
