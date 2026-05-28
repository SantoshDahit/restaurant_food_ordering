import api from './axios'
import type { RestaurantTableResponse, TableCreateRequest, TablePatchRequest, PageResponse, TableStatus } from '@/types'

export const tableApi = {
  search: (params: { restaurantCode: string; status?: TableStatus }) =>
    api.get<PageResponse<RestaurantTableResponse>>('/tables/search', { params }).then(r => r.data),

  get: (code: string) =>
    api.get<RestaurantTableResponse>(`/tables/${code}`).then(r => r.data),

  create: (data: TableCreateRequest) =>
    api.post<RestaurantTableResponse>('/tables', data).then(r => r.data),

  update: (code: string, data: TablePatchRequest) =>
    api.patch<RestaurantTableResponse>(`/tables/${code}`, data).then(r => r.data),

  delete: (code: string) =>
    api.delete(`/tables/${code}`),

  generateQr: (code: string) =>
    api.post<RestaurantTableResponse>(`/tables/${code}/qr`).then(r => r.data),

  getByToken: (token: string) =>
    api.get<RestaurantTableResponse>(`/tables/by-token/${token}`).then(r => r.data),

  getByTableCode: (tableCode: string) =>
    api.get<RestaurantTableResponse>(`/tables/by-table-code/${tableCode}`).then(r => r.data),
}
