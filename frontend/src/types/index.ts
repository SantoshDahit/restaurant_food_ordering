// ─── Enums ───────────────────────────────────────────────────────────────────

export type UserRole = 'ADMIN' | 'MANAGER'
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
  createAt: string
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
  role: UserRole
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
  createAt: string
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
  phone?: string
  email?: string
  currency?: string
  fileCode?: string
}

// ─── Table ────────────────────────────────────────────────────────────────────

export interface RestaurantTableResponse {
  code: string
  restaurantCode: string
  tableNumber: string
  capacity: number
  status: TableStatus
  qrCodeUrl?: string
  qrCodeToken?: string
  isActive: boolean
  createAt: string
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
  createAt: string
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
  createAt: string
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
  createAt: string
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
  createAt: string
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
  createAt: string
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
  createAt: string
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
  createAt: string
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
