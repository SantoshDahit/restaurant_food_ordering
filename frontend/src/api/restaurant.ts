import api from './axios'
import type { RestaurantResponse, RestaurantCreateRequest, RestaurantPatchRequest, PageResponse } from '@/types'

export const restaurantApi = {
  get: (code: string) =>
    api.get<RestaurantResponse>(`/restaurants/${code}`).then(r => r.data),

  search: (params?: { name?: string }) =>
    api.get<PageResponse<RestaurantResponse>>('/restaurants/search', { params }).then(r => r.data),

  create: (data: RestaurantCreateRequest) =>
    api.post<RestaurantResponse>('/restaurants', data).then(r => r.data),

  update: (code: string, data: RestaurantPatchRequest) =>
    api.patch<RestaurantResponse>(`/restaurants/${code}`, data).then(r => r.data),

  getByOwner: (userCode: string) =>
    api.get<RestaurantResponse>(`/restaurants/by-owner/${userCode}`).then(r => r.data),

  getByKioskCode: (kioskCode: string) =>
    api.get<RestaurantResponse>(`/restaurants/by-kiosk-code/${kioskCode}`).then(r => r.data),

  delete: (code: string) =>
    api.delete(`/restaurants/${code}`),
}
