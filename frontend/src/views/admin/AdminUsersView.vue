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
  ADMIN:   'bg-warning/10 text-warning ring-warning/20',
  MANAGER: 'bg-primary/10 text-primary ring-primary/30',
  STAFF:   'bg-info/10 text-info ring-info/20',
}
</script>

<template>
  <PageHeader title="Users" :description="`${users.length} accounts on the platform`" />

  <!-- Filters -->
  <div class="bg-card rounded-2xl ring-1 ring-border shadow-soft p-3 sm:p-4 mb-4 flex flex-col sm:flex-row gap-2">
    <div class="relative flex-1">
      <Search class="w-4 h-4 text-muted-foreground absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
      <input
        v-model="query"
        placeholder="Search by name, email, phone…"
        class="w-full pl-9 pr-3 py-2 bg-muted border border-border rounded-xl text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 focus:bg-card transition-all"
      />
    </div>
    <select v-model="roleFilter" @change="load"
      class="px-3 py-2 bg-muted border border-border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30">
      <option value="">All roles</option>
      <option v-for="r in roles" :key="r" :value="r">{{ r }}</option>
    </select>
  </div>

  <div v-if="loading" class="text-center py-16 text-muted-foreground">Loading users…</div>

  <div v-else-if="!filtered.length" class="bg-card rounded-2xl ring-1 ring-border shadow-soft">
    <EmptyState :icon="Users" title="No users found" description="Try a different search or filter." />
  </div>

  <div v-else class="bg-card rounded-2xl ring-1 ring-border shadow-soft overflow-x-auto">
    <table class="w-full text-sm min-w-[720px]">
      <thead class="bg-muted text-muted-foreground uppercase text-[11px] tracking-wide">
        <tr>
          <th class="px-5 py-3 text-left">User</th>
          <th class="px-5 py-3 text-left">Email</th>
          <th class="px-5 py-3 text-left">Role</th>
          <th class="px-5 py-3 text-left">Status</th>
          <th class="px-5 py-3 text-right">Actions</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-border">
        <tr v-for="u in filtered" :key="u.code" class="hover:bg-accent">
          <td class="px-5 py-3">
            <div class="flex items-center gap-2.5">
              <div class="w-8 h-8 rounded-full bg-primary text-primary-foreground text-xs font-semibold flex items-center justify-center flex-shrink-0">
                {{ initials(u.fullName) }}
              </div>
              <div class="min-w-0">
                <div class="font-medium text-foreground truncate">{{ u.fullName }}</div>
                <div class="text-xs text-muted-foreground truncate">{{ u.phone || '—' }}</div>
              </div>
            </div>
          </td>
          <td class="px-5 py-3 text-muted-foreground truncate max-w-xs">{{ u.email }}</td>
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
                ? 'bg-success/10 text-success ring-success/20'
                : 'bg-muted text-muted-foreground ring-border'
            ]">
              <Circle :class="['w-2 h-2', u.isActive ? 'fill-success text-success' : 'fill-muted-foreground text-muted-foreground']" />
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
                  ? 'bg-destructive/10 text-destructive ring-destructive/20 hover:bg-destructive/20'
                  : 'bg-success/10 text-success ring-success/20 hover:bg-success/20'
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
