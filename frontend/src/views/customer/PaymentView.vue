<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ordersApi } from '@/api/orders'
import { paymentApi } from '@/api/payment'
import { toast } from 'vue-sonner'
import {
  Wallet, Smartphone, Building2, CreditCard, Banknote, Check,
  Loader2, Sparkles, ArrowRight, Mail, Receipt,
} from 'lucide-vue-next'
import type { Component } from 'vue'
import type { OrdersResponse, PaymentMethod } from '@/types'

const route = useRoute()
const router = useRouter()

const orderCode = computed(() => route.query.orderCode as string)
const restaurantCode = computed(() => route.query.restaurantCode as string)
const source = computed(() => route.query.source as 'qr' | 'kiosk' | 'table' | undefined)
const sourceToken = computed(() => route.query.token as string | undefined)
const sourceKioskCode = computed(() => route.query.kioskCode as string | undefined)
const sourceTableCode = computed(() => route.query.tableCode as string | undefined)

function goBackToStart() {
  if (source.value === 'qr' && sourceToken.value) {
    router.push(`/qr/${sourceToken.value}`)
  } else if (source.value === 'kiosk' && sourceKioskCode.value) {
    router.push(`/kiosk/${sourceKioskCode.value}`)
  } else if (source.value === 'table' && sourceTableCode.value) {
    router.push(`/table/${sourceTableCode.value}`)
  } else {
    router.push('/')
  }
}

const order = ref<OrdersResponse | null>(null)
const selectedMethod = ref<PaymentMethod | null>(null)
const step = ref<'method' | 'processing' | 'success'>('method')
const payment = ref<{ code: string; amount: number } | null>(null)
const orderNumber = ref('')
const loading = ref(true)

const paymentMethods: { id: PaymentMethod; name: string; description: string; icon: Component; gradient: string }[] = [
  { id: 'ESEWA',    name: 'eSewa',     description: 'Digital wallet',  icon: Wallet,     gradient: 'from-emerald-500 to-green-500' },
  { id: 'KHALTI',   name: 'Khalti',    description: 'Digital wallet',  icon: Smartphone, gradient: 'from-purple-500 to-fuchsia-500' },
  { id: 'PHONEPAY', name: 'PhonePay',  description: 'UPI payment',     icon: Smartphone, gradient: 'from-indigo-500 to-blue-500' },
  { id: 'IBANK',    name: 'iBank',     description: 'Internet banking',icon: Building2,  gradient: 'from-sky-500 to-blue-500' },
  { id: 'POS',      name: 'POS Machine', description: 'Card payment',  icon: CreditCard, gradient: 'from-violet-500 to-fuchsia-500' },
  { id: 'CASH',     name: 'Cash',      description: 'Pay at counter',  icon: Banknote,   gradient: 'from-slate-500 to-slate-600' },
]

const finalTotal = computed(() => order.value?.totalAmount ?? 0)
const ticketNumber = computed(() => order.value?.ticketNumber ?? null)

// CASH / POS are settled at the counter — the success screen shows "pay at the
// counter, show your receipt" rather than implying the payment is already done.
const isCounterPayment = computed(() => selectedMethod.value === 'CASH' || selectedMethod.value === 'POS')

// Cart is persisted per table/token by QROrderingView so a failed payment can
// resume without losing items; clear it once the payment actually succeeds.
const storedCartKey = computed(() => {
  const id = sourceToken.value ?? sourceTableCode.value
  return id ? `qr_cart_${id}` : null
})
function clearStoredCart() {
  if (storedCartKey.value) sessionStorage.removeItem(storedCartKey.value)
}

async function loadOrder() {
  if (!orderCode.value) {
    toast.error('No order code provided'); return
  }
  try {
    order.value = await ordersApi.get(orderCode.value)
    orderNumber.value = order.value.orderNumber
  } catch {
    toast.error('Failed to load order details')
  } finally {
    loading.value = false
  }
}

async function processPayment() {
  if (!selectedMethod.value) {
    toast.error('Please select a payment method'); return
  }
  // eSewa goes through the real hosted gateway (redirect + verified callback).
  if (selectedMethod.value === 'ESEWA') {
    await payWithEsewa(); return
  }
  step.value = 'processing'
  try {
    const result = await paymentApi.create({
      restaurantCode: restaurantCode.value,
      orderCode: orderCode.value,
      paymentMethod: selectedMethod.value,
      amount: finalTotal.value,
    })
    payment.value = { code: result.code, amount: result.amount }
    clearStoredCart()
    step.value = 'success'
  } catch {
    toast.error('Payment failed. Please try again.')
    step.value = 'method'
  }
}

