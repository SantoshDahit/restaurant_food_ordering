<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { restaurantApi } from '@/api/restaurant'
import { userApi } from '@/api/user'
import { ordersApi } from '@/api/orders'
import { adminApi } from '@/api/admin'
import { toast } from 'vue-sonner'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import {
  ArrowLeft, Store, Mail, Phone, MapPin, ShoppingBag, TrendingUp,
  Circle, UserCircle, Users, Clock4, Trophy,
} from 'lucide-vue-next'
import type { RestaurantResponse, RestaurantOverview, OrdersResponse, UserResponse } from '@/types'

const route = useRoute()
const router = useRouter()

const restaurant = ref<RestaurantResponse | null>(null)
const owner = ref<UserResponse | null>(null)
const overview = ref<RestaurantOverview | null>(null)
const recentOrders = ref<OrdersResponse[]>([])
const loading = ref(true)
const updating = ref(false)

onMounted(async () => {
  const code = route.params.code as string
  try {
    restaurant.value = await restaurantApi.get(code)
    const ownerCode = restaurant.value.userCode
    const [ov, ords, ownr] = await Promise.all([
      adminApi.restaurantOverview(code),
      ordersApi.search({ restaurantCode: code, size: 5 }),
      ownerCode ? userApi.get(ownerCode).catch(() => null) : Promise.resolve(null),
    ])
    overview.value = ov
    recentOrders.value = ords.content
    owner.value = ownr
  } catch {
    toast.error('Failed to load restaurant')
  } finally {
    loading.value = false
  }
})

async function toggleOwnerActive() {
  if (!owner.value) return
  updating.value = true
  try {
    owner.value = await adminApi.setUserActive(owner.value.code, !owner.value.isActive)
    toast.success(owner.value.isActive ? 'Owner reactivated' : 'Owner suspended')
  } catch {
    toast.error('Failed to update owner status')
  } finally {
    updating.value = false
  }
}

function fmtMoney(n: number | undefined): string {
  return 'NPR ' + (n ?? 0).toLocaleString(undefined, { maximumFractionDigits: 0 })
}

