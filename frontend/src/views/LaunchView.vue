<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showKioskInput = ref(false)
const kioskCode = ref('')
const showTableInput = ref(false)
const tableCode = ref('')

function launchKiosk() {
  if (!kioskCode.value.trim()) return
  router.push(`/kiosk/${kioskCode.value.trim()}`)
}

function launchTable() {
  if (!tableCode.value.trim()) return
  router.push(`/table/${tableCode.value.trim()}`)
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-accent to-background">
    <div class="container mx-auto px-4 py-10 sm:py-16">
      <div class="text-center mb-10 sm:mb-16">
        <h1 class="text-3xl sm:text-4xl md:text-5xl font-bold text-foreground mb-4 leading-tight font-serif">
          Launch a mode
        </h1>
        <p class="text-base sm:text-xl text-muted-foreground max-w-2xl mx-auto">
          Direct entry points for QR ordering, kiosks, waiter mode, and the admin dashboard.
        </p>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 max-w-6xl mx-auto">
        <button @click="router.push('/login')"
          class="group bg-card rounded-2xl p-6 sm:p-8 shadow-card hover:shadow-lifted transition-all duration-300 hover:-translate-y-1 text-left">
          <div class="bg-accent w-16 h-16 rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-8 h-8 text-primary" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-foreground mb-2">Dashboard</h3>
          <p class="text-muted-foreground text-sm">Manage menu, orders, employees, and analytics.</p>
        </button>

        <div class="bg-card rounded-2xl p-6 sm:p-8 shadow-card">
          <div class="bg-info/10 w-16 h-16 rounded-xl flex items-center justify-center mb-4">
            <svg class="w-8 h-8 text-info" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-foreground mb-2">Table Ordering</h3>
          <p class="text-muted-foreground text-sm mb-4">Open a table's ordering page directly by its table code.</p>
          <div v-if="!showTableInput">
            <button @click="showTableInput = true"
              class="w-full py-2 bg-info text-info-foreground rounded-xl text-sm font-medium hover:bg-info/90 transition-colors">
              Open Table
            </button>
          </div>
          <div v-else class="space-y-2">
            <input v-model="tableCode" @keyup.enter="launchTable" placeholder="Enter table code" autofocus
              class="w-full px-3 py-2 border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-info/40" />
            <div class="flex gap-2">
              <button @click="launchTable" :disabled="!tableCode.trim()"
                class="flex-1 py-2 bg-info text-info-foreground rounded-lg text-sm font-medium hover:bg-info/90 disabled:opacity-40 transition-colors">Go</button>
              <button @click="showTableInput = false; tableCode = ''"
                class="px-3 py-2 bg-muted text-muted-foreground rounded-lg text-sm hover:bg-accent transition-colors">✕</button>
            </div>
          </div>
        </div>

        <div class="bg-card rounded-2xl p-6 sm:p-8 shadow-card">
          <div class="bg-success/10 w-16 h-16 rounded-xl flex items-center justify-center mb-4">
            <svg class="w-8 h-8 text-success" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-foreground mb-2">Kiosk Mode</h3>
          <p class="text-muted-foreground text-sm mb-4">Large touch-screen interface for self-service.</p>
          <div v-if="!showKioskInput">
            <button @click="showKioskInput = true"
              class="w-full py-2 bg-success text-success-foreground rounded-xl text-sm font-medium hover:bg-success/90 transition-colors">
              Launch Kiosk
            </button>
          </div>
          <div v-else class="space-y-2">
            <input v-model="kioskCode" @keyup.enter="launchKiosk" placeholder="Enter kiosk code" autofocus
              class="w-full px-3 py-2 border border-border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-success/40" />
            <div class="flex gap-2">
              <button @click="launchKiosk" :disabled="!kioskCode.trim()"
                class="flex-1 py-2 bg-success text-success-foreground rounded-lg text-sm font-medium hover:bg-success/90 disabled:opacity-40 transition-colors">Go</button>
              <button @click="showKioskInput = false; kioskCode = ''"
                class="px-3 py-2 bg-muted text-muted-foreground rounded-lg text-sm hover:bg-accent transition-colors">✕</button>
            </div>
          </div>
        </div>

        <button @click="router.push('/login')"
          class="group bg-card rounded-2xl p-6 sm:p-8 shadow-card hover:shadow-lifted transition-all duration-300 hover:-translate-y-1 text-left">
          <div class="bg-success/10 w-16 h-16 rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <svg class="w-8 h-8 text-success" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <h3 class="text-xl font-semibold text-foreground mb-2">Waiter Mode</h3>
          <p class="text-muted-foreground text-sm">Tablet interface for waiters to take orders at tables.</p>
        </button>
      </div>

      <div class="mt-12 text-center">
        <router-link to="/" class="text-primary hover:text-primary/90 text-sm font-medium">← Back to home</router-link>
      </div>
    </div>
  </div>
</template>
