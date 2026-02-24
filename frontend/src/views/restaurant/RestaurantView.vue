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

const form = ref({ name: '', address: '', phone: '', email: '', currency: 'NPR' })
const createForm = ref({ name: '', address: '', businessNumber: '', phone: '', email: '', currency: 'NPR' })
const creating = ref(false)

onMounted(loadRestaurant)

async function loadRestaurant() {
  if (!auth.restaurantCode) return
  loading.value = true
  notFound.value = false
  try {
    restaurant.value = await restaurantApi.get(auth.restaurantCode)
    form.value = {
      name: restaurant.value.name,
      address: restaurant.value.address,
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
  if (!createForm.value.name || !createForm.value.businessNumber) {
    toast.error('Name and business number are required')
    return
  }
  creating.value = true
  try {
    restaurant.value = await restaurantApi.create(createForm.value)
    notFound.value = false
    // Update local restaurantCode so all subsequent API calls use the correct code
    if (auth.user && restaurant.value) {
      auth.user = { ...auth.user, restaurantCode: restaurant.value.code }
      localStorage.setItem('user', JSON.stringify(auth.user))
    }
    toast.success('Restaurant created!')
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Failed to create restaurant')
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
            class="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700">
            Edit
          </button>
        </template>
        <template v-else-if="editing">
          <button @click="editing = false"
            class="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50">
            Cancel
          </button>
          <button @click="save" :disabled="loading"
            class="px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 disabled:opacity-50">
            {{ loading ? 'Saving...' : 'Save' }}
          </button>
        </template>
      </template>
    </PageHeader>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12 text-gray-400">Loading...</div>

    <!-- Restaurant not found → Create form -->
    <div v-else-if="notFound" class="bg-white rounded-xl shadow-sm border border-orange-200 p-6 max-w-2xl">
      <div class="mb-5 p-4 bg-orange-50 rounded-lg border border-orange-100">
        <p class="text-sm font-medium text-orange-800">No restaurant found for code <span class="font-mono font-bold">{{ auth.restaurantCode }}</span></p>
        <p class="text-xs text-orange-600 mt-1">Create your restaurant profile below to get started.</p>
      </div>

      <form @submit.prevent="create" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Restaurant Name *</label>
          <input v-model="createForm.name" required placeholder="My Restaurant"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Business Number *</label>
          <input v-model="createForm.businessNumber" required placeholder="e.g. 123456789"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Address</label>
          <input v-model="createForm.address" placeholder="123 Main St"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
            <input v-model="createForm.phone" placeholder="Optional"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Currency</label>
            <input v-model="createForm.currency" placeholder="NPR"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="createForm.email" type="email" placeholder="info@restaurant.com"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <button type="submit" :disabled="creating"
          class="w-full py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50 transition-colors">
          {{ creating ? 'Creating...' : 'Create Restaurant' }}
        </button>
      </form>
    </div>

    <!-- Restaurant found → View / Edit -->
    <div v-else-if="restaurant" class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 max-w-2xl">
      <div v-if="!editing" class="space-y-4">
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
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Address</label>
          <input v-model="form.address"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
          <input v-model="form.phone"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input v-model="form.email" type="email"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Currency</label>
          <input v-model="form.currency"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        </div>
      </form>
    </div>
  </div>
</template>
