<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import { orderItemApi } from '@/api/orderItem'
import { tableApi } from '@/api/table'
import { fileApi } from '@/api/file'
import { toast } from 'vue-sonner'
import api from '@/api/axios'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import { UtensilsCrossed, CheckCircle2, Loader2, ArrowLeft } from 'lucide-vue-next'
import type {
  RestaurantTableResponse, MenuItemResponse, MenuCategoryResponse, PageResponse,
  OrdersResponse, OrderDetailResponse,
} from '@/types'

const router = useRouter()
const auth = useAuthStore()

const tables = ref<RestaurantTableResponse[]>([])
const categories = ref<MenuCategoryResponse[]>([])
const items = ref<MenuItemResponse[]>([])
const activeOrders = ref<OrdersResponse[]>([])
const selectedTable = ref<RestaurantTableResponse | null>(null)
const viewingOrder = ref<OrderDetailResponse | null>(null)
const cart = ref<Record<string, number>>({})
const activeCategory = ref('All')
const notes = ref('')
const loading = ref(true)
const submitting = ref(false)
const updatingStatus = ref(false)
// When true, the menu/cart is open to ADD items to the existing viewingOrder
// (open-tab top-up) rather than to create a brand-new order.
const appendMode = ref(false)
const fileUrlCache = ref<Record<string, string>>({})
const submittedOrder = ref<{ orderNumber: string; ticketNumber?: number | null; tableNumber: string } | null>(null)

let pollTimer: ReturnType<typeof setInterval> | null = null

const restaurantCode = computed(() => auth.restaurantCode)

const headerSubtitle = computed(() => {
  if (appendMode.value && selectedTable.value) return `Adding items — Table ${selectedTable.value.tableNumber}`
  if (viewingOrder.value) return `Order for Table ${selectedTable.value?.tableNumber}`
  if (selectedTable.value) return `Taking order for Table ${selectedTable.value.tableNumber}`
  return 'Select a table to start'
})

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

// The active order currently sitting on a given table, if any.
const orderForTable = (tableCode: string) =>
  activeOrders.value.find(o => o.tableCode === tableCode)

// Waiter can hand over / close an order once it exists; "served" is available
// until it's served, "complete" only after it's been served.
const canMarkServed = computed(() =>
  viewingOrder.value != null && viewingOrder.value.status !== 'SERVED' && viewingOrder.value.status !== 'COMPLETED'
)
const canMarkComplete = computed(() => viewingOrder.value?.status === 'SERVED')

const statusClass = (status: string) => {
  const map: Record<string, string> = {
    AVAILABLE: 'bg-green-100 text-green-700 hover:bg-green-200',
    OCCUPIED: 'bg-red-100 text-red-700 hover:bg-red-200',          // clickable: view the active order
    RESERVED: 'bg-yellow-100 text-yellow-700 hover:bg-yellow-200',
    CLEANING: 'bg-gray-100 text-gray-600 opacity-50 cursor-not-allowed',
  }
  return map[status] ?? 'bg-gray-100 text-gray-600'
}

async function load() {
  loading.value = true
  const rCode = restaurantCode.value
  if (!rCode) {
    toast.error('No restaurant on file — set one up before taking orders.')
    loading.value = false
    return
  }
  // Load each resource independently so one failure (e.g. empty menu) doesn't
  // wipe out the others — the table grid must always render.
  const [tblsRes, catsRes, itsRes, ordersRes] = await Promise.allSettled([
    tableApi.search({ restaurantCode: rCode }),
    api.get<PageResponse<MenuCategoryResponse>>('/menu-categories/search', { params: { restaurantCode: rCode, size: 50 } }).then(r => r.data),
    api.get<PageResponse<MenuItemResponse>>('/menu-items/search', { params: { restaurantCode: rCode, availability: 'AVAILABLE', size: 200 } }).then(r => r.data),
    ordersApi.getActiveByRestaurant(rCode),
  ])
  if (tblsRes.status === 'fulfilled') tables.value = tblsRes.value.content
  else toast.error('Failed to load tables')
  if (catsRes.status === 'fulfilled') categories.value = catsRes.value.content
  if (itsRes.status === 'fulfilled') items.value = itsRes.value.content
  if (ordersRes.status === 'fulfilled') activeOrders.value = ordersRes.value

  try {
    const codes = items.value.filter(i => i.fileCode).map(i => i.fileCode!)
    await Promise.all(codes.map(async (code) => {
      try {
        const f = await fileApi.get(code)
        fileUrlCache.value[code] = f.url
      } catch { /* silent */ }
    }))
  } catch { /* silent */ } finally {
    loading.value = false
  }
}

