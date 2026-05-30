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
  Undo2, X,
} from 'lucide-vue-next'
import type { OrderDetailResponse, OrderType } from '@/types'

const auth = useAuthStore()
const orders = ref<OrderDetailResponse[]>([])
const loading = ref(true)
const advancing = ref<Record<string, boolean>>({})
const now = ref(Date.now())

let pollTimer: ReturnType<typeof setInterval> | null = null
let clockTimer: ReturnType<typeof setInterval> | null = null

// Sorted: PENDING first, then PREPARING; within each, oldest first.
const orderedQueue = computed(() => {
  const rank = (s: string) => s === 'PENDING' ? 0 : 1
  return [...orders.value].sort((a, b) => {
    const r = rank(a.status) - rank(b.status)
    if (r !== 0) return r
    return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  })
})

const counts = computed(() => ({
  pending: orders.value.filter(o => o.status === 'PENDING').length,
  preparing: orders.value.filter(o => o.status === 'PREPARING').length,
}))

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
  if (minutes >= 30) return 'text-rose-600'
  if (minutes >= 15) return 'text-amber-600'
  return 'text-slate-500'
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
  const next = o.status === 'PENDING' ? 'PREPARING' : 'READY'
  if (advancing.value[o.code]) return
  advancing.value = { ...advancing.value, [o.code]: true }
  try {
    await ordersApi.updateStatus(o.code, { status: next })
    toast.success(next === 'PREPARING' ? `#${o.ticketNumber ?? o.orderNumber} → Preparing` : `#${o.ticketNumber ?? o.orderNumber} → Ready 🔔`)
    await refresh(false)
  } catch {
    toast.error('Failed to update status')
  } finally {
    advancing.value = { ...advancing.value, [o.code]: false }
  }
}

async function revert(o: OrderDetailResponse) {
  // PREPARING → PENDING. PENDING has no earlier state to revert to.
  if (o.status !== 'PREPARING') return
  if (advancing.value[o.code]) return
  advancing.value = { ...advancing.value, [o.code]: true }
  try {
    await ordersApi.updateStatus(o.code, { status: 'PENDING' })
    toast.success(`#${o.ticketNumber ?? o.orderNumber} → back to Pending`)
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
        <span class="text-sm text-slate-500">
          {{ counts.pending }} pending · {{ counts.preparing }} preparing · refreshes every 5 seconds
        </span>
      </template>
    </PageHeader>

    <div v-if="loading" class="flex items-center justify-center py-24">
      <Loader2 class="w-10 h-10 text-violet-500 animate-spin" />
    </div>

    <div v-else-if="!orderedQueue.length"
      class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm py-20 text-center">
      <div class="w-16 h-16 mx-auto rounded-2xl bg-emerald-50 ring-1 ring-emerald-200 flex items-center justify-center mb-3">
        <ChefHat class="w-8 h-8 text-emerald-600" />
      </div>
      <p class="text-lg font-semibold text-slate-900">All clear!</p>
      <p class="text-sm text-slate-500 mt-1">No active tickets. New orders will appear here automatically.</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div v-for="o in orderedQueue" :key="o.code"
        :class="['kitchen-card bg-white rounded-2xl ring-2 shadow-sm overflow-hidden flex flex-col transition-all',
          o.status === 'PENDING' ? 'ring-amber-300 shadow-amber-500/10' : 'ring-violet-300 shadow-violet-500/10']">

        <!-- Card header -->
        <div :class="['px-4 py-2.5 flex items-center justify-between gap-2 text-white',
          o.status === 'PENDING' ? 'bg-gradient-to-r from-amber-500 to-orange-500' : 'bg-gradient-to-r from-violet-500 to-fuchsia-500']">
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
            <p class="text-[10px] uppercase tracking-widest text-slate-500 font-semibold">Ticket</p>
            <p v-if="o.ticketNumber != null"
              class="text-4xl sm:text-5xl font-extrabold text-slate-900 tabular-nums leading-none">
              {{ String(o.ticketNumber).padStart(3, '0') }}
            </p>
            <p v-else class="text-lg font-mono font-semibold text-slate-900 tabular-nums truncate">
              #{{ o.orderNumber }}
            </p>
            <p class="text-[10px] text-slate-400 mt-1 font-mono tabular-nums">{{ o.orderNumber }}</p>
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
            <span class="inline-flex w-7 h-7 rounded-lg bg-slate-100 ring-1 ring-slate-200 items-center justify-center text-sm font-bold text-violet-700 tabular-nums flex-shrink-0">
              {{ item.quantity }}
            </span>
            <div class="min-w-0 flex-1">
              <p class="font-medium text-slate-900 leading-tight">{{ item.menuItemName ?? '—' }}</p>
              <p v-if="item.notes" class="text-[11px] text-rose-600 italic mt-0.5">⚠ {{ item.notes }}</p>
              <p v-if="item.spiceLevel" class="text-[11px] text-amber-600 mt-0.5">🌶 {{ item.spiceLevel }}</p>
            </div>
          </li>
          <li v-if="!o.items.length" class="text-xs text-slate-400 italic py-2">No items listed.</li>
        </ul>

        <!-- Special notes -->
        <div v-if="o.specialNotes" class="mx-4 mb-2 px-3 py-2 rounded-lg bg-rose-50 ring-1 ring-rose-200 text-xs text-rose-700">
          <span class="font-semibold">Note:</span> {{ o.specialNotes }}
        </div>

        <!-- Action buttons — tap target sized for tablet -->
        <div class="m-3 mt-1 space-y-2">
          <button @click="advance(o)" :disabled="advancing[o.code]"
            :class="['w-full py-3 rounded-xl font-bold text-white shadow-md transition-all flex items-center justify-center gap-2 text-base disabled:opacity-60',
              o.status === 'PENDING'
                ? 'bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 shadow-violet-500/30'
                : 'bg-gradient-to-r from-emerald-500 to-green-600 hover:from-emerald-600 hover:to-green-700 shadow-emerald-500/30']">
            <Loader2 v-if="advancing[o.code]" class="w-5 h-5 animate-spin" />
            <template v-else>
              <component :is="o.status === 'PENDING' ? ChefHat : BellRing" class="w-5 h-5" />
              {{ o.status === 'PENDING' ? 'Start preparing' : 'Mark ready' }}
            </template>
          </button>
          <!-- Secondary row: undo (only when PREPARING) + cancel -->
          <div class="flex gap-2">
            <button v-if="o.status === 'PREPARING'" @click="revert(o)" :disabled="advancing[o.code]"
              class="flex-1 py-2 rounded-lg text-sm font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 ring-1 ring-slate-200 transition-colors disabled:opacity-60 inline-flex items-center justify-center gap-1.5">
              <Undo2 class="w-3.5 h-3.5" /> Undo
            </button>
            <button @click="cancel(o)" :disabled="advancing[o.code]"
              class="flex-1 py-2 rounded-lg text-sm font-semibold text-rose-600 bg-rose-50 hover:bg-rose-100 ring-1 ring-rose-200 transition-colors disabled:opacity-60 inline-flex items-center justify-center gap-1.5">
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
