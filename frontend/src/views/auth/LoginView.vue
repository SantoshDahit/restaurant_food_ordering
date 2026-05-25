<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { emailVerificationApi } from '@/api/emailVerification'
import { toast } from 'vue-sonner'
import {
  UtensilsCrossed, Mail, KeyRound, ArrowRight, Check,
  User as UserIcon, Phone, Lock,
} from 'lucide-vue-next'

const router = useRouter()
const auth = useAuthStore()

const tab = ref<'login' | 'register'>('login')

const loginForm = ref({ email: '', password: '' })
const loginLoading = ref(false)

const registerStep = ref<'email' | 'details'>('email')
const registerForm = ref({
  fullName: '', email: '', phone: '', password: '', confirmPassword: '',
})
const verificationCode = ref('')
const pinInput = ref('')
const sendingPin = ref(false)
const verifyingPin = ref(false)
const registerLoading = ref(false)
const pinSentAt = ref<number | null>(null)
const now = ref(Date.now())
setInterval(() => { now.value = Date.now() }, 1000)

const resendAvailableIn = computed(() => {
  if (!pinSentAt.value) return 0
  const elapsed = Math.floor((now.value - pinSentAt.value) / 1000)
  return Math.max(0, 30 - elapsed)
})

async function handleLogin() {
  if (!loginForm.value.email || !loginForm.value.password) {
    toast.error('Please fill in all fields'); return
  }
  loginLoading.value = true
  try {
    await auth.login(loginForm.value)
    router.push(auth.homePath)
  } catch {
    toast.error('Invalid email or password')
  } finally {
    loginLoading.value = false
  }
}

async function sendPin() {
  const email = registerForm.value.email.trim()
  if (!email) { toast.error('Enter your email first'); return }
  sendingPin.value = true
  try {
    const result = await emailVerificationApi.send({ email, purpose: 'JOIN' })
    verificationCode.value = result.code
    pinSentAt.value = Date.now()
    toast.success(`Verification code sent to ${email}`)
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Failed to send code')
  } finally {
    sendingPin.value = false
  }
}

async function verifyPin() {
  if (!verificationCode.value || pinInput.value.length !== 6) {
    toast.error('Enter the 6-digit code from your email'); return
  }
  verifyingPin.value = true
  try {
    await emailVerificationApi.verify({
      code: verificationCode.value,
      email: registerForm.value.email.trim(),
      pin: pinInput.value,
      purpose: 'JOIN',
    })
    toast.success('Email verified — finish creating your account')
    registerStep.value = 'details'
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Invalid or expired code')
  } finally {
    verifyingPin.value = false
  }
}

async function handleRegister() {
  if (!registerForm.value.fullName || !registerForm.value.password) {
    toast.error('Please fill in all required fields'); return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    toast.error('Passwords do not match'); return
  }
  if (!verificationCode.value) {
    toast.error('Email verification missing — start over')
    registerStep.value = 'email'; return
  }
  registerLoading.value = true
  try {
    await auth.register({
      fullName: registerForm.value.fullName,
      email: registerForm.value.email,
      phone: registerForm.value.phone || undefined,
      password: registerForm.value.password,
      emailVerificationCode: verificationCode.value,
    })
    toast.success('Account created!')
    router.push(auth.homePath)
  } catch (e: any) {
    toast.error(e?.response?.data?.message || 'Registration failed')
  } finally {
    registerLoading.value = false
  }
}

function resetRegister() {
  registerStep.value = 'email'
  verificationCode.value = ''
  pinInput.value = ''
  pinSentAt.value = null
}

function switchTab(t: 'login' | 'register') {
  tab.value = t
  if (t === 'register') resetRegister()
}

const inputBase = 'w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-400 focus:bg-white transition-all'
</script>

