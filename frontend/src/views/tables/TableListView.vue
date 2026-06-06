<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import { useAuthStore } from '@/stores/auth'
import { tableApi } from '@/api/table'
import { ordersApi } from '@/api/orders'
import { paymentApi } from '@/api/payment'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import ConfirmDialog from '@/components/shared/ConfirmDialog.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import { toast } from 'vue-sonner'
import type { RestaurantTableResponse, TableStatus, OrdersResponse, OrderStatus, PaymentResponse, PaymentStatus } from '@/types'

const ORDER_STATUSES: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SERVED', 'COMPLETED', 'CANCELLED']
const PAYMENT_STATUSES: PaymentStatus[] = ['PENDING', 'COMPLETED', 'FAILED', 'REFUNDED']

const auth = useAuthStore()
const router = useRouter()
const tables = ref<RestaurantTableResponse[]>([])
const activeOrderByTable = ref<Record<string, OrdersResponse>>({})
const paymentByOrder = ref<Record<string, PaymentResponse | null>>({})
const updatingOrder = ref<string | null>(null)
const updatingPayment = ref<string | null>(null)
const loading = ref(false)
const showFormDialog = ref(false)
const deleteTarget = ref<string | null>(null)
const deleting = ref(false)
const editTarget = ref<RestaurantTableResponse | null>(null)
const form = ref({ tableNumber: '', capacity: 4 })

// QR modal state
const qrModal = ref<{ table: RestaurantTableResponse; dataUrl: string } | null>(null)

const statusBorder: Record<TableStatus, string> = {
  AVAILABLE: 'border-green-400',
  OCCUPIED: 'border-red-400',
  RESERVED: 'border-yellow-400',
  CLEANING: 'border-gray-400',
}

const ACTIVE_STATUSES: ReadonlySet<OrderStatus> = new Set(['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SERVED'])

onMounted(async () => {
  await Promise.all([loadTables(), loadActiveOrders()])
})

async function loadTables() {
  if (!auth.restaurantCode) return
  loading.value = true
  try {
    const data = await tableApi.search({ restaurantCode: auth.restaurantCode, size: 200 })
    tables.value = data.content
  } catch {
    toast.error('Failed to load tables')
  } finally {
    loading.value = false
  }
}

async function loadActiveOrders() {
  if (!auth.restaurantCode) return
  try {
    const data = await ordersApi.search({ restaurantCode: auth.restaurantCode, size: 200 })
    const map: Record<string, OrdersResponse> = {}
    for (const order of data.content) {
      if (!order.tableCode) continue
      if (!ACTIVE_STATUSES.has(order.status)) continue
      // Search returns createdAt desc, so first hit per table is the most recent.
      if (!map[order.tableCode]) map[order.tableCode] = order
    }
    activeOrderByTable.value = map

    // Pull each active order's payment (if any) so the card can show + update
    // its payment status. Independent calls; failures leave the entry undefined.
    const payments = await Promise.all(
      Object.values(map).map(o => paymentApi.getByOrder(o.code).catch(() => null)))
    const payMap: Record<string, PaymentResponse | null> = {}
    Object.values(map).forEach((o, i) => { payMap[o.code] = payments[i] })
    paymentByOrder.value = payMap
  } catch {
    // Non-fatal: tables still render without the active-order panel.
  }
}

async function updateOrderStatus(orderCode: string, status: OrderStatus) {
  updatingOrder.value = orderCode
  try {
    await ordersApi.updateStatus(orderCode, { status })
    toast.success(`Order set to ${status}`)
    await loadActiveOrders()
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Could not update order status')
  } finally {
    updatingOrder.value = null
  }
}

async function updatePaymentStatus(paymentCode: string, status: PaymentStatus) {
  updatingPayment.value = paymentCode
  try {
    await paymentApi.updateStatus(paymentCode, { status })
    toast.success(`Payment set to ${status}`)
    await loadActiveOrders()
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Could not update payment status')
  } finally {
    updatingPayment.value = null
  }
}

// Record a cash payment at the counter for an order that has none yet, and mark
// it paid — the common "customer paid cash at the till" case.
async function markCashPaid(order: OrdersResponse) {
  updatingPayment.value = order.code
  try {
    const payment = await paymentApi.create({
      restaurantCode: order.restaurantCode,
      orderCode: order.code,
      paymentMethod: 'CASH',
      amount: order.totalAmount,
    })
    await paymentApi.updateStatus(payment.code, { status: 'COMPLETED' })
    toast.success('Marked paid (cash)')
    await loadActiveOrders()
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Could not record payment')
  } finally {
    updatingPayment.value = null
  }
}

function openOrder(code: string) {
  router.push({ name: 'order-detail', params: { code } })
}

function openAdd() {
  editTarget.value = null
  form.value = { tableNumber: '', capacity: 4 }
  showFormDialog.value = true
}

function openEdit(table: RestaurantTableResponse) {
  editTarget.value = table
  form.value = { tableNumber: table.tableNumber, capacity: table.capacity }
  showFormDialog.value = true
}

