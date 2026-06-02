<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ordersApi } from '@/api/orders'
import {
  UtensilsCrossed, Clock, ChefHat, BellRing, CheckCircle2,
  Loader2, AlertTriangle, Sparkles, Receipt,
} from 'lucide-vue-next'
import type { OrderDetailResponse, OrdersResponse, OrderStatus } from '@/types'

const route = useRoute()
const orderNumber = computed(() => route.params.orderNumber as string)

const order = ref<OrderDetailResponse | null>(null)
const activeOrders = ref<OrdersResponse[]>([])
const loading = ref(true)
const notFound = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null

// ── 4-step pipeline ──────────────────────────────────────────────────────
const STEPS = [
  { key: 'placed',    label: 'Order Placed', icon: Clock,        statuses: ['PENDING', 'CONFIRMED'] as OrderStatus[] },
  { key: 'preparing', label: 'Preparing',    icon: ChefHat,      statuses: ['PREPARING'] as OrderStatus[] },
  { key: 'ready',     label: 'Ready',        icon: BellRing,     statuses: ['READY'] as OrderStatus[] },
  { key: 'served',    label: 'Served',       icon: CheckCircle2, statuses: ['SERVED', 'COMPLETED'] as OrderStatus[] },
]

// Only today's tickets belong on the live pickup list; stale active orders
// from previous days shouldn't clutter it.
function isToday(iso: string): boolean {
  const d = new Date(iso)
  const now = new Date()
  return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()
}

function stepIndex(status: OrderStatus | undefined): number {
  if (!status) return -1
  return STEPS.findIndex(s => s.statuses.includes(status))
}

const currentStep = computed(() => stepIndex(order.value?.status))

// All active orders currently READY for pickup. We include the customer's own
// ticket so the board matches what's shown on the in-store screens.
const readyOrders = computed(() =>
  activeOrders.value.filter(o => o.status === 'READY' && isToday(o.createdAt)),
)
const isOwnTicket = (orderNum: string) => orderNum === orderNumber.value

// ── Celebration when this order flips to READY ────────────────────────────
const celebrate = ref(false)
const confetti = ref<Array<{ left: string; bg: string; delay: string; duration: string }>>([])

watch(
  () => order.value?.status,
  (next, prev) => {
    if (prev && prev !== 'READY' && next === 'READY') triggerCelebration()
  },
)

function triggerCelebration() {
  confetti.value = Array.from({ length: 60 }, () => ({
    left: `${Math.random() * 100}%`,
    bg: ['#10b981', '#a855f7', '#ec4899', '#f59e0b', '#3b82f6'][Math.floor(Math.random() * 5)],
    delay: `${Math.random() * 0.5}s`,
    duration: `${1.6 + Math.random() * 1.4}s`,
  }))
  celebrate.value = true
  setTimeout(() => { celebrate.value = false }, 3500)
}

// ── Data loading + polling ────────────────────────────────────────────────
async function refresh(initial = false) {
  try {
    if (initial) loading.value = true
    const detail = await ordersApi.getByOrderNumber(orderNumber.value)
    order.value = detail
    notFound.value = false
    // Don't keep showing the page once the order is served — but allow staff to keep it open
    const list = await ordersApi.getActiveByRestaurant(detail.restaurantCode)
    activeOrders.value = list
  } catch (e: any) {
    if (e?.response?.status === 404) notFound.value = true
  } finally {
    if (initial) loading.value = false
  }
}

