<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ordersApi } from '@/api/orders'
import PageHeader from '@/components/shared/PageHeader.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import {
  Armchair, ShoppingBag, Smartphone, Monitor,
  Clock, ChefHat, BellRing, Loader2,
} from 'lucide-vue-next'
import type { OrdersResponse, OrderType, OrderStatus } from '@/types'

const auth = useAuthStore()
const orders = ref<OrdersResponse[]>([])
const loading = ref(true)
let pollTimer: ReturnType<typeof setInterval> | null = null

// ── Layout groups: one column per orderType ──────────────────────────────────
const TYPE_COLUMNS: Array<{ key: OrderType; label: string; icon: any; tint: string }> = [
  { key: 'DINE_IN',  label: 'Dine-in',    icon: Armchair,   tint: 'from-violet-500 to-fuchsia-500' },
  { key: 'QR_ORDER', label: 'QR order',   icon: Smartphone, tint: 'from-sky-500 to-blue-500' },
  { key: 'KIOSK',    label: 'Kiosk',      icon: Monitor,    tint: 'from-emerald-500 to-teal-500' },
  { key: 'TAKEAWAY', label: 'Takeaway',   icon: ShoppingBag, tint: 'from-amber-500 to-orange-500' },
]

const STATUS_BADGE: Record<OrderStatus, { label: string; tint: string; icon: any }> = {
  PENDING:   { label: 'Pending',   tint: 'bg-slate-100 text-slate-700 ring-slate-200',     icon: Clock },
  CONFIRMED: { label: 'Confirmed', tint: 'bg-violet-50 text-violet-700 ring-violet-200',   icon: Clock },
  PREPARING: { label: 'Preparing', tint: 'bg-amber-50 text-amber-700 ring-amber-200',      icon: ChefHat },
  READY:     { label: 'Ready',     tint: 'bg-emerald-50 text-emerald-700 ring-emerald-300',icon: BellRing },
  COMPLETED: { label: 'Completed', tint: 'bg-slate-50 text-slate-500 ring-slate-200',      icon: Clock },
  CANCELLED: { label: 'Cancelled', tint: 'bg-rose-50 text-rose-700 ring-rose-200',         icon: Clock },
}

const groupedByType = computed(() => {
  const buckets: Record<OrderType, OrdersResponse[]> = {
    DINE_IN: [], TAKEAWAY: [], QR_ORDER: [], KIOSK: [],
  }
  for (const o of orders.value) {
    if (buckets[o.orderType]) buckets[o.orderType].push(o)
  }
  // Within each bucket: READY first, then by createdAt asc (oldest pending at top)
  const rank = (s: OrderStatus) => s === 'READY' ? 0 : s === 'PREPARING' ? 1 : 2
  for (const key of Object.keys(buckets) as OrderType[]) {
    buckets[key].sort((a, b) => {
      const r = rank(a.status) - rank(b.status)
      if (r !== 0) return r
      return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    })
  }
  return buckets
})

const totalActive = computed(() => orders.value.length)

async function refresh(initial = false) {
  if (!auth.restaurantCode) return
  try {
    if (initial) loading.value = true
    orders.value = await ordersApi.getActiveByRestaurant(auth.restaurantCode)
  } catch { /* silent — keep last good data on transient errors */ }
  finally { if (initial) loading.value = false }
}

onMounted(async () => {
  await refresh(true)
  pollTimer = setInterval(() => refresh(false), 3000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <RestaurantGuard resource="pickup board">
    <PageHeader title="Pickup Board" :description="`Live tickets across every order type — ${totalActive} active`" />

    <div v-if="loading" class="flex items-center justify-center py-24">
      <Loader2 class="w-10 h-10 text-violet-500 animate-spin" />
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
      <div v-for="col in TYPE_COLUMNS" :key="col.key"
        class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm overflow-hidden flex flex-col">
        <!-- Column header -->
        <div :class="['px-4 py-3 text-white flex items-center justify-between gap-2 bg-gradient-to-r', col.tint]">
          <div class="flex items-center gap-2 min-w-0">
            <component :is="col.icon" class="w-5 h-5 flex-shrink-0" />
            <span class="font-semibold text-sm sm:text-base truncate">{{ col.label }}</span>
          </div>
          <span class="text-xs font-semibold bg-white/20 ring-1 ring-white/30 rounded-full px-2 py-0.5 tabular-nums">
            {{ groupedByType[col.key].length }}
          </span>
        </div>

        <!-- Tickets -->
        <div class="flex-1 p-3 min-h-[14rem]">
          <p v-if="groupedByType[col.key].length === 0"
            class="text-center text-xs text-slate-400 py-10">
            Nothing active.
          </p>

          <ul v-else class="grid grid-cols-2 gap-2">
            <li v-for="o in groupedByType[col.key]" :key="o.code">
              <router-link
                :to="{ name: 'order-detail', params: { code: o.code } }"
                :class="['block rounded-xl ring-1 px-3 py-2.5 text-center transition-all hover:shadow-md hover:-translate-y-0.5',
                  o.status === 'READY'
                    ? 'bg-emerald-50/70 ring-emerald-300'
                    : o.status === 'PREPARING'
                      ? 'bg-amber-50/60 ring-amber-200'
                      : 'bg-slate-50 ring-slate-200']">
                <p v-if="o.ticketNumber != null"
                  :class="['text-2xl sm:text-3xl font-extrabold tabular-nums leading-none',
                    o.status === 'READY' ? 'text-emerald-700' : 'text-slate-900']">
                  {{ String(o.ticketNumber).padStart(3, '0') }}
                </p>
                <p v-else class="text-sm font-mono font-semibold text-slate-900 tabular-nums truncate">
                  #{{ o.orderNumber }}
                </p>
                <div class="mt-1.5 flex items-center justify-center gap-1">
                  <span
                    :class="['inline-flex items-center gap-1 text-[10px] font-semibold uppercase tracking-wider px-1.5 py-0.5 rounded ring-1',
                      STATUS_BADGE[o.status].tint]">
                    <component :is="STATUS_BADGE[o.status].icon" class="w-3 h-3" />
                    {{ STATUS_BADGE[o.status].label }}
                  </span>
                </div>
                <p class="text-[10px] text-slate-400 mt-1 tabular-nums">NPR {{ o.totalAmount.toFixed(0) }}</p>
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <p class="text-center text-[11px] text-slate-400 mt-4">
      Refreshes every 3 seconds · Click any ticket to open the order
    </p>
  </RestaurantGuard>
</template>
