<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import { orderItemApi } from '@/api/orderItem'
import { tableApi } from '@/api/table'
import { toast } from 'vue-sonner'
import api from '@/api/axios'
import type { RestaurantTableResponse, MenuItemResponse, MenuCategoryResponse, PageResponse } from '@/types'

const router = useRouter()
const auth = useAuthStore()

const tables = ref<RestaurantTableResponse[]>([])
const categories = ref<MenuCategoryResponse[]>([])
const items = ref<MenuItemResponse[]>([])
const selectedTable = ref<RestaurantTableResponse | null>(null)
const cart = ref<Record<string, number>>({})
const activeCategory = ref('All')
const notes = ref('')
const loading = ref(true)
const submitting = ref(false)

const restaurantCode = computed(() => auth.restaurantCode)

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

const statusColor = (status: string) => {
  const map: Record<string, string> = {
    AVAILABLE: 'bg-green-100 text-green-700 hover:bg-green-200',
    OCCUPIED: 'bg-red-100 text-red-700 opacity-50 cursor-not-allowed',
    RESERVED: 'bg-yellow-100 text-yellow-700 hover:bg-yellow-200',
    CLEANING: 'bg-gray-100 text-gray-600 opacity-50 cursor-not-allowed',
  }
  return map[status] ?? 'bg-gray-100 text-gray-600'
}

async function load() {
  try {
    loading.value = true
    const rCode = restaurantCode.value
    const [tbls, cats, its] = await Promise.all([
      tableApi.search({ restaurantCode: rCode }),
      api.get<PageResponse<MenuCategoryResponse>>('/menu-categories/search', { params: { restaurantCode: rCode, size: 50 } }).then(r => r.data),
      api.get<PageResponse<MenuItemResponse>>('/menu-items/search', { params: { restaurantCode: rCode, availability: 'AVAILABLE', size: 200 } }).then(r => r.data),
    ])
    tables.value = tbls.content
    categories.value = cats.content
    items.value = its.content
  } catch {
    toast.error('Failed to load data')
  } finally {
    loading.value = false
  }
}