async function save() {
  try {
    if (editTarget.value) {
      await tableApi.update(editTarget.value.code, form.value)
      toast.success('Table updated')
    } else {
      await tableApi.create({ restaurantCode: auth.restaurantCode, ...form.value })
      toast.success('Table added')
    }
    showFormDialog.value = false
    loadTables()
  } catch {
    toast.error('Operation failed')
  }
}

async function showQr(table: RestaurantTableResponse) {
  const url = tableUrl(table.tableCode)
  const dataUrl = await QRCode.toDataURL(url, { width: 300, margin: 2, color: { dark: '#111827', light: '#ffffff' } })
  qrModal.value = { table, dataUrl }
}

function tableUrl(tableCode: string) {
  return `${window.location.origin}/table/${tableCode}`
}

// Open this table's ordering view in a new tab, so staff keep the dashboard open.
// The ?shared=1 flag marks it as a shared restaurant tablet (vs a customer who
// SCANS the table QR on their own phone) so the payment screen shows scan-to-pay
// (Fonepay) + cash/POS instead of personal-login wallets.
function launchTableMode(tableCode: string) {
  window.open(`${tableUrl(tableCode)}?shared=1`, '_blank')
}

function downloadQr() {
  if (!qrModal.value) return
  const a = document.createElement('a')
  a.href = qrModal.value.dataUrl
  a.download = `table-${qrModal.value.table.tableNumber}-qr.png`
  a.click()
}

async function copyToClipboard(text: string): Promise<boolean> {
  // navigator.clipboard requires a secure context (https or localhost).
  // Fall back to a hidden textarea + execCommand for plain-http LAN access.
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
      return true
    }
  } catch { /* fall through */ }
  try {
    const ta = document.createElement('textarea')
    ta.value = text
    ta.setAttribute('readonly', '')
    ta.style.position = 'fixed'
    ta.style.top = '0'
    ta.style.left = '0'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    const ok = document.execCommand('copy')
    document.body.removeChild(ta)
    return ok
  } catch {
    return false
  }
}

