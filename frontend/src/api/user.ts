import api from './axios'
import type { UserResponse } from '@/types'

export const userApi = {
  updateRestaurantCode: (userCode: string, restaurantCode: string) =>
    api.patch<UserResponse>(`/users/${userCode}/restaurant`, null, {
      params: { restaurantCode },
    }).then(r => r.data),
}