function selectTable(table: RestaurantTableResponse) {
  if (table.status === 'OCCUPIED' || table.status === 'CLEANING') return
  selectedTable.value = table
  cart.value = {}
  notes.value = ''
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

async function submitOrder() {
  if (!selectedTable.value) {
    toast.error('Please select a table first')
    return
  }
  if (!cartItems.value.length) {
    toast.error('Please add items to the order')
    return
  }
  submitting.value = true
  try {
    const order = await ordersApi.create({
      restaurantCode: restaurantCode.value,
      tableCode: selectedTable.value.code,
      orderType: 'DINE_IN',
      specialNotes: notes.value || undefined,
      deviceType: 'WAITER_TABLET',
    })
    for (const item of cartItems.value) {
      await orderItemApi.add(order.code, { menuItemCode: item.code, quantity: item.quantity })
    }
    toast.success(`Order submitted for Table ${selectedTable.value.tableNumber}`)
    selectedTable.value = null
    cart.value = {}
    notes.value = ''
  } catch {
    toast.error('Failed to submit order')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="h-screen bg-gray-100 flex flex-col overflow-hidden">
    <!-- Header -->
    <header class="bg-white border-b border-gray-200 px-6 py-4 flex-shrink-0">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Waiter Mode</h1>
          <p class="text-sm text-gray-500">
            {{ selectedTable ? `Taking order for Table ${selectedTable.tableNumber}` : 'Select a table to start' }}
          </p>
        </div>
        <button @click="router.push('/admin')"
          class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 text-sm font-medium">
          Back to Dashboard
        </button>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="w-12 h-12 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <div v-else class="flex-1 flex overflow-hidden">
      <!-- Left Panel -->
      <div class="flex-1 flex flex-col overflow-hidden">

        <!-- Table Selection -->
        <div v-if="!selectedTable" class="flex-1 p-6 overflow-y-auto">
          <h2 class="text-xl font-semibold text-gray-900 mb-4">Select Table</h2>
          <div class="grid grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-3">
            <button v-for="table in tables" :key="table.code"
              @click="selectTable(table)"
              :disabled="table.status === 'OCCUPIED' || table.status === 'CLEANING'"
              :class="statusColor(table.status)"
              class="aspect-square rounded-xl p-4 flex flex-col items-center justify-center transition-all font-semibold text-sm">
              <svg class="w-6 h-6 mb-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span>Table {{ table.tableNumber }}</span>
              <span class="text-xs opacity-75">{{ table.capacity }} seats</span>
              <span class="text-xs mt-1 capitalize">{{ table.status.toLowerCase() }}</span>
            </button>
          </div>
        </div>

        <!-- Menu Selection -->
        <div v-else class="flex-1 flex flex-col overflow-hidden p-4">
          <div class="flex items-center justify-between mb-3">
            <h2 class="text-xl font-semibold text-gray-900">Menu Items</h2>
            <button @click="() => { selectedTable = null; cart = {}; notes = '' }"
              class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 text-sm">
              Change Table
            </button>
          </div>

          <!-- Category Tabs -->
          <div class="flex gap-2 mb-4 overflow-x-auto pb-1 flex-shrink-0">
            <button @click="activeCategory = 'All'"
              :class="activeCategory === 'All' ? 'bg-orange-500 text-white' : 'bg-white text-gray-700 border border-gray-200'"
              class="px-4 py-2 rounded-lg whitespace-nowrap text-sm font-medium transition-colors">
              All
            </button>
            <button v-for="cat in categories" :key="cat.code" @click="activeCategory = cat.code"
              :class="activeCategory === cat.code ? 'bg-orange-500 text-white' : 'bg-white text-gray-700 border border-gray-200'"
              class="px-4 py-2 rounded-lg whitespace-nowrap text-sm font-medium transition-colors">
              {{ cat.name }}
            </button>
          </div>

          <!-- Menu Grid -->
          <div class="flex-1 overflow-y-auto">
            <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
              <button v-for="item in filteredItems" :key="item.code" @click="addToCart(item.code)"
                class="bg-white rounded-xl p-4 border-2 text-left relative hover:border-orange-500 transition-colors"
                :class="cart[item.code] ? 'border-orange-500' : 'border-gray-200'">
                <div v-if="cart[item.code]"
                  class="absolute -top-2 -right-2 bg-orange-500 text-white w-7 h-7 rounded-full flex items-center justify-center text-sm font-bold">
                  {{ cart[item.code] }}
                </div>
                <h3 class="font-medium text-gray-900 text-sm mb-1">{{ item.name }}</h3>
                <p class="text-xs text-gray-500 mb-2 capitalize">{{ item.categoryCode ? categories.find(c => c.code === item.categoryCode)?.name ?? '' : '' }}</p>
                <p class="text-base font-bold text-orange-500">NPR {{ item.price.toFixed(0) }}</p>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Panel: Order Summary -->
      <div class="w-80 bg-white border-l border-gray-200 flex flex-col flex-shrink-0">
        <div class="p-4 border-b">
          <h2 class="text-lg font-semibold text-gray-900">Order Summary</h2>
          <p v-if="selectedTable" class="text-sm text-gray-500">Table {{ selectedTable.tableNumber }}</p>
        </div>

        <div class="flex-1 overflow-y-auto p-4">
          <div v-if="cartItems.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400 py-8">
            <svg class="w-12 h-12 mb-2 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <p>No items added yet</p>
          </div>
          <div v-else class="space-y-3">
            <div v-for="item in cartItems" :key="item.code" class="bg-gray-50 rounded-lg p-3">
              <div class="flex justify-between items-start mb-2">
                <div class="flex-1 min-w-0">
                  <h3 class="font-medium text-gray-900 text-sm truncate">{{ item.name }}</h3>
                  <p class="text-xs text-gray-500">NPR {{ item.price.toFixed(0) }} each</p>
                </div>
                <button @click="() => { const u = { ...cart }; delete u[item.code]; cart = u }"
                  class="text-red-400 hover:text-red-600 ml-2">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <div class="flex items-center gap-2">
                <button @click="removeFromCart(item.code)"
                  class="p-1 bg-gray-200 rounded hover:bg-gray-300">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4" />
                  </svg>
                </button>
                <span class="w-8 text-center font-medium text-gray-900">{{ item.quantity }}</span>
                <button @click="addToCart(item.code)"
                  class="p-1 bg-gray-200 rounded hover:bg-gray-300">
                  <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                  </svg>
                </button>
                <span class="flex-1 text-right font-bold text-gray-900 text-sm">
                  NPR {{ (item.price * item.quantity).toFixed(0) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="cartItems.length > 0" class="p-4 border-t space-y-3">
          <textarea v-model="notes" placeholder="Order notes (allergies, special requests...)"
            class="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 text-gray-900 resize-none text-sm"
            rows="2"></textarea>

          <div class="bg-orange-50 rounded-lg p-3">
            <div class="flex justify-between text-sm mb-1">
              <span class="text-gray-700">Subtotal</span>
              <span class="font-medium text-gray-900">NPR {{ cartTotal.toFixed(0) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="font-semibold text-gray-900">Total</span>
              <span class="font-bold text-orange-500 text-lg">NPR {{ cartTotal.toFixed(0) }}</span>
            </div>
          </div>

          <button @click="submitOrder" :disabled="submitting"
            class="w-full py-3 bg-orange-500 text-white font-medium rounded-lg hover:bg-orange-600 transition-colors disabled:opacity-60 flex items-center justify-center gap-2">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            {{ submitting ? 'Submitting...' : 'Submit Order' }}
          </button>

          <button @click="() => { cart = {}; notes = '' }"
            class="w-full py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm">
            Clear Order
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
