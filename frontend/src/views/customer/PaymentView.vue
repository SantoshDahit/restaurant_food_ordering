<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ordersApi } from '@/api/orders'
import { paymentApi } from '@/api/payment'
import { toast } from 'vue-sonner'
import type { OrdersResponse, PaymentMethod } from '@/types'

const route = useRoute()
const router = useRouter()

const orderCode = computed(() => route.query.orderCode as string)
const restaurantCode = computed(() => route.query.restaurantCode as string)

const order = ref<OrdersResponse | null>(null)
const selectedMethod = ref<PaymentMethod | null>(null)
const step = ref<'method' | 'processing' | 'success'>('method')
const payment = ref<{ code: string; amount: number } | null>(null)
const orderNumber = ref('')
const loading = ref(true)

const paymentMethods: { id: PaymentMethod; name: string; icon: string; description: string; color: string }[] = [
  { id: 'ESEWA', name: 'eSewa', icon: '💰', description: 'Digital wallet', color: 'bg-green-500' },
  { id: 'KHALTI', name: 'Khalti', icon: '🟣', description: 'Digital wallet', color: 'bg-purple-500' },
  { id: 'PHONEPAY', name: 'PhonePay', icon: '📱', description: 'UPI payment', color: 'bg-indigo-500' },
  { id: 'IBANK', name: 'iBank', icon: '🏦', description: 'Internet banking', color: 'bg-blue-500' },
  { id: 'POS', name: 'POS Machine', icon: '💳', description: 'Card payment', color: 'bg-orange-500' },
  { id: 'CASH', name: 'Cash', icon: '💵', description: 'Pay at counter', color: 'bg-gray-600' },
]

const finalTotal = computed(() => order.value?.totalAmount ?? 0)

async function loadOrder() {
  if (!orderCode.value) {
    toast.error('No order code provided')
    return
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
    toast.error('Please select a payment method')
    return
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
    step.value = 'success'
  } catch {
    toast.error('Payment failed. Please try again.')
    step.value = 'method'
  }
}

onMounted(loadOrder)
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center p-4">
    <!-- Loading -->
    <div v-if="loading" class="text-center">
      <div class="w-12 h-12 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-500">Loading order details...</p>
    </div>

    <!-- Processing -->
    <div v-else-if="step === 'processing'" class="text-center">
      <div class="w-16 h-16 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mx-auto mb-6"></div>
      <h2 class="text-2xl font-bold text-gray-900 mb-2">Processing Payment...</h2>
      <p class="text-gray-500">Please wait</p>
    </div>

    <!-- Success -->
    <div v-else-if="step === 'success'"
      class="max-w-md w-full bg-white rounded-2xl shadow-2xl p-8 text-center">
      <div class="w-24 h-24 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
        <svg class="w-12 h-12 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
      </div>
      <h2 class="text-3xl font-bold text-gray-900 mb-2">Payment Successful!</h2>
      <p class="text-gray-500 mb-6">Your order has been confirmed</p>

      <div class="bg-gray-50 rounded-xl p-5 mb-5 text-left space-y-3">
        <div class="flex justify-between text-sm">
          <span class="text-gray-500">Order Number</span>
          <span class="font-mono font-medium text-gray-900">#{{ orderNumber }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-gray-500">Payment Method</span>
          <span class="font-medium text-gray-900">{{ paymentMethods.find(m => m.id === selectedMethod)?.name }}</span>
        </div>
        <div class="border-t border-gray-200 pt-3 flex justify-between">
          <span class="font-semibold text-gray-900">Total Paid</span>
          <span class="text-xl font-bold text-green-600">NPR {{ (payment?.amount ?? finalTotal).toFixed(0) }}</span>
        </div>
      </div>

      <div class="bg-orange-50 rounded-xl p-4 mb-6">
        <div class="flex items-center gap-2 mb-2">
          <span class="text-lg">✨</span>
          <span class="font-semibold text-orange-700">Points Earned</span>
        </div>
        <p class="text-2xl font-bold text-orange-600">+{{ Math.floor(finalTotal) }} Points</p>
      </div>

      <p class="text-sm text-gray-500 mb-4">
        Estimated preparation time: <span class="font-medium">15–20 minutes</span>
      </p>

      <div class="space-y-3">
        <button @click="router.push('/')"
          class="w-full py-3 bg-orange-500 text-white font-medium rounded-xl hover:bg-orange-600 transition-colors">
          Back to Start
        </button>
        <button @click="toast.success('Receipt sent to your email')"
          class="w-full py-3 bg-gray-100 text-gray-700 font-medium rounded-xl hover:bg-gray-200 transition-colors">
          Email Receipt
        </button>
      </div>
    </div>

    <!-- Payment Method Selection -->
    <div v-else class="max-w-2xl w-full">
      <!-- Header -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-200 p-5 mb-5">
        <h1 class="text-2xl font-bold text-gray-900 mb-1">Payment</h1>
        <p class="text-gray-500 text-sm">Choose your payment method</p>
      </div>

      <!-- Order Summary -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 mb-5">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Order Summary</h2>
        <div class="space-y-3">
          <div class="flex justify-between text-gray-700">
            <span>Order #{{ orderNumber }}</span>
            <span>NPR {{ finalTotal.toFixed(0) }}</span>
          </div>
          <div class="border-t border-gray-200 pt-3 flex justify-between">
            <span class="text-lg font-semibold text-gray-900">Total</span>
            <span class="text-2xl font-bold text-orange-500">NPR {{ finalTotal.toFixed(0) }}</span>
          </div>
        </div>
      </div>

      <!-- Payment Methods -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-5 mb-5">
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Select Payment Method</h2>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <button v-for="method in paymentMethods" :key="method.id"
            @click="selectedMethod = method.id"
            :class="selectedMethod === method.id
              ? 'border-orange-500 bg-orange-50'
              : 'border-gray-200 hover:border-gray-300'"
            class="p-4 rounded-xl border-2 transition-all text-left">
            <div class="flex items-center gap-3">
              <div class="text-3xl">{{ method.icon }}</div>
              <div class="flex-1">
                <h3 class="font-semibold text-gray-900">{{ method.name }}</h3>
                <p class="text-xs text-gray-500">{{ method.description }}</p>
              </div>
              <div v-if="selectedMethod === method.id"
                class="w-6 h-6 bg-orange-500 rounded-full flex items-center justify-center flex-shrink-0">
                <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
              </div>
            </div>
          </button>
        </div>
      </div>

      <!-- Pay Button -->
      <button @click="processPayment" :disabled="!selectedMethod"
        :class="selectedMethod ? 'bg-orange-500 hover:bg-orange-600 active:scale-95' : 'bg-gray-300 cursor-not-allowed'"
        class="w-full py-4 text-white font-semibold rounded-xl transition-all text-lg">
        {{ selectedMethod ? `Pay NPR ${finalTotal.toFixed(0)}` : 'Select Payment Method' }}
      </button>
    </div>
  </div>
</template>
