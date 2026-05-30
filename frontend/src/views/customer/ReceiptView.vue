<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ordersApi } from '@/api/orders'
import { receiptApi } from '@/api/receipt'
import { paymentApi } from '@/api/payment'
import { Loader2, AlertTriangle, Printer, ArrowLeft } from 'lucide-vue-next'
import type { OrderDetailResponse, ReceiptResponse, ReceiptItemSnapshot, PaymentResponse } from '@/types'

const route = useRoute()
const orderNumber = computed(() => route.params.orderNumber as string)

// Where to send the user when they click "Back".
// `?from=order&code=<uuid>` is set by the admin's Order Detail page so staff
// land back where they came from. Everything else (customer flows) goes home
// to the tracking page for that order.
const backTarget = computed(() => {
  if (route.query.from === 'order' && route.query.code) {
    return `/dashboard/orders/${route.query.code}`
  }
  if (route.query.from === 'payments') return '/dashboard/payments'
  return `/track/${orderNumber.value}`
})
const backLabel = computed(() => {
  if (route.query.from === 'order') return 'Back to order'
  if (route.query.from === 'payments') return 'Back to payments'
  return 'Back to tracking'
})

// Either we render from a real receipt row (preferred) or fall back to the
// live order detail for legacy orders that pre-date the receipt table.
const receipt = ref<ReceiptResponse | null>(null)
const order = ref<OrderDetailResponse | null>(null)
const payment = ref<PaymentResponse | null>(null)
const loading = ref(true)
const notFound = ref(false)

async function load() {
  try {
    loading.value = true
    const detail = await ordersApi.getByOrderNumber(orderNumber.value)
    order.value = detail
    // Receipt + live payment — load in parallel; either may be absent for
    // unpaid orders. We use the live payment row for the *current* status
    // (PENDING vs. COMPLETED) since the receipt snapshot is frozen at issue.
    const [r, p] = await Promise.allSettled([
      receiptApi.getByOrder(detail.code),
      paymentApi.getByOrder(detail.code),
    ])
    if (r.status === 'fulfilled') receipt.value = r.value
    if (p.status === 'fulfilled') payment.value = p.value
  } catch (e: any) {
    if (e?.response?.status === 404) notFound.value = true
  } finally {
    loading.value = false
  }
}

const paymentStatus = computed(() => payment.value?.status ?? receipt.value?.paymentStatus ?? null)
const paymentStatusTint = computed(() => {
  const s = paymentStatus.value
  if (s === 'COMPLETED') return 'bg-emerald-50 text-emerald-700 ring-emerald-200'
  if (s === 'PENDING')   return 'bg-amber-50 text-amber-700 ring-amber-200'
  if (s === 'FAILED' || s === 'REFUNDED') return 'bg-rose-50 text-rose-700 ring-rose-200'
  return 'bg-slate-50 text-slate-600 ring-slate-200'
})

function print() {
  window.print()
}

// ── Display helpers ─────────────────────────────────────────────────────────
const restaurantName = computed(() => receipt.value?.restaurantNameSnapshot ?? order.value?.restaurantName ?? 'Restaurant')
const displayOrderNumber = computed(() => receipt.value?.orderNumberSnapshot ?? order.value?.orderNumber ?? '')
const displayTableNumber = computed(() => receipt.value?.tableNumberSnapshot ?? order.value?.tableNumber ?? null)
const subtotal = computed(() => Number(receipt.value?.subtotal ?? order.value?.subtotal ?? 0))
const discount = computed(() => Number(receipt.value?.discountAmount ?? order.value?.discountAmount ?? 0))
const tax = computed(() => Number(receipt.value?.taxAmount ?? order.value?.taxAmount ?? 0))
const total = computed(() => Number(receipt.value?.totalAmount ?? order.value?.totalAmount ?? 0))
const orderType = computed(() => (order.value?.orderType ?? '').replace(/_/g, ' '))
const orderStatus = computed(() => order.value?.status ?? '')

// 3-digit daily ticket. Prefer the receipt's stored number; otherwise fall back
// to the order's own ticketNumber (V12+); legacy orders without either fall
// through to the last 3 digits of the orderNumber.
const ticket = computed(() => {
  if (receipt.value?.receiptNumber != null) return String(receipt.value.receiptNumber).padStart(3, '0')
  if (order.value?.ticketNumber != null) return String(order.value.ticketNumber).padStart(3, '0')
  const n = order.value?.orderNumber ?? ''
  return n.length >= 3 ? n.slice(-3) : n
})

