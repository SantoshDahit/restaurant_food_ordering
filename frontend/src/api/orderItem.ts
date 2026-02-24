import api from './axios'
import type { OrderItemResponse, OrderItemCreateRequest, OrderItemPatchRequest } from '@/types'

export const orderItemApi = {
  getByOrder: (orderCode: string) =>
    api.get<OrderItemResponse[]>(`/orders/${orderCode}/items`).then(r => r.data),

  add: (orderCode: string, data: OrderItemCreateRequest) =>
    api.post<OrderItemResponse>(`/orders/${orderCode}/items`, data).then(r => r.data),

  update: (orderCode: string, code: string, data: OrderItemPatchRequest) =>
    api.patch<OrderItemResponse>(`/orders/${orderCode}/items/${code}`, data).then(r => r.data),

  remove: (orderCode: string, code: string) =>
    api.delete(`/orders/${orderCode}/items/${code}`),
}
