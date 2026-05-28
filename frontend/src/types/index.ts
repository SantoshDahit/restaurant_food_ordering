// ─── Enums ───────────────────────────────────────────────────────────────────

export type UserRole = 'ADMIN' | 'MANAGER' | 'STAFF'
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED'
export type OrderType = 'DINE_IN' | 'TAKEAWAY' | 'QR_ORDER' | 'KIOSK'
export type PaymentMethod = 'CASH' | 'POS' | 'ESEWA' | 'KHALTI' | 'PHONEPAY' | 'IBANK'
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED'
export type ItemAvailability = 'AVAILABLE' | 'OUT_OF_STOCK' | 'HIDDEN'
export type MenuCategoryType = 'VEG' | 'NON_VEG' | 'DRINKS' | 'SPECIALS' | 'DESSERTS' | 'APPETIZERS' | 'SIDES'
export type TableStatus = 'AVAILABLE' | 'OCCUPIED' | 'RESERVED' | 'CLEANING'
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'HALF_DAY' | 'LEAVE' | 'HOLIDAY'
export type SalaryStatus = 'PENDING' | 'PAID' | 'ON_HOLD'

// ─── File ─────────────────────────────────────────────────────────────────────

export interface FileResponse {
  code: string
  type: 'IMAGE' | 'PDF' | 'DOCUMENT'
  url: string
  isSuccess: boolean
  createdAt: string
}

// ─── API Response Wrappers ────────────────────────────────────────────────────

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// ─── Auth ─────────────────────────────────────────────────────────────────────

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: UserResponse
}

export interface RegisterRequest {
  fullName: string
  email: string
  phone?: string
  password: string
  emailVerificationCode: string
}

// ─── Admin ────────────────────────────────────────────────────────────────────

export interface PlatformStats {
  totalRestaurants: number
  activeRestaurants: number
  totalUsers: number
  totalManagers: number
  totalStaff: number
  totalOrders: number
  ordersToday: number
  totalRevenue: number
  revenueToday: number
}

export interface RestaurantOverview {
  totalOrders: number
  totalRevenue: number
  activeStaffCount: number
  lastOrderAt?: string | null
  topItems: TopItem[]
}

// ─── Email verification ───────────────────────────────────────────────────────

export type EmailVerificationPurpose = 'JOIN' | 'PASSWORD_RESET' | 'CHANGE_EMAIL'
export type EmailVerificationStatus = 'PENDING' | 'VERIFIED' | 'USED' | 'EXPIRED'

export interface EmailVerificationSendRequest {
  email: string
  purpose: EmailVerificationPurpose
}

export interface EmailVerificationVerifyRequest {
  code: string
  email: string
  pin: string
  purpose: EmailVerificationPurpose
}

export interface EmailVerificationResponse {
  code: string
  email: string
  purpose: EmailVerificationPurpose
  status: EmailVerificationStatus
  verifiedAt?: string
  expiredAt: string
  createdAt: string
}

export interface UserResponse {
  code: string
  fullName: string
  email: string
  phone?: string
  role: UserRole
  fileCode?: string
  isActive: boolean
  createdAt: string
  updatedAt?: string
}

// ─── Restaurant ───────────────────────────────────────────────────────────────

export interface RestaurantResponse {
  code: string
  kioskCode: string
  userCode?: string
  name: string
  address: string
  businessNumber: string
  phone?: string
  email?: string
  currency: string
  fileCode?: string
  isActive: boolean
  createdAt: string
  updateAt?: string
}

export interface RestaurantCreateRequest {
  name: string
  address: string
  businessNumber: string
  userCode: string
  phone?: string
  email?: string
  currency?: string
  fileCode?: string
}

export interface RestaurantPatchRequest {
  name?: string
  address?: string
  businessNumber?: string
  phone?: string
  email?: string
  currency?: string
  fileCode?: string
}

// ─── Table ────────────────────────────────────────────────────────────────────

export interface RestaurantTableResponse {
  code: string
  tableCode: string
  restaurantCode: string
  tableNumber: string
  capacity: number
  status: TableStatus
  qrCodeUrl?: string
  qrCodeToken?: string
  isActive: boolean
  createdAt: string
  updateAt?: string
}

