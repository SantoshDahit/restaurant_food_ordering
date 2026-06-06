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

// Fonepay merchant credentials. Secrets are never returned by the API, so the
// form always starts blank — saving sends a full set (backend does a full upsert).
const editingFonepay = ref(false)
const savingFonepay = ref(false)
const fonepayForm = ref({ merchantCode: '', username: '', password: '', secretKey: '', enabled: true })

// eSewa merchant credentials — same blank-form / full-upsert behaviour as Fonepay.
const editingEsewa = ref(false)
const savingEsewa = ref(false)
const esewaForm = ref({ productCode: '', secretKey: '', enabled: true })

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

function startEditFonepay() {
  // Always blank — we never receive the stored secrets back.
  fonepayForm.value = {
    merchantCode: '', username: '', password: '', secretKey: '',
    enabled: restaurant.value?.fonepayEnabled ?? true,
  }
  editingFonepay.value = true
}

async function saveFonepay() {
  if (!auth.restaurantCode) return
  const f = fonepayForm.value
  if (!f.merchantCode.trim() || !f.username.trim() || !f.password.trim() || !f.secretKey.trim()) {
    toast.error('Merchant code, username, password and secret key are all required')
    return
  }
  savingFonepay.value = true
  try {
    restaurant.value = await restaurantApi.updateFonepayCredentials(auth.restaurantCode, {
      merchantCode: f.merchantCode.trim(),
      username: f.username.trim(),
      password: f.password.trim(),
      secretKey: f.secretKey.trim(),
      enabled: f.enabled,
    })
    editingFonepay.value = false
    toast.success('Fonepay credentials saved')
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Failed to save Fonepay credentials')
  } finally {
    savingFonepay.value = false
  }
}

function startEditEsewa() {
  esewaForm.value = { productCode: '', secretKey: '', enabled: restaurant.value?.esewaEnabled ?? true }
  editingEsewa.value = true
}

