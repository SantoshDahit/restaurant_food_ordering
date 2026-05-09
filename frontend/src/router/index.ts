import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ─── Public landing page ──────────────────────────────────────────────
    { path: '/', name: 'home', component: () => import('@/views/HomeView.vue') },

    // ─── Customer-facing routes (no auth required) ────────────────────────
    { path: '/qr/:token', name: 'qr-ordering', component: () => import('@/views/customer/QROrderingView.vue') },
    { path: '/kiosk/:kioskCode', name: 'kiosk', component: () => import('@/views/customer/KioskView.vue') },
    { path: '/payment', name: 'payment', component: () => import('@/views/customer/PaymentView.vue') },

    // ─── Auth ──────────────────────────────────────────────────────────────
    {
      path: '/login',
      component: () => import('@/layouts/AuthLayout.vue'),
      children: [
        { path: '', name: 'login', component: () => import('@/views/auth/LoginView.vue') },
      ],
    },

    // ─── Admin Dashboard (auth required) ──────────────────────────────────
    {
      path: '/admin',
      component: () => import('@/layouts/DashboardLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'dashboard', component: () => import('@/views/dashboard/DashboardView.vue') },
        { path: 'restaurant', name: 'restaurant', component: () => import('@/views/restaurant/RestaurantView.vue') },
        { path: 'tables', name: 'tables', component: () => import('@/views/tables/TableListView.vue') },
        { path: 'menu/categories', name: 'menu-categories', component: () => import('@/views/menu/MenuCategoryView.vue') },
        { path: 'menu/items', name: 'menu-items', component: () => import('@/views/menu/MenuItemView.vue') },
        { path: 'orders', name: 'orders', component: () => import('@/views/orders/OrderListView.vue') },
        { path: 'orders/:code', name: 'order-detail', component: () => import('@/views/orders/OrderDetailView.vue') },
        { path: 'payments', name: 'payments', component: () => import('@/views/payments/PaymentListView.vue') },
        { path: 'employees', name: 'employees', component: () => import('@/views/employees/EmployeeListView.vue') },
        { path: 'attendance', name: 'attendance', component: () => import('@/views/attendance/AttendanceView.vue') },
        { path: 'payroll', name: 'payroll', component: () => import('@/views/payroll/PayrollView.vue') },
        { path: 'waiter', name: 'waiter', component: () => import('@/views/customer/WaiterView.vue') },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'login' }
  }
  if (to.name === 'login' && auth.isLoggedIn) {
    return { name: 'dashboard' }  // redirects to /admin
  }
})

export default router
