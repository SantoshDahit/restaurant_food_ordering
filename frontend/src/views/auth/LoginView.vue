<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { toast } from 'vue-sonner'
import type { UserRole } from '@/types'

const router = useRouter()
const auth = useAuthStore()

const tab = ref<'login' | 'register'>('login')

const loginForm = ref({ email: '', password: '' })
const loginLoading = ref(false)

const registerForm = ref({
  restaurantCode: '',
  fullName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: 'ADMIN' as UserRole,
})
const registerLoading = ref(false)

async function handleLogin() {
  if (!loginForm.value.email || !loginForm.value.password) {
    toast.error('Please fill in all fields')
    return
  }
  loginLoading.value = true
  try {
    await auth.login(loginForm.value)
    router.push('/admin')
  } catch {
    toast.error('Invalid email or password')
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.value.restaurantCode || !registerForm.value.fullName || !registerForm.value.email || !registerForm.value.password) {
    toast.error('Please fill in all required fields')
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    toast.error('Passwords do not match')
    return
  }
  registerLoading.value = true
  try {
    await auth.register({
      restaurantCode: registerForm.value.restaurantCode,
      fullName: registerForm.value.fullName,
      email: registerForm.value.email,
      phone: registerForm.value.phone || undefined,
      password: registerForm.value.password,
      role: registerForm.value.role,
    })
    toast.success('Account created successfully!')
    router.push('/admin')
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Registration failed')
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <div class="w-full max-w-md">
    <div class="bg-white rounded-2xl shadow-lg p-8">
      <div class="text-center mb-6">
        <div class="text-4xl mb-3">🍽️</div>
        <h1 class="text-2xl font-bold text-gray-900">RestaurantOS</h1>
      </div>

      <!-- Tabs -->
      <div class="flex rounded-lg bg-gray-100 p-1 mb-6">
        <button
          @click="tab = 'login'"
          :class="['flex-1 py-2 text-sm font-medium rounded-md transition-colors', tab === 'login' ? 'bg-white shadow text-gray-900' : 'text-gray-500 hover:text-gray-700']"
        >
          Sign In
        </button>
        <button
          @click="tab = 'register'"
          :class="['flex-1 py-2 text-sm font-medium rounded-md transition-colors', tab === 'register' ? 'bg-white shadow text-gray-900' : 'text-gray-500 hover:text-gray-700']"
        >
          Register
        </button>
      </div>

      <!-- Login Form -->
      <form v-if="tab === 'login'" @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input
            v-model="loginForm.email"
            type="email"
            placeholder="admin@example.com"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
          <input
            v-model="loginForm.password"
            type="password"
            placeholder="••••••••"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button
          type="submit"
          :disabled="loginLoading"
          class="w-full py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          {{ loginLoading ? 'Signing in...' : 'Sign In' }}
        </button>
      </form>

      <!-- Register Form -->
      <form v-else @submit.prevent="handleRegister" class="space-y-3">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Restaurant Code *</label>
          <input
            v-model="registerForm.restaurantCode"
            placeholder="e.g. REST001"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <p class="text-xs text-gray-400 mt-1">Must match an existing restaurant code in the system</p>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Full Name *</label>
          <input
            v-model="registerForm.fullName"
            placeholder="John Doe"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email *</label>
          <input
            v-model="registerForm.email"
            type="email"
            placeholder="admin@restaurant.com"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
          <input
            v-model="registerForm.phone"
            placeholder="Optional"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Role</label>
          <select
            v-model="registerForm.role"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="ADMIN">ADMIN</option>
            <option value="MANAGER">MANAGER</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Password *</label>
          <input
            v-model="registerForm.password"
            type="password"
            placeholder="••••••••"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Confirm Password *</label>
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="••••••••"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <button
          type="submit"
          :disabled="registerLoading"
          class="w-full py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50 transition-colors"
        >
          {{ registerLoading ? 'Creating account...' : 'Create Account' }}
        </button>
      </form>
    </div>
  </div>
</template>
