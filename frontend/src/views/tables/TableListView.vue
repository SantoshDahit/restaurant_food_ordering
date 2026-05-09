<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import QRCode from 'qrcode'
import { useAuthStore } from '@/stores/auth'
import { tableApi } from '@/api/table'
import { ordersApi } from '@/api/orders'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import ConfirmDialog from '@/components/shared/ConfirmDialog.vue'
import { toast } from 'vue-sonner'
import type { RestaurantTableResponse, TableStatus, OrdersResponse, OrderStatus } from '@/types'

const auth = useAuthStore()
const router = useRouter()
const tables = ref<RestaurantTableResponse[]>([])
const activeOrderByTable = ref<Record<string, OrdersResponse>>({})
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

const ACTIVE_STATUSES: ReadonlySet<OrderStatus> = new Set(['PENDING', 'CONFIRMED', 'PREPARING', 'READY'])

onMounted(async () => {
  await Promise.all([loadTables(), loadActiveOrders()])
})

async function loadTables() {
  loading.value = true
  try {
    const data = await tableApi.search({ restaurantCode: auth.restaurantCode })
    tables.value = data.content
  } catch {
    toast.error('Failed to load tables')
  } finally {
    loading.value = false
  }
}

async function loadActiveOrders() {
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
  } catch {
    // Non-fatal: tables still render without the active-order panel.
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

async function generateQr(table: RestaurantTableResponse) {
  try {
    const updated = await tableApi.generateQr(table.code)
    toast.success('QR code generated')
    await loadTables()
    // Auto-open QR modal with the freshly generated token
    await showQr(updated)
  } catch {
    toast.error('Failed to generate QR')
  }
}

async function showQr(table: RestaurantTableResponse) {
  if (!table.qrCodeToken) {
    toast.error('No QR token — click "Gen QR" first')
    return
  }
  const url = `${window.location.origin}/qr/${table.qrCodeToken}`
  const dataUrl = await QRCode.toDataURL(url, { width: 300, margin: 2, color: { dark: '#111827', light: '#ffffff' } })
  qrModal.value = { table, dataUrl }
}

function qrUrl(token: string) {
  return `${window.location.origin}/qr/${token}`
}

function downloadQr() {
  if (!qrModal.value) return
  const a = document.createElement('a')
  a.href = qrModal.value.dataUrl
  a.download = `table-${qrModal.value.table.tableNumber}-qr.png`
  a.click()
}

function copyUrl() {
  if (!qrModal.value?.table.qrCodeToken) return
  navigator.clipboard.writeText(qrUrl(qrModal.value.table.qrCodeToken))
  toast.success('URL copied!')
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
  <div>
    <PageHeader title="Tables" description="Manage restaurant tables">
      <template #actions>
        <button @click="openAdd"
          class="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">
          + Add Table
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-gray-400">Loading...</div>

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

        <!-- Active order panel -->
        <button
          v-if="activeOrderByTable[table.code]"
          @click="openOrder(activeOrderByTable[table.code].code)"
          class="w-full mb-3 text-left bg-blue-50 hover:bg-blue-100 border border-blue-200 rounded-lg p-2 transition-colors">
          <div class="flex items-center justify-between mb-1">
            <span class="text-xs font-semibold text-blue-700">{{ activeOrderByTable[table.code].orderNumber }}</span>
            <StatusBadge :status="activeOrderByTable[table.code].status" />
          </div>
          <div class="text-xs text-gray-600">
            Total <span class="font-semibold text-gray-900">{{ activeOrderByTable[table.code].totalAmount.toFixed(2) }}</span>
          </div>
        </button>

        <!-- QR preview thumbnail if token exists -->
        <div v-if="table.qrCodeToken" class="mb-3">
          <button @click="showQr(table)" class="w-full group">
            <div class="bg-gray-50 rounded-lg p-2 flex items-center gap-2 hover:bg-orange-50 transition-colors border border-gray-100 hover:border-orange-200">
              <svg class="w-4 h-4 text-gray-400 group-hover:text-orange-500 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
              </svg>
              <span class="text-xs text-gray-500 group-hover:text-orange-600 font-medium">View QR Code</span>
            </div>
          </button>
        </div>

        <div class="flex flex-wrap gap-1">
          <button @click="openEdit(table)"
            class="text-xs px-2 py-1 bg-gray-100 rounded hover:bg-gray-200">Edit</button>
          <button @click="generateQr(table)"
            class="text-xs px-2 py-1 bg-orange-100 text-orange-700 rounded hover:bg-orange-200">
            {{ table.qrCodeToken ? 'Regen QR' : 'Gen QR' }}
          </button>
          <button @click="deleteTarget = table.code"
            class="text-xs px-2 py-1 bg-red-100 text-red-600 rounded hover:bg-red-200">Del</button>
        </div>
      </div>
      <div v-if="!tables.length" class="col-span-full text-center py-12 text-gray-400">
        No tables found
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <Teleport to="body">
      <div v-if="showFormDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="showFormDialog = false" />
        <div class="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
          <h3 class="text-lg font-semibold mb-4">{{ editTarget ? 'Edit Table' : 'Add Table' }}</h3>
          <form @submit.prevent="save" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Table Number</label>
              <input v-model="form.tableNumber" required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Capacity</label>
              <input v-model.number="form.capacity" type="number" min="1"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showFormDialog = false"
                class="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
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
            class="absolute top-3 right-3 p-1.5 hover:bg-gray-100 rounded-lg text-gray-500">
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
              {{ qrUrl(qrModal.table.qrCodeToken!) }}
            </p>
          </div>

          <div class="flex gap-2">
            <button @click="downloadQr"
              class="flex-1 py-2.5 bg-orange-500 text-white text-sm font-medium rounded-xl hover:bg-orange-600 transition-colors">
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
  </div>
</template>