export interface TableCreateRequest {
  restaurantCode: string
  tableNumber: string
  capacity?: number
}

export interface TablePatchRequest {
  tableNumber?: string
  capacity?: number
  status?: TableStatus
}

// ─── Menu Category ────────────────────────────────────────────────────────────

export interface MenuCategoryResponse {
  code: string
  restaurantCode: string
  name: string
  categoryType?: MenuCategoryType
  fileCode?: string
  sortOrder: number
  isActive: boolean
  createdAt: string
}

export interface MenuCategoryCreateRequest {
  restaurantCode: string
  name: string
  categoryType?: MenuCategoryType
  fileCode?: string
  sortOrder?: number
}

export interface MenuCategoryPatchRequest {
  name?: string
  categoryType?: MenuCategoryType
  fileCode?: string
  sortOrder?: number
}

// ─── Menu Item ────────────────────────────────────────────────────────────────

export interface MenuItemResponse {
  code: string
  restaurantCode: string
  categoryCode?: string
  name: string
  description?: string
  price: number
  discountPercent: number
  fileCode?: string
  availability: ItemAvailability
  isFeatured: boolean
  isVeg: boolean
  prepTimeMinutes: number
  sortOrder: number
  createdAt: string
}

export interface MenuItemCreateRequest {
  restaurantCode: string
  categoryCode?: string
  name: string
  description?: string
  price: number
  discountPercent?: number
  fileCode?: string
  isVeg?: boolean
  prepTimeMinutes?: number
  sortOrder?: number
  availability?: ItemAvailability
}

export interface MenuItemPatchRequest {
  categoryCode?: string
  name?: string
  description?: string
  price?: number
  discountPercent?: number
  fileCode?: string
  availability?: ItemAvailability
  isFeatured?: boolean
  isVeg?: boolean
  prepTimeMinutes?: number
  sortOrder?: number
}

// ─── Orders ───────────────────────────────────────────────────────────────────

export interface OrdersResponse {
  code: string
  restaurantCode: string
  tableCode?: string
  waiterCode?: string
  orderNumber: string
  ticketNumber?: number | null
  orderType: OrderType
  status: OrderStatus
  subtotal: number
  discountAmount: number
  taxAmount: number
  totalAmount: number
  specialNotes?: string
  deviceType?: string
  createdAt: string
  updatedAt?: string
}

export interface OrderItemDetail {
  code: string
  orderCode: string
  menuItemCode: string
  menuItemName?: string
  quantity: number
  unitPrice: number
  discountAmount: number
  totalPrice: number
  spiceLevel?: string
  notes?: string
  status: OrderStatus
  createdAt: string
}

export interface OrderDetailResponse {
  code: string
  restaurantCode: string
  restaurantName?: string
  tableCode?: string
  tableNumber?: string
  waiterCode?: string
  waiterName?: string
  orderNumber: string
  ticketNumber?: number | null
  orderType: OrderType
  status: OrderStatus
  subtotal: number
  discountAmount: number
  taxAmount: number
  totalAmount: number
  specialNotes?: string
  deviceType?: string
  createdAt: string
  updatedAt: string
  items: OrderItemDetail[]
}

export interface OrderCreateRequest {
  restaurantCode: string
  tableCode?: string
  waiterCode?: string
  orderType: OrderType
  specialNotes?: string
  deviceType?: string
}

export interface OrderStatusUpdateRequest {
  status: OrderStatus
}

// ─── Order Item ───────────────────────────────────────────────────────────────

export interface OrderItemResponse {
  code: string
  orderCode: string
  menuItemCode: string
  quantity: number
  unitPrice: number
  discountAmount: number
  totalPrice: number
  spiceLevel?: string
  notes?: string
  status: OrderStatus
  createdAt: string
}

export interface OrderItemCreateRequest {
  menuItemCode: string
  quantity: number
  discountAmount?: number
  spiceLevel?: string
  notes?: string
}

export interface OrderItemPatchRequest {
  quantity?: number
  spiceLevel?: string
  notes?: string
}

// ─── Payment ──────────────────────────────────────────────────────────────────

