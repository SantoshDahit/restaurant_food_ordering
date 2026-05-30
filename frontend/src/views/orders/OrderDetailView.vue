<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderTypeLabel } from '@/utils/orderType'
import QRCode from 'qrcode'
import { ordersApi } from '@/api/orders'
import { orderItemApi } from '@/api/orderItem'
import { menuItemApi } from '@/api/menuItem'
import { useAuthStore } from '@/stores/auth'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import PageHeader from '@/components/shared/PageHeader.vue'
import ConfirmDialog from '@/components/shared/ConfirmDialog.vue'
import { toast } from 'vue-sonner'
import type { OrderDetailResponse, OrderItemDetail, MenuItemResponse, OrderStatus } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const order = ref<OrderDetailResponse | null>(null)
const orderItems = ref<OrderItemDetail[]>([])
const menuItems = ref<MenuItemResponse[]>([])
const loading = ref(false)
const removeTarget = ref<string | null>(null)
const removing = ref(false)
const showAddItem = ref(false)

const orderStatuses: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED']

const addForm = ref({
  menuItemCode: '',
  quantity: 1,
  spiceLevel: '',
  notes: '',
})

async function loadDetail(code: string) {
  const data = await ordersApi.getDetail(code)
  order.value = data
  orderItems.value = data.items
}

onMounted(async () => {
  const code = route.params.code as string
  loading.value = true
  try {
    const [, menuData] = await Promise.all([
      loadDetail(code),
      menuItemApi.search({ restaurantCode: auth.restaurantCode, size: 200 }),
    ])
    menuItems.value = menuData.content
  } catch {
    toast.error('Failed to load order')
  } finally {
    loading.value = false
  }
})

async function updateStatus(status: OrderStatus) {
  if (!order.value) return
  try {
    await ordersApi.updateStatus(order.value.code, { status })
    await loadDetail(order.value.code)
    toast.success('Status updated')
  } catch {
    toast.error('Failed to update status')
  }
}

async function addItem() {
  if (!order.value || !addForm.value.menuItemCode) { toast.error('Select a menu item'); return }
  try {
    await orderItemApi.add(order.value.code, {
      menuItemCode: addForm.value.menuItemCode,
      quantity: addForm.value.quantity,
      spiceLevel: addForm.value.spiceLevel || undefined,
      notes: addForm.value.notes || undefined,
    })
    await loadDetail(order.value.code)
    showAddItem.value = false
    addForm.value = { menuItemCode: '', quantity: 1, spiceLevel: '', notes: '' }
    toast.success('Item added')
  } catch {
    toast.error('Failed to add item')
  }
}

async function removeItem() {
  if (!order.value || !removeTarget.value) return
  removing.value = true
  try {
    await orderItemApi.remove(order.value.code, removeTarget.value)
    await loadDetail(order.value.code)
    removeTarget.value = null
    toast.success('Item removed')
  } catch {
    toast.error('Failed to remove item')
  } finally {
    removing.value = false
  }
}

function getMenuItemName(item: OrderItemDetail) {
  return item.menuItemName || item.menuItemCode
}

// ── Tracking URL + QR ───────────────────────────────────────────────────────
const trackingUrl = computed(() =>
  order.value ? `${window.location.origin}/track/${order.value.orderNumber}` : '',
)
const trackingQr = ref<string>('')

watch(trackingUrl, async (url) => {
  if (!url) { trackingQr.value = ''; return }
  trackingQr.value = await QRCode.toDataURL(url, {
    width: 220, margin: 2, color: { dark: '#111827', light: '#ffffff' },
  })
}, { immediate: true })

async function copyToClipboard(text: string): Promise<boolean> {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text); return true
    }
  } catch { /* fall through */ }
  try {
    const ta = document.createElement('textarea')
    ta.value = text; ta.setAttribute('readonly', '')
    ta.style.position = 'fixed'; ta.style.opacity = '0'
    document.body.appendChild(ta); ta.select()
    const ok = document.execCommand('copy')
    document.body.removeChild(ta); return ok
  } catch { return false }
}

async function copyTrackingUrl() {
  const ok = await copyToClipboard(trackingUrl.value)
  ok ? toast.success('Tracking URL copied!') : toast.error('Copy failed')
}

function openTrackingPage() {
  if (trackingUrl.value) window.open(trackingUrl.value, '_blank')
}
</script>

