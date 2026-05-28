<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import { restaurantApi } from '@/api/restaurant'
import { analyticsApi } from '@/api/analytics'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import {
  ShoppingBag, Clock4, TrendingUp, QrCode,
  Copy, Link2, ExternalLink, Check,
  Pizza, Armchair, Users, ChefHat, ClipboardList, BellRing,
  BarChart3, Trophy,
} from 'lucide-vue-next'
import type { OrdersResponse, RevenueSeries, TopItem } from '@/types'

const router = useRouter()

const quickActions = [
  { label: 'Take an order',     description: 'Use waiter mode',    icon: ChefHat,        gradient: 'from-violet-500 to-fuchsia-500', path: '/dashboard/waiter' },
  { label: 'Kitchen',           description: 'Live kitchen queue', icon: ClipboardList,  gradient: 'from-amber-500 to-orange-500',   path: '/dashboard/kitchen' },
  { label: 'Pickup Board',      description: 'Ready for pickup',   icon: BellRing,       gradient: 'from-emerald-500 to-green-600',  path: '/dashboard/pickup-board' },
  { label: 'Add menu item',     description: 'Grow the menu',      icon: Pizza,          gradient: 'from-pink-500 to-rose-500',      path: '/dashboard/menu/items' },
  { label: 'Manage tables',     description: 'See active tables',  icon: Armchair,       gradient: 'from-blue-500 to-indigo-500',    path: '/dashboard/tables' },
  { label: 'Add employee',      description: 'Onboard staff',      icon: Users,          gradient: 'from-sky-500 to-cyan-500',       path: '/dashboard/employees' },
]

const auth = useAuthStore()
const recentOrders = ref<OrdersResponse[]>([])
const stats = ref({ totalOrders: 0, revenue: 0, pendingOrders: 0 })
const copiedTarget = ref<'code' | 'link' | null>(null)

// ── Analytics ───────────────────────────────────────────────────────────────
const today = new Date()
function toIso(d: Date) { return d.toISOString().slice(0, 10) }
function isoDaysAgo(n: number) {
  const d = new Date(today)
  d.setDate(d.getDate() - n)
  return toIso(d)
}
const range = ref<{ from: string; to: string }>({ from: isoDaysAgo(13), to: toIso(today) })
const presets = [
  { key: '7d',  label: 'Last 7 days',   days: 6 },
  { key: '14d', label: 'Last 14 days',  days: 13 },
  { key: '30d', label: 'Last 30 days',  days: 29 },
] as const
const activePreset = ref<typeof presets[number]['key'] | 'custom'>('14d')

const revenueSeries = ref<RevenueSeries | null>(null)
const topItems = ref<TopItem[]>([])
const analyticsLoading = ref(false)

async function loadAnalytics() {
  if (!auth.restaurantCode) return
  analyticsLoading.value = true
  try {
    const [series, items] = await Promise.all([
      analyticsApi.revenue(auth.restaurantCode, range.value.from, range.value.to),
      analyticsApi.topItems(auth.restaurantCode, range.value.from, range.value.to, 5),
    ])
    revenueSeries.value = series
    topItems.value = items
  } catch { /* silent */ }
  finally { analyticsLoading.value = false }
}

function applyPreset(p: typeof presets[number]) {
  activePreset.value = p.key
  range.value = { from: isoDaysAgo(p.days), to: toIso(today) }
  loadAnalytics()
}

function applyCustomRange() {
  activePreset.value = 'custom'
  loadAnalytics()
}

const maxRevenue = computed(() => {
  const points = revenueSeries.value?.points ?? []
  return points.reduce((m, p) => Math.max(m, p.revenue), 0)
})
const maxTopItemQty = computed(() => topItems.value.reduce((m, t) => Math.max(m, t.quantity), 0))

function formatDayLabel(iso: string) {
  const d = new Date(iso + 'T00:00:00')
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })
}

