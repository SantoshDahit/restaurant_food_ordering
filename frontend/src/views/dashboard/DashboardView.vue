<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import { restaurantApi } from '@/api/restaurant'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import {
  ShoppingBag, Clock4, TrendingUp, QrCode,
  Copy, Link2, ExternalLink, Check,
  Pizza, Armchair, Users, ChefHat, ClipboardList,
} from 'lucide-vue-next'
import type { OrdersResponse } from '@/types'

const router = useRouter()

const quickActions = [
  { label: 'Take an order',     description: 'Use waiter mode', icon: ChefHat,        gradient: 'from-violet-500 to-fuchsia-500', path: '/dashboard/waiter' },
  { label: 'Add menu item',     description: 'Grow the menu',   icon: Pizza,          gradient: 'from-pink-500 to-rose-500',    path: '/dashboard/menu/items' },
  { label: 'Manage tables',     description: 'See active tables', icon: Armchair,     gradient: 'from-blue-500 to-indigo-500',  path: '/dashboard/tables' },
  { label: 'Add employee',      description: 'Onboard staff',   icon: Users,          gradient: 'from-emerald-500 to-teal-500', path: '/dashboard/employees' },
]

const auth = useAuthStore()
const recentOrders = ref<OrdersResponse[]>([])
const stats = ref({ totalOrders: 0, revenue: 0, pendingOrders: 0 })
const copiedTarget = ref<'code' | 'link' | null>(null)

const kioskUrl = computed(() =>
  auth.kioskCode ? `${window.location.origin}/kiosk/${auth.kioskCode}` : ''
)

async function copyToClipboard(text: string, target: 'code' | 'link') {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    copiedTarget.value = target
    setTimeout(() => { copiedTarget.value = null }, 1500)
  } catch { /* clipboard blocked — silent */ }
}

