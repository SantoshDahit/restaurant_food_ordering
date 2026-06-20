<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { menuCategoryApi } from '@/api/menuCategory'
import PageHeader from '@/components/shared/PageHeader.vue'
import ConfirmDialog from '@/components/shared/ConfirmDialog.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import { ListTree } from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import type { MenuCategoryResponse, MenuCategoryType } from '@/types'

const auth = useAuthStore()
const categories = ref<MenuCategoryResponse[]>([])
const loading = ref(false)
const showFormDialog = ref(false)
const deleteTarget = ref<string | null>(null)
const deleting = ref(false)
const editTarget = ref<MenuCategoryResponse | null>(null)

const categoryTypes: MenuCategoryType[] = ['VEG', 'NON_VEG', 'DRINKS', 'SPECIALS', 'DESSERTS', 'APPETIZERS', 'SIDES']

const form = ref({
  name: '',
  categoryType: '' as MenuCategoryType | '',
  sortOrder: 0,
})

onMounted(loadCategories)

async function loadCategories() {
  if (!auth.restaurantCode) return
  loading.value = true
  try {
    const data = await menuCategoryApi.search({ restaurantCode: auth.restaurantCode, size: 200 })
    categories.value = data.content
  } catch {
    toast.error('Failed to load categories')
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editTarget.value = null
  form.value = { name: '', categoryType: '', sortOrder: 0 }
  showFormDialog.value = true
}

function openEdit(cat: MenuCategoryResponse) {
  editTarget.value = cat
  form.value = { name: cat.name, categoryType: cat.categoryType || '', sortOrder: cat.sortOrder }
  showFormDialog.value = true
}

async function save() {
  if (!form.value.name) { toast.error('Name is required'); return }
  try {
    if (editTarget.value) {
      await menuCategoryApi.update(editTarget.value.code, {
        name: form.value.name,
        categoryType: form.value.categoryType || undefined,
        sortOrder: form.value.sortOrder,
      })
      toast.success('Category updated')
    } else {
      await menuCategoryApi.create({
        restaurantCode: auth.restaurantCode,
        name: form.value.name,
        categoryType: form.value.categoryType || undefined,
        sortOrder: form.value.sortOrder,
      })
      toast.success('Category created')
    }
    showFormDialog.value = false
    loadCategories()
  } catch {
    toast.error('Operation failed')
  }
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await menuCategoryApi.delete(deleteTarget.value)
    toast.success('Category deleted')
    deleteTarget.value = null
    loadCategories()
  } catch {
    toast.error('Delete failed')
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <RestaurantGuard resource="menu categories">
    <PageHeader title="Menu Categories" description="Manage menu categories">
      <template #actions>
        <button @click="openAdd"
          class="px-4 py-2 bg-primary hover:bg-primary/90 text-primary-foreground text-sm rounded-xl shadow-soft transition-all">
          + Add Category
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-muted-foreground">Loading...</div>

    <div v-else class="bg-card rounded-2xl shadow-card ring-1 ring-border overflow-x-auto">
      <table class="w-full text-sm min-w-[600px]">
        <thead class="bg-muted text-muted-foreground uppercase text-[11px] tracking-wide">
          <tr>
            <th class="px-5 py-3 text-left">Name</th>
            <th class="px-5 py-3 text-left">Type</th>
            <th class="px-5 py-3 text-center">Sort Order</th>
            <th class="px-5 py-3 text-center">Active</th>
            <th class="px-5 py-3 text-right sticky right-0 bg-muted shadow-[-4px_0_8px_-4px_rgba(0,0,0,0.08)]">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-border">
          <tr v-for="cat in categories" :key="cat.code" class="hover:bg-accent transition-colors">
            <td class="px-5 py-3 font-medium text-foreground">{{ cat.name }}</td>
            <td class="px-5 py-3 text-muted-foreground">{{ cat.categoryType || '—' }}</td>
            <td class="px-5 py-3 text-center text-muted-foreground">{{ cat.sortOrder }}</td>
            <td class="px-5 py-3 text-center">
              <span :class="cat.isActive ? 'text-success' : 'text-destructive'">
                {{ cat.isActive ? 'Yes' : 'No' }}
              </span>
            </td>
            <td class="px-5 py-3 text-right sticky right-0 bg-card shadow-[-4px_0_8px_-4px_rgba(0,0,0,0.08)]">
              <div class="flex justify-end gap-2">
                <button @click="openEdit(cat)"
                  class="text-xs px-2.5 py-1 bg-muted ring-1 ring-border text-foreground rounded-lg hover:bg-accent transition-colors">Edit</button>
                <button @click="deleteTarget = cat.code"
                  class="text-xs px-2.5 py-1 bg-destructive/10 ring-1 ring-destructive/20 text-destructive rounded-lg hover:bg-destructive/20 transition-colors">Delete</button>
              </div>
            </td>
          </tr>
          <tr v-if="!categories.length">
            <td colspan="5" class="p-0">
              <EmptyState
                :icon="ListTree"
                title="No categories yet"
                description="Create your first category to group menu items."
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
          <h3 class="text-lg font-semibold mb-4">{{ editTarget ? 'Edit Category' : 'Add Category' }}</h3>
          <form @submit.prevent="save" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Name *</label>
              <input v-model="form.name" required
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Type</label>
              <select v-model="form.categoryType"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all">
                <option value="">— None —</option>
                <option v-for="t in categoryTypes" :key="t" :value="t">{{ t }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-foreground mb-1">Sort Order</label>
              <input v-model.number="form.sortOrder" type="number" min="0"
                class="w-full px-3 py-2 bg-card border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-ring/20 focus:border-primary/30 transition-all" />
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showFormDialog = false"
                class="px-4 py-2 text-sm bg-card ring-1 ring-border text-foreground rounded-xl hover:bg-accent transition-colors">Cancel</button>
              <button type="submit"
                class="px-4 py-2 text-sm bg-primary text-primary-foreground rounded-lg hover:bg-primary/90">Save</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <ConfirmDialog
      :open="!!deleteTarget"
      title="Delete Category"
      message="Are you sure you want to delete this category?"
      :loading="deleting"
      @confirm="confirmDelete"
      @cancel="deleteTarget = null"
    />
  </RestaurantGuard>
</template>
