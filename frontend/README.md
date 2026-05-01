# SESHOP Frontend - Hướng dẫn Triển khai

**Frontend**: React + TypeScript + Vite + Tailwind CSS  
**Status**: Planning Phase  
**Last Updated**: 2026-04-29

---

## 📋 Cấu trúc Frontend

```
src/
├── components/
│   ├── layout/
│   │   ├── Navbar.tsx               # Top navigation
│   │   ├── Sidebar.tsx              # Left sidebar
│   │   ├── Footer.tsx               # Footer
│   │   └── Layout.tsx               # Main layout wrapper
│   │
│   ├── auth/
│   │   ├── LoginPage.tsx            # Login page
│   │   ├── RegisterPage.tsx         # Registration page
│   │   ├── ProtectedRoute.tsx       # Route guard
│   │   └── LoginForm.tsx            # Login form component
│   │
│   ├── catalog/
│   │   ├── ProductList.tsx          # Product grid view
│   │   ├── ProductDetail.tsx        # Single product page
│   │   ├── ProductCard.tsx          # Product card component
│   │   ├── FilterSidebar.tsx        # Filters (category, price)
│   │   ├── SearchBar.tsx            # Search component
│   │   └── SortOptions.tsx          # Sort dropdown
│   │
│   ├── cart/
│   │   ├── CartPage.tsx             # Cart page
│   │   ├── CartItem.tsx             # Cart item row
│   │   ├── CartSummary.tsx          # Cart summary widget
│   │   └── CartBadge.tsx            # Item count badge
│   │
│   ├── checkout/
│   │   ├── CheckoutPage.tsx         # Main checkout page
│   │   ├── CheckoutStepper.tsx      # Step indicator
│   │   ├── ShippingForm.tsx         # Shipping address form
│   │   ├── PaymentForm.tsx          # Payment method form
│   │   ├── OrderReview.tsx          # Order review step
│   │   └── SuccessPage.tsx          # Order confirmation
│   │
│   ├── customer/
│   │   ├── CustomerDashboard.tsx    # Customer home
│   │   ├── ProfilePage.tsx          # Profile & settings
│   │   ├── OrderHistory.tsx         # Past orders
│   │   ├── OrderDetail.tsx          # Order details
│   │   ├── TrackingPage.tsx         # Shipment tracking
│   │   ├── ReviewForm.tsx           # Product review form
│   │   └── ReturnRequest.tsx        # Return request form
│   │
│   ├── admin/
│   │   ├── AdminDashboard.tsx       # Admin home
│   │   ├── UserManagement.tsx       # User list & CRUD
│   │   ├── RoleManagement.tsx       # Role list & edit
│   │   ├── PermissionManagement.tsx # Permission assignment
│   │   ├── AuditLog.tsx             # Audit trail viewer
│   │   ├── SystemSettings.tsx       # System config
│   │   └── AdminSidebar.tsx         # Admin navigation
│   │
│   ├── staff/
│   │   ├── StaffDashboard.tsx       # Staff home
│   │   ├── InventoryDashboard.tsx   # Stock overview
│   │   ├── InventoryDetail.tsx      # Location stock detail
│   │   ├── POSPage.tsx              # POS interface
│   │   ├── POSProductSearch.tsx     # POS product search
│   │   ├── POSSale.tsx              # POS sale form
│   │   ├── OrderManagement.tsx      # Staff order list
│   │   ├── OrderProcessing.tsx      # Process order
│   │   ├── StockTransfer.tsx        # Stock transfer form
│   │   ├── SalesReport.tsx          # Daily sales report
│   │   ├── PurchaseOrder.tsx        # Purchase order form
│   │   ├── GoodsReceipt.tsx         # Goods receipt form
│   │   ├── ShiftOpen.tsx            # Open POS shift
│   │   ├── ShiftClose.tsx           # Close POS shift
│   │   └── CashReconciliation.tsx   # Cash count form
│   │
│   ├── instagram/
│   │   ├── InstagramPage.tsx        # Instagram main page
│   │   ├── ConnectionPage.tsx       # Account connection
│   │   ├── DraftComposer.tsx        # Draft composer
│   │   ├── DraftPreview.tsx         # Draft preview
│   │   ├── DraftHistory.tsx         # Past drafts
│   │   └── MediaUpload.tsx          # Image/video upload
│   │
│   └── common/
│       ├── Modal.tsx                # Reusable modal
│       ├── Button.tsx               # Reusable button
│       ├── Card.tsx                 # Card component
│       ├── Input.tsx                # Form input
│       ├── Select.tsx               # Dropdown
│       ├── Checkbox.tsx             # Checkbox
│       ├── Radio.tsx                # Radio button
│       ├── Form.tsx                 # Form wrapper
│       ├── Table.tsx                # Data table
│       ├── TableRow.tsx             # Table row
│       ├── Pagination.tsx           # Pagination
│       ├── Spinner.tsx              # Loading spinner
│       ├── Toast.tsx                # Toast notification
│       ├── Badge.tsx                # Status badge
│       ├── ErrorMessage.tsx         # Error display
│       ├── EmptyState.tsx           # Empty state message
│       ├── ConfirmDialog.tsx        # Confirm dialog
│       └── ErrorBoundary.tsx        # Error boundary
│
├── hooks/
│   ├── useAuth.ts                   # Auth state & methods
│   ├── useCart.ts                   # Cart state management
│   ├── useUser.ts                   # User profile data
│   ├── useNotification.ts           # Toast notifications
│   ├── useForm.ts                   # Form handling
│   ├── useData.ts                   # Generic data fetching
│   ├── useDebounce.ts               # Debounce hook
│   ├── useLocalStorage.ts           # Local storage sync
│   ├── useAsync.ts                  # Async operations
│   └── usePagination.ts             # Pagination logic
│
├── context/
│   ├── AuthContext.tsx              # Auth context provider
│   ├── CartContext.tsx              # Cart context provider
│   ├── NotificationContext.tsx      # Toast context provider
│   └── ThemeContext.tsx             # Theme (light/dark)
│
├── services/
│   ├── api/
│   │   ├── client.ts                # Axios instance
│   │   ├── interceptors.ts          # Request/response interceptors
│   │   ├── auth.ts                  # Auth API calls
│   │   ├── catalog.ts               # Catalog API calls
│   │   ├── cart.ts                  # Cart API calls
│   │   ├── order.ts                 # Order API calls
│   │   ├── user.ts                  # User API calls
│   │   ├── inventory.ts             # Inventory API calls
│   │   ├── pos.ts                   # POS API calls
│   │   ├── payment.ts               # Payment API calls
│   │   ├── admin.ts                 # Admin API calls
│   │   ├── instagram.ts             # Instagram API calls
│   │   └── ai.ts                    # AI API calls
│   │
│   └── storage/
│       ├── localStorage.ts          # Local storage helper
│       └── sessionStorage.ts        # Session storage helper
│
├── store/
│   ├── store.ts                     # Redux/Zustand setup
│   ├── slices/
│   │   ├── authSlice.ts             # Auth reducer
│   │   ├── cartSlice.ts             # Cart reducer
│   │   ├── uiSlice.ts               # UI state (modals, etc)
│   │   └── notificationSlice.ts     # Notification reducer
│   │
│   └── actions/
│       ├── authActions.ts           # Auth async actions
│       ├── cartActions.ts           # Cart async actions
│       └── orderActions.ts          # Order async actions
│
├── utils/
│   ├── validators.ts                # Form validation utils
│   ├── formatters.ts                # Data formatting (price, date)
│   ├── constants.ts                 # App constants
│   ├── helpers.ts                   # General helpers
│   ├── dateUtils.ts                 # Date operations
│   ├── currencyUtils.ts             # Currency operations
│   ├── storageUtils.ts              # Storage utilities
│   └── errorUtils.ts                # Error handling
│
├── styles/
│   ├── globals.css                  # Global styles
│   ├── variables.css                # CSS variables
│   ├── tailwind.css                 # Tailwind config
│   └── animations.css               # Custom animations
│
├── types/
│   ├── index.ts                     # Re-export all types
│   ├── auth.ts                      # Auth types
│   ├── product.ts                   # Product types
│   ├── order.ts                     # Order types
│   ├── user.ts                      # User types
│   ├── cart.ts                      # Cart types
│   ├── inventory.ts                 # Inventory types
│   ├── payment.ts                   # Payment types
│   ├── common.ts                    # Common types
│   └── api.ts                       # API response types
│
├── config/
│   ├── routes.ts                    # Route definitions
│   ├── constants.ts                 # App constants
│   ├── envConfig.ts                 # Env variables
│   └── apiConfig.ts                 # API config
│
├── pages/                           # Route-based page components
│   ├── customer/
│   │   ├── Home.tsx
│   │   ├── Products.tsx
│   │   ├── ProductPage.tsx
│   │   └── Orders.tsx
│   │
│   ├── staff/
│   │   ├── Dashboard.tsx
│   │   ├── Inventory.tsx
│   │   ├── POS.tsx
│   │   └── Orders.tsx
│   │
│   └── admin/
│       ├── Dashboard.tsx
│       └── Management.tsx
│
├── App.tsx                          # Main App component
├── main.tsx                         # Entry point
└── vite-env.d.ts                    # Vite env types

public/
├── images/
│   ├── logo.png
│   ├── banner.jpg
│   └── placeholder.jpg
├── icons/
├── favicon.ico
└── robots.txt

.env                                 # Environment variables
.env.example                         # Template
package.json
tsconfig.json
vite.config.ts
tailwind.config.js
postcss.config.js
Dockerfile
.dockerignore
README.md
```

