import api from './axios'
import type { RestaurantResponse, RestaurantCreateRequest, RestaurantPatchRequest, FonepayCredentialsRequest, EsewaCredentialsRequest, PageResponse } from '@/types'

export const restaurantApi = {
  get: (code: string) =>
    api.get<RestaurantResponse>(`/restaurants/${code}`).then(r => r.data),

  search: (params?: { name?: string }) =>
    api.get<PageResponse<RestaurantResponse>>('/restaurants/search', { params }).then(r => r.data),

  create: (data: RestaurantCreateRequest) =>
    api.post<RestaurantResponse>('/restaurants', data).then(r => r.data),

  update: (code: string, data: RestaurantPatchRequest) =>
    api.patch<RestaurantResponse>(`/restaurants/${code}`, data).then(r => r.data),

  // Save this restaurant's Fonepay merchant credentials (encrypted at rest server-side).
  updateFonepayCredentials: (code: string, data: FonepayCredentialsRequest) =>
    api.patch<RestaurantResponse>(`/restaurants/${code}/fonepay-credentials`, data).then(r => r.data),

  // Save this restaurant's eSewa merchant credentials (encrypted at rest server-side).
  updateEsewaCredentials: (code: string, data: EsewaCredentialsRequest) =>
    api.patch<RestaurantResponse>(`/restaurants/${code}/esewa-credentials`, data).then(r => r.data),

  getByOwner: (userCode: string) =>
    api.get<RestaurantResponse>(`/restaurants/by-owner/${userCode}`).then(r => r.data),

  getByKioskCode: (kioskCode: string) =>
    api.get<RestaurantResponse>(`/restaurants/by-kiosk-code/${kioskCode}`).then(r => r.data),

  delete: (code: string) =>
    api.delete(`/restaurants/${code}`),
}
