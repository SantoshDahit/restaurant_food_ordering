import api from './axios'
import type { MenuItemResponse, MenuItemCreateRequest, MenuItemPatchRequest, PageResponse, ItemAvailability } from '@/types'

export const menuItemApi = {
  search: (params: { restaurantCode?: string; categoryCode?: string; availability?: ItemAvailability; isFeatured?: boolean; isVeg?: boolean; size?: number; page?: number }) =>
    api.get<PageResponse<MenuItemResponse>>('/menu-items/search', { params }).then(r => r.data),

  get: (code: string) =>
    api.get<MenuItemResponse>(`/menu-items/${code}`).then(r => r.data),

  create: (data: MenuItemCreateRequest) =>
    api.post<MenuItemResponse>('/menu-items', data).then(r => r.data),

  update: (code: string, data: MenuItemPatchRequest) =>
    api.patch<MenuItemResponse>(`/menu-items/${code}`, data).then(r => r.data),

  delete: (code: string) =>
    api.delete(`/menu-items/${code}`),
}
