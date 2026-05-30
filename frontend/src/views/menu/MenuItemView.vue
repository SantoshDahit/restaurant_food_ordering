<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { menuItemApi } from '@/api/menuItem'
import { menuCategoryApi } from '@/api/menuCategory'
import { fileApi } from '@/api/file'
import PageHeader from '@/components/shared/PageHeader.vue'
import StatusBadge from '@/components/shared/StatusBadge.vue'
import ConfirmDialog from '@/components/shared/ConfirmDialog.vue'
import RestaurantGuard from '@/components/shared/RestaurantGuard.vue'
import EmptyState from '@/components/shared/EmptyState.vue'
import { Pizza } from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import type { MenuItemResponse, MenuCategoryResponse, ItemAvailability } from '@/types'

const auth = useAuthStore()
const items = ref<MenuItemResponse[]>([])
const categories = ref<MenuCategoryResponse[]>([])
const loading = ref(false)
const showFormDialog = ref(false)
const deleteTarget = ref<string | null>(null)
const deleting = ref(false)
const editTarget = ref<MenuItemResponse | null>(null)

// Image upload state
const imageFile = ref<File | null>(null)
const imagePreview = ref<string | null>(null)
const uploadingImage = ref(false)
const currentFileCode = ref<string | null>(null)
const fileUrlCache = ref<Record<string, string>>({})

const availabilities: ItemAvailability[] = ['AVAILABLE', 'OUT_OF_STOCK', 'HIDDEN']

const form = ref({
  name: '',
  description: '',
  price: 0,
  discountPercent: 0,
  categoryCode: '',
  isVeg: false,
  isFeatured: false,
  prepTimeMinutes: 0,
  sortOrder: 0,
  availability: 'AVAILABLE' as ItemAvailability,
})

async function loadItems() {
  if (!auth.restaurantCode) return
  loading.value = true
  try {
    const data = await menuItemApi.search({ restaurantCode: auth.restaurantCode })
    items.value = data.content
    // Preload image URLs
    const codes = data.content.filter(i => i.fileCode).map(i => i.fileCode!)
    await Promise.all(codes.map(preloadImage))
  } catch {
    toast.error('Failed to load menu items')
  } finally {
    loading.value = false
  }
}

async function preloadImage(fileCode: string) {
  if (fileUrlCache.value[fileCode]) return
  try {
    const f = await fileApi.get(fileCode)
    fileUrlCache.value[fileCode] = f.url
  } catch { /* silent */ }
}

async function loadCategories() {
  if (!auth.restaurantCode) return
  try {
    const data = await menuCategoryApi.search({ restaurantCode: auth.restaurantCode })
    categories.value = data.content
  } catch { /* silent */ }
}

function openAdd() {
  editTarget.value = null
  form.value = { name: '', description: '', price: 0, discountPercent: 0, categoryCode: '', isVeg: false, isFeatured: false, prepTimeMinutes: 0, sortOrder: 0, availability: 'AVAILABLE' }
  imageFile.value = null
  imagePreview.value = null
  currentFileCode.value = null
  showFormDialog.value = true
}

function openEdit(item: MenuItemResponse) {
  editTarget.value = item
  form.value = {
    name: item.name,
    description: item.description || '',
    price: item.price,
    discountPercent: item.discountPercent,
    categoryCode: item.categoryCode || '',
    isVeg: item.isVeg,
    isFeatured: item.isFeatured,
    prepTimeMinutes: item.prepTimeMinutes,
    sortOrder: item.sortOrder,
    availability: item.availability,
  }
  imageFile.value = null
  imagePreview.value = null
  currentFileCode.value = item.fileCode || null
  showFormDialog.value = true
}

function onImageSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  imageFile.value = file
  imagePreview.value = URL.createObjectURL(file)
}

function clearImage() {
  imageFile.value = null
  imagePreview.value = null
  currentFileCode.value = null
}

