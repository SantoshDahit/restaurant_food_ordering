import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { restaurantApi } from '@/api/restaurant'
import type { UserResponse, LoginRequest, RegisterRequest, RestaurantResponse } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserResponse | null>(
    JSON.parse(localStorage.getItem('user') || 'null')
  )
  const token = ref<string | null>(localStorage.getItem('access_token'))
  const restaurantCode = ref<string>(localStorage.getItem('restaurant_code') || '')
  const kioskCode = ref<string>(localStorage.getItem('kiosk_code') || '')

  const isLoggedIn = computed(() => !!token.value && !!user.value)

  function _persist(accessToken: string, userData: UserResponse) {
    token.value = accessToken
    user.value = userData
    localStorage.setItem('access_token', accessToken)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  function _setRestaurant(restaurant: RestaurantResponse) {
    restaurantCode.value = restaurant.code
    kioskCode.value = restaurant.kioskCode || ''
    localStorage.setItem('restaurant_code', restaurant.code)
    localStorage.setItem('kiosk_code', restaurant.kioskCode || '')
  }

  // After login, look up the restaurant by owner to get the actual restaurant code.
  // ADMINs are platform-wide and don't own a restaurant, so skip the lookup.
  async function _syncRestaurantCode(userData: UserResponse) {
    if (userData.role !== 'MANAGER') return
    try {
      const restaurant = await restaurantApi.getByOwner(userData.code)
      if (restaurant) {
        _setRestaurant(restaurant)
      }
    } catch {
      // Restaurant not created yet — that's fine, RestaurantView will handle it
    }
  }

  const homePath = computed(() => {
    if (user.value?.role === 'ADMIN') return '/admin'
    return '/dashboard'
  })

  function setRestaurantCode(code: string) {
    restaurantCode.value = code
    localStorage.setItem('restaurant_code', code)
  }

  function setRestaurant(restaurant: RestaurantResponse) {
    _setRestaurant(restaurant)
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
    kioskCode.value = ''
    localStorage.removeItem('access_token')
    localStorage.removeItem('user')
    localStorage.removeItem('restaurant_code')
    localStorage.removeItem('kiosk_code')
  }

  return { user, token, isLoggedIn, restaurantCode, kioskCode, homePath, setRestaurantCode, setRestaurant, login, register, logout }
})
