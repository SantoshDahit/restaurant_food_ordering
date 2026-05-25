import api from './axios'
import type { FileResponse } from '@/types'

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

    const res = await api.post<FileResponse>('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data)

    return { code: res.code, url: res.url }
  },

  get: (code: string): Promise<FileResponse> =>
    api.get<FileResponse>(`/files/${code}`).then(r => r.data),
}
