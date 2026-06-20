<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { employeeApi } from '@/api/employee'
import PageHeader from '@/components/shared/PageHeader.vue'
import ConfirmDialog from '@/components/shared/ConfirmDialog.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import { Users } from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import type { EmployeeResponse } from '@/types'

const auth = useAuthStore()
const employees = ref<EmployeeResponse[]>([])
const loading = ref(false)
const showFormDialog = ref(false)
const deleteTarget = ref<string | null>(null)
const deleting = ref(false)
const editTarget = ref<EmployeeResponse | null>(null)

const form = ref({
  fullName: '',
  phone: '',
  joinDate: '',
  baseSalary: 0,
  bankAccount: '',
  bankName: '',
})

onMounted(loadEmployees)

async function loadEmployees() {
  if (!auth.restaurantCode) return
  loading.value = true
  try {
    const data = await employeeApi.search({ restaurantCode: auth.restaurantCode, size: 200 })
    employees.value = data.content
  } catch {
    toast.error('Failed to load employees')
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editTarget.value = null
  form.value = { fullName: '', phone: '', joinDate: new Date().toISOString().split('T')[0], baseSalary: 0, bankAccount: '', bankName: '' }
  showFormDialog.value = true
}

function openEdit(emp: EmployeeResponse) {
  editTarget.value = emp
  form.value = {
    fullName: emp.fullName,
    phone: emp.phone || '',
    joinDate: emp.joinDate,
    baseSalary: emp.baseSalary,
    bankAccount: emp.bankAccount || '',
    bankName: emp.bankName || '',
  }
  showFormDialog.value = true
}

async function save() {
  if (!form.value.fullName || !form.value.joinDate || form.value.baseSalary <= 0) {
    toast.error('Name, join date and salary are required')
    return
  }
  try {
    if (editTarget.value) {
      await employeeApi.update(editTarget.value.code, {
        fullName: form.value.fullName,
        phone: form.value.phone || undefined,
        baseSalary: form.value.baseSalary,
        bankAccount: form.value.bankAccount || undefined,
        bankName: form.value.bankName || undefined,
      })
      toast.success('Employee updated')
    } else {
      await employeeApi.create({
        restaurantCode: auth.restaurantCode,
        fullName: form.value.fullName,
        phone: form.value.phone || undefined,
        joinDate: form.value.joinDate,
        baseSalary: form.value.baseSalary,
        bankAccount: form.value.bankAccount || undefined,
        bankName: form.value.bankName || undefined,
      })
      toast.success('Employee added')
    }
    showFormDialog.value = false
    loadEmployees()
  } catch {
    toast.error('Operation failed')
  }
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await employeeApi.delete(deleteTarget.value)
    toast.success('Employee deleted')
    deleteTarget.value = null
    loadEmployees()
  } catch {
    toast.error('Delete failed')
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <RestaurantGuard resource="employees">
    <PageHeader title="Employees" description="Manage restaurant employees">
      <template #actions>
        <button @click="openAdd"
          class="px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground text-sm rounded-xl shadow-soft transition-all">
          + Add Employee
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-muted-foreground">Loading...</div>

    <div v-else class="bg-card rounded-2xl shadow-card ring-1 ring-border overflow-x-auto">
      <table class="w-full text-sm min-w-[640px]">
        <thead class="bg-muted text-muted-foreground uppercase text-[11px] tracking-wide">
          <tr>
            <th class="px-5 py-3 text-left">Name</th>
            <th class="px-5 py-3 text-left">Phone</th>
            <th class="px-5 py-3 text-left">Join Date</th>
            <th class="px-5 py-3 text-right">Salary</th>
            <th class="px-5 py-3 text-left">Bank</th>
            <th class="px-5 py-3 text-center">Active</th>
            <th class="px-5 py-3 text-right">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-border">
          <tr v-for="emp in employees" :key="emp.code" class="hover:bg-accent transition-colors">
            <td class="px-5 py-3 font-medium text-foreground">{{ emp.fullName }}</td>
            <td class="px-5 py-3 text-muted-foreground">{{ emp.phone || '—' }}</td>
            <td class="px-5 py-3 text-muted-foreground">{{ emp.joinDate }}</td>
            <td class="px-5 py-3 text-right font-medium">{{ emp.baseSalary.toFixed(0) }}</td>
            <td class="px-5 py-3 text-muted-foreground text-xs">{{ emp.bankName || '—' }}</td>
            <td class="px-5 py-3 text-center">
              <span :class="emp.isActive ? 'text-success' : 'text-destructive'">
                {{ emp.isActive ? 'Yes' : 'No' }}
              </span>
            </td>
            <td class="px-5 py-3 text-right">
              <div class="flex justify-end gap-2">
                <button @click="openEdit(emp)"
                  class="text-xs px-2.5 py-1 bg-muted ring-1 ring-border text-foreground rounded-lg hover:bg-accent transition-colors">Edit</button>
                <button @click="deleteTarget = emp.code"
                  class="text-xs px-2.5 py-1 bg-destructive/10 ring-1 ring-destructive/20 text-destructive rounded-lg hover:bg-destructive/20 transition-colors">Delete</button>
              </div>
            </td>
          </tr>
          <tr v-if="!employees.length">
            <td colspan="7" class="p-0">
              <EmptyState
                :icon="Users"
                title="No employees yet"
                description="Add team members to manage shifts, attendance, and payroll."
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Form Dialog -->
    <Teleport to="body">
      <div v-if="showFormDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="showFormDialog = false" />
        <div class="relative bg-card rounded-2xl shadow-lifted ring-1 ring-border p-6 w-full max-w-sm mx-4">
          <h3 class="text-lg font-semibold mb-4">{{ editTarget ? 'Edit Employee' : 'Add Employee' }}</h3>
          <form @submit.prevent="save" class="space-y-3">
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Full Name *</label>
              <input v-model="form.fullName" required
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Phone</label>
              <input v-model="form.phone"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Join Date *</label>
              <input v-model="form.joinDate" type="date" :disabled="!!editTarget"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all disabled:bg-muted" />
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Base Salary *</label>
              <input v-model.number="form.baseSalary" type="number" min="0"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Bank Name</label>
              <input v-model="form.bankName"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Bank Account</label>
              <input v-model="form.bankAccount"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showFormDialog = false"
                class="px-4 py-2 text-sm bg-card ring-1 ring-border text-foreground rounded-xl hover:bg-muted transition-colors">Cancel</button>
              <button type="submit"
                class="px-4 py-2 text-sm bg-primary text-primary-foreground rounded-lg hover:bg-primary/90">Save</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <ConfirmDialog
      :open="!!deleteTarget"
      title="Delete Employee"
      message="Are you sure you want to delete this employee?"
      :loading="deleting"
      @confirm="confirmDelete"
      @cancel="deleteTarget = null"
    />
  </RestaurantGuard>
</template>
