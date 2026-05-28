<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import {
  UtensilsCrossed, QrCode, Monitor, Users, Smartphone, BadgeDollarSign,
  ArrowRight, Check, Star, Sparkles, Menu, X, Mountain,
} from 'lucide-vue-next'
import { useScrollReveal, useCountUp } from '@/composables/useScrollReveal'

const router = useRouter()

// ── Mobile nav ──────────────────────────────────────────────────────────────
const mobileNavOpen = ref(false)

// ── Hero typewriter ─────────────────────────────────────────────────────────
const typedText = ref('')
const fullText = 'Run your restaurant from one place.'
onMounted(() => {
  let i = 0
  const timer = setInterval(() => {
    if (i <= fullText.length) {
      typedText.value = fullText.slice(0, i)
      i++
    } else {
      clearInterval(timer)
    }
  }, 45)
  onBeforeUnmount(() => clearInterval(timer))
})

// ── Scroll reveal targets ───────────────────────────────────────────────────
const revealEls = ref<HTMLElement[]>([])
useScrollReveal(revealEls)

// ── Stat counters ───────────────────────────────────────────────────────────
const statRestaurants = ref<HTMLElement | null>(null)
const statOrders = ref<HTMLElement | null>(null)
const statUptime = ref<HTMLElement | null>(null)
const statRating = ref<HTMLElement | null>(null)
useCountUp(statRestaurants, 120)
useCountUp(statOrders, 48000)
useCountUp(statUptime, 99)
useCountUp(statRating, 49)

// ── Features ────────────────────────────────────────────────────────────────
const features = [
  { icon: QrCode, title: 'QR Ordering', text: 'Diners scan the table QR, browse the menu, and order from their phone — no app install required.', tint: 'from-violet-500 to-fuchsia-500' },
  { icon: Monitor, title: 'Kiosk Mode', text: 'Full-screen self-service kiosks for thakali, momo joints, and quick-serve counters.', tint: 'from-sky-500 to-blue-500' },
  { icon: Users, title: 'Waiter Mode', text: 'Tablet-friendly order entry for floor staff with live table status, in any restaurant size.', tint: 'from-emerald-500 to-teal-500' },
  { icon: Smartphone, title: 'Tablet Ordering', text: 'Per-table tablets that double as menus and order pads — no running back to the counter.', tint: 'from-amber-500 to-orange-500' },
  { icon: BadgeDollarSign, title: 'Payroll & Attendance', text: 'Clock-in, schedules, and monthly payroll for your team — built into the same dashboard.', tint: 'from-rose-500 to-pink-500' },
]

// ── Pricing ─────────────────────────────────────────────────────────────────
const plans = [
  { name: 'Starter', price: 'NPR 2,999', period: '/mo', highlight: false,
    perks: ['1 restaurant', 'QR ordering', 'Kiosk mode', 'Up to 5 staff accounts', 'Email support'] },
  { name: 'Growth', price: 'NPR 5,999', period: '/mo', highlight: true,
    perks: ['Everything in Starter', 'Tablet & Waiter modes', 'Payroll & attendance', 'Unlimited staff', 'Priority support'] },
  { name: 'Enterprise', price: 'Custom', period: '', highlight: false,
    perks: ['Multi-location', 'Dedicated success manager', 'SLA & custom integrations', 'On-prem option', '24/7 phone support'] },
]

function scrollTo(id: string) {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  mobileNavOpen.value = false
}

function goSignup() {
  router.push({ path: '/login', query: { tab: 'register' } })
  mobileNavOpen.value = false
}
</script>