async function payWithEsewa() {
  step.value = 'processing'
  try {
    const returnUrl = `${window.location.origin}/payment/esewa/callback`
    // Stash the context the callback needs to verify, show success, and
    // navigate back — eSewa only round-trips a `data` blob, nothing else.
    sessionStorage.setItem('esewa_return', JSON.stringify({
      orderCode: orderCode.value,
      restaurantCode: restaurantCode.value,
      orderNumber: orderNumber.value,
      ticketNumber: ticketNumber.value,
      amount: finalTotal.value,
      source: source.value ?? null,
      token: sourceToken.value ?? null,
      kioskCode: sourceKioskCode.value ?? null,
      tableCode: sourceTableCode.value ?? null,
    }))
    const res = await paymentApi.esewaInitiate({
      restaurantCode: restaurantCode.value,
      orderCode: orderCode.value,
      amount: finalTotal.value,
      successUrl: returnUrl,
      failureUrl: returnUrl,
    })
    // Build + auto-submit a hidden form so the browser navigates to eSewa.
    const form = document.createElement('form')
    form.method = 'POST'
    form.action = res.formUrl
    Object.entries(res.fields).forEach(([name, value]) => {
      const input = document.createElement('input')
      input.type = 'hidden'
      input.name = name
      input.value = value
      form.appendChild(input)
    })
    document.body.appendChild(form)
    form.submit()
  } catch {
    toast.error('Could not start eSewa payment. Please try again.')
    step.value = 'method'
  }
}

