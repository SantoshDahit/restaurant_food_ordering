/**
 * Host detection for the platform-admin portal.
 *
 * The admin portal is reachable on its own hostname (e.g. admin.example.com)
 * in addition to the /admin path on the main host. No domain is hardcoded:
 * any host whose first label is "admin" counts, which also covers
 * admin.localhost:5173 in local development. Set VITE_ADMIN_HOST to pin an
 * exact hostname instead when the subdomain is not literally "admin".
 *
 * Note this is presentation only — access control lives in the router's role
 * guard and, authoritatively, in the backend's /v1/admin/** rule.
 */
export function isAdminHost(hostname: string = window.location.hostname): boolean {
  const configured = import.meta.env.VITE_ADMIN_HOST?.trim()
  if (configured) return hostname === configured

  return hostname.split('.')[0] === 'admin'
}
