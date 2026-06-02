<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { restaurantApi } from '@/api/restaurant'
import { ordersApi } from '@/api/orders'
import { BellRing, Loader2, AlertTriangle } from 'lucide-vue-next'
import type { OrdersResponse, RestaurantResponse } from '@/types'

const route = useRoute()
const kioskCode = computed(() => route.params.kioskCode as string)

const restaurant = ref<RestaurantResponse | null>(null)
const orders = ref<OrdersResponse[]>([])
const loading = ref(true)
const notFound = ref(false)
const now = ref(new Date())

let pollTimer: ReturnType<typeof setInterval> | null = null
let clockTimer: ReturnType<typeof setInterval> | null = null

// Today's READY tickets only — stale active orders from earlier days don't belong here.
function isToday(iso: string): boolean {
  const d = new Date(iso)
  const n = new Date()
  return d.getFullYear() === n.getFullYear() && d.getMonth() === n.getMonth() && d.getDate() === n.getDate()
}
const readyOrders = computed(() => orders.value.filter(o => o.status === 'READY' && isToday(o.createdAt)))

async function refresh(initial = false) {
  try {
    if (initial) {
      loading.value = true
      restaurant.value = await restaurantApi.getByKioskCode(kioskCode.value)
    }
    if (!restaurant.value) return
    orders.value = await ordersApi.getActiveByRestaurant(restaurant.value.code)
    notFound.value = false
  } catch (e: any) {
    if (e?.response?.status === 404) notFound.value = true
  } finally {
    if (initial) loading.value = false
  }
}

const timeLabel = computed(() =>
  now.value.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' }),
)
const dateLabel = computed(() =>
  now.value.toLocaleDateString(undefined, { weekday: 'long', month: 'short', day: 'numeric' }),
)

onMounted(async () => {
  await refresh(true)
  pollTimer = setInterval(() => refresh(false), 3000)
  clockTimer = setInterval(() => { now.value = new Date() }, 1000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (clockTimer) clearInterval(clockTimer)
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-emerald-950 text-white overflow-hidden">

    <!-- Loading -->
    <div v-if="loading" class="min-h-screen flex items-center justify-center">
      <div class="text-center">
        <Loader2 class="w-16 h-16 text-emerald-400 mx-auto mb-4 animate-spin" />
        <p class="text-slate-400 text-lg">Loading pickup board…</p>
      </div>
    </div>

    <!-- Not found -->
    <div v-else-if="notFound" class="min-h-screen flex items-center justify-center px-4">
      <div class="text-center max-w-md">
        <div class="w-20 h-20 rounded-2xl bg-rose-500/10 ring-1 ring-rose-500/30 flex items-center justify-center mx-auto mb-4">
          <AlertTriangle class="w-10 h-10 text-rose-400" />
        </div>
        <p class="text-2xl font-bold">Restaurant not found</p>
        <p class="text-slate-400 mt-2">Check the kiosk code in the URL.</p>
      </div>
    </div>

    <template v-else-if="restaurant">
      <!-- Header bar -->
      <header class="relative border-b border-white/10 backdrop-blur-sm bg-white/5">
        <div class="px-6 sm:px-12 py-5 sm:py-7 flex items-center justify-between gap-6">
          <div class="flex items-center gap-4 min-w-0">
            <div class="w-12 h-12 sm:w-14 sm:h-14 rounded-2xl bg-gradient-to-br from-emerald-400 to-green-500 flex items-center justify-center shadow-lg shadow-emerald-500/30 flex-shrink-0">
              <BellRing class="w-7 h-7 text-white" />
            </div>
            <div class="min-w-0">
              <h1 class="text-2xl sm:text-4xl xl:text-5xl font-bold tracking-tight truncate">
                {{ restaurant.name }}
              </h1>
              <p class="text-emerald-300 text-sm sm:text-base font-semibold uppercase tracking-wider mt-1">
                Now serving · Pickup board
              </p>
            </div>
          </div>
          <div class="text-right flex-shrink-0">
            <p class="text-3xl sm:text-5xl xl:text-6xl font-bold tabular-nums leading-none">{{ timeLabel }}</p>
            <p class="text-xs sm:text-sm text-slate-400 mt-1.5">{{ dateLabel }}</p>
          </div>
        </div>
      </header>

      <!-- Tickets -->
      <main class="px-6 sm:px-12 py-8 sm:py-12">
        <div v-if="readyOrders.length === 0"
          class="min-h-[60vh] flex flex-col items-center justify-center text-center">
          <div class="w-24 h-24 rounded-3xl bg-white/5 ring-1 ring-white/10 flex items-center justify-center mb-5">
            <BellRing class="w-12 h-12 text-slate-500" />
          </div>
          <p class="text-3xl sm:text-4xl font-bold text-slate-300">No orders ready yet</p>
          <p class="text-slate-500 mt-2 text-base sm:text-lg">Tickets will appear here when they're ready for pickup.</p>
        </div>

        <div v-else
          class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 sm:gap-6">
          <div v-for="o in readyOrders" :key="o.code"
            class="ticket-card relative rounded-3xl bg-gradient-to-br from-emerald-400/20 via-emerald-500/15 to-green-600/20 ring-2 ring-emerald-400/60 shadow-xl shadow-emerald-500/30 p-5 sm:p-7 flex items-center justify-center aspect-square">
            <p v-if="o.ticketNumber != null"
              class="text-7xl sm:text-8xl xl:text-9xl font-extrabold tabular-nums leading-none text-white drop-shadow-[0_0_30px_rgba(16,185,129,0.6)]">
              {{ String(o.ticketNumber).padStart(3, '0') }}
            </p>
            <p v-else class="text-2xl sm:text-3xl font-bold text-white/80 font-mono tabular-nums break-all text-center">
              #{{ o.orderNumber }}
            </p>
          </div>
        </div>
      </main>
    </template>
  </div>
</template>

<style scoped>
/* Soft pulsing glow on each ready ticket to draw the eye */
@keyframes ticket-pulse {
  0%, 100% { box-shadow: 0 20px 50px -10px rgba(16, 185, 129, 0.35), 0 0 0 0 rgba(16, 185, 129, 0.4); }
  50%      { box-shadow: 0 25px 60px -10px rgba(16, 185, 129, 0.5),  0 0 0 6px rgba(16, 185, 129, 0); }
}
.ticket-card {
  animation: ticket-pulse 2.4s ease-in-out infinite;
}
@media (prefers-reduced-motion: reduce) {
  .ticket-card { animation: none; }
}
</style>