// Lightweight refresh of just the live state (tables + active orders, and the
// open order's detail) — keeps the grid and status in sync without a reload.
async function refreshLive() {
  const rCode = restaurantCode.value
  if (!rCode) return
  const [tblsRes, ordersRes] = await Promise.allSettled([
    tableApi.search({ restaurantCode: rCode }),
    ordersApi.getActiveByRestaurant(rCode),
  ])
  if (tblsRes.status === 'fulfilled') tables.value = tblsRes.value.content
  if (ordersRes.status === 'fulfilled') activeOrders.value = ordersRes.value
  if (viewingOrder.value) {
    try { viewingOrder.value = await ordersApi.getDetail(viewingOrder.value.code) } catch { /* ignore */ }
  }
}

async function selectTable(table: RestaurantTableResponse) {
  if (table.status === 'CLEANING') return
  if (table.status === 'OCCUPIED') { await openOrder(table); return }
  // Available / reserved → start a new order.
  selectedTable.value = table
  viewingOrder.value = null
  cart.value = {}
  notes.value = ''
}

async function openOrder(table: RestaurantTableResponse) {
  const active = orderForTable(table.code)
  if (!active) {
    toast.error('No active order found for this table')
    await refreshLive()
    return
  }
  selectedTable.value = table
  cart.value = {}
  try {
    viewingOrder.value = await ordersApi.getDetail(active.code)
  } catch {
    toast.error('Failed to load the order')
    selectedTable.value = null
  }
}

function backToTables() {
  selectedTable.value = null
  viewingOrder.value = null
  cart.value = {}
  notes.value = ''
  appendMode.value = false
}

// Open the menu to add more items to the order already on this table.
function startAddingItems() {
  if (!viewingOrder.value) return
  cart.value = {}
  notes.value = ''
  appendMode.value = true
}

// Leave add-items mode and return to the order's status view.
function cancelAppend() {
  appendMode.value = false
  cart.value = {}
  notes.value = ''
}

async function addItemsToExistingOrder() {
  if (!viewingOrder.value) return
  if (!cartItems.value.length) {
    toast.error('Please add items to the order')
    return
  }
  submitting.value = true
  try {
    for (const item of cartItems.value) {
      await orderItemApi.add(viewingOrder.value.code, { menuItemCode: item.code, quantity: item.quantity })
    }
    toast.success('Items added to the order')
    cart.value = {}
    notes.value = ''
    appendMode.value = false
    viewingOrder.value = await ordersApi.getDetail(viewingOrder.value.code)
    await refreshLive()
  } catch (e: any) {
    toast.error(e?.response?.data?.message ?? 'Failed to add items')
  } finally {
    submitting.value = false
  }
}

// Cart's primary action: append to the open order, or create a new one.
function submitCart() {
  if (appendMode.value) addItemsToExistingOrder()
  else submitOrder()
}

async function updateOrderStatus(status: 'SERVED' | 'COMPLETED') {
  if (!viewingOrder.value || updatingStatus.value) return
  updatingStatus.value = true
  try {
    await ordersApi.updateStatus(viewingOrder.value.code, { status })
    if (status === 'COMPLETED') {
      toast.success(`Table ${selectedTable.value?.tableNumber ?? ''} — order completed, table freed`)
      backToTables()
    } else {
      toast.success('Order marked as served')
      viewingOrder.value = await ordersApi.getDetail(viewingOrder.value.code)
    }
    await refreshLive()
  } catch {
    toast.error('Failed to update status')
  } finally {
    updatingStatus.value = false
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
    submittedOrder.value = {
      orderNumber: order.orderNumber,
      ticketNumber: order.ticketNumber ?? null,
      tableNumber: selectedTable.value.tableNumber,
    }
    backToTables()
    await refreshLive()
  } catch (e: any) {
    toast.error(e?.response?.data?.message ?? 'Failed to submit order')
  } finally {
    submitting.value = false
  }
}

