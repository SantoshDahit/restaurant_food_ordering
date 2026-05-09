import api from './axios'
import type { OrdersResponse, OrderDetailResponse, OrderCreateRequest, OrderStatusUpdateRequest, PageResponse, OrderStatus, OrderType } from '@/types'

export const ordersApi = {
  search: (params: { restaurantCode?: string; status?: OrderStatus; orderType?: OrderType; tableCode?: string; size?: number; page?: number }) =>
    api.get<PageResponse<OrdersResponse>>('/orders/search', { params }).then(r => r.data),

  get: (code: string) =>
    api.get<OrdersResponse>(`/orders/${code}`).then(r => r.data),

  getDetail: (code: string) =>
    api.get<OrderDetailResponse>(`/orders/${code}/detail`).then(r => r.data),

  create: (data: OrderCreateRequest) =>
    api.post<OrdersResponse>('/orders', data).then(r => r.data),

  updateStatus: (code: string, data: OrderStatusUpdateRequest) =>
    api.patch<OrdersResponse>(`/orders/${code}/status`, data).then(r => r.data),

  delete: (code: string) =>
    api.delete(`/orders/${code}`),
}