---

## 🎯 Các Trang Chính

### Customer Portal

#### 1. Public Pages (No Auth Required)
- `/` - Home page
  - Featured products
  - Promotions/banners
  - Categories
  
- `/products` - Product listing
  - Grid/list view toggle
  - Filters (category, price, attributes)
  - Search
  - Sorting (new, popular, price)
  - Pagination
  
- `/products/:id` - Product detail
  - Product info & images
  - SKU selection (size, color)
  - Reviews section
  - Recommendations
  - Add to cart button
  
- `/auth/login` - Login page
  - Email/password form
  - Social login option
  - "Forgot password" link
  
- `/auth/register` - Registration page
  - Form with validation
  - Email verification
  - Auto-login after register

#### 2. Customer Pages (Auth Required)
- `/dashboard` - Customer dashboard
  - Order summary
  - Recent orders
  - Wishlist
  - Recommendations
  
- `/cart` - Shopping cart
  - Cart items with quantity control
  - Remove/update item
  - Apply discount code
  - Cart summary with totals
  - Proceed to checkout
  
- `/checkout` - Multi-step checkout
  - **Step 1**: Shipping address
    - Address form
    - Save as default
  - **Step 2**: Shipping method
    - Available carriers
    - Cost display
  - **Step 3**: Payment method
    - Credit card (Stripe)
    - COD (Cash on Delivery)
  - **Step 4**: Order review
    - Items review
    - Totals & breakdown
    - Place order button
  - **Step 5**: Success
    - Order number
    - Confirmation email notice
    - Tracking link
  
