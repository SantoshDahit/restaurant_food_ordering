# File

S3-backed file uploads. Used for menu item images, restaurant logos, employee photos.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/files/pre-signed-url` | Body: list of `{ fileName, folderName, type }`. Returns list of `{ id, url, preSignedUrl }`. Frontend then PUTs the file bytes directly to `preSignedUrl`. URL valid 15 minutes. |
| GET | `/v1/files/{code}` | Returns the stored File entity, including the public `url`. |

Entity (`File`): `code`, `url` (public S3 URL), `type` (IMAGE/PDF/DOCUMENT).

Bucket: `bookingsystemstorage` (ap-northeast-2). Public-read bucket policy on `*` so the public URLs returned by `GET /v1/files/{code}` resolve directly in the browser.

Flow used by the frontend (`api/file.ts`):
1. POST `/files/pre-signed-url` with file metadata.
2. PUT the raw bytes to the returned `preSignedUrl`.
3. Save the returned `id` as `fileCode` on the owning entity (MenuItem, Employee, Restaurant…).
4. Render `<img src="...url...">` using the stored public URL.

## Missing / planned features

- [ ] **Multipart upload endpoint** (currently disabled — `FileController.upload` is commented out). Optional fallback when the presigned-URL flow can't be used (e.g. server-side jobs).
- [ ] **DELETE `/v1/files/{code}`** — current entity has no delete path. Should soft-delete the row AND call `deleteFileFromS3(key)` (method exists in `FilePresignedUrlService` but unused).
- [ ] **Switch to presigned GET URLs** if the bucket should be re-locked down (currently public-read). Backend `getById` would generate a time-limited signed URL per request.
- [ ] **Thumbnails / resizing** — pre-generate small versions for menu list views. Either Lambda-on-upload or an inline image-processing service.
- [ ] **Per-file type restrictions** — backend currently accepts any `type` value with no MIME validation server-side. Either validate at presign time or use S3 bucket-level policy.
- [ ] **Owner ID on File entity** — `restaurantCode` or `uploaderUserCode` so files can be scoped to a tenant and orphan files can be reaped.
- [ ] **Orphan cleanup job** — files that have no referencing FK (no MenuItem.fileCode etc. points at them) after N days → delete.
- [ ] **Image-only validation client-side** (frontend file picker `accept="image/*"`) for menu items.
