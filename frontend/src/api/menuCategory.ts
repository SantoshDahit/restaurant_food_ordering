import api from './axios'
import type { MenuCategoryResponse, MenuCategoryCreateRequest, MenuCategoryPatchRequest, PageResponse, MenuCategoryType } from '@/types'

export const menuCategoryApi = {
  search: (params: { restaurantCode?: string; categoryType?: MenuCategoryType; size?: number; page?: number }) =>
    api.get<PageResponse<MenuCategoryResponse>>('/menu-categories/search', { params }).then(r => r.data),

  get: (code: string) =>
    api.get<MenuCategoryResponse>(`/menu-categories/${code}`).then(r => r.data),

  create: (data: MenuCategoryCreateRequest) =>
    api.post<MenuCategoryResponse>('/menu-categories', data).then(r => r.data),

  update: (code: string, data: MenuCategoryPatchRequest) =>
    api.patch<MenuCategoryResponse>(`/menu-categories/${code}`, data).then(r => r.data),

  delete: (code: string) =>
    api.delete(`/menu-categories/${code}`),
}
