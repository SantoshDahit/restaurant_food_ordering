import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { restaurantApi } from '@/api/restaurant'
import type { UserResponse, LoginRequest, RegisterRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserResponse | null>(
    JSON.parse(localStorage.getItem('user') || 'null')
  )
  const token = ref<string | null>(localStorage.getItem('access_token'))

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const restaurantCode = computed(() => user.value?.restaurantCode ?? '')

  function _persist(accessToken: string, userData: UserResponse) {
    token.value = accessToken
    user.value = userData
    localStorage.setItem('access_token', accessToken)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  // After login, look up the restaurant by owner to get the actual restaurant code
  async function _syncRestaurantCode(userData: UserResponse) {
    try {
      const restaurant = await restaurantApi.getByOwner(userData.code)
      if (restaurant && user.value) {
        user.value = { ...user.value, restaurantCode: restaurant.code }
        localStorage.setItem('user', JSON.stringify(user.value))
      }
    } catch {
      // Restaurant not created yet — that's fine, RestaurantView will handle it
    }
  }

  async function login(credentials: LoginRequest) {
    const response = await authApi.login(credentials)
    _persist(response.accessToken, response.user)
    await _syncRestaurantCode(response.user)
  }

  async function register(data: RegisterRequest) {
    const response = await authApi.register(data)
    _persist(response.accessToken, response.user)
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('access_token')
    localStorage.removeItem('user')
  }

  return { user, token, isLoggedIn, restaurantCode, login, register, logout }
})