function fmtDateTime(iso: string | null | undefined): string {
  if (!iso) return '—'
  return new Date(iso).toLocaleString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

const maxTopQty = computed(() => (overview.value?.topItems ?? []).reduce((m, t) => Math.max(m, t.quantity), 0))
</script>

<template>
  <button @click="router.back()"
    class="inline-flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-700 mb-4">
    <ArrowLeft class="w-4 h-4" />
    Back to restaurants
  </button>

  <div v-if="loading" class="text-center py-16 text-slate-400">Loading…</div>

  <template v-else-if="restaurant">
    <!-- Header -->
    <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5 sm:p-6 mb-4">
      <div class="flex items-start gap-4">
        <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center text-white shadow-lg shadow-amber-500/30 flex-shrink-0">
          <Store class="w-6 h-6" />
        </div>
        <div class="min-w-0 flex-1">
          <div class="flex items-center gap-2 flex-wrap">
            <h2 class="text-xl sm:text-2xl font-bold text-slate-900">{{ restaurant.name }}</h2>
            <span :class="[
              'inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-md ring-1',
              restaurant.isActive
                ? 'bg-emerald-50 text-emerald-700 ring-emerald-200/60'
                : 'bg-slate-100 text-slate-500 ring-slate-200/60'
            ]">
              <Circle :class="['w-2 h-2', restaurant.isActive ? 'fill-emerald-500 text-emerald-500' : 'fill-slate-400 text-slate-400']" />
              {{ restaurant.isActive ? 'Active' : 'Inactive' }}
            </span>
          </div>
          <div class="mt-2 grid grid-cols-1 sm:grid-cols-2 gap-1.5 text-sm text-slate-600">
            <div class="flex items-center gap-2"><MapPin class="w-4 h-4 text-slate-400" /> {{ restaurant.address || '—' }}</div>
            <div class="flex items-center gap-2"><Phone class="w-4 h-4 text-slate-400" /> {{ restaurant.phone || '—' }}</div>
            <div class="flex items-center gap-2"><Mail class="w-4 h-4 text-slate-400" /> {{ restaurant.email || '—' }}</div>
            <div class="flex items-center gap-2">
              <span class="text-[10px] font-mono px-1.5 py-0.5 rounded bg-teal-50 text-teal-700 ring-1 ring-teal-200/60">{{ restaurant.kioskCode }}</span>
              <span class="text-xs text-slate-400">kiosk code</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 mb-4">
      <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-[11px] font-semibold text-slate-500 uppercase tracking-wide">Total orders</span>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center text-white bg-gradient-to-br from-emerald-500 to-teal-500 shadow-sm">
            <ShoppingBag class="w-5 h-5" />
          </div>
        </div>
        <div class="text-2xl sm:text-3xl font-bold text-slate-900 tabular-nums">{{ (overview?.totalOrders ?? 0).toLocaleString() }}</div>
      </div>
      <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-[11px] font-semibold text-slate-500 uppercase tracking-wide">Total revenue</span>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center text-white bg-gradient-to-br from-rose-500 to-pink-500 shadow-sm">
            <TrendingUp class="w-5 h-5" />
          </div>
        </div>
        <div class="text-2xl sm:text-3xl font-bold text-slate-900 tabular-nums">{{ fmtMoney(overview?.totalRevenue) }}</div>
      </div>
      <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-[11px] font-semibold text-slate-500 uppercase tracking-wide">Active staff</span>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center text-white bg-gradient-to-br from-sky-500 to-indigo-500 shadow-sm">
            <Users class="w-5 h-5" />
          </div>
        </div>
        <div class="text-2xl sm:text-3xl font-bold text-slate-900 tabular-nums">{{ (overview?.activeStaffCount ?? 0).toLocaleString() }}</div>
      </div>
      <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-[11px] font-semibold text-slate-500 uppercase tracking-wide">Last order</span>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center text-white bg-gradient-to-br from-amber-500 to-orange-500 shadow-sm">
            <Clock4 class="w-5 h-5" />
          </div>
        </div>
        <div class="text-sm sm:text-base font-semibold text-slate-900 leading-tight">{{ fmtDateTime(overview?.lastOrderAt) }}</div>
      </div>
    </div>

    <!-- Top sellers -->
    <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5 mb-4">
      <div class="flex items-center gap-2 mb-3">
        <Trophy class="w-4 h-4 text-amber-500" />
        <h3 class="text-sm font-semibold text-slate-900">Top sellers</h3>
        <span class="text-xs text-slate-400">all time</span>
      </div>
      <div v-if="!overview?.topItems?.length" class="text-sm text-slate-500 text-center py-6">
        No items sold yet.
      </div>
      <ol v-else class="space-y-2.5">
        <li v-for="(item, idx) in overview.topItems" :key="item.menuItemCode" class="flex items-center gap-2.5">
          <span class="inline-flex w-6 h-6 rounded-lg bg-slate-100 ring-1 ring-slate-200 items-center justify-center text-xs font-bold text-slate-600 tabular-nums">
            {{ idx + 1 }}
          </span>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-slate-900 truncate">{{ item.menuItemName ?? '—' }}</p>
            <div class="mt-1 h-1.5 bg-slate-200 rounded-full overflow-hidden">
              <div class="h-full bg-gradient-to-r from-amber-400 to-orange-500 rounded-full"
                :style="{ width: maxTopQty > 0 ? `${(item.quantity / maxTopQty) * 100}%` : '0%' }" />
            </div>
          </div>
          <div class="text-right flex-shrink-0">
            <p class="text-sm font-bold text-slate-900 tabular-nums">{{ item.quantity }}</p>
            <p class="text-[10px] text-slate-400 tabular-nums">NPR {{ item.revenue.toFixed(0) }}</p>
          </div>
        </li>
      </ol>
    </div>

    <!-- Owner -->
    <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5 mb-4">
      <h3 class="text-sm font-semibold text-slate-900 mb-3">Manager / owner</h3>
      <div v-if="owner" class="flex items-center gap-3">
        <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-violet-400 to-fuchsia-500 flex items-center justify-center text-white shadow-sm">
          <UserCircle class="w-5 h-5" />
        </div>
        <div class="min-w-0 flex-1">
          <div class="font-medium text-slate-900 truncate">{{ owner.fullName }}</div>
          <div class="text-xs text-slate-500 truncate">{{ owner.email }} · {{ owner.phone || '—' }}</div>
        </div>
        <button
          @click="toggleOwnerActive"
          :disabled="updating"
          :class="[
            'px-3 py-1.5 text-xs font-medium rounded-lg ring-1 transition-colors disabled:opacity-50',
            owner.isActive
              ? 'bg-rose-50 text-rose-600 ring-rose-200/60 hover:bg-rose-100'
              : 'bg-emerald-50 text-emerald-600 ring-emerald-200/60 hover:bg-emerald-100'
          ]"
        >
          {{ owner.isActive ? 'Suspend' : 'Reactivate' }}
        </button>
      </div>
      <p v-else class="text-sm text-slate-500">No owner linked.</p>
    </div>

    <!-- Recent orders -->
    <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5">
      <h3 class="text-sm font-semibold text-slate-900 mb-3">Recent orders</h3>
      <div v-if="!recentOrders.length" class="text-sm text-slate-500 text-center py-6">
        No orders yet.
      </div>
      <ul v-else class="divide-y divide-slate-100">
        <li v-for="o in recentOrders" :key="o.code"
          class="py-2.5 flex items-center justify-between gap-3">
          <div class="min-w-0">
            <div class="text-sm font-medium text-slate-900">#{{ o.orderNumber }}</div>
            <div class="text-xs text-slate-500">{{ o.orderType.replace(/_/g, ' ') }} · {{ new Date(o.createdAt).toLocaleString() }}</div>
          </div>
          <div class="flex items-center gap-3 flex-shrink-0">
            <span class="text-sm font-semibold tabular-nums">{{ fmtMoney(o.totalAmount) }}</span>
            <StatusBadge :status="o.status" />
          </div>
        </li>
      </ul>
    </div>
  </template>
</template>