<template>
  <div class="min-h-screen bg-white text-slate-900 antialiased">

    <!-- ── Nav ──────────────────────────────────────────────────────────── -->
    <header class="fixed top-0 inset-x-0 z-50 backdrop-blur-xl bg-white/80 border-b border-slate-200/60">
      <nav class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <a href="#top" class="flex items-center gap-2">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center shadow-md shadow-violet-500/30">
            <UtensilsCrossed class="w-5 h-5 text-white" />
          </div>
          <span class="font-bold text-lg tracking-tight">RestaurantOS</span>
        </a>

        <div class="hidden md:flex items-center gap-8 text-sm font-medium text-slate-600">
          <button @click="scrollTo('features')" class="hover:text-slate-900 transition-colors">Features</button>
          <button @click="scrollTo('pricing')" class="hover:text-slate-900 transition-colors">Pricing</button>
          <router-link to="/launch" class="hover:text-slate-900 transition-colors">Launch</router-link>
        </div>

        <div class="hidden md:flex items-center gap-2">
          <button @click="router.push('/login')"
            class="px-4 py-2 text-sm font-medium text-slate-700 hover:text-slate-900 transition-colors">
            Login
          </button>
          <button @click="goSignup"
            class="px-5 py-2 text-sm font-semibold text-white bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 rounded-xl shadow-md shadow-violet-500/30 transition-all">
            Get started
          </button>
        </div>

        <button @click="mobileNavOpen = !mobileNavOpen" class="md:hidden p-2 -mr-2 text-slate-700">
          <component :is="mobileNavOpen ? X : Menu" class="w-6 h-6" />
        </button>
      </nav>

      <!-- Mobile menu -->
      <div v-if="mobileNavOpen" class="md:hidden border-t border-slate-200/60 bg-white">
        <div class="px-4 py-4 space-y-1">
          <button @click="scrollTo('features')" class="block w-full text-left py-2 text-slate-700 hover:text-slate-900">Features</button>
          <button @click="scrollTo('pricing')" class="block w-full text-left py-2 text-slate-700 hover:text-slate-900">Pricing</button>
          <router-link to="/launch" class="block w-full text-left py-2 text-slate-700 hover:text-slate-900">Launch a mode</router-link>
          <div class="pt-3 mt-3 border-t border-slate-100 flex gap-2">
            <button @click="router.push('/login')"
              class="flex-1 py-2.5 text-sm font-medium text-slate-700 ring-1 ring-slate-200 rounded-xl hover:bg-slate-50 transition-colors">Login</button>
            <button @click="goSignup"
              class="flex-1 py-2.5 text-sm font-semibold text-white bg-gradient-to-r from-violet-500 to-fuchsia-500 rounded-xl">Get started</button>
          </div>
        </div>
      </div>
    </header>

    <!-- ── Hero ─────────────────────────────────────────────────────────── -->
    <section id="top" class="relative pt-32 pb-28 sm:pt-40 sm:pb-36 overflow-hidden">
      <!-- background gradient & blobs -->
      <div aria-hidden="true" class="absolute inset-0 bg-gradient-to-br from-violet-50 via-white to-fuchsia-50/60"></div>
      <div aria-hidden="true" class="pointer-events-none absolute -top-32 -left-32 w-[28rem] h-[28rem] bg-violet-300/30 rounded-full blur-3xl animate-blob-slow"></div>
      <div aria-hidden="true" class="pointer-events-none absolute top-20 -right-32 w-[24rem] h-[24rem] bg-fuchsia-300/30 rounded-full blur-3xl animate-blob-slower"></div>

      <!-- floating Nepali food emojis -->
      <div aria-hidden="true" class="pointer-events-none absolute inset-0 hidden md:block">
        <span class="absolute text-4xl opacity-45 animate-float-1" style="top:18%; left:8%;">🥟</span>
        <span class="absolute text-4xl opacity-45 animate-float-2" style="top:30%; right:10%;">🍛</span>
        <span class="absolute text-4xl opacity-45 animate-float-3" style="bottom:22%; left:14%;">🍵</span>
        <span class="absolute text-4xl opacity-45 animate-float-1" style="bottom:32%; right:18%;">🌶️</span>
        <span class="absolute text-3xl opacity-40 animate-float-2" style="top:55%; left:42%;">🫓</span>
      </div>

      <div class="relative max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-violet-100/80 ring-1 ring-violet-200 text-violet-700 text-xs font-semibold mb-6">
          🙏 Namaste — built for Nepali restaurants
        </div>

        <h1 class="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-bold tracking-tight leading-[1.05]">
          <span class="bg-gradient-to-r from-violet-600 via-fuchsia-600 to-pink-600 bg-clip-text text-transparent">{{ typedText }}</span><span class="inline-block w-[3px] h-[0.9em] align-middle bg-violet-500 animate-caret"></span>
        </h1>

        <p class="mt-6 text-lg sm:text-xl text-slate-600 max-w-2xl mx-auto animate-fade-in-up" style="animation-delay:1.8s;">
          Menus, orders, kiosks, payments, and payroll — a single dashboard built for restaurants from Kathmandu to Pokhara.
        </p>

        <div class="mt-9 flex flex-col sm:flex-row items-center justify-center gap-3 animate-fade-in-up" style="animation-delay:2.1s;">
          <button @click="goSignup"
            class="group inline-flex items-center justify-center gap-2 px-7 py-3.5 text-base font-semibold text-white bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 rounded-2xl shadow-lg shadow-violet-500/30 transition-all hover:scale-[1.02]">
            Get started
            <ArrowRight class="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
          </button>
          <router-link to="/launch"
            class="inline-flex items-center justify-center gap-2 px-6 py-3.5 text-base font-semibold text-slate-700 bg-white hover:bg-slate-50 ring-1 ring-slate-200 rounded-2xl shadow-sm transition-all">
            Launch a mode
          </router-link>
        </div>

        <p class="mt-5 text-xs text-slate-500 animate-fade-in-up" style="animation-delay:2.4s;">
          Already onboarded? <button @click="router.push('/login')" class="text-violet-600 hover:text-violet-700 font-medium">Sign in →</button>
        </p>
      </div>

      <!-- Himalayan silhouette at hero base -->
      <svg aria-hidden="true" class="absolute -bottom-px inset-x-0 w-full text-slate-100" viewBox="0 0 1200 120" preserveAspectRatio="none">
        <path d="M0,80 L120,55 L220,90 L340,30 L460,75 L560,50 L680,95 L820,40 L940,70 L1080,45 L1200,80 L1200,120 L0,120 Z" fill="currentColor" />
        <path d="M0,95 L160,72 L300,100 L440,60 L600,90 L760,68 L920,95 L1080,70 L1200,92 L1200,120 L0,120 Z" fill="white" opacity="0.6" />
      </svg>
    </section>

    <!-- ── Stats ────────────────────────────────────────────────────────── -->
    <section class="py-14 border-y border-slate-200/60 bg-slate-50/60">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
        <div>
          <p class="text-3xl sm:text-4xl font-bold text-slate-900 tabular-nums"><span ref="statRestaurants">0</span>+</p>
          <p class="text-sm text-slate-500 mt-1">Restaurants onboarded</p>
        </div>
        <div>
          <p class="text-3xl sm:text-4xl font-bold text-slate-900 tabular-nums"><span ref="statOrders">0</span>+</p>
          <p class="text-sm text-slate-500 mt-1">Orders processed monthly</p>
        </div>
        <div>
          <p class="text-3xl sm:text-4xl font-bold text-slate-900 tabular-nums"><span ref="statUptime">0</span>.9%</p>
          <p class="text-sm text-slate-500 mt-1">Uptime guarantee</p>
        </div>
        <div>
          <p class="text-3xl sm:text-4xl font-bold text-slate-900 tabular-nums">
            <span ref="statRating">0</span><span class="text-slate-400 text-2xl">/50</span>
          </p>
          <p class="text-sm text-slate-500 mt-1">Average customer rating</p>
        </div>
      </div>
    </section>

    <!-- ── Features ─────────────────────────────────────────────────────── -->
    <section id="features" class="py-20 sm:py-28">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center max-w-2xl mx-auto mb-14 reveal" :ref="(el) => { if (el) revealEls.push(el as HTMLElement) }">
          <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-violet-50 ring-1 ring-violet-200 text-violet-700 text-xs font-semibold mb-4">
            <Sparkles class="w-3.5 h-3.5" /> Five modes, one dashboard
          </div>
          <h2 class="text-3xl sm:text-4xl font-bold tracking-tight">Everything your restaurant needs.</h2>
          <p class="mt-4 text-lg text-slate-600">From a busy momo stall to a multi-floor thakali bhansa — RestaurantOS scales with you.</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          <div v-for="(f, i) in features" :key="f.title"
            class="reveal group bg-white rounded-2xl p-6 ring-1 ring-slate-200/70 hover:ring-slate-300 hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
            :style="{ transitionDelay: `${i * 60}ms` }"
            :ref="(el) => { if (el) revealEls.push(el as HTMLElement) }">
            <div :class="['w-12 h-12 rounded-xl flex items-center justify-center text-white shadow-md mb-4 bg-gradient-to-br', f.tint, 'group-hover:scale-110 transition-transform']">
              <component :is="f.icon" class="w-6 h-6" />
            </div>
            <h3 class="text-lg font-semibold text-slate-900">{{ f.title }}</h3>
            <p class="mt-2 text-sm text-slate-600 leading-relaxed">{{ f.text }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ── Pricing ──────────────────────────────────────────────────────── -->
    <section id="pricing" class="relative py-20 sm:py-28 bg-gradient-to-b from-white to-violet-50/40 overflow-hidden">
      <Mountain aria-hidden="true" class="absolute top-10 right-10 w-32 h-32 text-violet-100" />

      <div class="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center max-w-2xl mx-auto mb-14 reveal" :ref="(el) => { if (el) revealEls.push(el as HTMLElement) }">
          <h2 class="text-3xl sm:text-4xl font-bold tracking-tight">Simple, transparent pricing.</h2>
          <p class="mt-4 text-lg text-slate-600">All prices in NPR. Pick the plan that fits today — upgrade or downgrade anytime.</p>
        </div>

        <div class="relative">
          <!-- Plans (visually blurred — pricing not yet finalized) -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-5 blur-md pointer-events-none select-none" aria-hidden="true">
            <div v-for="p in plans" :key="p.name"
              class="relative bg-white rounded-3xl p-7 ring-1"
              :class="p.highlight ? 'ring-violet-300 shadow-xl shadow-violet-500/10' : 'ring-slate-200'">
              <div v-if="p.highlight"
                class="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-1 rounded-full bg-gradient-to-r from-violet-500 to-fuchsia-500 text-white text-xs font-semibold shadow-md">
                Most popular
              </div>
              <h3 class="text-lg font-semibold text-slate-900">{{ p.name }}</h3>
              <p class="mt-3 flex items-end gap-1">
                <span class="text-4xl font-bold tracking-tight text-slate-900">{{ p.price }}</span>
                <span class="text-slate-500 text-sm pb-1">{{ p.period }}</span>
              </p>
              <ul class="mt-5 space-y-2.5">
                <li v-for="perk in p.perks" :key="perk" class="flex items-start gap-2.5 text-sm text-slate-700">
                  <span class="w-5 h-5 rounded-full bg-violet-100 text-violet-600 flex items-center justify-center flex-shrink-0 mt-0.5">
                    <Check class="w-3 h-3 stroke-[3]" />
                  </span>
                  {{ perk }}
                </li>
              </ul>
              <button :class="['mt-6 w-full py-2.5 rounded-xl text-sm font-semibold',
                p.highlight
                  ? 'bg-gradient-to-r from-violet-500 to-fuchsia-500 text-white shadow-md shadow-violet-500/30'
                  : 'bg-slate-100 text-slate-900']">
                Get started
              </button>
            </div>
          </div>

          <!-- Overlay -->
          <div class="absolute inset-0 flex items-center justify-center">
            <div class="text-center bg-white/75 backdrop-blur-sm ring-1 ring-slate-200 rounded-2xl px-8 py-6 shadow-xl">
              <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-violet-100 ring-1 ring-violet-200 text-violet-700 text-xs font-semibold mb-3">
                <Sparkles class="w-3.5 h-3.5" /> Coming soon
              </div>
              <h3 class="text-xl font-bold text-slate-900">Pricing in finalization</h3>
              <p class="mt-1.5 text-sm text-slate-600 max-w-sm">We're finalizing plans for the Nepali market. Create an account to start using RestaurantOS today.</p>
              <button @click="goSignup"
                class="mt-4 inline-flex items-center gap-2 px-5 py-2.5 text-sm font-semibold text-white bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 rounded-xl shadow-md shadow-violet-500/30 transition-all">
                Get started
                <ArrowRight class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ── Bottom CTA ───────────────────────────────────────────────────── -->
    <section class="relative py-20 sm:py-24 overflow-hidden">
      <div aria-hidden="true" class="absolute inset-0 bg-gradient-to-br from-violet-600 via-fuchsia-600 to-pink-600 animate-gradient-shift bg-[length:200%_200%]"></div>
      <!-- decorative marigold dots -->
      <div aria-hidden="true" class="absolute inset-0 opacity-20"
           style="background-image: radial-gradient(circle at 20% 30%, rgba(255,255,255,0.4) 2px, transparent 2px), radial-gradient(circle at 80% 70%, rgba(255,255,255,0.3) 2px, transparent 2px); background-size: 80px 80px;"></div>

      <div class="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-white">
        <h2 class="text-3xl sm:text-4xl font-bold tracking-tight">Ready to modernize your restaurant?</h2>
        <p class="mt-4 text-lg text-white/90">Join the restaurants already running their day-to-day on RestaurantOS.</p>
        <button @click="goSignup"
          class="mt-8 inline-flex items-center gap-2 px-8 py-3.5 text-base font-semibold text-violet-700 bg-white hover:bg-violet-50 rounded-2xl shadow-xl transition-all hover:scale-[1.03] animate-pulse-soft">
          Get started
          <ArrowRight class="w-4 h-4" />
        </button>
        <div class="mt-6 flex items-center justify-center gap-1.5 text-sm text-white/90">
          <Star class="w-4 h-4 fill-amber-300 text-amber-300" />
          <Star class="w-4 h-4 fill-amber-300 text-amber-300" />
          <Star class="w-4 h-4 fill-amber-300 text-amber-300" />
          <Star class="w-4 h-4 fill-amber-300 text-amber-300" />
          <Star class="w-4 h-4 fill-amber-300 text-amber-300" />
          <span class="ml-2">Loved by restaurants across Nepal</span>
        </div>
      </div>
    </section>

    <!-- ── Footer ───────────────────────────────────────────────────────── -->
    <footer class="border-t border-slate-200/60 bg-white">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center">
            <UtensilsCrossed class="w-4 h-4 text-white" />
          </div>
          <span class="font-semibold text-slate-900">RestaurantOS</span>
        </div>
        <p class="text-sm text-slate-500">© {{ new Date().getFullYear() }} RestaurantOS · Made in Nepal 🇳🇵</p>
        <div class="flex items-center gap-4 text-sm text-slate-600">
          <router-link to="/login" class="hover:text-slate-900 transition-colors">Login</router-link>
          <router-link to="/launch" class="hover:text-slate-900 transition-colors">Launch mode</router-link>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* ── Scroll reveal ── */
