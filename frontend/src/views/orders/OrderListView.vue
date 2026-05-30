<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import { ClipboardList } from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import type { OrdersResponse, OrderStatus, OrderType } from '@/types'

const auth = useAuthStore()
const router = useRouter()
const orders = ref<OrdersResponse[]>([])
const loading = ref(false)
const filterStatus = ref<OrderStatus | ''>('')
const filterType = ref<OrderType | ''>('')

const orderStatuses: OrderStatus[] = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED']
const orderTypes: OrderType[] = ['DINE_IN', 'TAKEAWAY', 'QR_ORDER', 'KIOSK']

onMounted(loadOrders)

async function loadOrders() {
  if (!auth.restaurantCode) return
  loading.value = true
  try {
    const data = await ordersApi.search({
      restaurantCode: auth.restaurantCode,
      status: filterStatus.value || undefined,
      orderType: filterType.value || undefined,
      size: 200,
    })
    orders.value = data.content
  } catch {
    toast.error('Failed to load orders')
  } finally {
    loading.value = false
  }
}

async function updateStatus(code: string, status: OrderStatus) {
  try {
    await ordersApi.updateStatus(code, { status })
    toast.success('Status updated')
    loadOrders()
  } catch {
    toast.error('Failed to update status')
  }
}
</script>

<template>
  <RestaurantGuard resource="orders">
    <PageHeader title="Orders" description="Manage and track all orders" />

    <!-- Filters -->
    <div class="flex flex-wrap gap-3 mb-4">
      <select v-model="filterStatus" @change="loadOrders"
        class="px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
        <option value="">All Statuses</option>
        <option v-for="s in orderStatuses" :key="s" :value="s">{{ s }}</option>
      </select>
      <select v-model="filterType" @change="loadOrders"
        class="px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
        <option value="">All Types</option>
        <option v-for="t in orderTypes" :key="t" :value="t">{{ t.replace(/_/g, ' ') }}</option>
      </select>
      <button @click="loadOrders"
        class="px-4 py-2 text-sm bg-gray-100 rounded-lg hover:bg-gray-200">Refresh</button>
    </div>

    <div v-if="loading" class="text-center py-12 text-slate-400">Loading...</div>

    <div v-else class="bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60 overflow-x-auto">
      <table class="w-full text-sm min-w-[640px]">
        <thead class="bg-slate-50/60 text-slate-500 uppercase text-[11px] tracking-wide">
          <tr>
            <th class="px-5 py-3 text-left">Ticket</th>
            <th class="px-5 py-3 text-left">Order #</th>
            <th class="px-5 py-3 text-left">Type</th>
            <th class="px-5 py-3 text-left">Status</th>
            <th class="px-5 py-3 text-right">Total</th>
            <th class="px-5 py-3 text-left">Date</th>
            <th class="px-5 py-3 text-center">Update Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="order in orders" :key="order.code"
            class="hover:bg-gray-50 cursor-pointer"
            @click="router.push({ name: 'order-detail', params: { code: order.code } })">
            <td class="px-5 py-3">
              <span v-if="order.ticketNumber != null"
                class="inline-flex items-center justify-center min-w-[2.5rem] px-2 py-0.5 rounded-md bg-violet-50 text-violet-700 ring-1 ring-violet-200 font-mono font-bold text-sm tabular-nums">
                {{ String(order.ticketNumber).padStart(3, '0') }}
              </span>
              <span v-else class="text-slate-300 text-xs">—</span>
            </td>
            <td class="px-5 py-3 font-medium text-blue-600">{{ order.orderNumber }}</td>
            <td class="px-5 py-3 text-slate-500">{{ order.orderType.replace(/_/g, ' ') }}</td>
            <td class="px-5 py-3"><StatusBadge :status="order.status" /></td>
            <td class="px-5 py-3 text-right font-medium">{{ order.totalAmount.toFixed(2) }}</td>
            <td class="px-5 py-3 text-gray-400">{{ new Date(order.createdAt).toLocaleDateString() }}</td>
            <td class="px-5 py-3 text-center" @click.stop>
              <select
                :value="order.status"
                @change="updateStatus(order.code, ($event.target as HTMLSelectElement).value as OrderStatus)"
                class="text-xs px-2 py-1 bg-white border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300">
                <option v-for="s in orderStatuses" :key="s" :value="s">{{ s }}</option>
              </select>
            </td>
          </tr>
          <tr v-if="!orders.length">
            <td colspan="7" class="p-0">
              <EmptyState
                :icon="ClipboardList"
                title="No orders yet"
                description="Try adjusting the filters above, or have a customer place their first order via QR or kiosk."
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </RestaurantGuard>
</template>
