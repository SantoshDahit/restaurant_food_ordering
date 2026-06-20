<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { paymentApi } from '@/api/payment'
import { ordersApi } from '@/api/orders'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import { CreditCard, Receipt } from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import type { PaymentResponse, PaymentStatus, PaymentMethod, OrdersResponse } from '@/types'

const auth = useAuthStore()
const router = useRouter()
const payments = ref<PaymentResponse[]>([])
const orders = ref<OrdersResponse[]>([])
const loading = ref(false)
const showCreateDialog = ref(false)
const creating = ref(false)

const paymentMethods: PaymentMethod[] = ['CASH', 'POS', 'ESEWA', 'KHALTI', 'FONEPAY']
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
  if (!auth.restaurantCode) return
  loading.value = true
  try {
    const data = await paymentApi.search({ restaurantCode: auth.restaurantCode, size: 200 })
    payments.value = data.content
  } catch {
    toast.error('Failed to load payments')
  } finally {
    loading.value = false
  }
}

async function loadOrders() {
  if (!auth.restaurantCode) return
  try {
    const data = await ordersApi.search({ restaurantCode: auth.restaurantCode, size: 50 })
    orders.value = data.content
  } catch { /* silent — picker just falls back to empty list */ }
}

// Load orders the first time the dialog opens, and auto-fill amount when an order is picked
watch(showCreateDialog, (open) => {
  if (open && orders.value.length === 0) loadOrders()
})
watch(() => form.value.orderCode, (code) => {
  const o = orders.value.find(x => x.code === code)
  if (o) form.value.amount = o.totalAmount
})

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

async function viewReceipt(orderCode: string) {
  try {
    const order = await ordersApi.get(orderCode)
    router.push({ path: `/receipt/${order.orderNumber}`, query: { from: 'payments' } })
  } catch {
    toast.error('Failed to open receipt')
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
  <RestaurantGuard resource="payments">
    <PageHeader title="Payments" description="Manage payment records">
      <template #actions>
        <button @click="showCreateDialog = true"
          class="px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground text-sm rounded-xl shadow-soft transition-all">
          + Record Payment
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-muted-foreground">Loading...</div>

    <div v-else class="bg-card rounded-2xl shadow-card ring-1 ring-border overflow-x-auto">
      <table class="w-full text-sm min-w-[760px]">
        <thead class="bg-muted text-muted-foreground uppercase text-[11px] tracking-wide">
          <tr>
            <th class="px-5 py-3 text-left">Order</th>
            <th class="px-5 py-3 text-left">Method</th>
            <th class="px-5 py-3 text-right">Amount</th>
            <th class="px-5 py-3 text-left">Status</th>
            <th class="px-5 py-3 text-left">Ref #</th>
            <th class="px-5 py-3 text-left">Date</th>
            <th class="px-5 py-3 text-center">Update</th>
            <th class="px-5 py-3 text-center sticky right-0 bg-muted shadow-[-4px_0_8px_-4px_rgba(0,0,0,0.08)]">Receipt</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-border">
          <tr v-for="payment in payments" :key="payment.code" class="hover:bg-accent transition-colors">
            <td class="px-5 py-3 font-medium text-info">{{ payment.orderCode.slice(0, 8) }}...</td>
            <td class="px-5 py-3 text-muted-foreground">{{ payment.paymentMethod }}</td>
            <td class="px-5 py-3 text-right font-semibold">{{ payment.amount.toFixed(2) }}</td>
            <td class="px-5 py-3"><StatusBadge :status="payment.status" /></td>
            <td class="px-5 py-3 text-muted-foreground text-xs">{{ payment.receiptNumber || '—' }}</td>
            <td class="px-5 py-3 text-muted-foreground">{{ payment.createdAt ? new Date(payment.createdAt).toLocaleDateString() : '—' }}</td>
            <td class="px-5 py-3 text-center">
              <select
                :value="payment.status"
                @change="updateStatus(payment.code, ($event.target as HTMLSelectElement).value as PaymentStatus)"
                class="text-xs px-2 py-1 bg-card border border-border rounded-lg focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30">
                <option v-for="s in paymentStatuses" :key="s" :value="s">{{ s }}</option>
              </select>
            </td>
            <td class="px-5 py-3 text-center sticky right-0 bg-card shadow-[-4px_0_8px_-4px_rgba(0,0,0,0.08)]">
              <button @click="viewReceipt(payment.orderCode)"
                class="inline-flex items-center gap-1 text-xs px-2.5 py-1 bg-primary hover:bg-primary/90 text-primary-foreground rounded-lg shadow-soft transition-all">
                <Receipt class="w-3.5 h-3.5" /> View
              </button>
            </td>
          </tr>
          <tr v-if="!payments.length">
            <td colspan="7" class="p-0">
              <EmptyState
                :icon="CreditCard"
                title="No payments yet"
                description="Payments will appear here when orders are settled."
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Dialog -->
    <Teleport to="body">
      <div v-if="showCreateDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-foreground/50" @click="showCreateDialog = false" />
        <div class="relative bg-card rounded-2xl shadow-lifted ring-1 ring-border p-6 w-full max-w-sm mx-4">
          <h3 class="text-lg font-semibold mb-4">Record Payment</h3>
          <form @submit.prevent="createPayment" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Order *</label>
              <select v-model="form.orderCode" required
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all">
                <option value="" disabled>Select an order…</option>
                <option v-for="o in orders" :key="o.code" :value="o.code">
                  #{{ o.orderNumber }} — NPR {{ o.totalAmount.toFixed(0) }} · {{ o.status }}
                </option>
              </select>
              <p v-if="!orders.length" class="text-xs text-muted-foreground mt-1">No recent orders. Create an order first.</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Payment Method</label>
              <select v-model="form.paymentMethod"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all">
                <option v-for="m in paymentMethods" :key="m" :value="m">{{ m }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Amount *</label>
              <input v-model.number="form.amount" type="number" min="0" step="0.01"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Transaction Ref</label>
              <input v-model="form.transactionRef"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Receipt Number</label>
              <input v-model="form.receiptNumber"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showCreateDialog = false"
                class="px-4 py-2 text-sm bg-card ring-1 ring-border text-foreground rounded-xl hover:bg-accent transition-colors">Cancel</button>
              <button type="submit" :disabled="creating"
                class="px-4 py-2 text-sm bg-info text-info-foreground rounded-lg hover:bg-info/90 disabled:opacity-50">
                {{ creating ? 'Saving...' : 'Save' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </RestaurantGuard>
</template>
