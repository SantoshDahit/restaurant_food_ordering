import api from './axios'
import type { RevenueSeries, TopItem } from '@/types'

export const analyticsApi = {
  revenue: (restaurantCode: string, from: string, to: string) =>
    api.get<RevenueSeries>('/analytics/revenue', { params: { restaurantCode, from, to } }).then(r => r.data),

  topItems: (restaurantCode: string, from: string, to: string, limit = 5) =>
    api.get<TopItem[]>('/analytics/top-items', { params: { restaurantCode, from, to, limit } }).then(r => r.data),
}
