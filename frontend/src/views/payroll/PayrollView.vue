<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { payrollApi } from '@/api/payroll'
import { employeeApi } from '@/api/employee'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import { toast } from 'vue-sonner'
import type { PayrollResponse, EmployeeResponse, SalaryStatus } from '@/types'

const auth = useAuthStore()
const payrolls = ref<PayrollResponse[]>([])
const employees = ref<EmployeeResponse[]>([])
const loading = ref(false)
const showCreateDialog = ref(false)
const creating = ref(false)

const salaryStatuses: SalaryStatus[] = ['PENDING', 'PAID', 'ON_HOLD']

const form = ref({
  employeeCode: '',
  payPeriodStart: '',
  payPeriodEnd: '',
  netSalary: 0,
  bonus: 0,
  deductions: 0,
  overtimePay: 0,
})

onMounted(async () => {
  await Promise.all([loadPayrolls(), loadEmployees()])
})

async function loadPayrolls() {
  loading.value = true
  try {
    const data = await payrollApi.search({ restaurantCode: auth.restaurantCode })
    payrolls.value = data.content
  } catch {
    toast.error('Failed to load payroll records')
  } finally {
    loading.value = false
  }
}

async function loadEmployees() {
  try {
    const data = await employeeApi.search({ restaurantCode: auth.restaurantCode })
    employees.value = data.content
  } catch { /* silent */ }
}

function openCreate() {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0]
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0]
  form.value = { employeeCode: '', payPeriodStart: start, payPeriodEnd: end, netSalary: 0, bonus: 0, deductions: 0, overtimePay: 0 }
  showCreateDialog.value = true
}

async function createPayroll() {
  if (!form.value.employeeCode || form.value.netSalary <= 0) {
    toast.error('Employee and net salary are required')
    return
  }
  creating.value = true
  try {
    await payrollApi.create({
      restaurantCode: auth.restaurantCode,
      employeeCode: form.value.employeeCode,
      payPeriodStart: form.value.payPeriodStart,
      payPeriodEnd: form.value.payPeriodEnd,
      netSalary: form.value.netSalary,
      bonus: form.value.bonus,
      deductions: form.value.deductions,
      overtimePay: form.value.overtimePay,
    })
    toast.success('Payroll record created')
    showCreateDialog.value = false
    loadPayrolls()
  } catch {
    toast.error('Failed to create payroll')
  } finally {
    creating.value = false
  }
}

async function updateStatus(code: string, status: SalaryStatus) {
  try {
    await payrollApi.updateStatus(code, { status })
    toast.success('Status updated')
    loadPayrolls()
  } catch {
    toast.error('Failed to update status')
  }
}

function getEmployeeName(code: string) {
  return employees.value.find(e => e.code === code)?.fullName || code
}
</script>

<template>
  <div>
    <PageHeader title="Payroll" description="Manage employee payroll records">
      <template #actions>
        <button @click="openCreate"
          class="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">
          + Create Payroll
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-gray-400">Loading...</div>

    <div v-else class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 text-gray-500 uppercase text-xs">
          <tr>
            <th class="px-5 py-3 text-left">Employee</th>
            <th class="px-5 py-3 text-left">Period</th>
            <th class="px-5 py-3 text-right">Net Salary</th>
            <th class="px-5 py-3 text-right">Bonus</th>
            <th class="px-5 py-3 text-right">Deductions</th>
            <th class="px-5 py-3 text-left">Status</th>
            <th class="px-5 py-3 text-left">Paid At</th>
            <th class="px-5 py-3 text-center">Update</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="payroll in payrolls" :key="payroll.code" class="hover:bg-gray-50">
            <td class="px-5 py-3 font-medium text-gray-900">{{ getEmployeeName(payroll.employeeCode) }}</td>
            <td class="px-5 py-3 text-gray-500 text-xs">
              {{ payroll.payPeriodStart }} – {{ payroll.payPeriodEnd }}
            </td>
            <td class="px-5 py-3 text-right font-semibold">{{ payroll.netSalary.toFixed(0) }}</td>
            <td class="px-5 py-3 text-right text-green-600">+{{ payroll.bonus.toFixed(0) }}</td>
            <td class="px-5 py-3 text-right text-red-500">-{{ payroll.deductions.toFixed(0) }}</td>
            <td class="px-5 py-3"><StatusBadge :status="payroll.status" /></td>
            <td class="px-5 py-3 text-gray-400 text-xs">
              {{ payroll.paidAt ? new Date(payroll.paidAt).toLocaleDateString() : '—' }}
            </td>
            <td class="px-5 py-3 text-center">
              <select
                :value="payroll.status"
                @change="updateStatus(payroll.code, ($event.target as HTMLSelectElement).value as SalaryStatus)"
                class="text-xs px-2 py-1 border border-gray-300 rounded focus:outline-none">
                <option v-for="s in salaryStatuses" :key="s" :value="s">{{ s }}</option>
              </select>
            </td>
          </tr>
          <tr v-if="!payrolls.length">
            <td colspan="8" class="px-5 py-8 text-center text-gray-400">No payroll records found</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Dialog -->
    <Teleport to="body">
      <div v-if="showCreateDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="showCreateDialog = false" />
        <div class="relative bg-white rounded-xl shadow-xl p-6 w-full max-w-sm mx-4">
          <h3 class="text-lg font-semibold mb-4">Create Payroll</h3>
          <form @submit.prevent="createPayroll" class="space-y-3">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Employee *</label>
              <select v-model="form.employeeCode" required
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="">Select employee...</option>
                <option v-for="emp in employees" :key="emp.code" :value="emp.code">{{ emp.fullName }}</option>
              </select>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Period Start</label>
                <input v-model="form.payPeriodStart" type="date"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Period End</label>
                <input v-model="form.payPeriodEnd" type="date"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Net Salary *</label>
              <input v-model.number="form.netSalary" type="number" min="0"
                class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
            </div>
            <div class="grid grid-cols-3 gap-3">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Bonus</label>
                <input v-model.number="form.bonus" type="number" min="0"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Deductions</label>
                <input v-model.number="form.deductions" type="number" min="0"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Overtime</label>
                <input v-model.number="form.overtimePay" type="number" min="0"
                  class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showCreateDialog = false"
                class="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50">Cancel</button>
              <button type="submit" :disabled="creating"
                class="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {{ creating ? 'Creating...' : 'Create' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>