- `/orders` - Order history
  - List of orders
  - Filter by status
  - Search by order number
  
- `/orders/:id` - Order detail
  - Order summary
  - Items list
  - Shipping info & tracking
  - Timeline of status changes
  - Return request button
  
- `/tracking/:orderId` - Shipment tracking
  - Real-time tracking
  - Map view (optional)
  - Status timeline
  
- `/profile` - User profile
  - Personal info
  - Addresses
  - Payment methods
  - Settings
  - Logout
  
- `/products/:id/reviews` - Leave review
  - Star rating
  - Photo upload
  - Review text
  - Verification (must have purchased)
  
- `/returns` - Return requests
  - Create new return
  - Return history
  - Return status tracking
  - Refund status

#### 3. AI Recommendation
- `/ai-chat` - AI recommendation chat
  - Chat interface
  - Product recommendations
  - Context-aware suggestions
  - Product quick view from chat

---

### Staff Portal

#### 1. Staff Pages (require staff role)
- `/staff/dashboard` - Staff home
  - Daily metrics
  - Pending orders
  - Stock alerts
  - Recent sales
  
- `/staff/inventory` - Inventory dashboard
  - Stock by location
  - Low stock alerts
  - Stock transfer form
  - Transfer history
  
- `/staff/inventory/:locationId` - Location detail
  - SKU levels
  - Recent transactions
  - Cycle count
  - Adjustment history
  