async function copyUrl() {
  if (!qrModal.value) return
  const ok = await copyToClipboard(tableUrl(qrModal.value.table.tableCode))
  ok ? toast.success('URL copied!') : toast.error('Copy failed — long-press to copy manually')
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await tableApi.delete(deleteTarget.value)
    toast.success('Table deleted')
    deleteTarget.value = null
    loadTables()
  } catch {
    toast.error('Delete failed')
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <RestaurantGuard resource="tables">
    <PageHeader title="Tables" description="Manage restaurant tables">
      <template #actions>
        <button @click="openAdd"
          class="px-4 py-2 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white text-sm rounded-xl shadow-md shadow-violet-500/30 transition-all">
          + Add Table
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-slate-400">Loading...</div>

    <div v-else class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
      <div
        v-for="table in tables"
        :key="table.code"
        :class="['bg-white rounded-xl p-4 shadow-sm border-2', statusBorder[table.status]]"
      >
        <div class="flex items-start justify-between mb-3">
          <div>
            <p class="text-xl font-bold text-gray-900">#{{ table.tableNumber }}</p>
            <p class="text-xs text-gray-400">Cap: {{ table.capacity }}</p>
          </div>
          <StatusBadge :status="table.status" />
        </div>

        <!-- Launch this table's ordering view in a new tab -->
        <button @click="launchTableMode(table.tableCode)"
          class="w-full mb-3 inline-flex items-center justify-center gap-2 px-3 py-2 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white text-sm font-semibold rounded-lg shadow-sm shadow-violet-500/30 transition-all">
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
          Launch table mode
        </button>

        <!-- Active order: info + quick order/payment status controls -->
        <div v-if="activeOrderByTable[table.code]"
          class="w-full mb-3 bg-blue-50 border border-blue-200 rounded-lg p-2 space-y-2">
          <button @click="openOrder(activeOrderByTable[table.code].code)" class="w-full text-left">
            <div class="flex items-center justify-between mb-1">
              <span class="text-xs font-semibold text-blue-700">{{ activeOrderByTable[table.code].orderNumber }}</span>
              <StatusBadge :status="activeOrderByTable[table.code].status" />
            </div>
            <div class="text-xs text-gray-600">
              Total <span class="font-semibold text-gray-900">{{ activeOrderByTable[table.code].totalAmount.toFixed(2) }}</span>
            </div>
          </button>

          <!-- Order status -->
          <div>
            <label class="block text-[10px] font-medium text-slate-500 uppercase tracking-wide mb-0.5">Order status</label>
            <select
              :value="activeOrderByTable[table.code].status"
              :disabled="updatingOrder === activeOrderByTable[table.code].code"
              @change="updateOrderStatus(activeOrderByTable[table.code].code, ($event.target as HTMLSelectElement).value as OrderStatus)"
              class="w-full text-xs rounded-md border border-slate-200 bg-white px-2 py-1 focus:outline-none focus:ring-2 focus:ring-violet-500/40 disabled:opacity-50">
              <option v-for="s in ORDER_STATUSES" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>

          <!-- Payment status -->
          <div>
            <label class="block text-[10px] font-medium text-slate-500 uppercase tracking-wide mb-0.5">Payment</label>
            <select v-if="paymentByOrder[activeOrderByTable[table.code].code]"
              :value="paymentByOrder[activeOrderByTable[table.code].code]!.status"
              :disabled="updatingPayment === paymentByOrder[activeOrderByTable[table.code].code]!.code"
              @change="updatePaymentStatus(paymentByOrder[activeOrderByTable[table.code].code]!.code, ($event.target as HTMLSelectElement).value as PaymentStatus)"
              class="w-full text-xs rounded-md border border-slate-200 bg-white px-2 py-1 focus:outline-none focus:ring-2 focus:ring-emerald-500/40 disabled:opacity-50">
              <option v-for="s in PAYMENT_STATUSES" :key="s" :value="s">{{ s }}</option>
            </select>
            <button v-else
              @click="markCashPaid(activeOrderByTable[table.code])"
              :disabled="updatingPayment === activeOrderByTable[table.code].code"
              class="w-full text-xs px-2 py-1 bg-emerald-50 ring-1 ring-emerald-200 text-emerald-700 rounded-md hover:bg-emerald-100 transition-colors disabled:opacity-50">
              Mark paid (cash)
            </button>
          </div>
        </div>

        <!-- QR preview thumbnail (always available, points at /table/<code>) -->
        <div class="mb-3">
          <button @click="showQr(table)" class="w-full group">
            <div class="bg-gray-50 rounded-lg p-2 flex items-center gap-2 hover:bg-violet-50 transition-colors border border-gray-100 hover:border-violet-200">
              <svg class="w-4 h-4 text-gray-400 group-hover:text-violet-500 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
              </svg>
              <span class="text-xs text-gray-500 group-hover:text-violet-600 font-medium">View QR Code</span>
            </div>
          </button>
        </div>

        <div class="flex flex-wrap gap-1">
          <button @click="openEdit(table)"
            class="text-xs px-2 py-1 bg-slate-50 ring-1 ring-slate-200 text-slate-700 rounded-lg hover:bg-slate-100 transition-colors">Edit</button>
          <button @click="deleteTarget = table.code"
            class="text-xs px-2 py-1 bg-rose-50 ring-1 ring-rose-200 text-rose-600 rounded-lg hover:bg-rose-100 transition-colors">Del</button>
        </div>
      </div>
      <div v-if="!tables.length" class="col-span-full text-center py-12 text-slate-400">
        No tables found
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <Teleport to="body">
      <div v-if="showFormDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="showFormDialog = false" />
        <div class="relative bg-white rounded-2xl shadow-xl ring-1 ring-slate-200/60 p-6 w-full max-w-sm mx-4">
          <h3 class="text-lg font-semibold mb-4">{{ editTarget ? 'Edit Table' : 'Add Table' }}</h3>
          <form @submit.prevent="save" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Table Number</label>
              <input v-model="form.tableNumber" required
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Capacity</label>
              <input v-model.number="form.capacity" type="number" min="1"
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showFormDialog = false"
                class="px-4 py-2 text-sm bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">Cancel</button>
              <button type="submit"
                class="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700">Save</button>
            </div>
          </form>
        </div>
      </div>

      <!-- QR Code Modal -->
      <div v-if="qrModal" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/60" @click="qrModal = null" />
        <div class="relative bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm mx-4 text-center">
          <button @click="qrModal = null"
            class="absolute top-3 right-3 p-1.5 hover:bg-gray-100 rounded-lg text-slate-500">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>

          <h3 class="text-lg font-bold text-gray-900 mb-1">Table {{ qrModal.table.tableNumber }}</h3>
          <p class="text-sm text-gray-500 mb-4">Scan to order from this table</p>

          <!-- QR Image -->
          <div class="flex justify-center mb-4">
            <img :src="qrModal.dataUrl" alt="QR Code"
              class="w-56 h-56 rounded-xl border border-gray-100 shadow-sm" />
          </div>

          <!-- URL display -->
          <div class="bg-gray-50 rounded-xl p-3 mb-4 break-all">
            <p class="text-xs text-gray-500 mb-1 font-medium">Customer URL</p>
            <p class="text-xs text-gray-700 font-mono">
              {{ tableUrl(qrModal.table.tableCode) }}
            </p>
          </div>

          <div class="flex gap-2">
            <button @click="downloadQr"
              class="flex-1 py-2.5 bg-violet-500 text-white text-sm font-medium rounded-xl hover:bg-violet-600 transition-colors">
              Download PNG
            </button>
            <button @click="copyUrl"
              class="flex-1 py-2.5 bg-gray-100 text-gray-700 text-sm font-medium rounded-xl hover:bg-gray-200 transition-colors">
              Copy URL
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <ConfirmDialog
      :open="!!deleteTarget"
      title="Delete Table"
      message="Are you sure you want to delete this table?"
      :loading="deleting"
      @confirm="confirmDelete"
      @cancel="deleteTarget = null"
    />
  </RestaurantGuard>
</template>
