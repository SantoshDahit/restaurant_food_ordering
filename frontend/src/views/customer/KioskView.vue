<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ordersApi } from '@/api/orders'
import { orderItemApi } from '@/api/orderItem'
import { restaurantApi } from '@/api/restaurant'
import { toast } from 'vue-sonner'
import api from '@/api/axios'
import { fileApi } from '@/api/file'
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
    // Preload images
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

function clearCart() {
  cart.value = {}
}

async function checkout() {
  if (!cartItems.value.length) {
    toast.error('Please add items to your order')
    return
  }
  if (!restaurant.value) {
    toast.error('Restaurant not loaded')
    return
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
    router.push({ path: '/payment', query: { orderCode: order.code, restaurantCode: restaurant.value.code } })
  } catch {
    toast.error('Checkout failed. Please try again.')
    ordering.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="h-screen bg-gray-100 flex flex-col overflow-hidden">
    <!-- Header -->
    <header class="bg-orange-500 text-white px-6 py-4 flex items-center justify-between flex-shrink-0">
      <div>
        <h1 class="text-2xl font-bold">{{ restaurant?.name ?? 'Self Order Kiosk' }}</h1>
        <p class="text-orange-100 text-sm">Touch items to add to your order</p>
      </div>
      <div class="text-right">
        <p class="text-3xl font-bold">NPR {{ cartTotal.toFixed(0) }}</p>
        <p class="text-orange-100 text-sm">{{ cartCount }} items</p>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="text-center">
        <div class="w-16 h-16 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-gray-500 text-lg">Loading menu...</p>
      </div>
    </div>

    <div v-else class="flex-1 flex overflow-hidden">
      <!-- Left: Menu -->
      <div class="flex-1 flex flex-col overflow-hidden">
        <!-- Category Tabs -->
        <div class="flex gap-2 px-4 py-3 bg-white border-b overflow-x-auto flex-shrink-0">
          <button @click="activeCategory = 'All'"
            :class="activeCategory === 'All' ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-700'"
            class="px-5 py-2.5 rounded-xl font-semibold whitespace-nowrap text-sm transition-colors">
            All Items
          </button>
          <button v-for="cat in categories" :key="cat.code" @click="activeCategory = cat.code"
            :class="activeCategory === cat.code ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-700'"
            class="px-5 py-2.5 rounded-xl font-semibold whitespace-nowrap text-sm transition-colors">
            {{ cat.name }}
          </button>
        </div>

        <!-- Items Grid -->
        <div class="flex-1 overflow-y-auto p-4">
          <div v-if="filteredItems.length === 0" class="flex items-center justify-center h-full">
            <p class="text-gray-400 text-xl">No items available</p>
          </div>
          <div v-else class="grid grid-cols-3 lg:grid-cols-4 gap-4">
            <button v-for="item in filteredItems" :key="item.code" @click="addToCart(item.code)"
              class="bg-white rounded-2xl shadow-sm border-2 hover:border-orange-500 transition-all p-4 text-left relative active:scale-95"
              :class="cart[item.code] ? 'border-orange-500' : 'border-gray-100'">
              <div v-if="cart[item.code]"
                class="absolute -top-2 -right-2 bg-orange-500 text-white rounded-full w-8 h-8 flex items-center justify-center font-bold text-sm">
                {{ cart[item.code] }}
              </div>
              <div class="bg-orange-50 rounded-xl h-28 flex items-center justify-center mb-3 overflow-hidden">
                <img v-if="item.fileCode && fileUrlCache[item.fileCode]"
                  :src="fileUrlCache[item.fileCode]" :alt="item.name"
                  class="w-full h-full object-cover rounded-xl" />
                <span v-else class="text-5xl">🍽️</span>
              </div>
              <h3 class="font-semibold text-gray-900 text-sm leading-tight mb-1">{{ item.name }}</h3>
              <p v-if="item.description" class="text-xs text-gray-400 mb-2 line-clamp-2">{{ item.description }}</p>
              <p class="text-lg font-bold text-orange-500">NPR {{ item.price.toFixed(0) }}</p>
            </button>
          </div>
        </div>
      </div>

      <!-- Right: Cart -->
      <div class="w-80 bg-white border-l border-gray-200 flex flex-col flex-shrink-0">
        <div class="p-4 border-b">
          <h2 class="text-lg font-bold text-gray-900">Your Order</h2>
          <p class="text-sm text-gray-500">{{ cartCount }} item(s)</p>
        </div>

        <div class="flex-1 overflow-y-auto">
          <div v-if="cartItems.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400 py-12">
            <svg class="w-16 h-16 mb-3 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                d="M3 3h2l.4 2M7 13h10l4-9H5.4m0 0L7 13m0 0l-2 9m2-9h10m0 0l2 9" />
            </svg>
            <p>No items added yet</p>
          </div>
          <div v-else class="p-3 space-y-2">
            <div v-for="item in cartItems" :key="item.code"
              class="bg-gray-50 rounded-xl p-3">
              <div class="flex justify-between items-start mb-2">
                <div class="flex-1 min-w-0">
                  <p class="font-medium text-gray-900 text-sm truncate">{{ item.name }}</p>
                  <p class="text-xs text-gray-500">NPR {{ item.price.toFixed(0) }} each</p>
                </div>
                <button @click="() => { const updated = { ...cart }; delete updated[item.code]; cart = updated }"
                  class="text-red-400 hover:text-red-600 ml-2 flex-shrink-0">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <button @click="removeFromCart(item.code)"
                    class="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center font-bold hover:bg-gray-300">−</button>
                  <span class="w-8 text-center font-bold">{{ item.quantity }}</span>
                  <button @click="addToCart(item.code)"
                    class="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center font-bold text-white hover:bg-orange-600">+</button>
                </div>
                <span class="font-bold text-gray-900">NPR {{ (item.price * item.quantity).toFixed(0) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="cartItems.length > 0" class="p-4 border-t space-y-3">
          <div class="bg-orange-50 rounded-xl p-3">
            <div class="flex justify-between text-sm mb-1">
              <span class="text-gray-600">Subtotal</span>
              <span class="font-medium">NPR {{ cartTotal.toFixed(0) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="font-bold text-gray-900">Total</span>
              <span class="font-bold text-orange-500 text-xl">NPR {{ cartTotal.toFixed(0) }}</span>
            </div>
          </div>
          <button @click="checkout" :disabled="ordering"
            class="w-full py-4 bg-orange-500 text-white font-bold rounded-xl hover:bg-orange-600 disabled:opacity-60 transition-colors text-lg">
            {{ ordering ? 'Processing...' : 'Proceed to Payment' }}
          </button>
          <button @click="clearCart"
            class="w-full py-2 bg-gray-100 text-gray-700 font-medium rounded-xl hover:bg-gray-200 transition-colors">
            Clear Order
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