- `/staff/pos` - POS system
  - Product search
  - Add to cart
  - Quantity input
  - Discount application
  - Payment processing
  - Receipt print
  
- `/staff/pos/shift` - Shift management
  - Open shift
  - Close shift
  - Cash reconciliation
  - Shift history
  
- `/staff/orders` - Order management
  - Pending orders
  - Order processing
  - Fulfillment allocation
  - Shipping update
  - Filter & search
  
- `/staff/orders/:id` - Order processing
  - Order details
  - Allocate to location
  - Generate packing slip
  - Update tracking
  - Print label
  
- `/staff/stock-transfer` - Stock transfer
  - Create transfer
  - Transfer history
  - Approve transfers (if manager)
  - Receive goods
  
- `/staff/purchase-orders` - Purchase orders
  - Create PO
  - Supplier list
  - Approve PO
  - Goods receipt
  
- `/staff/sales-report` - Daily reports
  - Sales summary
  - Top products
  - Payment breakdown
  - Trending items

---

### Admin Portal

#### 1. Admin Pages (require admin role)
- `/admin/dashboard` - Admin dashboard
  - System health
  - Key metrics
  - Recent activities
  - User access summary
  
- `/admin/users` - User management
  - User list with pagination
  - Search & filter
  - Create user
  - Edit user
  - Deactivate/delete user
  - Reset password
  
- `/admin/roles` - Role management
  - Role list
  - Create role
  - Edit role
  - Permission assignment
  - Delete role
  
- `/admin/permissions` - Permission management
  - Permission list
  - Create permission
  - Permission details
  - Permission usage
  
- `/admin/audit-logs` - Audit trail
  - Log list with pagination
  - Filter by user/action/date
  - Log details
  - Export logs
  
- `/admin/settings` - System settings
  - General settings
  - Payment configuration
  - Shipping settings
  - Email templates
  - Instagram integration settings

---

### Instagram Integration

- `/instagram` - Instagram page
  - Status of connection
  - Draft list
  - Compose new draft link
  
- `/instagram/connect` - OAuth connection
  - Login flow
  - Permission request
  - Redirect after connection
  
- `/instagram/compose` - Draft composer
  - Image/video upload
  - Caption editor
  - Hashtag input
  - Product tagging
  - Preview
  - Save/schedule draft
  - Submit for review
  
- `/instagram/drafts` - Draft history
  - Draft list
  - Status (NEW, EDITING, READY_FOR_REVIEW, APPROVED, ARCHIVED)
  - Edit/delete/publish draft
  - View analytics (if published)

---

## 🔐 Authentication & Authorization

### Authentication Flow
```
1. User inputs credentials
2. POST /api/v1/auth/login
3. Backend returns JWT token
4. Frontend stores token in localStorage
5. Token included in API headers
6. Protected routes check token validity
```

### Authorization
```
1. Get user roles & permissions from backend
2. Check permission before showing UI
3. Backend re-validates permission on API call
4. Show "Access Denied" if unauthorized
```

### Protected Routes
```tsx
<ProtectedRoute
  roles={['CUSTOMER']}
  path="/checkout"
  component={CheckoutPage}
/>

<ProtectedRoute
  roles={['STAFF', 'ADMIN']}
  permissions={['order.read']}
  path="/staff/orders"
  component={OrderManagement}
/>
```

---

