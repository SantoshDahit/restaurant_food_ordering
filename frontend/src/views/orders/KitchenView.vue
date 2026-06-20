<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { orderTypeLabel } from '@/utils/orderType'
import { ordersApi } from '@/api/orders'
import PageHeader from '@/components/shared/PageHeader.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import { toast } from 'vue-sonner'
import {
  Clock4, ChefHat, BellRing, Loader2, Armchair, ShoppingBag, Smartphone, Monitor,
  Undo2, X, ClipboardCheck,
} from 'lucide-vue-next'
import type { OrderDetailResponse, OrderType, OrderStatus } from '@/types'

const auth = useAuthStore()
const orders = ref<OrderDetailResponse[]>([])
const loading = ref(true)
const advancing = ref<Record<string, boolean>>({})
const now = ref(Date.now())

let pollTimer: ReturnType<typeof setInterval> | null = null
let clockTimer: ReturnType<typeof setInterval> | null = null

// Kitchen flow: PENDING → CONFIRMED → PREPARING → READY (READY leaves the queue).
const NEXT_STATUS: Record<string, OrderStatus> = { PENDING: 'CONFIRMED', CONFIRMED: 'PREPARING', PREPARING: 'READY' }
const PREV_STATUS: Record<string, OrderStatus> = { CONFIRMED: 'PENDING', PREPARING: 'CONFIRMED' }

// Sorted by stage (PENDING → CONFIRMED → PREPARING); within each, oldest first.
const orderedQueue = computed(() => {
  const rank = (s: string) => s === 'PENDING' ? 0 : s === 'CONFIRMED' ? 1 : 2
  return [...orders.value].sort((a, b) => {
    const r = rank(a.status) - rank(b.status)
    if (r !== 0) return r
    return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  })
})

const counts = computed(() => ({
  pending: orders.value.filter(o => o.status === 'PENDING').length,
  confirmed: orders.value.filter(o => o.status === 'CONFIRMED').length,
  preparing: orders.value.filter(o => o.status === 'PREPARING').length,
}))

function advanceLabel(s: string): string {
  return s === 'PENDING' ? 'Confirm order' : s === 'CONFIRMED' ? 'Start preparing' : 'Mark ready'
}
function advanceIcon(s: string) {
  return s === 'PENDING' ? ClipboardCheck : s === 'CONFIRMED' ? ChefHat : BellRing
}
function cardRing(s: string): string {
  return s === 'PENDING' ? 'ring-warning/30 shadow-soft'
    : s === 'CONFIRMED' ? 'ring-info/30 shadow-soft'
    : 'ring-primary/30 shadow-soft'
}
function headerBg(s: string): string {
  return s === 'PENDING' ? 'bg-warning'
    : s === 'CONFIRMED' ? 'bg-info'
    : 'bg-primary'
}

const TYPE_ICON: Record<OrderType, any> = {
  DINE_IN: Armchair, TAKEAWAY: ShoppingBag, QR_ORDER: Smartphone, KIOSK: Monitor,
}

function elapsed(iso: string): string {
  const ms = now.value - new Date(iso).getTime()
  const minutes = Math.max(0, Math.floor(ms / 60000))
  if (minutes < 1) return 'just now'
  if (minutes < 60) return `${minutes} min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m === 0 ? `${h} hr` : `${h} hr ${m} min`
}

function elapsedTint(iso: string): string {
  const minutes = (now.value - new Date(iso).getTime()) / 60000
  if (minutes >= 30) return 'text-destructive'
  if (minutes >= 15) return 'text-warning'
  return 'text-muted-foreground'
}

async function refresh(initial = false) {
  if (!auth.restaurantCode) return
  try {
    if (initial) loading.value = true
    orders.value = await ordersApi.kitchen(auth.restaurantCode)
  } catch { /* silent — keep last good data */ }
  finally { if (initial) loading.value = false }
}

async function advance(o: OrderDetailResponse) {
  const next = NEXT_STATUS[o.status]
  if (!next || advancing.value[o.code]) return
  advancing.value = { ...advancing.value, [o.code]: true }
  try {
    await ordersApi.updateStatus(o.code, { status: next })
    const label = next === 'CONFIRMED' ? 'Confirmed' : next === 'PREPARING' ? 'Preparing' : 'Ready 🔔'
    toast.success(`#${o.ticketNumber ?? o.orderNumber} → ${label}`)
    await refresh(false)
  } catch {
    toast.error('Failed to update status')
  } finally {
    advancing.value = { ...advancing.value, [o.code]: false }
  }
}

