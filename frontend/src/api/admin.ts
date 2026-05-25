import api from './axios'
import type { PlatformStats, RestaurantOverview, UserResponse, UserRole } from '@/types'

export const adminApi = {
  stats: () =>
    api.get<PlatformStats>('/admin/stats').then(r => r.data),

  restaurantOverview: (code: string) =>
    api.get<RestaurantOverview>(`/admin/restaurants/${code}/overview`).then(r => r.data),

  changeUserRole: (code: string, role: UserRole) =>
    api.patch<UserResponse>(`/admin/users/${code}/role`, { role }).then(r => r.data),

  setUserActive: (code: string, active: boolean) =>
    api.patch<UserResponse>(`/admin/users/${code}/active`, { active }).then(r => r.data),
}