## 🎨 Design System (Tailwind CSS)

### Colors
```
Primary: Blue (#3B82F6)
Secondary: Gray (#6B7280)
Success: Green (#10B981)
Warning: Amber (#F59E0B)
Error: Red (#EF4444)
```

### Typography
```
Heading 1: 32px / 1.2 / 700
Heading 2: 24px / 1.3 / 600
Heading 3: 20px / 1.4 / 600
Body: 16px / 1.5 / 400
Small: 14px / 1.5 / 400
```

### Spacing (8px base)
```
xs: 4px
sm: 8px
md: 16px
lg: 24px
xl: 32px
```

### Shadows
```
sm: 0 1px 2px rgba(0,0,0,0.05)
md: 0 4px 6px rgba(0,0,0,0.1)
lg: 0 10px 15px rgba(0,0,0,0.1)
```

---

## 📱 Responsive Breakpoints (Tailwind)
```
sm: 640px
md: 768px
lg: 1024px
xl: 1280px
2xl: 1536px
```

---

## 🔄 State Management (Redux/Zustand)

### Redux Slices
```typescript
// authSlice
- user: User | null
- token: string | null
- isLoading: boolean
- error: string | null

// cartSlice
- items: CartItem[]
- total: number
- discountCode: string | null
- discountAmount: number

// uiSlice
- modals: { [key: string]: boolean }
- notifications: Toast[]
- sidebarOpen: boolean
- theme: 'light' | 'dark'
```

---

## 🧪 Testing Strategy

### Unit Tests (Vitest)
- Utils & formatters
- Validators
- Hooks

### Component Tests (Vitest + React Testing Library)
- Form components
- Button interactions
- Modal opening/closing
- List rendering

### E2E Tests (Cypress/Playwright)
- Login flow
- Add product to cart
- Checkout flow
- Order tracking
- Admin functions

---

## 📚 Implementation Phases

### Phase 1 (Week 1): Setup
- Vite + React project
- TypeScript config
- Tailwind CSS
- Routing (React Router)
- State management setup
- API client

### Phase 2 (Weeks 1-2): Authentication & Public Pages
- Login/Register pages
- Home page
- Product listing
- Product detail
- Auth context

### Phase 3 (Weeks 2-3): Customer Shopping
- Shopping cart
- Cart context
- Checkout flow (multi-step)
- Order confirmation
- Payment integration

### Phase 4 (Weeks 3-4): Customer Account
- Order history
- Order detail
- Tracking page
- Profile page
- Return requests

### Phase 5 (Week 4): Staff Portal
- Staff dashboard
- Inventory dashboard
- POS system
- Order management
- Stock transfer

### Phase 6 (Weeks 4-5): Admin Portal
- Admin dashboard
- User management
- Role management
- Permission management
- Audit logs

### Phase 7 (Week 5): Instagram & AI
- Instagram connection
- Draft composer
- AI chat interface
- Recommendations

### Phase 8 (Week 5): Polish & Testing
- Component tests
- E2E tests
- Performance optimization
- Responsive design
- Accessibility

---

## 📝 Development Guidelines

1. **File Organization**: Grouped by domain/feature
2. **Naming Convention**: PascalCase for components, camelCase for files
3. **TypeScript**: All files should have .ts or .tsx extension
4. **Styling**: Tailwind CSS primarily, CSS modules for scoped styles
5. **Constants**: Keep in `src/config/constants.ts`
6. **API Calls**: Use custom hooks & services
7. **Error Handling**: Toast notifications + error boundaries
8. **Loading States**: Use Spinner component
9. **Forms**: React Hook Form + Zod validation
10. **Accessibility**: ARIA labels, semantic HTML

---

## 🚀 Performance Tips

- **Code Splitting**: Route-based lazy loading
- **Image Optimization**: WebP format, responsive images
- **Memoization**: useMemo, useCallback for expensive operations
- **Virtual Lists**: For large product/order lists
- **Caching**: API response caching with React Query
- **Bundle Analysis**: Monitor with Vite analysis tools