function formatWeekday(iso: string) {
  const d = new Date(iso + 'T00:00:00')
  return d.toLocaleDateString(undefined, { weekday: 'short' })
}

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
  loadAnalytics()
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
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
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

    <!-- ── Analytics ────────────────────────────────────────────────── -->
    <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm overflow-hidden mb-6">
      <div class="px-5 py-4 border-b border-slate-100 flex flex-wrap items-start justify-between gap-3">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center shadow-md shadow-violet-500/30">
            <BarChart3 class="w-5 h-5 text-white" />
          </div>
          <div>
            <h3 class="font-semibold text-slate-800">Analytics</h3>
            <p class="text-xs text-slate-500 mt-0.5">Revenue and best-sellers for the period.</p>
          </div>
        </div>

        <!-- Range picker -->
        <div class="flex flex-wrap items-center gap-2">
          <div class="inline-flex rounded-xl bg-slate-100 p-0.5">
            <button v-for="p in presets" :key="p.key" @click="applyPreset(p)"
              :class="['px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors',
                activePreset === p.key ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700']">
              {{ p.label }}
            </button>
          </div>
          <div class="flex items-center gap-1.5 bg-slate-50 ring-1 ring-slate-200 rounded-xl px-2 py-1">
            <input type="date" v-model="range.from" @change="applyCustomRange"
              class="bg-transparent text-xs outline-none text-slate-700 tabular-nums" />
            <span class="text-slate-400 text-xs">→</span>
            <input type="date" v-model="range.to" @change="applyCustomRange"
              class="bg-transparent text-xs outline-none text-slate-700 tabular-nums" />
          </div>
        </div>
      </div>

      <div class="p-5 grid grid-cols-1 lg:grid-cols-3 gap-5">
        <!-- Revenue bar chart -->
        <div class="lg:col-span-2 bg-slate-50/60 rounded-xl ring-1 ring-slate-200/60 p-4">
          <div class="flex items-end justify-between mb-3">
            <div>
              <p class="text-xs text-slate-500 font-semibold uppercase tracking-wider">Total revenue</p>
              <p class="text-2xl sm:text-3xl font-bold text-slate-900 tabular-nums">
                NPR {{ (revenueSeries?.totalRevenue ?? 0).toFixed(0) }}
              </p>
              <p class="text-xs text-slate-500 mt-0.5 tabular-nums">
                {{ revenueSeries?.totalOrders ?? 0 }} orders
              </p>
            </div>
            <div v-if="analyticsLoading" class="text-xs text-slate-400">Loading…</div>
          </div>

          <div v-if="!revenueSeries?.points?.length" class="h-32 flex items-center justify-center text-xs text-slate-400">
            No data for this period.
          </div>
          <div v-else class="h-44 sm:h-52 flex items-end gap-1">
            <div v-for="p in revenueSeries.points" :key="p.date"
              class="flex-1 group relative flex flex-col items-stretch justify-end h-full">
              <div
                :class="['w-full rounded-t transition-all',
                  p.revenue > 0 ? 'bg-gradient-to-t from-violet-500 to-fuchsia-500' : 'bg-slate-200']"
                :style="{ height: maxRevenue > 0 ? `${Math.max(2, (p.revenue / maxRevenue) * 100)}%` : '2px' }"
                :title="`${p.date}: NPR ${p.revenue.toFixed(0)} (${p.orderCount} orders)`"
              />
              <!-- Tooltip-ish label on hover -->
              <div class="hidden group-hover:block absolute -top-10 left-1/2 -translate-x-1/2 bg-slate-900 text-white text-[10px] rounded px-2 py-1 whitespace-nowrap tabular-nums z-10">
                NPR {{ p.revenue.toFixed(0) }} · {{ p.orderCount }}
              </div>
            </div>
          </div>

          <!-- Day labels: show ~7 evenly spaced -->
          <div v-if="revenueSeries?.points?.length" class="flex justify-between mt-2 text-[10px] text-slate-400 tabular-nums">
            <span v-for="(p, i) in revenueSeries.points" :key="p.date + '-l'"
              :class="(i === 0 || i === revenueSeries.points.length - 1 || i % Math.ceil(revenueSeries.points.length / 7) === 0) ? '' : 'invisible'">
              {{ formatDayLabel(p.date) }}
            </span>
          </div>
        </div>

        <!-- Top 5 items -->
        <div class="bg-slate-50/60 rounded-xl ring-1 ring-slate-200/60 p-4">
          <div class="flex items-center gap-2 mb-3">
            <Trophy class="w-4 h-4 text-amber-500" />
            <p class="text-xs text-slate-500 font-semibold uppercase tracking-wider">Top sellers</p>
          </div>
          <div v-if="!topItems.length" class="text-xs text-slate-400 py-6 text-center">
            No items sold yet.
          </div>
          <ol v-else class="space-y-2.5">
            <li v-for="(item, idx) in topItems" :key="item.menuItemCode" class="flex items-center gap-2.5">
              <span class="inline-flex w-6 h-6 rounded-lg bg-white ring-1 ring-slate-200 items-center justify-center text-xs font-bold text-slate-600 tabular-nums">
                {{ idx + 1 }}
              </span>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-slate-900 truncate">{{ item.menuItemName ?? '—' }}</p>
                <div class="mt-1 h-1.5 bg-slate-200 rounded-full overflow-hidden">
                  <div class="h-full bg-gradient-to-r from-amber-400 to-orange-500 rounded-full"
                    :style="{ width: maxTopItemQty > 0 ? `${(item.quantity / maxTopItemQty) * 100}%` : '0%' }" />
                </div>
              </div>
              <div class="text-right flex-shrink-0">
                <p class="text-sm font-bold text-slate-900 tabular-nums">{{ item.quantity }}</p>
                <p class="text-[10px] text-slate-400 tabular-nums">NPR {{ item.revenue.toFixed(0) }}</p>
              </div>
            </li>
          </ol>
        </div>
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