export interface PaymentResponse {
  code: string
  restaurantCode: string
  orderCode: string
  processedBy?: string
  paymentMethod: PaymentMethod
  amount: number
  status: PaymentStatus
  transactionRef?: string
  receiptNumber?: string
  refundedAmount: number
  processedAt?: string
  createdAt: string
}

export interface PaymentCreateRequest {
  restaurantCode: string
  orderCode: string
  processedBy?: string
  paymentMethod: PaymentMethod
  amount: number
  transactionRef?: string
  receiptNumber?: string
}

export interface PaymentStatusUpdateRequest {
  status: PaymentStatus
  refundedAmount?: number
}

// ─── Receipt ──────────────────────────────────────────────────────────────────

export interface ReceiptItemSnapshot {
  menuItemCode: string
  name?: string | null
  quantity: number
  unitPrice: number
  totalPrice: number
  notes?: string | null
}

export interface ReceiptResponse {
  code: string
  receiptNumber: number
  businessDate: string
  restaurantCode: string
  orderCode: string
  paymentCode: string
  subtotal: number
  discountAmount: number
  taxAmount: number
  totalAmount: number
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  gatewayProvider?: string | null
  gatewayTransactionId?: string | null
  restaurantNameSnapshot: string
  restaurantAddress?: string | null
  restaurantPhone?: string | null
  restaurantBusinessNumber?: string | null
  orderNumberSnapshot: string
  tableNumberSnapshot?: string | null
  itemsJson: string
  customerName?: string | null
  customerEmail?: string | null
  customerPhone?: string | null
  notes?: string | null
  issuedAt: string
}

// ─── Analytics ────────────────────────────────────────────────────────────────

export interface RevenuePoint {
  date: string            // ISO yyyy-mm-dd
  orderCount: number
  revenue: number
}

export interface RevenueSeries {
  from: string
  to: string
  totalRevenue: number
  totalOrders: number
  points: RevenuePoint[]
}

export interface TopItem {
  menuItemCode: string
  menuItemName?: string | null
  quantity: number
  revenue: number
}

// ─── Employee ─────────────────────────────────────────────────────────────────

export interface EmployeeResponse {
  code: string
  restaurantCode: string
  fileCode?: string
  fullName: string
  phone?: string
  joinDate: string
  baseSalary: number
  bankAccount?: string
  bankName?: string
  isActive: boolean
  createdAt: string
}

export interface EmployeeCreateRequest {
  restaurantCode: string
  fullName: string
  phone?: string
  joinDate: string
  baseSalary: number
  bankAccount?: string
  bankName?: string
  fileCode?: string
}

export interface EmployeePatchRequest {
  fullName?: string
  phone?: string
  baseSalary?: number
  bankAccount?: string
  bankName?: string
  fileCode?: string
}

// ─── Attendance ───────────────────────────────────────────────────────────────

export interface AttendanceResponse {
  code: string
  employeeCode: string
  restaurantCode: string
  attendanceDate: string
  status: AttendanceStatus
  checkInTime?: string
  checkOutTime?: string
  workedHours?: number
  overtimeHours: number
  notes?: string
  createdAt: string
}

export interface AttendanceCreateRequest {
  employeeCode: string
  restaurantCode: string
  attendanceDate: string
  status?: AttendanceStatus
  checkInTime?: string
  checkOutTime?: string
  notes?: string
}

export interface AttendancePatchRequest {
  status?: AttendanceStatus
  checkInTime?: string
  checkOutTime?: string
  workedHours?: number
  overtimeHours?: number
  notes?: string
}

// ─── Payroll ──────────────────────────────────────────────────────────────────

export interface PayrollResponse {
  code: string
  restaurantCode: string
  employeeCode: string
  payPeriodStart: string
  payPeriodEnd: string
  overtimePay: number
  bonus: number
  deductions: number
  netSalary: number
  status: SalaryStatus
  paidAt?: string
  createdAt: string
}

export interface PayrollCreateRequest {
  restaurantCode: string
  employeeCode: string
  payPeriodStart: string
  payPeriodEnd: string
  overtimePay?: number
  bonus?: number
  deductions?: number
  netSalary: number
}

export interface PayrollStatusUpdateRequest {
  status: SalaryStatus
}
