<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import type { OrdersResponse } from '@/types'

const auth = useAuthStore()
const recentOrders = ref<OrdersResponse[]>([])
const stats = ref({ totalOrders: 0, revenue: 0, pendingOrders: 0 })

onMounted(async () => {
  try {
    const data = await ordersApi.search({ restaurantCode: auth.restaurantCode })
    recentOrders.value = data.content.slice(0, 10)
    stats.value.totalOrders = data.totalElements
    stats.value.pendingOrders = data.content.filter(o => o.status === 'PENDING').length
    stats.value.revenue = data.content
      .filter(o => o.status === 'COMPLETED')
      .reduce((sum, o) => sum + o.totalAmount, 0)
  } catch { /* silent */ }
})
</script>

<template>
  <div>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <p class="text-sm text-gray-500">Total Orders</p>
        <p class="text-3xl font-bold text-gray-900 mt-1">{{ stats.totalOrders }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <p class="text-sm text-gray-500">Pending Orders</p>
        <p class="text-3xl font-bold text-amber-600 mt-1">{{ stats.pendingOrders }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <p class="text-sm text-gray-500">Revenue (Completed)</p>
        <p class="text-3xl font-bold text-emerald-600 mt-1">{{ stats.revenue.toFixed(0) }}</p>
      </div>
      <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <p class="text-sm text-gray-500">Restaurant Code</p>
        <p class="text-sm font-semibold text-gray-800 mt-2 truncate">{{ auth.restaurantCode || '—' }}</p>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-sm border border-gray-100">
      <div class="px-5 py-4 border-b border-gray-100">
        <h3 class="font-semibold text-gray-800">Recent Orders</h3>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-gray-50 text-gray-500 uppercase text-xs">
            <tr>
              <th class="px-5 py-3 text-left">Order #</th>
              <th class="px-5 py-3 text-left">Type</th>
              <th class="px-5 py-3 text-left">Status</th>
              <th class="px-5 py-3 text-right">Total</th>
              <th class="px-5 py-3 text-left">Date</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr v-for="order in recentOrders" :key="order.code" class="hover:bg-gray-50">
              <td class="px-5 py-3 font-medium">{{ order.orderNumber }}</td>
              <td class="px-5 py-3 text-gray-500">{{ order.orderType.replace(/_/g, ' ') }}</td>
              <td class="px-5 py-3"><StatusBadge :status="order.status" /></td>
              <td class="px-5 py-3 text-right font-medium">{{ order.totalAmount.toFixed(2) }}</td>
              <td class="px-5 py-3 text-gray-400">{{ new Date(order.createAt).toLocaleDateString() }}</td>
            </tr>
            <tr v-if="!recentOrders.length">
              <td colspan="5" class="px-5 py-8 text-center text-gray-400">No orders yet</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
