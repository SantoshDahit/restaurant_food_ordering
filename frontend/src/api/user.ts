import api from './axios'
import type { UserResponse, UserRole, PageResponse } from '@/types'

export const userApi = {
  search: (params?: { role?: UserRole; fullName?: string; size?: number; page?: number }) =>
    api.get<PageResponse<UserResponse>>('/users/search', { params }).then(r => r.data),

  get: (code: string) =>
    api.get<UserResponse>(`/users/${code}`).then(r => r.data),

  updateRestaurantCode: (userCode: string, restaurantCode: string) =>
    api.patch<UserResponse>(`/users/${userCode}/restaurant`, null, {
      params: { restaurantCode },
    }).then(r => r.data),
}
