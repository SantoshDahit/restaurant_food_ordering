<script setup lang="ts">
import { UtensilsCrossed, Check } from 'lucide-vue-next'

const features = [
  'Real-time orders, tables & kitchen',
  'QR & kiosk self-ordering built in',
  'Payments, payroll, attendance — one app',
]
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-accent to-background relative overflow-hidden">
    <!-- Decorative blobs -->
    <div aria-hidden="true" class="pointer-events-none absolute -top-24 -left-24 w-96 h-96 bg-primary/10 rounded-full blur-3xl" />
    <div aria-hidden="true" class="pointer-events-none absolute -bottom-32 -right-24 w-[28rem] h-[28rem] bg-primary/10 rounded-full blur-3xl" />

    <div class="relative min-h-screen grid lg:grid-cols-2">
      <!-- Hero panel (lg+) -->
      <aside class="hidden lg:flex flex-col justify-between p-12 xl:p-16">
        <div class="inline-flex w-14 h-14 rounded-2xl bg-primary items-center justify-center shadow-soft">
          <UtensilsCrossed class="w-7 h-7 text-primary-foreground" />
        </div>

        <div class="max-w-lg">
          <h2 class="text-4xl xl:text-5xl font-bold text-foreground leading-tight tracking-tight font-serif">
            Run your restaurant from
            <span class="text-primary">one place</span>.
          </h2>
          <p class="text-lg text-muted-foreground mt-5">
            Menus, orders, payments, payroll — a single dashboard built for modern restaurants.
          </p>
          <ul class="mt-8 space-y-3">
            <li v-for="f in features" :key="f" class="flex items-center gap-3 text-foreground">
              <span class="w-6 h-6 rounded-full bg-accent text-primary flex items-center justify-center flex-shrink-0">
                <Check class="w-3.5 h-3.5 stroke-[3]" />
              </span>
              <span class="text-sm">{{ f }}</span>
            </li>
          </ul>
        </div>

        <p class="text-xs text-muted-foreground">© {{ new Date().getFullYear() }} RestaurantOS</p>
      </aside>

      <!-- Form panel -->
      <main class="flex items-center justify-center p-4 sm:p-8 lg:p-12">
        <router-view />
      </main>
    </div>
  </div>
</template>
