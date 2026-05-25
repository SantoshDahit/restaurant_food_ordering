import axios from 'axios'
import api from './axios'
import type { FileResponse } from '@/types'

// Standalone axios instance for multipart uploads: no default Content-Type
// so the browser sets `multipart/form-data; boundary=...` automatically.
const uploadClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
})
uploadClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export const fileApi = {
  /**
   * Upload a file directly to the backend (stored on the server's local disk).
   * Returns the file code and a server-relative public URL like /uploads/menu-items/...
   */
  upload: async (file: File, folderName = 'menu-items'): Promise<{ code: string; url: string }> => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('folderName', folderName)
    formData.append('type', 'IMAGE')

    const res = await uploadClient.post<FileResponse>('/files/upload', formData).then(r => r.data)
    return { code: res.code, url: res.url }
  },

  get: (code: string): Promise<FileResponse> =>
    api.get<FileResponse>(`/files/${code}`).then(r => r.data),
}
