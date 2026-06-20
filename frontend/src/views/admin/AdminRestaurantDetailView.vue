<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderTypeLabel } from '@/utils/orderType'
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
    class="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground mb-4">
    <ArrowLeft class="w-4 h-4" />
    Back to restaurants
  </button>

  <div v-if="loading" class="text-center py-16 text-muted-foreground">Loading…</div>

  <template v-else-if="restaurant">
    <!-- Header -->
    <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-5 sm:p-6 mb-4">
      <div class="flex items-start gap-4">
        <div class="w-14 h-14 rounded-2xl bg-warning flex items-center justify-center text-warning-foreground shadow-soft flex-shrink-0">
          <Store class="w-6 h-6" />
        </div>
        <div class="min-w-0 flex-1">
          <div class="flex items-center gap-2 flex-wrap">
            <h2 class="text-xl sm:text-2xl font-bold text-foreground">{{ restaurant.name }}</h2>
            <span :class="[
              'inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-md ring-1',
              restaurant.isActive
                ? 'bg-success/10 text-success ring-success/20'
                : 'bg-muted text-muted-foreground ring-border'
            ]">
              <Circle :class="['w-2 h-2', restaurant.isActive ? 'fill-success text-success' : 'fill-muted-foreground text-muted-foreground']" />
              {{ restaurant.isActive ? 'Active' : 'Inactive' }}
            </span>
          </div>
          <div class="mt-2 grid grid-cols-1 sm:grid-cols-2 gap-1.5 text-sm text-muted-foreground">
            <div class="flex items-center gap-2"><MapPin class="w-4 h-4 text-muted-foreground" /> {{ restaurant.address || '—' }}</div>
            <div class="flex items-center gap-2"><Phone class="w-4 h-4 text-muted-foreground" /> {{ restaurant.phone || '—' }}</div>
            <div class="flex items-center gap-2"><Mail class="w-4 h-4 text-muted-foreground" /> {{ restaurant.email || '—' }}</div>
            <div class="flex items-center gap-2">
              <span class="text-[10px] font-mono px-1.5 py-0.5 rounded bg-info/10 text-info ring-1 ring-info/20">{{ restaurant.kioskCode }}</span>
              <span class="text-xs text-muted-foreground">kiosk code</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-3 mb-4">
      <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wide">Total orders</span>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center text-success-foreground bg-success shadow-soft">
            <ShoppingBag class="w-5 h-5" />
          </div>
        </div>
        <div class="text-2xl sm:text-3xl font-bold text-foreground tabular-nums">{{ (overview?.totalOrders ?? 0).toLocaleString() }}</div>
      </div>
      <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wide">Total revenue</span>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center text-destructive-foreground bg-destructive shadow-soft">
            <TrendingUp class="w-5 h-5" />
          </div>
        </div>
        <div class="text-2xl sm:text-3xl font-bold text-foreground tabular-nums">{{ fmtMoney(overview?.totalRevenue) }}</div>
      </div>
      <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wide">Active staff</span>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center text-info-foreground bg-info shadow-soft">
            <Users class="w-5 h-5" />
          </div>
        </div>
        <div class="text-2xl sm:text-3xl font-bold text-foreground tabular-nums">{{ (overview?.activeStaffCount ?? 0).toLocaleString() }}</div>
      </div>
      <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-[11px] font-semibold text-muted-foreground uppercase tracking-wide">Last order</span>
          <div class="w-9 h-9 rounded-xl flex items-center justify-center text-warning-foreground bg-warning shadow-soft">
            <Clock4 class="w-5 h-5" />
          </div>
        </div>
        <div class="text-sm sm:text-base font-semibold text-foreground leading-tight">{{ fmtDateTime(overview?.lastOrderAt) }}</div>
      </div>
    </div>

    <!-- Top sellers -->
    <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-5 mb-4">
      <div class="flex items-center gap-2 mb-3">
        <Trophy class="w-4 h-4 text-warning" />
        <h3 class="text-sm font-semibold text-foreground">Top sellers</h3>
        <span class="text-xs text-muted-foreground">all time</span>
      </div>
      <div v-if="!overview?.topItems?.length" class="text-sm text-muted-foreground text-center py-6">
        No items sold yet.
      </div>
      <ol v-else class="space-y-2.5">
        <li v-for="(item, idx) in overview.topItems" :key="item.menuItemCode" class="flex items-center gap-2.5">
          <span class="inline-flex w-6 h-6 rounded-lg bg-muted ring-1 ring-border items-center justify-center text-xs font-bold text-muted-foreground tabular-nums">
            {{ idx + 1 }}
          </span>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-foreground truncate">{{ item.menuItemName ?? '—' }}</p>
            <div class="mt-1 h-1.5 bg-muted rounded-full overflow-hidden">
              <div class="h-full bg-warning rounded-full"
                :style="{ width: maxTopQty > 0 ? `${(item.quantity / maxTopQty) * 100}%` : '0%' }" />
            </div>
          </div>
          <div class="text-right flex-shrink-0">
            <p class="text-sm font-bold text-foreground tabular-nums">{{ item.quantity }}</p>
            <p class="text-[10px] text-muted-foreground tabular-nums">NPR {{ item.revenue.toFixed(0) }}</p>
          </div>
        </li>
      </ol>
    </div>

    <!-- Owner -->
    <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-5 mb-4">
      <h3 class="text-sm font-semibold text-foreground mb-3">Manager / owner</h3>
      <div v-if="owner" class="flex items-center gap-3">
        <div class="w-11 h-11 rounded-xl bg-primary flex items-center justify-center text-primary-foreground shadow-soft">
          <UserCircle class="w-5 h-5" />
        </div>
        <div class="min-w-0 flex-1">
          <div class="font-medium text-foreground truncate">{{ owner.fullName }}</div>
          <div class="text-xs text-muted-foreground truncate">{{ owner.email }} · {{ owner.phone || '—' }}</div>
        </div>
        <button
          @click="toggleOwnerActive"
          :disabled="updating"
          :class="[
            'px-3 py-1.5 text-xs font-medium rounded-lg ring-1 transition-colors disabled:opacity-50',
            owner.isActive
              ? 'bg-destructive/10 text-destructive ring-destructive/20 hover:bg-destructive/20'
              : 'bg-success/10 text-success ring-success/20 hover:bg-success/20'
          ]"
        >
          {{ owner.isActive ? 'Suspend' : 'Reactivate' }}
        </button>
      </div>
      <p v-else class="text-sm text-muted-foreground">No owner linked.</p>
    </div>

    <!-- Recent orders -->
    <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-5">
      <h3 class="text-sm font-semibold text-foreground mb-3">Recent orders</h3>
      <div v-if="!recentOrders.length" class="text-sm text-muted-foreground text-center py-6">
        No orders yet.
      </div>
      <ul v-else class="divide-y divide-border">
        <li v-for="o in recentOrders" :key="o.code"
          class="py-2.5 flex items-center justify-between gap-3">
          <div class="min-w-0">
            <div class="text-sm font-medium text-foreground">#{{ o.orderNumber }}</div>
            <div class="text-xs text-muted-foreground">{{ orderTypeLabel(o.orderType) }} · {{ new Date(o.createdAt).toLocaleString() }}</div>
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
