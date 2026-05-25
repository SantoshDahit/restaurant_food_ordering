<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { userApi } from '@/api/user'
import { adminApi } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'
import { toast } from 'vue-sonner'
import PageHeader from '@/components/shared/PageHeader.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import { Users, Search, Circle, UserCircle } from 'lucide-vue-next'
import type { UserResponse, UserRole } from '@/types'

const auth = useAuthStore()
const users = ref<UserResponse[]>([])
const loading = ref(true)
const query = ref('')
const roleFilter = ref<UserRole | ''>('')
const updating = ref<string | null>(null)

const roles: UserRole[] = ['ADMIN', 'MANAGER', 'STAFF']

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = await userApi.search({ role: roleFilter.value || undefined, size: 200 })
    users.value = data.content
  } catch {
    toast.error('Failed to load users')
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) return users.value
  return users.value.filter(u =>
    u.fullName.toLowerCase().includes(q) ||
    u.email.toLowerCase().includes(q) ||
    (u.phone ?? '').toLowerCase().includes(q)
  )
})

async function changeRole(u: UserResponse, newRole: UserRole) {
  if (u.role === newRole) return
  updating.value = u.code
  try {
    const updated = await adminApi.changeUserRole(u.code, newRole)
    Object.assign(u, updated)
    toast.success(`Role changed to ${newRole}`)
  } catch {
    toast.error('Failed to change role')
  } finally {
    updating.value = null
  }
}

async function toggleActive(u: UserResponse) {
  updating.value = u.code
  try {
    const updated = await adminApi.setUserActive(u.code, !u.isActive)
    Object.assign(u, updated)
    toast.success(updated.isActive ? 'User reactivated' : 'User suspended')
  } catch {
    toast.error('Failed to update status')
  } finally {
    updating.value = null
  }
}

function initials(name: string): string {
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map(s => s[0]?.toUpperCase() ?? '').join('') || '·'
}

const roleChip: Record<UserRole, string> = {
  ADMIN:   'bg-amber-50 text-amber-700 ring-amber-200/60',
  MANAGER: 'bg-violet-50 text-violet-700 ring-violet-200/60',
  STAFF:   'bg-blue-50 text-blue-700 ring-blue-200/60',
}
</script>

<template>
  <PageHeader title="Users" :description="`${users.length} accounts on the platform`" />

  <!-- Filters -->
  <div class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm p-3 sm:p-4 mb-4 flex flex-col sm:flex-row gap-2">
    <div class="relative flex-1">
      <Search class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
      <input
        v-model="query"
        placeholder="Search by name, email, phone…"
        class="w-full pl-9 pr-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-amber-500/40 focus:border-amber-400 focus:bg-white transition-all"
      />
    </div>
    <select v-model="roleFilter" @change="load"
      class="px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-amber-500/40 focus:border-amber-400">
      <option value="">All roles</option>
      <option v-for="r in roles" :key="r" :value="r">{{ r }}</option>
    </select>
  </div>

  <div v-if="loading" class="text-center py-16 text-slate-400">Loading users…</div>

  <div v-else-if="!filtered.length" class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm">
    <EmptyState :icon="Users" title="No users found" description="Try a different search or filter." />
  </div>

  <div v-else class="bg-white rounded-2xl ring-1 ring-slate-200/60 shadow-sm overflow-x-auto">
    <table class="w-full text-sm min-w-[720px]">
      <thead class="bg-slate-50/60 text-slate-500 uppercase text-[11px] tracking-wide">
        <tr>
          <th class="px-5 py-3 text-left">User</th>
          <th class="px-5 py-3 text-left">Email</th>
          <th class="px-5 py-3 text-left">Role</th>
          <th class="px-5 py-3 text-left">Status</th>
          <th class="px-5 py-3 text-right">Actions</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-slate-100">
        <tr v-for="u in filtered" :key="u.code" class="hover:bg-slate-50/60">
          <td class="px-5 py-3">
            <div class="flex items-center gap-2.5">
              <div class="w-8 h-8 rounded-full bg-gradient-to-br from-violet-400 to-fuchsia-500 text-white text-xs font-semibold flex items-center justify-center flex-shrink-0">
                {{ initials(u.fullName) }}
              </div>
              <div class="min-w-0">
                <div class="font-medium text-slate-900 truncate">{{ u.fullName }}</div>
                <div class="text-xs text-slate-500 truncate">{{ u.phone || '—' }}</div>
              </div>
            </div>
          </td>
          <td class="px-5 py-3 text-slate-600 truncate max-w-xs">{{ u.email }}</td>
          <td class="px-5 py-3">
            <select
              :value="u.role"
              :disabled="updating === u.code || u.code === auth.user?.code"
              @change="changeRole(u, ($event.target as HTMLSelectElement).value as UserRole)"
              :class="[
                'text-xs font-medium px-2 py-1 rounded-md ring-1 cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed',
                roleChip[u.role]
              ]"
            >
              <option v-for="r in roles" :key="r" :value="r">{{ r }}</option>
            </select>
          </td>
          <td class="px-5 py-3">
            <span :class="[
              'inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-md ring-1',
              u.isActive
                ? 'bg-emerald-50 text-emerald-700 ring-emerald-200/60'
                : 'bg-slate-100 text-slate-500 ring-slate-200/60'
            ]">
              <Circle :class="['w-2 h-2', u.isActive ? 'fill-emerald-500 text-emerald-500' : 'fill-slate-400 text-slate-400']" />
              {{ u.isActive ? 'Active' : 'Inactive' }}
            </span>
          </td>
          <td class="px-5 py-3 text-right">
            <button
              @click="toggleActive(u)"
              :disabled="updating === u.code || u.code === auth.user?.code"
              :class="[
                'text-xs font-medium px-3 py-1.5 rounded-lg ring-1 transition-colors disabled:opacity-50 disabled:cursor-not-allowed',
                u.isActive
                  ? 'bg-rose-50 text-rose-600 ring-rose-200/60 hover:bg-rose-100'
                  : 'bg-emerald-50 text-emerald-600 ring-emerald-200/60 hover:bg-emerald-100'
              ]"
            >
              {{ u.isActive ? 'Suspend' : 'Reactivate' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