onMounted(async () => {
  await refresh(true)
  pollTimer = setInterval(() => refresh(false), 5000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})

</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/30 text-slate-900">

    <!-- Loading -->
    <div v-if="loading" class="min-h-screen flex items-center justify-center">
      <div class="text-center">
        <Loader2 class="w-12 h-12 text-violet-500 mx-auto mb-3 animate-spin" />
        <p class="text-slate-500">Loading your order…</p>
      </div>
    </div>

    <!-- Not found -->
    <div v-else-if="notFound" class="min-h-screen flex items-center justify-center px-4">
      <div class="text-center max-w-sm">
        <div class="w-16 h-16 rounded-2xl bg-rose-50 ring-1 ring-rose-200 flex items-center justify-center mx-auto mb-3">
          <AlertTriangle class="w-7 h-7 text-rose-500" />
        </div>
        <p class="text-slate-900 font-semibold">Order not found</p>
        <p class="text-sm text-slate-500 mt-1">Check your tracking link and try again.</p>
      </div>
    </div>

    <template v-else-if="order">
      <!-- Header -->
      <header class="relative overflow-hidden bg-gradient-to-r from-violet-500 via-violet-500 to-fuchsia-500 text-white">
        <div aria-hidden="true" class="absolute -top-12 -right-12 w-48 h-48 rounded-full bg-white/10 blur-2xl" />
        <div aria-hidden="true" class="absolute -bottom-16 left-1/3 w-56 h-56 rounded-full bg-fuchsia-300/20 blur-3xl" />

        <div class="relative max-w-7xl mx-auto px-4 sm:px-8 py-5 sm:py-7 flex items-center justify-between gap-4">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-12 h-12 sm:w-14 sm:h-14 rounded-2xl bg-white/15 backdrop-blur ring-1 ring-white/30 flex items-center justify-center flex-shrink-0">
              <UtensilsCrossed class="w-6 h-6 sm:w-7 sm:h-7 text-white" />
            </div>
            <div class="min-w-0">
              <h1 class="text-xl sm:text-2xl xl:text-3xl font-bold tracking-tight truncate">{{ order.restaurantName ?? 'Order Tracking' }}</h1>
              <p class="text-violet-50/90 text-xs sm:text-sm flex items-center gap-1.5">
                <Sparkles class="w-3.5 h-3.5" />
                Live status — refreshes every 5 seconds
              </p>
            </div>
          </div>
          <div class="text-right flex-shrink-0 bg-white/10 backdrop-blur ring-1 ring-white/20 rounded-2xl px-3 sm:px-4 py-1.5 sm:py-2.5">
            <p class="text-[11px] sm:text-xs text-violet-50/90 uppercase tracking-wider">Total</p>
            <p class="text-lg sm:text-2xl font-bold tabular-nums">NPR {{ order.totalAmount.toFixed(0) }}</p>
          </div>
        </div>
      </header>

      <!-- Featured (current) order -->
      <section class="relative px-4 sm:px-8 pt-6 sm:pt-10 pb-4 max-w-7xl mx-auto">
        <div
          :class="[
            'relative bg-white rounded-3xl ring-1 shadow-xl overflow-hidden transition-all duration-500',
            order.status === 'READY' ? 'ring-emerald-300 shadow-emerald-500/20 scale-[1.01]' : 'ring-slate-200/60 shadow-slate-900/5',
            celebrate ? 'animate-celebrate' : '',
          ]"
        >
          <!-- Confetti -->
          <div v-if="celebrate" aria-hidden="true" class="pointer-events-none absolute inset-0 overflow-hidden">
            <span v-for="(c, i) in confetti" :key="i"
              class="confetti-piece"
              :style="{ left: c.left, background: c.bg, animationDelay: c.delay, animationDuration: c.duration }" />
          </div>

          <div class="p-6 sm:p-10 xl:p-12">
            <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 mb-6 sm:mb-8">
              <div>
                <p class="text-xs sm:text-sm font-semibold text-slate-500 uppercase tracking-wider">
                  {{ order.ticketNumber != null ? 'Your ticket' : 'Your order' }}
                </p>
                <p v-if="order.ticketNumber != null" class="text-6xl sm:text-7xl xl:text-8xl font-extrabold tracking-tight tabular-nums mt-1 leading-none">
                  {{ String(order.ticketNumber).padStart(3, '0') }}
                </p>
                <p v-else class="text-3xl sm:text-5xl xl:text-6xl font-bold tracking-tight tabular-nums mt-1">
                  #{{ order.orderNumber }}
                </p>
                <p class="text-slate-500 mt-2 text-xs sm:text-sm font-mono tabular-nums">{{ order.orderNumber }}</p>
                <p v-if="order.tableNumber" class="text-slate-500 mt-1 text-sm sm:text-base">Table {{ order.tableNumber }}</p>
              </div>
              <div
                :class="[
                  'inline-flex items-center gap-2 px-4 sm:px-5 py-2 sm:py-2.5 rounded-2xl text-sm sm:text-base font-semibold ring-1 transition-all',
                  order.status === 'READY'
                    ? 'bg-emerald-50 ring-emerald-200 text-emerald-700'
                    : order.status === 'PREPARING'
                      ? 'bg-amber-50 ring-amber-200 text-amber-700'
                      : order.status === 'COMPLETED'
                        ? 'bg-slate-100 ring-slate-200 text-slate-700'
                        : 'bg-violet-50 ring-violet-200 text-violet-700',
                ]"
              >
                <span class="w-2.5 h-2.5 rounded-full bg-current animate-pulse" />
                {{ order.status }}
              </div>
            </div>

            <!-- 4-step timeline -->
            <div class="relative">
              <!-- Connector base -->
              <div class="absolute left-0 right-0 top-7 sm:top-9 h-1 bg-slate-100 rounded-full" />
              <div
                class="absolute left-0 top-7 sm:top-9 h-1 rounded-full transition-all duration-700"
                :class="order.status === 'READY' ? 'bg-emerald-500' : 'bg-gradient-to-r from-violet-500 to-fuchsia-500'"
                :style="{ width: currentStep < 0 ? '0%' : currentStep === 0 ? '0%' : `${(currentStep / (STEPS.length - 1)) * 100}%` }"
              />

              <ol class="relative grid grid-cols-4 gap-2 sm:gap-4">
                <li v-for="(step, idx) in STEPS" :key="step.key" class="flex flex-col items-center text-center">
                  <div
                    :class="[
                      'w-14 h-14 sm:w-18 sm:h-18 xl:w-20 xl:h-20 rounded-full flex items-center justify-center ring-4 ring-white transition-all duration-500',
                      idx < currentStep
                        ? 'bg-gradient-to-br from-violet-500 to-fuchsia-500 text-white shadow-lg shadow-violet-500/30'
                        : idx === currentStep
                          ? step.key === 'ready'
                            ? 'bg-gradient-to-br from-emerald-500 to-green-600 text-white shadow-lg shadow-emerald-500/40 scale-110'
                            : 'bg-gradient-to-br from-violet-500 to-fuchsia-500 text-white shadow-lg shadow-violet-500/30 scale-110'
                          : 'bg-slate-100 text-slate-400',
                    ]"
                  >
                    <component :is="step.icon" class="w-6 h-6 sm:w-8 sm:h-8 xl:w-9 xl:h-9" />
                  </div>
                  <p
                    :class="[
                      'mt-2 sm:mt-3 text-xs sm:text-sm xl:text-base font-semibold transition-colors',
                      idx <= currentStep ? 'text-slate-900' : 'text-slate-400',
                    ]"
                  >
                    {{ step.label }}
                  </p>
                </li>
              </ol>
            </div>

            <!-- Items -->
            <div class="mt-8 sm:mt-10">
              <p class="text-xs sm:text-sm font-semibold text-slate-500 uppercase tracking-wider mb-3">Your items</p>
              <ul class="divide-y divide-slate-100 bg-slate-50/60 ring-1 ring-slate-200/60 rounded-2xl overflow-hidden">
                <li v-for="item in order.items" :key="item.code" class="flex items-center justify-between px-4 sm:px-5 py-3 sm:py-4 gap-3">
                  <div class="min-w-0 flex items-center gap-2 sm:gap-3">
                    <span class="inline-flex w-7 h-7 sm:w-8 sm:h-8 rounded-lg bg-white ring-1 ring-slate-200 items-center justify-center text-sm sm:text-base font-bold text-violet-600 tabular-nums">
                      {{ item.quantity }}
                    </span>
                    <span class="font-medium text-slate-900 text-sm sm:text-base truncate">{{ item.menuItemName ?? '—' }}</span>
                  </div>
                  <span class="text-sm sm:text-base font-semibold text-slate-900 tabular-nums flex-shrink-0">
                    NPR {{ item.totalPrice.toFixed(0) }}
                  </span>
                </li>
              </ul>
            </div>

            <!-- Receipt action -->
            <div class="mt-6 flex flex-col sm:flex-row gap-2">
              <button @click="$router.push(`/receipt/${order.orderNumber}`)"
                class="flex-1 inline-flex items-center justify-center gap-2 px-5 py-3 text-sm sm:text-base font-semibold text-white bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 rounded-xl shadow-md shadow-violet-500/30 transition-all">
                <Receipt class="w-4 h-4" />
                View / print receipt
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- Other active orders — Ready for pickup -->
      <section class="px-4 sm:px-8 pb-10 max-w-7xl mx-auto">
        <p class="text-sm sm:text-base font-semibold text-slate-500 uppercase tracking-wider mb-4 mt-4">Ready for pickup</p>
        <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm overflow-hidden">
          <div class="px-4 py-3 text-white font-semibold text-sm sm:text-base bg-gradient-to-r from-emerald-500 to-green-600">
            Ready
            <span class="ml-1.5 text-xs font-normal opacity-80">({{ readyOrders.length }})</span>
          </div>
          <ul v-if="readyOrders.length === 0" class="p-3">
            <li class="text-xs sm:text-sm text-slate-400 text-center py-6">No other tickets ready yet — yours will appear here on the screens too when it's done.</li>
          </ul>
          <ul v-else class="p-3 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2 sm:gap-3">
            <li v-for="o in readyOrders" :key="o.code"
              :class="['rounded-xl px-3 py-3 text-center transition-all',
                isOwnTicket(o.orderNumber)
                  ? 'bg-gradient-to-br from-emerald-400/30 to-green-500/20 ring-2 ring-emerald-400 shadow-md shadow-emerald-500/30 own-ticket-glow'
                  : 'bg-emerald-50/60 ring-1 ring-emerald-200/60']">
              <p v-if="isOwnTicket(o.orderNumber)" class="text-[9px] uppercase tracking-widest font-bold text-emerald-700 mb-1">You</p>
              <p v-if="o.ticketNumber != null"
                :class="['text-3xl sm:text-4xl font-extrabold tabular-nums leading-none',
                  isOwnTicket(o.orderNumber) ? 'text-emerald-800' : 'text-emerald-700']">
                {{ String(o.ticketNumber).padStart(3, '0') }}
              </p>
              <p v-else class="font-mono font-semibold text-slate-900 tabular-nums truncate text-sm">#{{ o.orderNumber }}</p>
              <p class="text-[10px] text-slate-400 mt-1 font-mono tabular-nums truncate">{{ o.orderNumber }}</p>
            </li>
          </ul>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
