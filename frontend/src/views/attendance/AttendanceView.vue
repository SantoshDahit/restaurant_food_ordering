<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { attendanceApi } from '@/api/attendance'
import { employeeApi } from '@/api/employee'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import { toast } from 'vue-sonner'
import type { AttendanceResponse, EmployeeResponse, AttendanceStatus } from '@/types'

const auth = useAuthStore()
const employees = ref<EmployeeResponse[]>([])
const attendance = ref<AttendanceResponse[]>([])
const selectedEmployee = ref('')
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const loading = ref(false)

const attendanceStatuses: AttendanceStatus[] = ['PRESENT', 'ABSENT', 'HALF_DAY', 'LEAVE', 'HOLIDAY']

const years = computed(() => {
  const y = new Date().getFullYear()
  return [y - 1, y, y + 1]
})

const months = [
  { v: 1, l: 'January' }, { v: 2, l: 'February' }, { v: 3, l: 'March' },
  { v: 4, l: 'April' }, { v: 5, l: 'May' }, { v: 6, l: 'June' },
  { v: 7, l: 'July' }, { v: 8, l: 'August' }, { v: 9, l: 'September' },
  { v: 10, l: 'October' }, { v: 11, l: 'November' }, { v: 12, l: 'December' },
]

const daysInMonth = computed(() => {
  return new Date(selectedYear.value, selectedMonth.value, 0).getDate()
})

const dayNumbers = computed(() => {
  return Array.from({ length: daysInMonth.value }, (_, i) => i + 1)
})

function getDateStr(day: number) {
  return `${selectedYear.value}-${String(selectedMonth.value).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}

function getAttendanceForDay(day: number) {
  const date = getDateStr(day)
  return attendance.value.find(a => a.attendanceDate === date)
}

onMounted(async () => {
  if (!auth.restaurantCode) return
  try {
    const data = await employeeApi.search({ restaurantCode: auth.restaurantCode, size: 200 })
    employees.value = data.content
    if (employees.value.length > 0) {
      selectedEmployee.value = employees.value[0].code
    }
  } catch {
    toast.error('Failed to load employees')
  }
})

watch([selectedEmployee, selectedYear, selectedMonth], loadAttendance)

async function loadAttendance() {
  if (!selectedEmployee.value) return
  loading.value = true
  try {
    const data = await attendanceApi.search({
      restaurantCode: auth.restaurantCode,
      employeeCode: selectedEmployee.value,
    })
    attendance.value = data.content
  } catch {
    toast.error('Failed to load attendance')
  } finally {
    loading.value = false
  }
}

async function setStatus(day: number, status: AttendanceStatus) {
  if (!selectedEmployee.value) return
  const dateStr = getDateStr(day)
  const existing = getAttendanceForDay(day)
  try {
    if (existing) {
      await attendanceApi.update(existing.code, { status })
    } else {
      await attendanceApi.create({
        employeeCode: selectedEmployee.value,
        restaurantCode: auth.restaurantCode,
        attendanceDate: dateStr,
        status,
      })
    }
    await loadAttendance()
  } catch {
    toast.error('Failed to update attendance')
  }
}
</script>

<template>
  <RestaurantGuard resource="attendance">
    <PageHeader title="Attendance" description="Track employee attendance by month" />

    <!-- Controls -->
    <div class="flex flex-wrap gap-3 mb-6">
      <select v-model="selectedEmployee"
        class="px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
        <option value="">Select Employee</option>
        <option v-for="emp in employees" :key="emp.code" :value="emp.code">{{ emp.fullName }}</option>
      </select>
      <select v-model.number="selectedYear"
        class="px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
        <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
      </select>
      <select v-model.number="selectedMonth"
        class="px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
        <option v-for="m in months" :key="m.v" :value="m.v">{{ m.l }}</option>
      </select>
    </div>

    <div v-if="!selectedEmployee" class="text-center py-12 text-slate-400">
      Select an employee to view attendance
    </div>

    <div v-else-if="loading" class="text-center py-12 text-slate-400">Loading...</div>

    <div v-else class="bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60 overflow-x-auto">
      <table class="w-full text-sm min-w-[900px]">
        <thead class="bg-slate-50/60 text-slate-500 uppercase text-[11px] tracking-wide">
          <tr>
            <th class="px-4 py-3 text-left">Day</th>
            <th class="px-4 py-3 text-left">Date</th>
            <th class="px-4 py-3 text-left">Status</th>
            <th class="px-4 py-3 text-left">Check In</th>
            <th class="px-4 py-3 text-left">Check Out</th>
            <th class="px-4 py-3 text-right">Hours</th>
            <th class="px-4 py-3 text-center">Set Status</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="day in dayNumbers" :key="day" class="hover:bg-slate-50/60 transition-colors">
            <td class="px-4 py-2 font-medium text-slate-700">{{ day }}</td>
            <td class="px-4 py-2 text-slate-500">{{ getDateStr(day) }}</td>
            <td class="px-4 py-2">
              <StatusBadge v-if="getAttendanceForDay(day)" :status="getAttendanceForDay(day)!.status" />
              <span v-else class="text-xs text-gray-400">—</span>
            </td>
            <td class="px-4 py-2 text-gray-500 text-xs">{{ getAttendanceForDay(day)?.checkInTime || '—' }}</td>
            <td class="px-4 py-2 text-gray-500 text-xs">{{ getAttendanceForDay(day)?.checkOutTime || '—' }}</td>
            <td class="px-4 py-2 text-right text-slate-500">{{ getAttendanceForDay(day)?.workedHours ?? '—' }}</td>
            <td class="px-4 py-2 text-center">
              <select
                :value="getAttendanceForDay(day)?.status || ''"
                @change="setStatus(day, ($event.target as HTMLSelectElement).value as AttendanceStatus)"
                class="text-xs px-2 py-1 bg-white border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300">
                <option value="">— Set —</option>
                <option v-for="s in attendanceStatuses" :key="s" :value="s">{{ s }}</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </RestaurantGuard>
</template>