onMounted(async () => {
  if (!auth.restaurantCode) return
  if (!auth.kioskCode) {
    try {
      const restaurant = await restaurantApi.get(auth.restaurantCode)
      if (restaurant) auth.setRestaurant(restaurant)
    } catch { /* silent */ }
  }
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
  <RestaurantGuard resource="the dashboard">
    <!-- Welcome -->
    <div class="mb-6">
      <h2 class="text-2xl sm:text-3xl font-bold text-slate-900">
        Welcome back, {{ auth.user?.fullName?.split(' ')[0] || 'there' }} 👋
      </h2>
      <p class="text-sm text-slate-500 mt-1">Here's what's happening at your restaurant today.</p>
    </div>

    <!-- Quick actions -->
    <div class="mb-6">
      <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2.5">Quick actions</p>
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <button
          v-for="action in quickActions"
          :key="action.path"
          @click="router.push(action.path)"
          class="group bg-white rounded-2xl p-4 ring-1 ring-slate-200/60 shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all text-left"
        >
          <div :class="['w-10 h-10 rounded-xl bg-gradient-to-br flex items-center justify-center shadow-sm mb-3 group-hover:scale-110 transition-transform', action.gradient]">
            <component :is="action.icon" class="w-5 h-5 text-white" />
          </div>
          <p class="font-semibold text-slate-900 text-sm">{{ action.label }}</p>
          <p class="text-xs text-slate-500 mt-0.5">{{ action.description }}</p>
        </button>
      </div>
    </div>

    <!-- Stats grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <!-- Total Orders -->
      <div class="group relative overflow-hidden bg-white rounded-2xl p-5 ring-1 ring-slate-200/60 shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">Total Orders</p>
            <p class="text-3xl font-bold text-slate-900 mt-2 tabular-nums">{{ stats.totalOrders }}</p>
          </div>
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-indigo-500 flex items-center justify-center shadow-sm shadow-blue-500/30">
            <ShoppingBag class="w-5 h-5 text-white" />
          </div>
        </div>
      </div>

      <!-- Pending -->
      <div class="group relative overflow-hidden bg-white rounded-2xl p-5 ring-1 ring-slate-200/60 shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">Pending Orders</p>
            <p class="text-3xl font-bold text-amber-600 mt-2 tabular-nums">{{ stats.pendingOrders }}</p>
          </div>
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-pink-400 to-violet-500 flex items-center justify-center shadow-sm shadow-fuchsia-500/30">
            <Clock4 class="w-5 h-5 text-white" />
          </div>
        </div>
      </div>

      <!-- Revenue -->
      <div class="group relative overflow-hidden bg-white rounded-2xl p-5 ring-1 ring-slate-200/60 shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-xs font-medium text-slate-500 uppercase tracking-wide">Revenue</p>
            <p class="text-3xl font-bold text-emerald-600 mt-2 tabular-nums">{{ stats.revenue.toFixed(0) }}</p>
          </div>
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-400 to-teal-500 flex items-center justify-center shadow-sm shadow-emerald-500/30">
            <TrendingUp class="w-5 h-5 text-white" />
          </div>
        </div>
      </div>

      <!-- Kiosk -->
      <div class="group relative overflow-hidden bg-gradient-to-br from-teal-50 to-white rounded-2xl p-5 ring-1 ring-teal-200/60 shadow-sm hover:shadow-md transition-shadow">
        <div class="flex items-start justify-between mb-1">
          <p class="text-xs font-medium text-teal-700 uppercase tracking-wide">Kiosk Code</p>
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-teal-500 to-cyan-500 flex items-center justify-center shadow-sm shadow-teal-500/30">
            <QrCode class="w-5 h-5 text-white" />
          </div>
        </div>
        <router-link
          v-if="auth.kioskCode"
          :to="`/kiosk/${auth.kioskCode}`"
          target="_blank"
          rel="noopener"
          class="block text-2xl font-mono font-bold tracking-[0.2em] text-teal-700 hover:text-teal-900 mt-1"
          :title="`Open /kiosk/${auth.kioskCode}`"
        >
          {{ auth.kioskCode }}
        </router-link>
        <p v-else class="text-2xl font-mono font-bold tracking-wider text-slate-300 mt-1">—</p>
        <div v-if="auth.kioskCode" class="flex flex-wrap items-center gap-1.5 mt-3">
          <button
            @click="copyToClipboard(auth.kioskCode, 'code')"
            :title="`Copy code: ${auth.kioskCode}`"
            class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-white hover:bg-slate-50 text-slate-700 rounded-lg ring-1 ring-slate-200/60 transition-colors"
          >
            <Check v-if="copiedTarget === 'code'" class="w-3.5 h-3.5 text-emerald-600" />
            <Copy v-else class="w-3.5 h-3.5" />
            {{ copiedTarget === 'code' ? 'Copied' : 'Code' }}
          </button>
          <button
            @click="copyToClipboard(kioskUrl, 'link')"
            :title="`Copy link: ${kioskUrl}`"
            class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-white hover:bg-slate-50 text-slate-700 rounded-lg ring-1 ring-slate-200/60 transition-colors"
          >
            <Check v-if="copiedTarget === 'link'" class="w-3.5 h-3.5 text-emerald-600" />
            <Link2 v-else class="w-3.5 h-3.5" />
            {{ copiedTarget === 'link' ? 'Copied' : 'Link' }}
          </button>
          <router-link
            :to="`/kiosk/${auth.kioskCode}`"
            target="_blank"
            rel="noopener"
            class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-gradient-to-r from-teal-500 to-cyan-500 hover:from-teal-600 hover:to-cyan-600 text-white rounded-lg shadow-sm transition-all"
          >
            <ExternalLink class="w-3.5 h-3.5" />
            Open
          </router-link>
        </div>
        <p v-else class="text-xs text-slate-400 mt-2">Create your restaurant first.</p>
      </div>
    </div>

    <!-- Recent Orders -->
    <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm overflow-hidden">
      <div class="px-5 py-4 border-b border-slate-100 flex items-center justify-between">
        <div>
          <h3 class="font-semibold text-slate-800">Recent Orders</h3>
          <p class="text-xs text-slate-500 mt-0.5">Last 10 orders for this restaurant.</p>
        </div>
        <router-link to="/dashboard/orders" class="text-xs text-violet-600 hover:text-violet-700 font-medium">
          View all →
        </router-link>
      </div>
      <div class="overflow-x-auto">
        <table class="w-full text-sm min-w-[640px]">
          <thead class="bg-slate-50/60 text-slate-500 uppercase text-[11px] tracking-wide">
            <tr>
              <th class="px-5 py-3 text-left font-medium">Order #</th>
              <th class="px-5 py-3 text-left font-medium">Type</th>
              <th class="px-5 py-3 text-left font-medium">Status</th>
              <th class="px-5 py-3 text-right font-medium">Total</th>
              <th class="px-5 py-3 text-left font-medium">Date</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="order in recentOrders" :key="order.code" class="hover:bg-slate-50/60 transition-colors">
              <td class="px-5 py-3 font-medium text-slate-900">{{ order.orderNumber }}</td>
              <td class="px-5 py-3 text-slate-500">{{ order.orderType.replace(/_/g, ' ') }}</td>
              <td class="px-5 py-3"><StatusBadge :status="order.status" /></td>
              <td class="px-5 py-3 text-right font-medium tabular-nums text-slate-900">{{ order.totalAmount.toFixed(2) }}</td>
              <td class="px-5 py-3 text-slate-400">{{ new Date(order.createdAt).toLocaleDateString() }}</td>
            </tr>
            <tr v-if="!recentOrders.length">
              <td colspan="5" class="p-0">
                <EmptyState
                  :icon="ClipboardList"
                  title="No orders yet"
                  description="When customers place an order from the kiosk, QR code, or waiter mode, it'll appear here."
                  cta-label="Open waiter mode"
                  tone="orange"
                  @cta="router.push('/dashboard/waiter')"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </RestaurantGuard>
</template>