/* Celebration scale + glow */
@keyframes celebrate {
  0%   { transform: scale(1); }
  20%  { transform: scale(1.03); box-shadow: 0 25px 60px -15px rgba(16, 185, 129, 0.5); }
  100% { transform: scale(1); }
}
.animate-celebrate {
  animation: celebrate 1.6s ease-out;
}

/* Confetti pieces */
.confetti-piece {
  position: absolute;
  top: -10px;
  width: 8px;
  height: 12px;
  border-radius: 2px;
  animation-name: confetti-fall;
  animation-timing-function: cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation-fill-mode: forwards;
}
@keyframes confetti-fall {
  0%   { transform: translateY(0) rotate(0deg);   opacity: 1; }
  100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
}

/* Slightly larger step circles on xl */
@media (min-width: 1280px) {
  .sm\:w-18 { width: 4.5rem; }
  .sm\:h-18 { height: 4.5rem; }
}

/* Soft glow on the customer's own ready ticket so they can spot it on the board */
@keyframes own-ticket-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.5), 0 8px 20px -6px rgba(16, 185, 129, 0.35); }
  50%      { box-shadow: 0 0 0 6px rgba(16, 185, 129, 0),    0 12px 28px -6px rgba(16, 185, 129, 0.5); }
}
.own-ticket-glow {
  animation: own-ticket-glow 2.2s ease-in-out infinite;
}
@media (prefers-reduced-motion: reduce) {
  .own-ticket-glow { animation: none; }
}
</style>
