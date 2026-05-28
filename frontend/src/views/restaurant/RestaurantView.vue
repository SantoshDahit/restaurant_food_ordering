<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { restaurantApi } from '@/api/restaurant'
import PageHeader from '@/components/shared/PageHeader.vue'
import { toast } from 'vue-sonner'
import type { RestaurantResponse } from '@/types'

const auth = useAuthStore()
const restaurant = ref<RestaurantResponse | null>(null)
const notFound = ref(false)
const editing = ref(false)
const loading = ref(false)

const form = ref({ name: '', address: '', businessNumber: '', phone: '', email: '', currency: 'NPR' })
const createForm = ref({ name: '', address: '', businessNumber: '', phone: '', email: '', currency: 'NPR' })
const creating = ref(false)

onMounted(loadRestaurant)

async function loadRestaurant() {
  if (!auth.restaurantCode) {
    notFound.value = true
    return
  }
  loading.value = true
  notFound.value = false
  try {
    restaurant.value = await restaurantApi.get(auth.restaurantCode)
    form.value = {
      name: restaurant.value.name,
      address: restaurant.value.address,
      businessNumber: restaurant.value.businessNumber || '',
      phone: restaurant.value.phone || '',
      email: restaurant.value.email || '',
      currency: restaurant.value.currency,
    }
  } catch (e: any) {
    if (e?.response?.status === 404) {
      notFound.value = true
    } else {
      toast.error('Failed to load restaurant info')
    }
  } finally {
    loading.value = false
  }
}

async function create() {
  const name = createForm.value.name.trim()
  const businessNumber = createForm.value.businessNumber.trim()
  const address = createForm.value.address.trim()
  if (!name || !businessNumber || !address) {
    toast.error('Name, business number, and address are required')
    return
  }
  if (!auth.user) {
    toast.error('You must be logged in')
    return
  }
  creating.value = true
  try {
    restaurant.value = await restaurantApi.create({
      name,
      address,
      businessNumber,
      phone: createForm.value.phone?.trim() || undefined,
      email: createForm.value.email?.trim() || undefined,
      currency: createForm.value.currency?.trim() || undefined,
      userCode: auth.user.code,
    })
    notFound.value = false
    // Sync both the UUID code (for FK lookups) and the kiosk code (for the share UI)
    if (restaurant.value) {
      auth.setRestaurant(restaurant.value)
    }
    toast.success('Restaurant created!')
  } catch (e: any) {
    const detail = e?.response?.data?.message
      || e?.response?.data?.error
      || e?.message
      || 'Failed to create restaurant'
    toast.error(detail)
    console.error('Restaurant create failed', e?.response?.status, e?.response?.data)
  } finally {
    creating.value = false
  }
}

async function save() {
  if (!auth.restaurantCode) return
  loading.value = true
  try {
    restaurant.value = await restaurantApi.update(auth.restaurantCode, form.value)
    editing.value = false
    toast.success('Restaurant updated')
  } catch {
    toast.error('Update failed')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <PageHeader title="Restaurant" description="Manage your restaurant information">
      <template #actions>
        <template v-if="restaurant && !editing">
          <button @click="editing = true"
            class="px-4 py-2 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white text-sm rounded-xl shadow-md shadow-violet-500/30 transition-all">
            Edit
          </button>
        </template>
        <template v-else-if="editing">
          <button @click="editing = false"
            class="px-4 py-2 text-sm bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">
            Cancel
          </button>
          <button @click="save" :disabled="loading"
            class="px-4 py-2 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white text-sm rounded-xl shadow-md shadow-violet-500/30 transition-all disabled:opacity-50">
            {{ loading ? 'Saving...' : 'Save' }}
          </button>
        </template>
      </template>
    </PageHeader>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12 text-slate-400">Loading...</div>

    <!-- Restaurant not found → Create form -->
    <div v-else-if="notFound" class="bg-white rounded-xl shadow-sm border border-violet-200 p-6 max-w-2xl">
      <div class="mb-5 p-4 bg-violet-50 rounded-lg border border-violet-100">
        <p class="text-sm font-medium text-violet-800">No restaurant found</p>
        <p class="text-xs text-violet-600 mt-1">Create your restaurant profile below to get started.</p>
      </div>

      <form @submit.prevent="create" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Restaurant Name *</label>
          <input v-model="createForm.name" required placeholder="My Restaurant"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Business Number *</label>
          <input v-model="createForm.businessNumber" required placeholder="e.g. 123456789"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Address *</label>
          <input v-model="createForm.address" required placeholder="123 Main St"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
            <input v-model="createForm.phone" placeholder="Optional"
              class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Currency</label>
            <input v-model="createForm.currency" placeholder="NPR"
              class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="createForm.email" type="email" placeholder="info@restaurant.com"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <button type="submit" :disabled="creating"
          class="w-full py-2.5 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white rounded-xl text-sm font-medium shadow-md shadow-violet-500/30 transition-all disabled:opacity-50 transition-colors">
          {{ creating ? 'Creating...' : 'Create Restaurant' }}
        </button>
      </form>
    </div>

    <!-- Restaurant found → View / Edit -->
    <div v-else-if="restaurant" class="bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60 p-6 max-w-2xl">
      <div v-if="!editing" class="space-y-4">
        <div class="flex gap-4">
          <span class="w-36 text-sm text-gray-500 flex-shrink-0">Kiosk Code</span>
          <span class="text-sm font-mono font-semibold tracking-wider px-2 py-0.5 rounded bg-teal-50 text-teal-700 border border-teal-200">{{ restaurant.kioskCode }}</span>
        </div>
        <div class="flex gap-4">
          <span class="w-36 text-sm text-gray-500 flex-shrink-0">Name</span>
          <span class="text-sm font-medium text-gray-900">{{ restaurant.name }}</span>
        </div>
        <div class="flex gap-4">
          <span class="w-36 text-sm text-gray-500 flex-shrink-0">Address</span>
          <span class="text-sm font-medium text-gray-900">{{ restaurant.address || '—' }}</span>
        </div>
        <div class="flex gap-4">
          <span class="w-36 text-sm text-gray-500 flex-shrink-0">Business Number</span>
          <span class="text-sm font-medium text-gray-900">{{ restaurant.businessNumber || '—' }}</span>
        </div>
        <div class="flex gap-4">
          <span class="w-36 text-sm text-gray-500 flex-shrink-0">Phone</span>
          <span class="text-sm font-medium text-gray-900">{{ restaurant.phone || '—' }}</span>
        </div>
        <div class="flex gap-4">
          <span class="w-36 text-sm text-gray-500 flex-shrink-0">Email</span>
          <span class="text-sm font-medium text-gray-900">{{ restaurant.email || '—' }}</span>
        </div>
        <div class="flex gap-4">
          <span class="w-36 text-sm text-gray-500 flex-shrink-0">Currency</span>
          <span class="text-sm font-medium text-gray-900">{{ restaurant.currency || '—' }}</span>
        </div>
      </div>

      <form v-else @submit.prevent="save" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
          <input v-model="form.name"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Address</label>
          <input v-model="form.address"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Business Number</label>
          <input v-model="form.businessNumber" placeholder="e.g. 123456789"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
          <input v-model="form.phone"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="form.email" type="email"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Currency</label>
          <input v-model="form.currency"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-300 transition-all" />
        </div>
      </form>
    </div>
  </div>
</template>
