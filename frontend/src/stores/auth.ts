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
  const restaurantCode = ref<string>(localStorage.getItem('restaurant_code') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)

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
      if (restaurant) {
        restaurantCode.value = restaurant.code
        localStorage.setItem('restaurant_code', restaurant.code)
      }
    } catch {
      // Restaurant not created yet — that's fine, RestaurantView will handle it
    }
  }

  function setRestaurantCode(code: string) {
    restaurantCode.value = code
    localStorage.setItem('restaurant_code', code)
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
    restaurantCode.value = ''
    localStorage.removeItem('access_token')
    localStorage.removeItem('user')
    localStorage.removeItem('restaurant_code')
  }

  return { user, token, isLoggedIn, restaurantCode, setRestaurantCode, login, register, logout }
})