async function save() {
  if (!form.value.name || form.value.price <= 0) {
    toast.error('Name and price are required')
    return
  }

  let fileCode = currentFileCode.value || undefined

  if (imageFile.value) {
    uploadingImage.value = true
    try {
      const uploaded = await fileApi.upload(imageFile.value)
      fileCode = uploaded.code
      fileUrlCache.value[uploaded.code] = uploaded.url
    } catch {
      toast.error('Image upload failed')
      uploadingImage.value = false
      return
    } finally {
      uploadingImage.value = false
    }
  }

  try {
    if (editTarget.value) {
      await menuItemApi.update(editTarget.value.code, {
        name: form.value.name,
        description: form.value.description || undefined,
        price: form.value.price,
        discountPercent: form.value.discountPercent,
        categoryCode: form.value.categoryCode || undefined,
        isVeg: form.value.isVeg,
        isFeatured: form.value.isFeatured,
        prepTimeMinutes: form.value.prepTimeMinutes,
        sortOrder: form.value.sortOrder,
        availability: form.value.availability,
        fileCode,
      })
      toast.success('Item updated')
    } else {
      await menuItemApi.create({
        restaurantCode: auth.restaurantCode,
        name: form.value.name,
        description: form.value.description || undefined,
        price: form.value.price,
        discountPercent: form.value.discountPercent,
        categoryCode: form.value.categoryCode || undefined,
        isVeg: form.value.isVeg,
        prepTimeMinutes: form.value.prepTimeMinutes,
        sortOrder: form.value.sortOrder,
        availability: form.value.availability,
        fileCode,
      })
      toast.success('Item created')
    }
    showFormDialog.value = false
    loadItems()
  } catch {
    toast.error('Operation failed')
  }
}

async function confirmDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await menuItemApi.delete(deleteTarget.value)
    toast.success('Item deleted')
    deleteTarget.value = null
    loadItems()
  } catch {
    toast.error('Delete failed')
  } finally {
    deleting.value = false
  }
}

function getCategoryName(code: string) {
  return categories.value.find(c => c.code === code)?.name || '—'
}

onMounted(async () => {
  await Promise.all([loadItems(), loadCategories()])
})
</script>

