import { Route, Routes } from 'react-router-dom';
import { RootLayout } from '@/app/layouts/RootLayout';
import { ProtectedRoute } from '@/features/auth/ui/ProtectedRoute';
import { LoginPage } from '@/features/auth/ui/LoginPage';
import { RegisterPage } from '@/features/auth/ui/RegisterPage';
import { AdminDashboard } from '@/pages/admin/Dashboard';
import { AuditLogs } from '@/pages/admin/AuditLogs';
import { LocationsManagement } from '@/pages/admin/LocationsManagement';
import { Settings } from '@/pages/admin/Settings';
import { UserRoleManagement } from '@/pages/admin/UserRoleManagement';
import { AiChat } from '@/pages/customer/AiChat';
import { Cart } from '@/pages/customer/Cart';
import { Checkout } from '@/pages/customer/Checkout';
import { Home } from '@/pages/customer/Home';
import { OrderTracking } from '@/pages/customer/OrderTracking';
import { Orders } from '@/pages/customer/Orders';
import { ProductDetail } from '@/pages/customer/ProductDetail';
import { Products } from '@/pages/customer/Products';
import { Profile } from '@/pages/customer/Profile';
import { Reviews } from '@/pages/customer/Reviews';
import { StockAvailability } from '@/pages/customer/StockAvailability';
import { InstagramConnection } from '@/pages/marketing/InstagramConnection';
import { InstagramDrafts } from '@/pages/marketing/InstagramDrafts';
import { CatalogManagement } from '@/pages/staff/CatalogManagement';
import { Discounts } from '@/pages/staff/Discounts';
import { InventoryAdjustment } from '@/pages/staff/InventoryAdjustment';
import { OrdersManagement } from '@/pages/staff/OrdersManagement';
import { POS } from '@/pages/staff/POS';
import { PurchaseOrders } from '@/pages/staff/PurchaseOrders';
import { ReturnsManagement } from '@/pages/staff/ReturnsManagement';
import { SalesReport } from '@/pages/staff/SalesReport';
import { ShiftClose } from '@/pages/staff/ShiftClose';
import { StaffDashboard } from '@/pages/staff/Dashboard';
import { StockTransfer } from '@/pages/staff/StockTransfer';
import { EmptyState } from '@/shared/ui/EmptyState';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/auth/login" element={<LoginPage />} />
      <Route path="/auth/register" element={<RegisterPage />} />

      <Route element={<RootLayout />}>
        <Route index element={<Home />} />
        <Route path="products" element={<Products />} />
        <Route path="products/:productId" element={<ProductDetail />} />
        <Route path="products/:productId/availability" element={<StockAvailability />} />
        <Route path="cart" element={<Cart />} />
        <Route path="ai-chat" element={<AiChat />} />

        <Route element={<ProtectedRoute role="CUSTOMER" />}>
          <Route path="checkout" element={<Checkout />} />
          <Route path="orders" element={<Orders />} />
          <Route path="orders/:orderId" element={<OrderTracking />} />
          <Route path="profile" element={<Profile />} />
          <Route path="products/:productId/reviews" element={<Reviews />} />
        </Route>

        <Route element={<ProtectedRoute role="STAFF" />}>
          <Route path="staff/dashboard" element={<StaffDashboard />} />
          <Route path="staff/catalog" element={<CatalogManagement />} />
          <Route path="staff/inventory" element={<InventoryAdjustment />} />
          <Route path="staff/stock-transfer" element={<StockTransfer />} />
          <Route path="staff/orders" element={<OrdersManagement />} />
          <Route path="staff/returns" element={<ReturnsManagement />} />
          <Route path="staff/discounts" element={<Discounts />} />
          <Route path="staff/pos" element={<POS />} />
          <Route path="staff/shift-close" element={<ShiftClose />} />
          <Route path="staff/purchase-orders" element={<PurchaseOrders />} />
          <Route path="staff/sales-report" element={<SalesReport />} />
          <Route path="instagram/drafts" element={<InstagramDrafts />} />
          <Route path="instagram/connect" element={<InstagramConnection />} />
        </Route>

        <Route element={<ProtectedRoute role="ADMIN" />}>
          <Route path="admin/dashboard" element={<AdminDashboard />} />
          <Route path="admin/users" element={<UserRoleManagement />} />
          <Route path="admin/locations" element={<LocationsManagement />} />
          <Route path="admin/audit-logs" element={<AuditLogs />} />
          <Route path="admin/settings" element={<Settings />} />
        </Route>

        <Route path="access-denied" element={<EmptyState title="Access denied" description="Your account does not have access to this route." />} />
        <Route path="*" element={<EmptyState title="Page not found" description="The requested route is not part of the SeShop shell." />} />
      </Route>
    </Routes>
  );
}
