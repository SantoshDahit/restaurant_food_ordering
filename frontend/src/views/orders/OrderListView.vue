<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
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
  loading.value = true
  try {
    const data = await ordersApi.search({
      restaurantCode: auth.restaurantCode,
      status: filterStatus.value || undefined,
      orderType: filterType.value || undefined,
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
  <div>
    <PageHeader title="Orders" description="Manage and track all orders" />

    <!-- Filters -->
    <div class="flex flex-wrap gap-3 mb-4">
      <select v-model="filterStatus" @change="loadOrders"
        class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
        <option value="">All Statuses</option>
        <option v-for="s in orderStatuses" :key="s" :value="s">{{ s }}</option>
      </select>
      <select v-model="filterType" @change="loadOrders"
        class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
        <option value="">All Types</option>
        <option v-for="t in orderTypes" :key="t" :value="t">{{ t.replace(/_/g, ' ') }}</option>
      </select>
      <button @click="loadOrders"
        class="px-4 py-2 text-sm bg-gray-100 rounded-lg hover:bg-gray-200">Refresh</button>
    </div>

    <div v-if="loading" class="text-center py-12 text-gray-400">Loading...</div>

    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-gray-500 uppercase text-xs">
          <tr>
            <th class="px-5 py-3 text-left">Order #</th>
            <th class="px-5 py-3 text-left">Type</th>
            <th class="px-5 py-3 text-left">Status</th>
            <th class="px-5 py-3 text-right">Total</th>
            <th class="px-5 py-3 text-left">Date</th>
            <th class="px-5 py-3 text-center">Update Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="order in orders" :key="order.code"
            class="hover:bg-gray-50 cursor-pointer"
            @click="router.push({ name: 'order-detail', params: { code: order.code } })">
            <td class="px-5 py-3 font-medium text-blue-600">{{ order.orderNumber }}</td>
            <td class="px-5 py-3 text-gray-500">{{ order.orderType.replace(/_/g, ' ') }}</td>
            <td class="px-5 py-3"><StatusBadge :status="order.status" /></td>
            <td class="px-5 py-3 text-right font-medium">{{ order.totalAmount.toFixed(2) }}</td>
            <td class="px-5 py-3 text-gray-400">{{ new Date(order.createdAt).toLocaleDateString() }}</td>
            <td class="px-5 py-3 text-center" @click.stop>
              <select
                :value="order.status"
                @change="updateStatus(order.code, ($event.target as HTMLSelectElement).value as OrderStatus)"
                class="text-xs px-2 py-1 border border-gray-300 rounded focus:outline-none">
                <option v-for="s in orderStatuses" :key="s" :value="s">{{ s }}</option>
              </select>
            </td>
          </tr>
          <tr v-if="!orders.length">
            <td colspan="6" class="px-5 py-8 text-center text-gray-400">No orders found</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