function closeSubmittedDialog() {
  submittedOrder.value = null
}

function viewReceipt() {
  if (submittedOrder.value) router.push(`/receipt/${submittedOrder.value.orderNumber}`)
}

function viewTracking() {
  if (submittedOrder.value) router.push(`/track/${submittedOrder.value.orderNumber}`)
}

onMounted(async () => {
  await load()
  pollTimer = setInterval(refreshLive, 5000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div class="min-h-screen md:h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/30 flex flex-col md:overflow-hidden">
    <!-- Header -->
    <header class="bg-white/80 backdrop-blur border-b border-slate-200/60 px-3 sm:px-6 py-3 sm:py-4 flex-shrink-0">
      <div class="flex items-center justify-between gap-2">
        <div class="min-w-0">
          <h1 class="text-lg sm:text-2xl font-bold text-gray-900 truncate">Waiter Mode</h1>
          <p class="text-xs sm:text-sm text-gray-500 truncate">{{ headerSubtitle }}</p>
        </div>
        <button @click="router.push('/dashboard')"
          class="px-3 sm:px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 text-xs sm:text-sm font-medium flex-shrink-0">
          <span class="hidden sm:inline">Back to Dashboard</span>
          <span class="sm:hidden">← Back</span>
        </button>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="w-12 h-12 border-4 border-violet-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <div v-else class="flex-1 flex flex-col md:flex-row md:overflow-hidden">
      <!-- Left Panel -->
      <div class="flex-1 flex flex-col md:overflow-hidden min-w-0">

        <!-- Table Selection -->
        <div v-if="!selectedTable" class="flex-1 p-4 sm:p-6 md:overflow-y-auto">
          <h2 class="text-lg sm:text-xl font-semibold text-gray-900 mb-4">Select Table</h2>
          <div class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-2 sm:gap-3">
            <button v-for="table in tables" :key="table.code"
              @click="selectTable(table)"
              :disabled="table.status === 'CLEANING'"
              :class="statusClass(table.status)"
              class="aspect-square rounded-2xl p-3 sm:p-4 flex flex-col items-center justify-center transition-all font-semibold text-sm ring-1 ring-inset">
              <svg class="w-6 h-6 mb-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <span>Table {{ table.tableNumber }}</span>
              <span class="text-xs opacity-75">{{ table.capacity }} seats</span>
              <span class="text-xs mt-1 capitalize">{{ table.status.toLowerCase() }}</span>
            </button>
          </div>
          <p class="text-xs text-gray-400 mt-4">Tap an available table to take an order, or an occupied table to view and update its order.</p>
        </div>

        <!-- Existing-order view (occupied table) -->
        <div v-else-if="viewingOrder && !appendMode" class="flex-1 flex flex-col md:overflow-hidden p-3 sm:p-4">
          <div class="flex items-center justify-between gap-2 mb-3">
            <h2 class="text-lg sm:text-xl font-semibold text-gray-900 truncate">Table {{ selectedTable?.tableNumber }} — Order</h2>
            <button @click="backToTables"
              class="px-3 sm:px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 text-xs sm:text-sm flex-shrink-0 inline-flex items-center gap-1.5">
              <ArrowLeft class="w-4 h-4" /> Tables
            </button>
          </div>

          <div class="flex-1 md:overflow-y-auto">
            <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-4 sm:p-5">
              <div class="flex items-start justify-between gap-3 mb-4">
                <div>
                  <p class="text-[11px] uppercase tracking-widest text-slate-500 font-semibold">Ticket</p>
                  <p v-if="viewingOrder.ticketNumber != null" class="text-4xl font-extrabold tabular-nums text-slate-900 leading-none">
                    {{ String(viewingOrder.ticketNumber).padStart(3, '0') }}
                  </p>
                  <p v-else class="text-lg font-mono font-semibold text-slate-900">#{{ viewingOrder.orderNumber }}</p>
                  <p class="text-[10px] text-slate-400 mt-1 font-mono tabular-nums">{{ viewingOrder.orderNumber }}</p>
                </div>
                <StatusBadge :status="viewingOrder.status" />
              </div>

              <ul class="divide-y divide-slate-100 bg-slate-50/60 ring-1 ring-slate-200/60 rounded-xl overflow-hidden">
                <li v-for="item in viewingOrder.items" :key="item.code" class="flex items-center justify-between px-4 py-3 gap-3">
                  <div class="flex items-center gap-2.5 min-w-0">
                    <span class="inline-flex w-7 h-7 rounded-lg bg-white ring-1 ring-slate-200 items-center justify-center text-sm font-bold text-violet-600 tabular-nums flex-shrink-0">
                      {{ item.quantity }}
                    </span>
                    <span class="font-medium text-slate-900 text-sm truncate">{{ item.menuItemName ?? '—' }}</span>
                  </div>
                  <span class="text-sm font-semibold text-slate-900 tabular-nums flex-shrink-0">NPR {{ item.totalPrice.toFixed(0) }}</span>
                </li>
                <li v-if="!viewingOrder.items.length" class="px-4 py-3 text-sm text-slate-400 italic">No items.</li>
              </ul>

              <div v-if="viewingOrder.specialNotes" class="mt-3 px-3 py-2 rounded-lg bg-rose-50 ring-1 ring-rose-200 text-xs text-rose-700">
                <span class="font-semibold">Note:</span> {{ viewingOrder.specialNotes }}
              </div>
            </div>
          </div>
        </div>

        <!-- Menu Selection (new order) -->
        <div v-else class="flex-1 flex flex-col md:overflow-hidden p-3 sm:p-4">
          <div class="flex items-center justify-between gap-2 mb-3">
            <h2 class="text-lg sm:text-xl font-semibold text-gray-900 truncate">
              {{ appendMode ? 'Add Items' : 'Menu Items' }}
            </h2>
            <button @click="appendMode ? cancelAppend() : backToTables()"
              class="px-3 sm:px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 text-xs sm:text-sm flex-shrink-0">
              {{ appendMode ? 'Cancel' : 'Change Table' }}
            </button>
          </div>

          <!-- Category Tabs -->
          <div class="flex gap-2 mb-4 overflow-x-auto pb-1 flex-shrink-0">
            <button @click="activeCategory = 'All'"
              :class="activeCategory === 'All' ? 'bg-violet-500 text-white' : 'bg-white text-gray-700 border border-gray-200'"
              class="px-4 py-2 rounded-lg whitespace-nowrap text-sm font-medium transition-colors">
              All
            </button>
            <button v-for="cat in categories" :key="cat.code" @click="activeCategory = cat.code"
              :class="activeCategory === cat.code ? 'bg-violet-500 text-white' : 'bg-white text-gray-700 border border-gray-200'"
              class="px-4 py-2 rounded-lg whitespace-nowrap text-sm font-medium transition-colors">
              {{ cat.name }}
            </button>
          </div>

          <!-- Menu Grid -->
          <div class="flex-1 md:overflow-y-auto">
            <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2 sm:gap-3">
              <button v-for="item in filteredItems" :key="item.code" @click="addToCart(item.code)"
                class="bg-white rounded-xl p-3 border-2 text-left relative hover:border-violet-500 transition-colors flex items-center gap-3"
                :class="cart[item.code] ? 'border-violet-500' : 'border-gray-200'">
                <div v-if="cart[item.code]"
                  class="absolute -top-2 -right-2 bg-violet-500 text-white w-7 h-7 rounded-full flex items-center justify-center text-sm font-bold z-10">
                  {{ cart[item.code] }}
                </div>
                <div class="w-14 h-14 rounded-lg bg-slate-100 flex-shrink-0 overflow-hidden flex items-center justify-center">
                  <img v-if="item.fileCode && fileUrlCache[item.fileCode]" :src="fileUrlCache[item.fileCode]" :alt="item.name"
                    class="w-full h-full object-cover" loading="lazy" />
                  <UtensilsCrossed v-else class="w-5 h-5 text-slate-300" />
                </div>
                <div class="min-w-0 flex-1">
                  <h3 class="font-medium text-gray-900 text-sm mb-0.5 truncate">{{ item.name }}</h3>
                  <p class="text-[11px] text-gray-500 capitalize truncate">{{ item.categoryCode ? categories.find(c => c.code === item.categoryCode)?.name ?? '' : '' }}</p>
                  <p class="text-sm font-bold text-violet-500 mt-1">NPR {{ item.price.toFixed(0) }}</p>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Panel -->
      <div class="w-full md:w-80 bg-white border-t md:border-t-0 md:border-l border-gray-200 flex flex-col md:flex-shrink-0">

        <!-- View-order: status + actions -->
        <template v-if="viewingOrder && !appendMode">
          <div class="p-4 border-b">
            <h2 class="text-lg font-semibold text-gray-900">Order Status</h2>
            <p class="text-sm text-gray-500">Table {{ selectedTable?.tableNumber }}</p>
          </div>
          <div class="md:flex-1 md:overflow-y-auto p-4 space-y-4">
            <div class="bg-slate-50 ring-1 ring-slate-200/60 rounded-xl p-4 text-center">
              <p class="text-[11px] uppercase tracking-wider text-slate-500 font-semibold mb-2">Current status</p>
              <StatusBadge :status="viewingOrder.status" />
            </div>
            <div class="flex justify-between items-end px-1">
              <span class="font-semibold text-gray-900">Total</span>
              <span class="font-bold text-violet-600 text-xl tabular-nums">NPR {{ viewingOrder.totalAmount.toFixed(0) }}</span>
            </div>
            <p v-if="canMarkServed && viewingOrder.status !== 'READY'" class="text-xs text-amber-600 bg-amber-50 ring-1 ring-amber-200 rounded-lg px-3 py-2">
              The kitchen hasn't marked this order ready yet — confirm it's been delivered before marking served.
            </p>
          </div>
          <div class="p-4 border-t space-y-2">
            <button @click="startAddingItems"
              class="w-full py-3 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white font-semibold rounded-xl shadow-md shadow-violet-500/30 transition-all flex items-center justify-center gap-2">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              Add items
            </button>
            <button v-if="canMarkServed" @click="updateOrderStatus('SERVED')" :disabled="updatingStatus"
              class="w-full py-3 bg-gradient-to-r from-teal-500 to-emerald-500 hover:from-teal-600 hover:to-emerald-600 text-white font-semibold rounded-xl shadow-md shadow-teal-500/30 transition-all disabled:opacity-60 flex items-center justify-center gap-2">
              <Loader2 v-if="updatingStatus" class="w-5 h-5 animate-spin" />
              <UtensilsCrossed v-else class="w-5 h-5" />
              Mark as Served
            </button>
            <button v-if="canMarkComplete" @click="updateOrderStatus('COMPLETED')" :disabled="updatingStatus"
              class="w-full py-3 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white font-semibold rounded-xl shadow-md shadow-violet-500/30 transition-all disabled:opacity-60 flex items-center justify-center gap-2">
              <Loader2 v-if="updatingStatus" class="w-5 h-5 animate-spin" />
              <CheckCircle2 v-else class="w-5 h-5" />
              Mark as Complete
            </button>
            <button @click="router.push(`/receipt/${viewingOrder.orderNumber}`)"
              class="w-full py-2.5 bg-white ring-1 ring-slate-200 text-slate-700 text-sm font-medium rounded-xl hover:bg-slate-50 transition-colors">
              View / print receipt
            </button>
          </div>
        </template>

        <!-- New order: cart -->
        <template v-else>
          <div class="p-4 border-b">
            <h2 class="text-lg font-semibold text-gray-900">Order Summary</h2>
            <p v-if="selectedTable" class="text-sm text-gray-500">Table {{ selectedTable.tableNumber }}</p>
          </div>

          <div class="md:flex-1 md:overflow-y-auto p-4">
            <div v-if="cartItems.length === 0" class="flex flex-col items-center justify-center h-full text-gray-400 py-8">
              <svg class="w-12 h-12 mb-2 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <p>No items added yet</p>
            </div>
            <div v-else class="space-y-3">
              <div v-for="item in cartItems" :key="item.code" class="bg-gray-50 rounded-lg p-3">
                <div class="flex justify-between items-start mb-2 gap-2">
                  <div class="w-10 h-10 rounded-md bg-white flex-shrink-0 overflow-hidden flex items-center justify-center ring-1 ring-slate-200">
                    <img v-if="item.fileCode && fileUrlCache[item.fileCode]" :src="fileUrlCache[item.fileCode]" :alt="item.name"
                      class="w-full h-full object-cover" loading="lazy" />
                    <UtensilsCrossed v-else class="w-4 h-4 text-slate-300" />
                  </div>
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
            <textarea v-if="!appendMode" v-model="notes" placeholder="Order notes (allergies, special requests...)"
              class="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500 text-gray-900 resize-none text-sm"
              rows="2"></textarea>

            <div class="bg-violet-50 rounded-lg p-3">
              <div class="flex justify-between text-sm mb-1">
                <span class="text-gray-700">Subtotal</span>
                <span class="font-medium text-gray-900">NPR {{ cartTotal.toFixed(0) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="font-semibold text-gray-900">Total</span>
                <span class="font-bold text-violet-500 text-lg">NPR {{ cartTotal.toFixed(0) }}</span>
              </div>
            </div>

            <button @click="submitCart" :disabled="submitting"
              class="w-full py-3 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white font-medium rounded-xl shadow-md shadow-violet-500/30 transition-all disabled:opacity-60 flex items-center justify-center gap-2">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              {{ submitting ? (appendMode ? 'Adding…' : 'Submitting...') : (appendMode ? 'Add to Order' : 'Submit Order') }}
            </button>

            <button @click="() => { cart = {}; notes = '' }"
              class="w-full py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm">
              Clear Order
            </button>
          </div>
        </template>
      </div>
    </div>

    <!-- Submitted-order dialog: shows the ticket + actions to view receipt / tracking -->
    <Teleport to="body">
      <div v-if="submittedOrder" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" @click="closeSubmittedDialog" />
        <div class="relative bg-white rounded-3xl shadow-2xl ring-1 ring-slate-200/60 w-full max-w-sm p-6 text-center">
          <button @click="closeSubmittedDialog"
            class="absolute top-3 right-3 w-8 h-8 rounded-full text-slate-400 hover:text-slate-700 hover:bg-slate-100 flex items-center justify-center transition-colors">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>

          <div class="w-14 h-14 mx-auto rounded-2xl bg-gradient-to-br from-emerald-400 to-green-500 flex items-center justify-center shadow-lg shadow-emerald-500/30 mb-3">
            <svg class="w-7 h-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 class="text-lg font-bold text-slate-900">Order submitted</h2>
          <p class="text-sm text-slate-500 mt-0.5">Table {{ submittedOrder.tableNumber }}</p>

          <div class="mt-5">
            <p class="text-[11px] font-semibold uppercase tracking-widest text-slate-500">Ticket</p>
            <p v-if="submittedOrder.ticketNumber != null"
              class="text-6xl font-extrabold tabular-nums leading-none mt-1 bg-gradient-to-br from-violet-600 to-fuchsia-600 bg-clip-text text-transparent">
              {{ String(submittedOrder.ticketNumber).padStart(3, '0') }}
            </p>
            <p v-else class="text-sm font-mono text-slate-700 mt-2">{{ submittedOrder.orderNumber }}</p>
            <p class="text-[10px] text-slate-400 mt-2 font-mono tabular-nums">{{ submittedOrder.orderNumber }}</p>
          </div>

          <div class="mt-6 space-y-2">
            <button @click="viewReceipt"
              class="w-full py-2.5 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white text-sm font-semibold rounded-xl shadow-md shadow-violet-500/30 transition-all">
              View / print receipt
            </button>
            <button @click="viewTracking"
              class="w-full py-2.5 bg-white ring-1 ring-slate-200 text-slate-700 text-sm font-medium rounded-xl hover:bg-slate-50 transition-colors">
              Open tracking page
            </button>
            <button @click="closeSubmittedDialog"
              class="w-full py-2.5 text-sm text-slate-500 hover:text-slate-700 transition-colors">
              Take another order
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