async function revert(o: OrderDetailResponse) {
  const prev = PREV_STATUS[o.status]
  if (!prev || advancing.value[o.code]) return
  advancing.value = { ...advancing.value, [o.code]: true }
  try {
    await ordersApi.updateStatus(o.code, { status: prev })
    toast.success(`#${o.ticketNumber ?? o.orderNumber} → back to ${prev === 'PENDING' ? 'Pending' : 'Confirmed'}`)
    await refresh(false)
  } catch {
    toast.error('Failed to revert status')
  } finally {
    advancing.value = { ...advancing.value, [o.code]: false }
  }
}

async function cancel(o: OrderDetailResponse) {
  if (!confirm(`Cancel order #${o.ticketNumber ?? o.orderNumber}? This cannot be undone from here.`)) return
  if (advancing.value[o.code]) return
  advancing.value = { ...advancing.value, [o.code]: true }
  try {
    await ordersApi.updateStatus(o.code, { status: 'CANCELLED' })
    toast.success(`#${o.ticketNumber ?? o.orderNumber} cancelled`)
    await refresh(false)
  } catch {
    toast.error('Failed to cancel order')
  } finally {
    advancing.value = { ...advancing.value, [o.code]: false }
  }
}

onMounted(async () => {
  await refresh(true)
  // Match the pickup board's 3s cadence so kitchen actions show on the board promptly.
  pollTimer = setInterval(() => refresh(false), 3000)
  clockTimer = setInterval(() => { now.value = Date.now() }, 30000)
})

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (clockTimer) clearInterval(clockTimer)
})
</script>