async function saveEsewa() {
  if (!auth.restaurantCode) return
  const f = esewaForm.value
  if (!f.productCode.trim() || !f.secretKey.trim()) {
    toast.error('Product code and secret key are both required')
    return
  }
  savingEsewa.value = true
  try {
    restaurant.value = await restaurantApi.updateEsewaCredentials(auth.restaurantCode, {
      productCode: f.productCode.trim(),
      secretKey: f.secretKey.trim(),
      enabled: f.enabled,
    })
    editingEsewa.value = false
    toast.success('eSewa credentials saved')
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Failed to save eSewa credentials')
  } finally {
    savingEsewa.value = false
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

    <!-- Fonepay (Scan & Pay) credentials -->
    <div v-if="restaurant && !editing" class="mt-6 bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60 p-6 max-w-2xl">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h2 class="text-base font-semibold text-slate-900">Fonepay (Scan &amp; Pay)</h2>
          <p class="text-sm text-slate-500 mt-0.5">Accept payments via Fonepay dynamic QR — funds settle to your own merchant account.</p>
          <div class="flex items-center gap-2 mt-3">
            <span v-if="restaurant.fonepayConfigured"
              class="text-xs font-medium px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">Configured</span>
            <span v-else
              class="text-xs font-medium px-2 py-0.5 rounded-full bg-slate-50 text-slate-500 border border-slate-200">Not configured</span>
            <span v-if="restaurant.fonepayConfigured"
              :class="restaurant.fonepayEnabled
                ? 'bg-violet-50 text-violet-700 border-violet-200'
                : 'bg-amber-50 text-amber-700 border-amber-200'"
              class="text-xs font-medium px-2 py-0.5 rounded-full border">
              {{ restaurant.fonepayEnabled ? 'Enabled' : 'Disabled' }}
            </span>
          </div>
        </div>
        <button v-if="!editingFonepay" @click="startEditFonepay"
          class="px-4 py-2 bg-gradient-to-r from-rose-500 to-red-500 hover:from-rose-600 hover:to-red-600 text-white text-sm rounded-xl shadow-md shadow-rose-500/30 transition-all flex-shrink-0">
          {{ restaurant.fonepayConfigured ? 'Update' : 'Configure' }}
        </button>
      </div>

      <form v-if="editingFonepay" @submit.prevent="saveFonepay" class="space-y-4 mt-5">
        <p v-if="restaurant.fonepayConfigured" class="text-xs text-amber-600 bg-amber-50 border border-amber-100 rounded-lg p-2.5">
          For security, stored credentials are never shown. Re-enter all fields to replace them.
        </p>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Merchant Code *</label>
          <input v-model="fonepayForm.merchantCode" autocomplete="off" placeholder="e.g. 1234"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-500/40 focus:border-rose-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">API Username *</label>
          <input v-model="fonepayForm.username" autocomplete="off"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-500/40 focus:border-rose-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">API Password *</label>
          <input v-model="fonepayForm.password" type="password" autocomplete="new-password"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-500/40 focus:border-rose-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Secret Key *</label>
          <input v-model="fonepayForm.secretKey" type="password" autocomplete="new-password"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-rose-500/40 focus:border-rose-300 transition-all" />
        </div>
        <label class="flex items-center gap-2 text-sm text-slate-700">
          <input v-model="fonepayForm.enabled" type="checkbox"
            class="w-4 h-4 rounded border-slate-300 text-rose-500 focus:ring-rose-500/40" />
          Enable Fonepay for customers
        </label>
        <div class="flex gap-2 pt-1">
          <button type="button" @click="editingFonepay = false"
            class="px-4 py-2 text-sm bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">
            Cancel
          </button>
          <button type="submit" :disabled="savingFonepay"
            class="px-4 py-2 bg-gradient-to-r from-rose-500 to-red-500 hover:from-rose-600 hover:to-red-600 text-white text-sm rounded-xl shadow-md shadow-rose-500/30 transition-all disabled:opacity-50">
            {{ savingFonepay ? 'Saving…' : 'Save credentials' }}
          </button>
        </div>
      </form>
    </div>

    <!-- eSewa credentials -->
    <div v-if="restaurant && !editing" class="mt-6 bg-white rounded-2xl shadow-sm ring-1 ring-slate-200/60 p-6 max-w-2xl">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h2 class="text-base font-semibold text-slate-900">eSewa</h2>
          <p class="text-sm text-slate-500 mt-0.5">Accept eSewa payments — funds settle to your own eSewa merchant account.</p>
          <div class="flex items-center gap-2 mt-3">
            <span v-if="restaurant.esewaConfigured"
              class="text-xs font-medium px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">Configured</span>
            <span v-else
              class="text-xs font-medium px-2 py-0.5 rounded-full bg-slate-50 text-slate-500 border border-slate-200">Not configured</span>
            <span v-if="restaurant.esewaConfigured"
              :class="restaurant.esewaEnabled
                ? 'bg-violet-50 text-violet-700 border-violet-200'
                : 'bg-amber-50 text-amber-700 border-amber-200'"
              class="text-xs font-medium px-2 py-0.5 rounded-full border">
              {{ restaurant.esewaEnabled ? 'Enabled' : 'Disabled' }}
            </span>
          </div>
        </div>
        <button v-if="!editingEsewa" @click="startEditEsewa"
          class="px-4 py-2 bg-gradient-to-r from-emerald-500 to-green-500 hover:from-emerald-600 hover:to-green-600 text-white text-sm rounded-xl shadow-md shadow-emerald-500/30 transition-all flex-shrink-0">
          {{ restaurant.esewaConfigured ? 'Update' : 'Configure' }}
        </button>
      </div>

      <form v-if="editingEsewa" @submit.prevent="saveEsewa" class="space-y-4 mt-5">
        <p v-if="restaurant.esewaConfigured" class="text-xs text-amber-600 bg-amber-50 border border-amber-100 rounded-lg p-2.5">
          For security, stored credentials are never shown. Re-enter all fields to replace them.
        </p>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Product / Merchant Code *</label>
          <input v-model="esewaForm.productCode" autocomplete="off" placeholder="e.g. EPAYTEST"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/40 focus:border-emerald-300 transition-all" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Secret Key *</label>
          <input v-model="esewaForm.secretKey" type="password" autocomplete="new-password"
            class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/40 focus:border-emerald-300 transition-all" />
        </div>
        <label class="flex items-center gap-2 text-sm text-slate-700">
          <input v-model="esewaForm.enabled" type="checkbox"
            class="w-4 h-4 rounded border-slate-300 text-emerald-500 focus:ring-emerald-500/40" />
          Enable eSewa for customers
        </label>
        <div class="flex gap-2 pt-1">
          <button type="button" @click="editingEsewa = false"
            class="px-4 py-2 text-sm bg-white ring-1 ring-slate-200 text-slate-700 rounded-xl hover:bg-slate-50 transition-colors">
            Cancel
          </button>
          <button type="submit" :disabled="savingEsewa"
            class="px-4 py-2 bg-gradient-to-r from-emerald-500 to-green-500 hover:from-emerald-600 hover:to-green-600 text-white text-sm rounded-xl shadow-md shadow-emerald-500/30 transition-all disabled:opacity-50">
            {{ savingEsewa ? 'Saving…' : 'Save credentials' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