onMounted(loadOrder)
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 via-white to-violet-50/30 flex items-center justify-center p-4">
    <!-- Loading -->
    <div v-if="loading" class="text-center">
      <Loader2 class="w-12 h-12 text-violet-500 mx-auto mb-3 animate-spin" />
      <p class="text-slate-500">Loading order details…</p>
    </div>

    <!-- Processing -->
    <div v-else-if="step === 'processing'" class="text-center">
      <Loader2 class="w-16 h-16 text-violet-500 mx-auto mb-5 animate-spin" />
      <h2 class="text-2xl font-bold text-slate-900 mb-1">Processing payment…</h2>
      <p class="text-slate-500">Hang tight, this should only take a moment.</p>
    </div>

    <!-- Success -->
    <div v-else-if="step === 'success'" class="max-w-md w-full bg-white rounded-3xl shadow-xl shadow-slate-900/5 ring-1 ring-slate-200/60 p-7 sm:p-8">
      <div class="text-center">
        <div :class="['w-20 h-20 rounded-2xl flex items-center justify-center mx-auto mb-5 shadow-lg bg-gradient-to-br',
          isCounterPayment ? 'from-violet-400 to-fuchsia-500 shadow-violet-500/30' : 'from-emerald-400 to-green-500 shadow-emerald-500/30']">
          <component :is="isCounterPayment ? Receipt : Check" class="w-10 h-10 text-white stroke-[3]" />
        </div>
        <h2 class="text-2xl sm:text-3xl font-bold text-slate-900 mb-1">
          {{ isCounterPayment ? 'Order placed' : 'Payment successful' }}
        </h2>
        <p class="text-slate-500 text-sm">
          {{ isCounterPayment ? 'Please pay at the counter — show your receipt below.' : 'Your order has been confirmed.' }}
        </p>
      </div>

      <div class="mt-6 bg-slate-50 ring-1 ring-slate-200/60 rounded-2xl p-5 space-y-3">
        <div v-if="ticketNumber != null" class="flex justify-between items-center text-sm">
          <span class="text-slate-500">Ticket number</span>
          <span class="font-bold text-violet-600 text-lg tabular-nums">#{{ String(ticketNumber).padStart(3, '0') }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-slate-500">Order number</span>
          <span class="font-mono font-semibold text-slate-900">#{{ orderNumber }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-slate-500">Payment method</span>
          <span class="font-medium text-slate-900">{{ paymentMethods.find(m => m.id === selectedMethod)?.name }}</span>
        </div>
        <div class="border-t border-slate-200 pt-3 flex justify-between items-end">
          <span class="font-semibold text-slate-900">{{ isCounterPayment ? 'Amount to pay' : 'Total paid' }}</span>
          <span :class="['text-2xl font-bold tabular-nums', isCounterPayment ? 'text-violet-600' : 'text-emerald-600']">NPR {{ (payment?.amount ?? finalTotal).toFixed(0) }}</span>
        </div>
      </div>

      <div class="mt-4 bg-gradient-to-br from-violet-50 to-fuchsia-50 ring-1 ring-violet-100/60 rounded-2xl p-4 flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center flex-shrink-0">
          <Sparkles class="w-5 h-5 text-white" />
        </div>
        <div class="min-w-0">
          <p class="text-xs font-semibold text-violet-700 uppercase tracking-wide">Points earned</p>
          <p class="text-xl font-bold text-violet-600 tabular-nums">+{{ Math.floor(finalTotal) }}</p>
        </div>
      </div>

      <p class="text-center text-sm text-slate-500 mt-4">
        Estimated preparation time: <span class="font-medium text-slate-700">15–20 minutes</span>
      </p>

      <div class="mt-6 space-y-2">
        <button @click="router.push(`/track/${orderNumber}`)"
          class="w-full py-3 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white font-semibold rounded-xl shadow-md shadow-violet-500/30 transition-all flex items-center justify-center gap-1.5">
          Track your order
          <ArrowRight class="w-4 h-4" />
        </button>
        <button @click="router.push(`/receipt/${orderNumber}`)"
          class="w-full py-2.5 bg-white hover:bg-slate-50 text-slate-700 font-medium rounded-xl ring-1 ring-slate-200/60 transition-colors flex items-center justify-center gap-1.5">
          <Receipt class="w-4 h-4" />
          View / print receipt
        </button>
        <button @click="goBackToStart"
          class="w-full py-2.5 bg-white hover:bg-slate-50 text-slate-700 font-medium rounded-xl ring-1 ring-slate-200/60 transition-colors flex items-center justify-center gap-1.5">
          Back to start
        </button>
        <button @click="toast.success('Receipt sent to your email')"
          class="w-full py-2.5 bg-slate-50 hover:bg-slate-100 text-slate-700 font-medium rounded-xl ring-1 ring-slate-200/60 transition-colors flex items-center justify-center gap-1.5">
          <Mail class="w-4 h-4" />
          Email receipt
        </button>
      </div>
    </div>

    <!-- Method selection -->
    <div v-else class="max-w-2xl w-full">
      <!-- Title -->
      <div class="text-center mb-6">
        <div class="inline-flex w-12 h-12 rounded-2xl bg-gradient-to-br from-violet-500 to-fuchsia-500 items-center justify-center shadow-lg shadow-violet-500/30 mb-3">
          <Receipt class="w-6 h-6 text-white" />
        </div>
        <h1 class="text-2xl font-bold text-slate-900">Complete your payment</h1>
        <p class="text-sm text-slate-500 mt-1">Choose how you'd like to pay.</p>
      </div>

      <!-- Order summary -->
      <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5 mb-4">
        <div class="flex items-center justify-between mb-3">
          <h2 class="text-sm font-semibold text-slate-500 uppercase tracking-wide">Order summary</h2>
          <span class="text-xs font-mono text-slate-400">#{{ orderNumber }}</span>
        </div>
        <div class="flex items-end justify-between">
          <span class="text-slate-600">Total amount</span>
          <span class="text-3xl font-bold text-violet-600 tabular-nums">NPR {{ finalTotal.toFixed(0) }}</span>
        </div>
      </div>

      <!-- Methods -->
      <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-5 mb-4">
        <h2 class="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-3">Payment method</h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
          <button v-for="method in paymentMethods" :key="method.id"
            @click="selectedMethod = method.id"
            :class="selectedMethod === method.id
              ? 'ring-2 ring-violet-500 bg-violet-50/50 shadow-sm'
              : 'ring-1 ring-slate-200 hover:ring-slate-300 bg-white'"
            class="p-3.5 rounded-2xl transition-all text-left">
            <div class="flex items-center gap-3">
              <div :class="['w-10 h-10 rounded-xl flex items-center justify-center text-white flex-shrink-0 bg-gradient-to-br', method.gradient]">
                <component :is="method.icon" class="w-5 h-5" />
              </div>
              <div class="flex-1 min-w-0">
                <h3 class="font-semibold text-slate-900 text-sm leading-tight">{{ method.name }}</h3>
                <p class="text-[11px] text-slate-500 truncate">{{ method.description }}</p>
              </div>
              <div v-if="selectedMethod === method.id"
                class="w-6 h-6 bg-violet-500 rounded-full flex items-center justify-center flex-shrink-0 shadow-sm">
                <Check class="w-3.5 h-3.5 text-white stroke-[3]" />
              </div>
            </div>
          </button>
        </div>
      </div>

      <!-- Pay -->
      <button @click="processPayment" :disabled="!selectedMethod"
        :class="selectedMethod
          ? 'bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 shadow-lg shadow-violet-500/30 active:scale-[0.99]'
          : 'bg-slate-200 text-slate-400 cursor-not-allowed'"
        class="w-full py-4 text-white font-bold rounded-2xl transition-all text-base flex items-center justify-center gap-2">
        {{ selectedMethod ? `Pay NPR ${finalTotal.toFixed(0)}` : 'Select a payment method' }}
        <ArrowRight v-if="selectedMethod" class="w-5 h-5" />
      </button>
    </div>
  </div>
</template>
