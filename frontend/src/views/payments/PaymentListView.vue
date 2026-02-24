<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { paymentApi } from '@/api/payment'
import { ordersApi } from '@/api/orders'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import { toast } from 'vue-sonner'
import type { PaymentResponse, PaymentStatus, PaymentMethod } from '@/types'

const auth = useAuthStore()
const payments = ref<PaymentResponse[]>([])
const loading = ref(false)
const showCreateDialog = ref(false)
const creating = ref(false)

const paymentMethods: PaymentMethod[] = ['CASH', 'POS', 'ESEWA', 'KHALTI', 'PHONEPAY', 'IBANK']
const paymentStatuses: PaymentStatus[] = ['PENDING', 'COMPLETED', 'FAILED', 'REFUNDED']

const form = ref({
  orderCode: '',
  paymentMethod: 'CASH' as PaymentMethod,
  amount: 0,
  transactionRef: '',
  receiptNumber: '',
})

onMounted(loadPayments)

async function loadPayments() {
  loading.value = true
  try {
    const data = await paymentApi.search({ restaurantCode: auth.restaurantCode })
    payments.value = data.content
  } catch {
    toast.error('Failed to load payments')
  } finally {
    loading.value = false
  }
}

async function createPayment() {
  if (!form.value.orderCode || form.value.amount <= 0) {
    toast.error('Order code and amount are required')
    return
  }
  creating.value = true
  try {
    await paymentApi.create({
      restaurantCode: auth.restaurantCode,
      orderCode: form.value.orderCode,
      paymentMethod: form.value.paymentMethod,
      amount: form.value.amount,
      transactionRef: form.value.transactionRef || undefined,
      receiptNumber: form.value.receiptNumber || undefined,
    })
    toast.success('Payment recorded')
    showCreateDialog.value = false
    form.value = { orderCode: '', paymentMethod: 'CASH', amount: 0, transactionRef: '', receiptNumber: '' }
    loadPayments()
  } catch {
    toast.error('Failed to create payment')
  } finally {
    creating.value = false
  }
}

async function updateStatus(code: string, status: PaymentStatus) {
  try {
    await paymentApi.updateStatus(code, { status })
    toast.success('Status updated')
    loadPayments()
  } catch {
    toast.error('Failed to update status')
  }
}
</script>

<template>
  <div>
    <PageHeader title="Payments" description="Manage payment records">
      <template #actions>
        <button @click="showCreateDialog = true"
          class="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">
          + Record Payment
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-gray-400">Loading...</div>

    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-gray-500 uppercase text-xs">
          <tr>
            <th class="px-5 py-3 text-left">Order</th>
            <th class="px-5 py-3 text-left">Method</th>
            <th class="px-5 py-3 text-right">Amount</th>
            <th class="px-5 py-3 text-left">Status</th>
            <th class="px-5 py-3 text-left">Receipt</th>
            <th class="px-5 py-3 text-left">Date</th>
            <th class="px-5 py-3 text-center">Update</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="payment in payments" :key="payment.code" class="hover:bg-gray-50">
            <td class="px-5 py-3 font-medium text-blue-600">{{ payment.orderCode.slice(0, 8) }}...</td>
            <td class="px-5 py-3 text-gray-600">{{ payment.paymentMethod }}</td>
            <td class="px-5 py-3 text-right font-semibold">{{ payment.amount.toFixed(2) }}</td>
            <td class="px-5 py-3"><StatusBadge :status="payment.status" /></td>
            <td class="px-5 py-3 text-gray-400 text-xs">{{ payment.receiptNumber || '—' }}</td>
            <td class="px-5 py-3 text-gray-400">{{ new Date(payment.createAt).toLocaleDateString() }}</td>
            <td class="px-5 py-3 text-center">
              <select
                :value="payment.status"
                @change="updateStatus(payment.code, ($event.target as HTMLSelectElement).value as PaymentStatus)"
                class="text-xs px-2 py-1 border border-gray-300 rounded focus:outline-none">
                <option v-for="s in paymentStatuses" :key="s" :value="s">{{ s }}</option>
              </select>
            </td>
          </tr>
          <tr v-if="!payments.length">
            <td colspan="7" class="px-5 py-8 text-center text-gray-400">No payments found</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Dialog -->
    <Teleport to="body">
      <div v-if="showCreateDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="showCreateDialog = false" />
        <div class="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
          <h3 class="text-lg font-semibold mb-4">Record Payment</h3>
          <form @submit.prevent="createPayment" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Order Code *</label>
              <input v-model="form.orderCode" required placeholder="Order code"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Payment Method</label>
              <select v-model="form.paymentMethod"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option v-for="m in paymentMethods" :key="m" :value="m">{{ m }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Amount *</label>
              <input v-model.number="form.amount" type="number" min="0" step="0.01"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Transaction Ref</label>
              <input v-model="form.transactionRef"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Receipt Number</label>
              <input v-model="form.receiptNumber"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showCreateDialog = false"
                class="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
              <button type="submit" :disabled="creating"
                class="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {{ creating ? 'Saving...' : 'Save' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>