const formattedDate = computed(() => {
  const iso = receipt.value?.issuedAt ?? order.value?.createdAt
  if (!iso) return ''
  return new Date(iso).toLocaleString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
})

const items = computed<ReceiptItemSnapshot[]>(() => {
  if (receipt.value) {
    try {
      const parsed = JSON.parse(receipt.value.itemsJson) as ReceiptItemSnapshot[]
      return Array.isArray(parsed) ? parsed : []
    } catch { return [] }
  }
  return (order.value?.items ?? []).map(i => ({
    menuItemCode: i.menuItemCode,
    name: i.menuItemName ?? null,
    quantity: i.quantity,
    unitPrice: i.unitPrice,
    totalPrice: i.totalPrice,
    notes: i.notes ?? null,
  }))
})

onMounted(load)
</script>

<template>
  <div class="min-h-screen bg-slate-100 receipt-page">

    <!-- Toolbar (hidden on print) -->
    <div class="no-print bg-white border-b border-slate-200/60 sticky top-0 z-10">
      <div class="max-w-3xl mx-auto px-4 py-3 flex items-center justify-between gap-2">
        <button @click="$router.push(backTarget)"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 rounded-lg transition-colors">
          <ArrowLeft class="w-4 h-4" /> {{ backLabel }}
        </button>
        <button @click="print" :disabled="!order"
          class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-semibold text-white bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 rounded-xl shadow-md shadow-violet-500/30 transition-all disabled:opacity-60">
          <Printer class="w-4 h-4" /> Print receipt
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="no-print flex items-center justify-center py-24">
      <div class="text-center">
        <Loader2 class="w-10 h-10 text-violet-500 mx-auto mb-3 animate-spin" />
        <p class="text-slate-500 text-sm">Loading receipt…</p>
      </div>
    </div>

    <!-- Not found -->
    <div v-else-if="notFound" class="no-print flex items-center justify-center py-24 px-4">
      <div class="text-center max-w-sm">
        <div class="w-14 h-14 rounded-2xl bg-rose-50 ring-1 ring-rose-200 flex items-center justify-center mx-auto mb-3">
          <AlertTriangle class="w-6 h-6 text-rose-500" />
        </div>
        <p class="text-slate-900 font-semibold">Order not found</p>
        <p class="text-sm text-slate-500 mt-1">Check the receipt link and try again.</p>
      </div>
    </div>

    <!-- Receipt -->
    <div v-else-if="order" class="max-w-3xl mx-auto px-4 py-6">
      <div class="receipt mx-auto bg-white text-slate-900 shadow-md print:shadow-none">
        <div class="px-6 py-5 receipt-inner">

          <!-- Restaurant header -->
          <div class="text-center">
            <h1 class="text-xl font-bold uppercase tracking-wide">{{ restaurantName }}</h1>
            <p v-if="receipt?.restaurantAddress" class="text-[11px] text-slate-600 mt-0.5">{{ receipt.restaurantAddress }}</p>
            <p v-if="receipt?.restaurantPhone" class="text-[11px] text-slate-600">Tel: {{ receipt.restaurantPhone }}</p>
            <p v-if="receipt?.restaurantBusinessNumber" class="text-[11px] text-slate-600">VAT/PAN: {{ receipt.restaurantBusinessNumber }}</p>
            <p class="text-[11px] text-slate-500 mt-1">Sales receipt</p>
          </div>

          <div class="my-3 border-t border-dashed border-slate-300" />

          <!-- Big 3-digit ticket -->
          <div class="text-center my-2">
            <p class="text-[10px] uppercase tracking-widest text-slate-500">Ticket number</p>
            <p class="text-6xl font-extrabold leading-none mt-1 tabular-nums">{{ ticket }}</p>
            <p v-if="receipt" class="text-[10px] text-slate-400 mt-1">resets daily</p>
          </div>

          <div class="my-3 border-t border-dashed border-slate-300" />

          <!-- Meta -->
          <div class="grid grid-cols-2 gap-y-1 text-xs">
            <span class="text-slate-500">Order #</span>
            <span class="text-right font-semibold font-mono tabular-nums">{{ displayOrderNumber }}</span>

            <span class="text-slate-500">Type</span>
            <span class="text-right">{{ orderType }}</span>

            <template v-if="displayTableNumber">
              <span class="text-slate-500">Table</span>
              <span class="text-right">{{ displayTableNumber }}</span>
            </template>

            <span class="text-slate-500">Date</span>
            <span class="text-right">{{ formattedDate }}</span>
          </div>

          <div class="my-3 border-t border-dashed border-slate-300" />

          <!-- Items -->
          <table class="w-full text-xs">
            <thead>
              <tr class="text-slate-500 border-b border-slate-200">
                <th class="text-left py-1 font-semibold">Item</th>
                <th class="text-right py-1 font-semibold w-10">Qty</th>
                <th class="text-right py-1 font-semibold w-16">Price</th>
                <th class="text-right py-1 font-semibold w-16">Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, idx) in items" :key="idx" class="align-top">
                <td class="py-1 pr-2">{{ item.name ?? '—' }}</td>
                <td class="py-1 text-right tabular-nums">{{ item.quantity }}</td>
                <td class="py-1 text-right tabular-nums">{{ Number(item.unitPrice).toFixed(0) }}</td>
                <td class="py-1 text-right tabular-nums">{{ Number(item.totalPrice).toFixed(0) }}</td>
              </tr>
              <tr v-if="!items.length">
                <td colspan="4" class="py-3 text-center text-slate-400">No items</td>
              </tr>
            </tbody>
          </table>

          <div class="my-3 border-t border-dashed border-slate-300" />

          <!-- Totals -->
          <div class="grid grid-cols-2 gap-y-1 text-xs">
            <span class="text-slate-500">Subtotal</span>
            <span class="text-right tabular-nums">NPR {{ subtotal.toFixed(2) }}</span>

            <template v-if="discount > 0">
              <span class="text-slate-500">Discount</span>
              <span class="text-right tabular-nums">- NPR {{ discount.toFixed(2) }}</span>
            </template>

            <template v-if="tax > 0">
              <span class="text-slate-500">Tax</span>
              <span class="text-right tabular-nums">NPR {{ tax.toFixed(2) }}</span>
            </template>
          </div>

          <div class="mt-2 pt-2 border-t border-slate-400 flex items-center justify-between">
            <span class="text-sm font-bold uppercase">Total</span>
            <span class="text-lg font-extrabold tabular-nums">NPR {{ total.toFixed(2) }}</span>
          </div>

          <!-- Payment -->
          <div v-if="receipt" class="mt-3 pt-2 border-t border-dashed border-slate-300 grid grid-cols-2 gap-y-1.5 text-xs">
            <span class="text-slate-500">Paid via</span>
            <span class="text-right font-semibold">{{ receipt.paymentMethod }}</span>
            <span class="text-slate-500">Status</span>
            <span class="text-right">
              <span :class="['inline-flex items-center gap-1 px-2 py-0.5 rounded ring-1 font-bold uppercase tracking-wider text-[10px]', paymentStatusTint]">
                <span class="w-1.5 h-1.5 rounded-full bg-current" />
                {{ paymentStatus }}
              </span>
            </span>
            <template v-if="receipt.gatewayProvider">
              <span class="text-slate-500">Gateway</span>
              <span class="text-right">{{ receipt.gatewayProvider }}</span>
            </template>
            <template v-if="receipt.gatewayTransactionId">
              <span class="text-slate-500">Txn ID</span>
              <span class="text-right font-mono tabular-nums truncate">{{ receipt.gatewayTransactionId }}</span>
            </template>
          </div>
          <p v-else class="mt-3 text-center text-[11px] italic text-slate-500">
            Payment not yet recorded
          </p>

          <div class="my-4 border-t border-dashed border-slate-300" />

          <!-- Footer -->
          <div class="text-center text-[11px] text-slate-500 leading-relaxed">
            <p class="font-semibold text-slate-700">Thank you!</p>
            <p>Please keep this receipt for your records.</p>
            <p v-if="receipt" class="text-[9px] mt-1 font-mono text-slate-400">{{ receipt.code }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.receipt {
  width: 100%;
  max-width: 360px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

@media print {
  @page {
    size: 80mm auto;
    margin: 0;
  }
  :global(body) { background: #fff !important; }
  .no-print { display: none !important; }
  .receipt-page { background: #fff !important; padding: 0 !important; }
  .receipt {
    max-width: none !important;
    width: 80mm !important;
    box-shadow: none !important;
    margin: 0 !important;
  }
  .receipt-inner { padding: 4mm !important; }
}
</style>