.reveal {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 700ms cubic-bezier(0.22, 1, 0.36, 1), transform 700ms cubic-bezier(0.22, 1, 0.36, 1);
}
.reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* ── Hero caret ── */
@keyframes caret {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
.animate-caret { animation: caret 1s steps(1) infinite; }

/* ── Fade in up ── */
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
.animate-fade-in-up {
  opacity: 0;
  animation: fade-in-up 700ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

/* ── Floating blobs ── */
@keyframes blob {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33%      { transform: translate(20px, -25px) scale(1.05); }
  66%      { transform: translate(-15px, 15px) scale(0.95); }
}
.animate-blob-slow   { animation: blob 14s ease-in-out infinite; }
.animate-blob-slower { animation: blob 18s ease-in-out infinite reverse; }

/* ── Floating emojis ── */
@keyframes float-1 { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-18px)} }
@keyframes float-2 { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-22px)} }
@keyframes float-3 { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-14px)} }
.animate-float-1 { animation: float-1 6s ease-in-out infinite; }
.animate-float-2 { animation: float-2 7.5s ease-in-out infinite; }
.animate-float-3 { animation: float-3 5.5s ease-in-out infinite; }

/* ── Soft pulse for CTA ── */
@keyframes pulse-soft {
  0%, 100% { box-shadow: 0 10px 25px -5px rgba(139, 92, 246, 0.25); }
  50%      { box-shadow: 0 20px 35px -5px rgba(139, 92, 246, 0.45); }
}
.animate-pulse-soft { animation: pulse-soft 3s ease-in-out infinite; }

/* ── Animated gradient for bottom CTA ── */
@keyframes gradient-shift {
  0%, 100% { background-position: 0% 50%; }
  50%      { background-position: 100% 50%; }
}
.animate-gradient-shift { animation: gradient-shift 8s ease infinite; }

/* ── Respect reduced motion ── */
@media (prefers-reduced-motion: reduce) {
  .reveal, .animate-fade-in-up, .animate-caret, .animate-blob-slow, .animate-blob-slower,
  .animate-float-1, .animate-float-2, .animate-float-3, .animate-pulse-soft, .animate-gradient-shift {
    animation: none !important;
    transition: none !important;
    opacity: 1 !important;
    transform: none !important;
  }
}
</style>