<template>
  <div>
    <PageHeader :title="`Order ${order?.orderNumber || ''}`" description="Order details and items">
      <template #actions>
        <button v-if="order" @click="$router.push({ path: `/receipt/${order.orderNumber}`, query: { from: 'order', code: order.code } })"
          class="px-4 py-2 text-sm bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">
          Print receipt
        </button>
        <button @click="router.push({ name: 'orders' })"
          class="px-4 py-2 text-sm bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">
          ← Back
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-slate-400">Loading...</div>

    <div v-else-if="order" class="space-y-6">
      <!-- Order Info -->
      <div class="bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60 p-6">
        <div class="grid grid-cols-2 md:grid-cols-5 gap-4 mb-4">
          <div>
            <p class="text-xs text-slate-500">Ticket</p>
            <p v-if="order.ticketNumber != null"
              class="inline-flex items-center justify-center min-w-[3rem] px-2 py-0.5 rounded-md bg-violet-50 text-violet-700 ring-1 ring-violet-200 font-mono font-bold text-lg tabular-nums">
              {{ String(order.ticketNumber).padStart(3, '0') }}
            </p>
            <p v-else class="text-slate-300 text-sm">—</p>
          </div>
          <div>
            <p class="text-xs text-slate-500">Order Number</p>
            <p class="font-semibold">{{ order.orderNumber }}</p>
          </div>
          <div>
            <p class="text-xs text-slate-500">Type</p>
            <p class="font-semibold">{{ orderTypeLabel(order.orderType) }}</p>
          </div>
          <div>
            <p class="text-xs text-slate-500">Status</p>
            <StatusBadge :status="order.status" />
          </div>
          <div>
            <p class="text-xs text-slate-500">Date</p>
            <p class="font-semibold">{{ new Date(order.createdAt).toLocaleString() }}</p>
          </div>
        </div>

        <div v-if="order.tableNumber || order.waiterName || order.restaurantName"
          class="grid grid-cols-2 md:grid-cols-3 gap-4 mt-4 pt-4 border-t border-gray-100">
          <div v-if="order.restaurantName">
            <p class="text-xs text-slate-500">Restaurant</p>
            <p class="font-medium">{{ order.restaurantName }}</p>
          </div>
          <div v-if="order.tableNumber">
            <p class="text-xs text-slate-500">Table</p>
            <p class="font-medium">{{ order.tableNumber }}</p>
          </div>
          <div v-if="order.waiterName">
            <p class="text-xs text-slate-500">Waiter</p>
            <p class="font-medium">{{ order.waiterName }}</p>
          </div>
        </div>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4 pt-4 border-t border-gray-100">
          <div>
            <p class="text-xs text-slate-500">Subtotal</p>
            <p class="font-medium">{{ order.subtotal.toFixed(2) }}</p>
          </div>
          <div>
            <p class="text-xs text-slate-500">Discount</p>
            <p class="font-medium text-red-600">-{{ order.discountAmount.toFixed(2) }}</p>
          </div>
          <div>
            <p class="text-xs text-slate-500">Tax</p>
            <p class="font-medium">{{ order.taxAmount.toFixed(2) }}</p>
          </div>
          <div>
            <p class="text-xs text-slate-500">Total</p>
            <p class="text-lg font-bold text-gray-900">{{ order.totalAmount.toFixed(2) }}</p>
          </div>
        </div>

        <!-- Update Status -->
        <div class="mt-4 pt-4 border-t border-gray-100 flex items-center gap-3">
          <span class="text-sm text-gray-600">Update Status:</span>
          <select
            :value="order.status"
            @change="updateStatus(($event.target as HTMLSelectElement).value as OrderStatus)"
            class="px-3 py-1.5 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
            <option v-for="s in orderStatuses" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
      </div>

      <!-- Customer Tracking -->
      <div class="bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60 p-6">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="font-semibold text-slate-800">Customer Tracking</h3>
            <p class="text-xs text-slate-500 mt-0.5">Share this link or QR so the customer can follow their order live.</p>
          </div>
        </div>
        <div class="flex flex-col sm:flex-row gap-5 items-start">
          <div class="flex-shrink-0 self-center sm:self-start">
            <div class="bg-white rounded-2xl ring-1 ring-slate-200 p-2">
              <img v-if="trackingQr" :src="trackingQr" alt="Tracking QR"
                class="w-40 h-40 sm:w-44 sm:h-44 block" />
            </div>
          </div>
          <div class="flex-1 min-w-0 w-full">
            <p class="text-xs text-slate-500 mb-1.5 font-medium uppercase tracking-wider">Tracking URL</p>
            <div class="flex items-center gap-2 bg-slate-50 ring-1 ring-slate-200 rounded-xl px-3 py-2">
              <code class="flex-1 min-w-0 text-xs sm:text-sm font-mono text-slate-700 truncate">{{ trackingUrl }}</code>
            </div>
            <div class="flex flex-wrap gap-2 mt-3">
              <button @click="copyTrackingUrl"
                class="px-4 py-2 text-sm font-medium bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white rounded-xl shadow-sm shadow-violet-500/30 transition-all">
                Copy URL
              </button>
              <button @click="openTrackingPage"
                class="px-4 py-2 text-sm font-medium bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">
                Open tracking page
              </button>
            </div>
            <p class="text-[11px] text-slate-400 mt-3">Customers see the live status, a 4-step timeline, and the items list. No login required.</p>
          </div>
        </div>
      </div>

      <!-- Order Items -->
      <div class="bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60">
        <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 class="font-semibold text-slate-800">Order Items</h3>
          <button @click="showAddItem = !showAddItem"
            class="px-3 py-1.5 text-sm bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white rounded-lg shadow-sm shadow-violet-500/30 transition-all">
            + Add Item
          </button>
        </div>

        <!-- Add Item Form -->
        <div v-if="showAddItem" class="px-5 py-4 bg-gray-50 border-b border-gray-100">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <div class="col-span-2">
              <label class="block text-xs font-medium text-gray-600 mb-1">Menu Item</label>
              <select v-model="addForm.menuItemCode"
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
                <option value="">Select item...</option>
                <option v-for="m in menuItems" :key="m.code" :value="m.code">
                  {{ m.name }} ({{ m.price.toFixed(2) }})
                </option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-600 mb-1">Qty</label>
              <input v-model.number="addForm.quantity" type="number" min="1"
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-600 mb-1">Spice Level</label>
              <input v-model="addForm.spiceLevel" placeholder="e.g. Mild"
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
            </div>
          </div>
          <div class="flex gap-2 mt-3">
            <button @click="addItem"
              class="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700">Add</button>
            <button @click="showAddItem = false"
              class="px-4 py-2 text-sm bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">Cancel</button>
          </div>
        </div>

        <div class="overflow-x-auto">
        <table class="w-full text-sm min-w-[640px]">
          <thead class="bg-slate-50/60 text-slate-500 uppercase text-[11px] tracking-wide">
            <tr>
              <th class="px-5 py-3 text-left">Item</th>
              <th class="px-5 py-3 text-center">Qty</th>
              <th class="px-5 py-3 text-right">Unit Price</th>
              <th class="px-5 py-3 text-right">Total</th>
              <th class="px-5 py-3 text-left">Notes</th>
              <th class="px-5 py-3 text-center">Action</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="item in orderItems" :key="item.code" class="hover:bg-slate-50/60 transition-colors">
              <td class="px-5 py-3 font-medium">{{ getMenuItemName(item) }}</td>
              <td class="px-5 py-3 text-center">{{ item.quantity }}</td>
              <td class="px-5 py-3 text-right">{{ item.unitPrice.toFixed(2) }}</td>
              <td class="px-5 py-3 text-right font-semibold">{{ item.totalPrice.toFixed(2) }}</td>
              <td class="px-5 py-3 text-gray-500 text-xs">{{ item.notes || '—' }}</td>
              <td class="px-5 py-3 text-center">
                <button @click="removeTarget = item.code"
                  class="text-xs px-2 py-1 bg-rose-50 ring-1 ring-rose-200 text-rose-600 rounded-lg hover:bg-rose-100 transition-colors">Remove</button>
              </td>
            </tr>
            <tr v-if="!orderItems.length">
              <td colspan="6" class="px-5 py-8 text-center text-slate-400">No items</td>
            </tr>
          </tbody>
        </table>
        </div>
      </div>
    </div>

    <ConfirmDialog
      :open="!!removeTarget"
      title="Remove Item"
      message="Remove this item from the order?"
      :loading="removing"
      @confirm="removeItem"
      @cancel="removeTarget = null"
    />
  </div>
</template>
