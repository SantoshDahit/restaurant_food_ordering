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
  <!-- Wall-mounted "now serving" display: force the dark palette for legibility from a distance. -->
  <div class="dark min-h-screen bg-background text-foreground overflow-hidden">

    <!-- Loading -->
    <div v-if="loading" class="min-h-screen flex items-center justify-center">
      <div class="text-center">
        <Loader2 class="w-16 h-16 text-success mx-auto mb-4 animate-spin" />
        <p class="text-muted-foreground text-lg">Loading pickup board…</p>
      </div>
    </div>

    <!-- Not found -->
    <div v-else-if="notFound" class="min-h-screen flex items-center justify-center px-4">
      <div class="text-center max-w-md">
        <div class="w-20 h-20 rounded-2xl bg-destructive/10 ring-1 ring-destructive/30 flex items-center justify-center mx-auto mb-4">
          <AlertTriangle class="w-10 h-10 text-destructive" />
        </div>
        <p class="text-2xl font-bold">Restaurant not found</p>
        <p class="text-muted-foreground mt-2">Check the kiosk code in the URL.</p>
      </div>
    </div>

    <template v-else-if="restaurant">
      <!-- Header bar -->
      <header class="relative border-b border-border backdrop-blur-sm bg-card">
        <div class="px-6 sm:px-12 py-5 sm:py-7 flex items-center justify-between gap-6">
          <div class="flex items-center gap-4 min-w-0">
            <div class="w-12 h-12 sm:w-14 sm:h-14 rounded-2xl bg-success flex items-center justify-center shadow-soft flex-shrink-0">
              <BellRing class="w-7 h-7 text-success-foreground" />
            </div>
            <div class="min-w-0">
              <h1 class="text-2xl sm:text-4xl xl:text-5xl font-bold tracking-tight truncate font-serif">
                {{ restaurant.name }}
              </h1>
              <p class="text-success text-sm sm:text-base font-semibold uppercase tracking-wider mt-1">
                Now serving · Pickup board
              </p>
            </div>
          </div>
          <div class="text-right flex-shrink-0">
            <p class="text-3xl sm:text-5xl xl:text-6xl font-bold tabular-nums leading-none">{{ timeLabel }}</p>
            <p class="text-xs sm:text-sm text-muted-foreground mt-1.5">{{ dateLabel }}</p>
          </div>
        </div>
      </header>

      <!-- Tickets -->
      <main class="px-6 sm:px-12 py-8 sm:py-12">
        <div v-if="readyOrders.length === 0"
          class="min-h-[60vh] flex flex-col items-center justify-center text-center">
          <div class="w-24 h-24 rounded-3xl bg-muted ring-1 ring-border flex items-center justify-center mb-5">
            <BellRing class="w-12 h-12 text-muted-foreground" />
          </div>
          <p class="text-3xl sm:text-4xl font-bold text-foreground">No orders ready yet</p>
          <p class="text-muted-foreground mt-2 text-base sm:text-lg">Tickets will appear here when they're ready for pickup.</p>
        </div>

        <div v-else
          class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4 sm:gap-6">
          <div v-for="o in readyOrders" :key="o.code"
            class="ticket-card relative rounded-3xl bg-success/10 ring-2 ring-success/30 shadow-lifted p-5 sm:p-7 flex items-center justify-center aspect-square">
            <p v-if="o.ticketNumber != null"
              class="text-7xl sm:text-8xl xl:text-9xl font-extrabold tabular-nums leading-none text-success">
              {{ String(o.ticketNumber).padStart(3, '0') }}
            </p>
            <p v-else class="text-2xl sm:text-3xl font-bold text-success/80 font-mono tabular-nums break-all text-center">
              #{{ o.orderNumber }}
            </p>
          </div>
        </div>
      </main>
    </template>
  </div>
</template>

<style scoped>
/* Soft pulsing glow on each ready ticket to draw the eye (sage 'success' token) */
@keyframes ticket-pulse {
  0%, 100% { box-shadow: 0 20px 50px -10px hsl(var(--success) / 0.35), 0 0 0 0 hsl(var(--success) / 0.45); }
  50%      { box-shadow: 0 25px 60px -10px hsl(var(--success) / 0.55), 0 0 0 6px hsl(var(--success) / 0); }
}
.ticket-card {
  animation: ticket-pulse 2.4s ease-in-out infinite;
}
@media (prefers-reduced-motion: reduce) {
  .ticket-card { animation: none; }
}
</style>
