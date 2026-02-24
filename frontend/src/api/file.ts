import api from './axios'
import type { FileResponse } from '@/types'

export const fileApi = {
  upload: (file: File): Promise<FileResponse> => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<FileResponse>('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data)
  },

  get: (code: string): Promise<FileResponse> =>
    api.get<FileResponse>(`/files/${code}`).then(r => r.data),
}