<template>
  <RestaurantGuard resource="kitchen display">
    <PageHeader title="Kitchen Display">
      <template #description>
        <span class="text-sm text-muted-foreground">
          {{ counts.pending }} pending · {{ counts.confirmed }} confirmed · {{ counts.preparing }} preparing · refreshes every 5 seconds
        </span>
      </template>
    </PageHeader>

    <div v-if="loading" class="flex items-center justify-center py-24">
      <Loader2 class="w-10 h-10 text-primary animate-spin" />
    </div>

    <div v-else-if="!orderedQueue.length"
      class="bg-card rounded-2xl ring-1 ring-border shadow-sm py-20 text-center">
      <div class="w-16 h-16 mx-auto rounded-2xl bg-success/10 ring-1 ring-success/20 flex items-center justify-center mb-3">
        <ChefHat class="w-8 h-8 text-success" />
      </div>
      <p class="text-lg font-semibold text-foreground">All clear!</p>
      <p class="text-sm text-muted-foreground mt-1">No active tickets. New orders will appear here automatically.</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div v-for="o in orderedQueue" :key="o.code"
        :class="['kitchen-card bg-card rounded-2xl ring-2 shadow-sm overflow-hidden flex flex-col transition-all', cardRing(o.status)]">

        <!-- Card header -->
        <div :class="['px-4 py-2.5 flex items-center justify-between gap-2 text-primary-foreground', headerBg(o.status)]">
          <div class="flex items-center gap-2 min-w-0">
            <component :is="TYPE_ICON[o.orderType]" class="w-4 h-4 flex-shrink-0" />
            <span class="text-xs font-semibold uppercase tracking-wider truncate">
              {{ orderTypeLabel(o.orderType) }}<span v-if="o.tableNumber"> · Table {{ o.tableNumber }}</span>
            </span>
          </div>
          <span class="text-[10px] font-semibold uppercase tracking-wider bg-white/20 rounded px-1.5 py-0.5">
            {{ o.status }}
          </span>
        </div>

        <!-- Ticket + elapsed -->
        <div class="px-4 pt-4 pb-2 flex items-end justify-between gap-3">
          <div>
            <p class="text-[10px] uppercase tracking-widest text-muted-foreground font-semibold">Ticket</p>
            <p v-if="o.ticketNumber != null"
              class="text-4xl sm:text-5xl font-extrabold text-foreground tabular-nums leading-none">
              {{ String(o.ticketNumber).padStart(3, '0') }}
            </p>
            <p v-else class="text-lg font-mono font-semibold text-foreground tabular-nums truncate">
              #{{ o.orderNumber }}
            </p>
            <p class="text-[10px] text-muted-foreground mt-1 font-mono tabular-nums">{{ o.orderNumber }}</p>
          </div>
          <div class="text-right">
            <div :class="['inline-flex items-center gap-1 text-xs font-semibold', elapsedTint(o.createdAt)]">
              <Clock4 class="w-3.5 h-3.5" />
              {{ elapsed(o.createdAt) }}
            </div>
          </div>
        </div>

        <!-- Items -->
        <ul class="px-4 py-2 flex-1 space-y-1 max-h-64 overflow-y-auto">
          <li v-for="item in o.items" :key="item.code" class="flex items-start gap-2 text-sm">
            <span class="inline-flex w-7 h-7 rounded-lg bg-muted ring-1 ring-border items-center justify-center text-sm font-bold text-primary tabular-nums flex-shrink-0">
              {{ item.quantity }}
            </span>
            <div class="min-w-0 flex-1">
              <p class="font-medium text-foreground leading-tight">{{ item.menuItemName ?? '—' }}</p>
              <p v-if="item.notes" class="text-[11px] text-destructive italic mt-0.5">⚠ {{ item.notes }}</p>
              <p v-if="item.spiceLevel" class="text-[11px] text-warning mt-0.5">🌶 {{ item.spiceLevel }}</p>
            </div>
          </li>
          <li v-if="!o.items.length" class="text-xs text-muted-foreground italic py-2">No items listed.</li>
        </ul>

        <!-- Special notes -->
        <div v-if="o.specialNotes" class="mx-4 mb-2 px-3 py-2 rounded-lg bg-destructive/10 ring-1 ring-destructive/20 text-xs text-destructive">
          <span class="font-semibold">Note:</span> {{ o.specialNotes }}
        </div>

        <!-- Action buttons — tap target sized for tablet -->
        <div class="m-3 mt-1 space-y-2">
          <button @click="advance(o)" :disabled="advancing[o.code]"
            :class="['w-full py-3 rounded-xl font-bold text-primary-foreground shadow-md transition-all flex items-center justify-center gap-2 text-base disabled:opacity-60',
              o.status === 'PREPARING'
                ? 'bg-success hover:bg-success/90 shadow-soft'
                : 'bg-primary hover:bg-primary/90 shadow-soft']">
            <Loader2 v-if="advancing[o.code]" class="w-5 h-5 animate-spin" />
            <template v-else>
              <component :is="advanceIcon(o.status)" class="w-5 h-5" />
              {{ advanceLabel(o.status) }}
            </template>
          </button>
          <!-- Secondary row: undo (when there's an earlier stage) + cancel -->
          <div class="flex gap-2">
            <button v-if="PREV_STATUS[o.status]" @click="revert(o)" :disabled="advancing[o.code]"
              class="flex-1 py-2 rounded-lg text-sm font-semibold text-foreground bg-muted hover:bg-accent ring-1 ring-border transition-colors disabled:opacity-60 inline-flex items-center justify-center gap-1.5">
              <Undo2 class="w-3.5 h-3.5" /> Undo
            </button>
            <button @click="cancel(o)" :disabled="advancing[o.code]"
              class="flex-1 py-2 rounded-lg text-sm font-semibold text-destructive bg-destructive/10 hover:bg-destructive/20 ring-1 ring-destructive/20 transition-colors disabled:opacity-60 inline-flex items-center justify-center gap-1.5">
              <X class="w-3.5 h-3.5" /> Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  </RestaurantGuard>
</template>

<style scoped>
.kitchen-card:hover { transform: translateY(-1px); box-shadow: 0 8px 24px -8px rgba(0, 0, 0, 0.12); }
</style>
