<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const showKioskInput = ref(false)
const kioskCode = ref('')

function launchKiosk() {
  if (!kioskCode.value.trim()) return
  router.push(`/kiosk/${kioskCode.value.trim()}`)
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-orange-50 to-amber-50">
    <div class="container mx-auto px-4 py-16">

      <!-- Header -->
      <div class="text-center mb-16">
        <h1 class="text-5xl font-bold text-gray-900 mb-4">
          Restaurant Management System
        </h1>
        <p class="text-xl text-gray-600 max-w-2xl mx-auto">
          Complete solution for managing your restaurant with admin dashboard,
          customer ordering, and multi-device support
        </p>
      </div>

      <!-- Mode Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 max-w-6xl mx-auto">

        <!-- Admin Dashboard -->
        <button
          @click="router.push('/login')"
          class="group bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-1 text-left"
        >
          <div class="bg-orange-100 w-16 h-16 rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-8 h-8 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Admin Dashboard</h3>
          <p class="text-gray-500 text-sm">Manage menu, orders, employees, and analytics</p>
        </button>

        <!-- QR Ordering -->
        <div class="bg-white rounded-2xl p-8 shadow-lg">
          <div class="bg-blue-100 w-16 h-16 rounded-xl flex items-center justify-center mb-4">
            <svg class="w-8 h-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">QR Ordering</h3>
          <p class="text-gray-500 text-sm mb-4">Mobile-friendly ordering interface for customers</p>
          <div class="bg-blue-50 rounded-xl p-3 text-center">
            <svg class="w-8 h-8 text-blue-400 mx-auto mb-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z" />
            </svg>
            <p class="text-xs text-blue-500 font-medium">Scan the QR code at your table</p>
          </div>
        </div>

        <!-- Kiosk Mode -->
        <div class="bg-white rounded-2xl p-8 shadow-lg">
          <div class="bg-purple-100 w-16 h-16 rounded-xl flex items-center justify-center mb-4">
            <svg class="w-8 h-8 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Kiosk Mode</h3>
          <p class="text-gray-500 text-sm mb-4">Large touch screen interface for self-service</p>

          <div v-if="!showKioskInput">
            <button
              @click="showKioskInput = true"
              class="w-full py-2 bg-purple-500 text-white rounded-xl text-sm font-medium hover:bg-purple-600 transition-colors"
            >
              Launch Kiosk
            </button>
          </div>
          <div v-else class="space-y-2">
            <input
              v-model="kioskCode"
              @keyup.enter="launchKiosk"
              placeholder="Enter restaurant code"
              autofocus
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
            <div class="flex gap-2">
              <button
                @click="launchKiosk"
                :disabled="!kioskCode.trim()"
                class="flex-1 py-2 bg-purple-500 text-white rounded-lg text-sm font-medium hover:bg-purple-600 disabled:opacity-40 transition-colors"
              >
                Go
              </button>
              <button
                @click="showKioskInput = false; kioskCode = ''"
                class="px-3 py-2 bg-gray-100 text-gray-600 rounded-lg text-sm hover:bg-gray-200 transition-colors"
              >
                ✕
              </button>
            </div>
          </div>
        </div>

        <!-- Waiter Mode -->
        <button
          @click="router.push('/login')"
          class="group bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-1 text-left"
        >
          <div class="bg-green-100 w-16 h-16 rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-8 h-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-gray-900 mb-2">Waiter Mode</h3>
          <p class="text-gray-500 text-sm">Tablet interface for waiters to take orders at tables</p>
        </button>

      </div>

      <div class="mt-16 text-center">
        <p class="text-gray-400 text-sm">A modern, scalable restaurant management solution</p>
      </div>
    </div>
  </div>
</template>