<template>
  <RestaurantGuard resource="menu items">
    <PageHeader title="Menu Items" description="Manage menu items">
      <template #actions>
        <button @click="openAdd"
          class="px-4 py-2 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white text-sm rounded-xl shadow-md shadow-violet-500/30 transition-all">
          + Add Item
        </button>
      </template>
    </PageHeader>

    <div v-if="loading" class="text-center py-12 text-slate-400">Loading...</div>

    <div v-else class="bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60 overflow-x-auto">
      <table class="w-full text-sm min-w-[860px]">
        <thead class="bg-slate-50/60 text-slate-500 uppercase text-[11px] tracking-wide">
          <tr>
            <th class="px-4 py-3 text-left w-16">Image</th>
            <th class="px-4 py-3 text-left">Name</th>
            <th class="px-4 py-3 text-left">Category</th>
            <th class="px-4 py-3 text-right">Price</th>
            <th class="px-4 py-3 text-center">Veg</th>
            <th class="px-4 py-3 text-left">Availability</th>
            <th class="px-4 py-3 text-right sticky right-0 bg-slate-50/60 shadow-[-4px_0_8px_-4px_rgba(0,0,0,0.08)]">Actions</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <tr v-for="item in items" :key="item.code" class="hover:bg-slate-50/60 transition-colors">
            <td class="px-4 py-2">
              <img
                v-if="item.fileCode && fileUrlCache[item.fileCode]"
                :src="fileUrlCache[item.fileCode]"
                :alt="item.name"
                class="w-12 h-12 object-cover rounded-lg border border-gray-100"
              />
              <div v-else class="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center text-gray-300">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
            </td>
            <td class="px-4 py-3 font-medium text-gray-900">{{ item.name }}</td>
            <td class="px-4 py-3 text-slate-500">{{ getCategoryName(item.categoryCode || '') }}</td>
            <td class="px-4 py-3 text-right">NPR {{ item.price.toFixed(0) }}</td>
            <td class="px-4 py-3 text-center">{{ item.isVeg ? '🌱' : '🍖' }}</td>
            <td class="px-4 py-3"><StatusBadge :status="item.availability" /></td>
            <td class="px-4 py-3 text-right sticky right-0 bg-white shadow-[-4px_0_8px_-4px_rgba(0,0,0,0.08)]">
              <div class="flex justify-end gap-2">
                <button @click="openEdit(item)"
                  class="text-xs px-2.5 py-1 bg-slate-50 ring-1 ring-slate-200 text-slate-700 rounded-lg hover:bg-slate-100 transition-colors">Edit</button>
                <button @click="deleteTarget = item.code"
                  class="text-xs px-2.5 py-1 bg-rose-50 ring-1 ring-rose-200 text-rose-600 rounded-lg hover:bg-rose-100 transition-colors">Delete</button>
              </div>
            </td>
          </tr>
          <tr v-if="!items.length">
            <td colspan="7" class="p-0">
              <EmptyState
                :icon="Pizza"
                title="No menu items yet"
                description="Add your first dish, drink, or special."
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
        <div class="relative bg-white rounded-2xl shadow-xl ring-1 ring-slate-200/60 p-6 w-full max-w-md mx-4 max-h-[90vh] overflow-y-auto">
          <h3 class="text-lg font-semibold mb-4">{{ editTarget ? 'Edit Item' : 'Add Item' }}</h3>
          <form @submit.prevent="save" class="space-y-3">

            <!-- Image Upload -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Image</label>
              <div class="flex items-center gap-3">
                <div class="w-20 h-20 rounded-xl border-2 border-dashed border-gray-200 overflow-hidden flex-shrink-0 flex items-center justify-center bg-gray-50">
                  <img v-if="imagePreview" :src="imagePreview" class="w-full h-full object-cover" />
                  <img v-else-if="currentFileCode && fileUrlCache[currentFileCode]"
                    :src="fileUrlCache[currentFileCode]" class="w-full h-full object-cover" />
                  <svg v-else class="w-8 h-8 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                      d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <div class="flex-1 space-y-1.5">
                  <label class="flex items-center justify-center gap-2 px-3 py-2 bg-gray-100 rounded-lg cursor-pointer hover:bg-gray-200 text-sm text-gray-700 w-full">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                    </svg>
                    Choose Image
                    <input type="file" accept="image/*" class="hidden" @change="onImageSelected" />
                  </label>
                  <button v-if="imagePreview || currentFileCode" type="button" @click="clearImage"
                    class="w-full px-3 py-1.5 text-xs text-red-600 bg-red-50 rounded-lg hover:bg-red-100">
                    Remove Image
                  </button>
                </div>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Name *</label>
              <input v-model="form.name" required
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea v-model="form.description" rows="2"
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Price *</label>
                <input v-model.number="form.price" type="number" min="0" step="0.01"
                  class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Discount %</label>
                <input v-model.number="form.discountPercent" type="number" min="0" max="100"
                  class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Category</label>
              <select v-model="form.categoryCode"
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
                <option value="">— None —</option>
                <option v-for="c in categories" :key="c.code" :value="c.code">{{ c.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">Availability</label>
              <select v-model="form.availability"
                class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all">
                <option v-for="a in availabilities" :key="a" :value="a">{{ a }}</option>
              </select>
            </div>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Prep Time (min)</label>
                <input v-model.number="form.prepTimeMinutes" type="number" min="0"
                  class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Sort Order</label>
                <input v-model.number="form.sortOrder" type="number" min="0"
                  class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
              </div>
            </div>
            <div class="flex gap-4">
              <label class="flex items-center gap-2 text-sm cursor-pointer">
                <input v-model="form.isVeg" type="checkbox" class="rounded" />
                Vegetarian
              </label>
              <label class="flex items-center gap-2 text-sm cursor-pointer">
                <input v-model="form.isFeatured" type="checkbox" class="rounded" />
                Featured
              </label>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" @click="showFormDialog = false"
                class="px-4 py-2 text-sm bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">Cancel</button>
              <button type="submit" :disabled="uploadingImage"
                class="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-60">
                {{ uploadingImage ? 'Uploading...' : 'Save' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <ConfirmDialog
      :open="!!deleteTarget"
      title="Delete Item"
      message="Are you sure you want to delete this menu item?"
      :loading="deleting"
      @confirm="confirmDelete"
      @cancel="deleteTarget = null"
    />
  </RestaurantGuard>
</template>