<template>
  <div class="w-full max-w-md">
    <!-- Brand (hidden on lg+: shown in AuthLayout aside instead) -->
    <div class="text-center mb-6 lg:hidden">
      <div class="inline-flex w-14 h-14 rounded-2xl bg-gradient-to-br from-violet-400 to-fuchsia-500 items-center justify-center shadow-lg shadow-violet-500/30 mb-3">
        <UtensilsCrossed class="w-7 h-7 text-white" />
      </div>
      <h1 class="text-2xl font-bold text-slate-900">RestaurantOS</h1>
      <p class="text-sm text-slate-500 mt-1">Run your restaurant from one place.</p>
    </div>

    <!-- lg+ heading inside the form panel -->
    <div class="hidden lg:block mb-6">
      <h1 class="text-2xl font-bold text-slate-900">Welcome back</h1>
      <p class="text-sm text-slate-500 mt-1">Sign in or create an account to continue.</p>
    </div>

    <div class="bg-white rounded-3xl shadow-xl shadow-slate-900/5 ring-1 ring-slate-200/60 p-6 sm:p-8">
      <!-- Tabs -->
      <div class="flex rounded-xl bg-slate-100 p-1 mb-6">
        <button
          @click="switchTab('login')"
          :class="['flex-1 py-2 text-sm font-medium rounded-lg transition-all whitespace-nowrap',
            tab === 'login' ? 'bg-white shadow-sm text-slate-900' : 'text-slate-500 hover:text-slate-700']"
        >Sign in</button>
        <button
          @click="switchTab('register')"
          :class="['flex-1 py-2 text-sm font-medium rounded-lg transition-all whitespace-nowrap',
            tab === 'register' ? 'bg-white shadow-sm text-slate-900' : 'text-slate-500 hover:text-slate-700']"
        >Create account</button>
      </div>

      <!-- Login -->
      <form v-if="tab === 'login'" @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">Email</label>
          <div class="relative">
            <Mail class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input v-model="loginForm.email" type="email" placeholder="you@restaurant.com" :class="[inputBase, 'pl-9']" />
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">Password</label>
          <div class="relative">
            <Lock class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input v-model="loginForm.password" type="password" placeholder="••••••••" :class="[inputBase, 'pl-9']" />
          </div>
        </div>
        <button
          type="submit"
          :disabled="loginLoading"
          class="w-full py-2.5 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white rounded-xl text-sm font-medium shadow-md shadow-violet-500/30 disabled:opacity-60 transition-all flex items-center justify-center gap-1.5"
        >
          {{ loginLoading ? 'Signing in…' : 'Sign in' }}
          <ArrowRight v-if="!loginLoading" class="w-4 h-4" />
        </button>
      </form>

      <!-- Register: Step indicator -->
      <template v-else>
        <div class="flex items-center gap-2 mb-5">
          <div class="flex items-center gap-2 flex-1">
            <div :class="['w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold transition-colors',
              registerStep === 'email' ? 'bg-violet-500 text-white' : 'bg-emerald-500 text-white']">
              <Check v-if="registerStep === 'details'" class="w-4 h-4" />
              <span v-else>1</span>
            </div>
            <span :class="['text-xs font-medium', registerStep === 'email' ? 'text-slate-900' : 'text-slate-500']">Email</span>
          </div>
          <div :class="['h-0.5 flex-1 rounded transition-colors', registerStep === 'details' ? 'bg-emerald-500' : 'bg-slate-200']" />
          <div class="flex items-center gap-2 flex-1 justify-end">
            <span :class="['text-xs font-medium', registerStep === 'details' ? 'text-slate-900' : 'text-slate-400']">Details</span>
            <div :class="['w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold transition-colors',
              registerStep === 'details' ? 'bg-violet-500 text-white' : 'bg-slate-200 text-slate-500']">2</div>
          </div>
        </div>

        <!-- Step 1: Email -->
        <form v-if="registerStep === 'email'" @submit.prevent="verificationCode ? verifyPin() : sendPin()" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">Email address *</label>
            <div class="flex gap-2">
              <div class="relative flex-1">
                <Mail class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                <input v-model="registerForm.email" type="email" required :disabled="!!verificationCode"
                  placeholder="you@restaurant.com"
                  :class="[inputBase, 'pl-9 disabled:bg-slate-100 disabled:text-slate-500']" />
              </div>
              <button
                v-if="!verificationCode"
                type="button"
                @click="sendPin"
                :disabled="sendingPin || !registerForm.email"
                class="px-3 py-2 bg-slate-900 hover:bg-slate-800 text-white text-sm rounded-xl disabled:opacity-50 transition-colors whitespace-nowrap"
              >{{ sendingPin ? 'Sending…' : 'Send code' }}</button>
            </div>
            <p v-if="!verificationCode" class="text-xs text-slate-500 mt-1.5">We'll email you a 6-digit code.</p>
          </div>

          <template v-if="verificationCode">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">Verification code *</label>
              <div class="relative">
                <KeyRound class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                <input v-model="pinInput" type="text" inputmode="numeric" maxlength="6" pattern="\d{6}"
                  placeholder="123456" autofocus
                  class="w-full pl-9 pr-3 py-3 bg-slate-50 border border-slate-200 rounded-xl text-center text-2xl font-mono tracking-[0.5em] focus:outline-none focus:ring-2 focus:ring-violet-500/40 focus:border-violet-400 focus:bg-white transition-all" />
              </div>
              <div class="flex items-center justify-between mt-2 text-xs">
                <button type="button" @click="sendPin" :disabled="resendAvailableIn > 0 || sendingPin"
                  class="text-violet-600 hover:text-violet-700 disabled:text-slate-400 disabled:cursor-not-allowed font-medium">
                  {{ resendAvailableIn > 0 ? `Resend in ${resendAvailableIn}s` : 'Resend code' }}
                </button>
                <button type="button" @click="resetRegister" class="text-slate-500 hover:text-slate-700">Change email</button>
              </div>
            </div>

            <button type="submit" :disabled="verifyingPin || pinInput.length !== 6"
              class="w-full py-2.5 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white rounded-xl text-sm font-medium shadow-md shadow-violet-500/30 disabled:opacity-50 transition-all flex items-center justify-center gap-1.5">
              {{ verifyingPin ? 'Verifying…' : 'Verify email' }}
              <ArrowRight v-if="!verifyingPin" class="w-4 h-4" />
            </button>
          </template>
        </form>

        <!-- Step 2: Account details -->
        <form v-else @submit.prevent="handleRegister" class="space-y-4">
          <div class="bg-emerald-50 ring-1 ring-emerald-200/60 rounded-xl p-3 flex items-start gap-2.5">
            <div class="w-6 h-6 rounded-full bg-emerald-500 text-white flex items-center justify-center flex-shrink-0">
              <Check class="w-3.5 h-3.5" />
            </div>
            <div class="min-w-0 flex-1">
              <p class="text-sm font-medium text-emerald-900">Email verified</p>
              <p class="text-xs text-emerald-700 truncate">{{ registerForm.email }}</p>
            </div>
            <button type="button" @click="resetRegister"
              class="text-xs text-emerald-700 hover:text-emerald-900 font-medium flex-shrink-0">Change</button>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">Full Name *</label>
            <div class="relative">
              <UserIcon class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input v-model="registerForm.fullName" required placeholder="John Doe" :class="[inputBase, 'pl-9']" />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">Phone</label>
            <div class="relative">
              <Phone class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input v-model="registerForm.phone" placeholder="Optional" :class="[inputBase, 'pl-9']" />
            </div>
          </div>

          <div class="bg-violet-50/60 ring-1 ring-violet-100 rounded-xl px-3.5 py-2.5 text-xs text-violet-700">
            You're creating a <span class="font-semibold">Manager</span> account. Staff accounts are created from inside your dashboard once you're signed in.
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">Password *</label>
              <input v-model="registerForm.password" type="password" required placeholder="••••••••" :class="inputBase" />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">Confirm *</label>
              <input v-model="registerForm.confirmPassword" type="password" required placeholder="••••••••" :class="inputBase" />
            </div>
          </div>

          <button type="submit" :disabled="registerLoading"
            class="w-full py-2.5 bg-gradient-to-r from-violet-500 to-fuchsia-500 hover:from-violet-600 hover:to-fuchsia-600 text-white rounded-xl text-sm font-medium shadow-md shadow-violet-500/30 disabled:opacity-50 transition-all flex items-center justify-center gap-1.5">
            {{ registerLoading ? 'Creating account…' : 'Create account' }}
            <ArrowRight v-if="!registerLoading" class="w-4 h-4" />
          </button>
        </form>
      </template>
    </div>

    <p class="text-center text-xs text-slate-400 mt-6 lg:hidden">
      © {{ new Date().getFullYear() }} RestaurantOS
    </p>
  </div>
</template>
