<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { tableApi } from '@/api/table'
import { ordersApi } from '@/api/orders'
import { orderItemApi } from '@/api/orderItem'
import { toast } from 'vue-sonner'
import api from '@/api/axios'
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

async function placeOrder() {
  if (!cartItems.value.length) {
    toast.error('Please add items to your order')
    return
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
    router.push({ path: '/payment', query: { orderCode: order.code, restaurantCode: table.value!.restaurantCode } })
  } catch {
    toast.error('Failed to place order. Please try again.')
    ordering.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <div class="text-center">
        <div class="w-12 h-12 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-gray-500">Loading menu...</p>
      </div>
    </div>

    <template v-else-if="table">
      <!-- Header -->
      <header class="bg-white border-b border-gray-200 sticky top-0 z-10 shadow-sm">
        <div class="px-4 py-3">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-lg font-bold text-gray-900">Order Menu</h1>
              <p class="text-sm text-orange-500 font-medium">Table {{ table.tableNumber }}</p>
            </div>
            <button
              @click="showCart = !showCart"
              class="relative p-2 bg-orange-500 text-white rounded-full"
            >
              <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 3h2l.4 2M7 13h10l4-9H5.4m0 0L7 13m0 0l-2 9m2-9h10m0 0l2 9" />
              </svg>
              <span v-if="cartCount > 0"
                class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
                {{ cartCount }}
              </span>
            </button>
          </div>
          <!-- Search -->
          <div class="mt-3">
            <input v-model="search" type="text" placeholder="Search menu..."
              class="w-full px-4 py-2 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 bg-gray-50" />
          </div>
          <!-- Category Pills -->
          <div class="flex gap-2 mt-3 overflow-x-auto pb-1">
            <button @click="activeCategory = 'All'"
              :class="activeCategory === 'All' ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-600'"
              class="px-4 py-1.5 rounded-full text-sm font-medium whitespace-nowrap flex-shrink-0 transition-colors">
              All
            </button>
            <button v-for="cat in categories" :key="cat.code" @click="activeCategory = cat.code"
              :class="activeCategory === cat.code ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-600'"
              class="px-4 py-1.5 rounded-full text-sm font-medium whitespace-nowrap flex-shrink-0 transition-colors">
              {{ cat.name }}
            </button>
          </div>
        </div>
      </header>

      <!-- Menu Grid -->
      <div class="p-4 pb-32">
        <div v-if="filteredItems.length === 0" class="text-center py-16 text-gray-400">
          No items found
        </div>
        <div v-else class="grid grid-cols-2 gap-3">
          <div v-for="item in filteredItems" :key="item.code"
            class="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
            <div class="bg-orange-50 h-24 flex items-center justify-center">
              <span class="text-4xl">🍽️</span>
            </div>
            <div class="p-3">
              <h3 class="font-semibold text-gray-900 text-sm leading-tight mb-1 line-clamp-2">{{ item.name }}</h3>
              <p v-if="item.description" class="text-xs text-gray-500 mb-2 line-clamp-1">{{ item.description }}</p>
              <div class="flex items-center justify-between">
                <span class="text-orange-500 font-bold text-sm">NPR {{ item.price.toFixed(0) }}</span>
                <div class="flex items-center gap-1">
                  <button v-if="cart[item.code]" @click="removeFromCart(item.code)"
                    class="w-7 h-7 bg-gray-100 rounded-full flex items-center justify-center text-gray-700 font-bold text-lg leading-none">−</button>
                  <span v-if="cart[item.code]"
                    class="text-sm font-bold text-gray-900 min-w-[20px] text-center">{{ cart[item.code] }}</span>
                  <button @click="addToCart(item.code)"
                    class="w-7 h-7 bg-orange-500 rounded-full flex items-center justify-center text-white font-bold text-lg leading-none">+</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom Order Bar -->
      <div v-if="cartCount > 0" class="fixed bottom-0 left-0 right-0 p-4 bg-white border-t border-gray-200 shadow-lg">
        <button @click="placeOrder" :disabled="ordering"
          class="w-full py-4 bg-orange-500 text-white font-bold rounded-xl hover:bg-orange-600 transition-colors disabled:opacity-60 flex items-center justify-between px-5">
          <span class="bg-white/20 rounded-lg px-2 py-0.5 text-sm">{{ cartCount }} items</span>
          <span>{{ ordering ? 'Placing Order...' : 'Place Order' }}</span>
          <span class="font-bold">NPR {{ cartTotal.toFixed(0) }}</span>
        </button>
      </div>

      <!-- Cart Slide Panel -->
      <div v-if="showCart" class="fixed inset-0 z-50">
        <div @click="showCart = false" class="absolute inset-0 bg-black/50"></div>
        <div class="absolute right-0 top-0 bottom-0 w-80 bg-white shadow-2xl flex flex-col">
          <div class="p-4 border-b flex items-center justify-between">
            <h2 class="text-lg font-bold">Your Cart</h2>
            <button @click="showCart = false" class="p-1 hover:bg-gray-100 rounded-lg">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="flex-1 overflow-y-auto p-4 space-y-3">
            <div v-if="cartItems.length === 0" class="text-center py-12 text-gray-400">Cart is empty</div>
            <div v-else v-for="item in cartItems" :key="item.code"
              class="flex items-center gap-3 bg-gray-50 rounded-lg p-3">
              <div class="flex-1 min-w-0">
                <p class="font-medium text-sm text-gray-900 truncate">{{ item.name }}</p>
                <p class="text-orange-500 text-sm font-bold">NPR {{ item.price.toFixed(0) }}</p>
              </div>
              <div class="flex items-center gap-2 flex-shrink-0">
                <button @click="removeFromCart(item.code)"
                  class="w-6 h-6 bg-gray-200 rounded-full flex items-center justify-center text-xs font-bold">−</button>
                <span class="text-sm font-bold w-5 text-center">{{ item.quantity }}</span>
                <button @click="addToCart(item.code)"
                  class="w-6 h-6 bg-orange-500 rounded-full flex items-center justify-center text-xs font-bold text-white">+</button>
              </div>
            </div>
          </div>
          <div class="p-4 border-t">
            <div class="flex justify-between mb-4">
              <span class="font-semibold">Total</span>
              <span class="font-bold text-orange-500 text-lg">NPR {{ cartTotal.toFixed(0) }}</span>
            </div>
            <button @click="() => { showCart = false; placeOrder() }" :disabled="ordering"
              class="w-full py-3 bg-orange-500 text-white font-bold rounded-xl hover:bg-orange-600 disabled:opacity-60 transition-colors">
              {{ ordering ? 'Placing...' : 'Place Order' }}
            </button>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="flex items-center justify-center min-h-screen">
      <div class="text-center">
        <p class="text-5xl mb-4">⚠️</p>
        <p class="text-gray-600 font-medium">Invalid QR code</p>
        <p class="text-sm text-gray-400 mt-1">Please scan the QR code on your table</p>
      </div>
    </div>
  </div>
</template>
