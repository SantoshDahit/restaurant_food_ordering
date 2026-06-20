<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { paymentApi } from '@/api/payment'
import { Check, Loader2, XCircle, ArrowRight, Receipt } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

type ReturnCtx = {
  orderCode: string
  restaurantCode: string
  orderNumber: string
  ticketNumber: number | null
  amount: number
  source: 'qr' | 'kiosk' | 'table' | null
  token: string | null
  kioskCode: string | null
  tableCode: string | null
}

const step = ref<'verifying' | 'success' | 'failed'>('verifying')
const ctx = ref<ReturnCtx | null>(null)
const paidAmount = ref(0)

function loadCtx(): ReturnCtx | null {
  try {
    const raw = sessionStorage.getItem('esewa_return')
    return raw ? (JSON.parse(raw) as ReturnCtx) : null
  } catch {
    return null
  }
}

// Where the customer's ordering session started (their table/QR menu). Used as
// the back-target so they never land back on this verifying URL.
function startPath(): string {
  const c = ctx.value
  if (c?.source === 'qr' && c.token) return `/qr/${c.token}`
  if (c?.source === 'kiosk' && c.kioskCode) return `/kiosk/${c.kioskCode}`
  if (c?.source === 'table' && c.tableCode) return `/table/${c.tableCode}`
  // Pay-at-end bill payments carry no source — fall back to the order's tracking page.
  if (c?.orderNumber) return `/track/${c.orderNumber}`
  return '/'
}

function clearStoredCart() {
  const c = ctx.value
  const id = c?.token ?? c?.tableCode
  if (id) sessionStorage.removeItem(`qr_cart_${id}`)
}

// Replace (not push) so this callback URL leaves the history stack — pressing
// the device back button from the receipt/tracking page must NOT re-trigger
// verification.
function goToStart() {
  router.replace(startPath())
}

function viewTracking() {
  router.replace(`/track/${ctx.value?.orderNumber}`)
}

async function viewReceipt() {
  // Land on tracking first so the receipt's back button (and device back) goes
  // to tracking rather than this verifying page.
  await router.replace(`/track/${ctx.value?.orderNumber}`)
  router.push(`/receipt/${ctx.value?.orderNumber}`)
}

// Failed payment → the order was cancelled, so retry restarts from the menu
// (the cart was preserved by the ordering view).
function retry() {
  router.replace(startPath())
}

async function verify() {
  ctx.value = loadCtx()
  const data = route.query.data as string | undefined
  // eSewa redirects to failure_url without a data blob when payment fails.
  if (!data) { await onFailed(); return }
  try {
    const payment = await paymentApi.esewaVerify(data)
    if (payment.status === 'COMPLETED') {
      paidAmount.value = payment.amount
      clearStoredCart()
      sessionStorage.removeItem('esewa_return')
      step.value = 'success'
    } else {
      await onFailed()
    }
  } catch {
    await onFailed()
  }
}

// Roll back the unpaid order (releases its ticket) so a failed payment never
// leaves an active order. Best-effort — ignore errors.
async function onFailed() {
  if (ctx.value?.orderCode) {
    try { await paymentApi.esewaCancel(ctx.value.orderCode) } catch { /* ignore */ }
  }
  step.value = 'failed'
}

onMounted(verify)
</script>

<template>
  <div class="min-h-screen bg-background flex items-center justify-center p-4">
    <!-- Verifying -->
    <div v-if="step === 'verifying'" class="text-center">
      <Loader2 class="w-16 h-16 text-primary mx-auto mb-5 animate-spin" />
      <h2 class="text-2xl font-bold text-foreground mb-1">Verifying payment…</h2>
      <p class="text-muted-foreground">Confirming your eSewa transaction.</p>
    </div>

    <!-- Success -->
    <div v-else-if="step === 'success'" class="max-w-md w-full bg-card rounded-3xl shadow-lifted ring-1 ring-border p-7 sm:p-8">
      <div class="text-center">
        <div class="w-20 h-20 bg-success rounded-2xl flex items-center justify-center mx-auto mb-5 shadow-soft">
          <Check class="w-10 h-10 text-success-foreground stroke-[3]" />
        </div>
        <h2 class="text-2xl sm:text-3xl font-bold text-foreground mb-1">Payment successful</h2>
        <p class="text-muted-foreground text-sm">Your eSewa payment has been confirmed.</p>
      </div>

      <div class="mt-6 bg-muted ring-1 ring-border rounded-2xl p-5 space-y-3">
        <div v-if="ctx?.ticketNumber != null" class="flex justify-between items-center text-sm">
          <span class="text-muted-foreground">Ticket number</span>
          <span class="font-bold text-primary text-lg tabular-nums">#{{ String(ctx.ticketNumber).padStart(3, '0') }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-muted-foreground">Order number</span>
          <span class="font-mono font-semibold text-foreground">#{{ ctx?.orderNumber }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-muted-foreground">Payment method</span>
          <span class="font-medium text-foreground">eSewa</span>
        </div>
        <div class="border-t border-border pt-3 flex justify-between items-end">
          <span class="font-semibold text-foreground">Total paid</span>
          <span class="text-2xl font-bold text-success tabular-nums">NPR {{ (paidAmount || ctx?.amount || 0).toFixed(0) }}</span>
        </div>
      </div>

      <div class="mt-6 space-y-2">
        <button @click="viewTracking"
          class="w-full py-3 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-xl shadow-soft transition-all flex items-center justify-center gap-1.5">
          Track your order
          <ArrowRight class="w-4 h-4" />
        </button>
        <button @click="viewReceipt"
          class="w-full py-2.5 bg-card hover:bg-accent text-foreground font-medium rounded-xl ring-1 ring-border transition-colors flex items-center justify-center gap-1.5">
          <Receipt class="w-4 h-4" />
          View / print receipt
        </button>
        <button @click="goToStart"
          class="w-full py-2.5 bg-card hover:bg-accent text-foreground font-medium rounded-xl ring-1 ring-border transition-colors flex items-center justify-center gap-1.5">
          Back to start
        </button>
      </div>
    </div>

    <!-- Failed -->
    <div v-else class="max-w-md w-full bg-card rounded-3xl shadow-lifted ring-1 ring-border p-7 sm:p-8">
      <div class="text-center">
        <div class="w-20 h-20 bg-destructive rounded-2xl flex items-center justify-center mx-auto mb-5 shadow-soft">
          <XCircle class="w-10 h-10 text-destructive-foreground stroke-[2.5]" />
        </div>
        <h2 class="text-2xl sm:text-3xl font-bold text-foreground mb-1">Payment not completed</h2>
        <p class="text-muted-foreground text-sm">Your eSewa payment was cancelled or could not be verified. No charge was made.</p>
      </div>

      <div class="mt-6 space-y-2">
        <button @click="retry"
          class="w-full py-3 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-xl shadow-soft transition-all flex items-center justify-center gap-1.5">
          Try again
          <ArrowRight class="w-4 h-4" />
        </button>
        <button @click="goToStart"
          class="w-full py-2.5 bg-card hover:bg-accent text-foreground font-medium rounded-xl ring-1 ring-border transition-colors flex items-center justify-center gap-1.5">
          Back to start
        </button>
      </div>
    </div>
  </div>
</template>
