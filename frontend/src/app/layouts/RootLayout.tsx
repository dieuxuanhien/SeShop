import { LogOut, Menu, ShoppingCart, X } from 'lucide-react';
import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth';
import { useCartStore } from '@/features/cart/model/cartStore';
import {
  ADMIN_DASHBOARD_PERMISSIONS,
  ADMIN_SETTINGS_PERMISSIONS,
  IDENTITY_ADMIN_PERMISSIONS,
  LOCATION_VIEW_PERMISSIONS,
  STAFF_OPERATION_PERMISSIONS,
} from '@/shared/lib/access';
import { hasAnyPermission, hasPermission } from '@/shared/lib/permissions';
import { Button } from '@/shared/ui/Button';
import { LocationProvider, useStaffLocation } from '@/shared/context/LocationContext';
const navGroups = [
  {
    label: 'Staff',
    links: [
      { to: '/staff/dashboard', label: 'Dashboard', permissions: STAFF_OPERATION_PERMISSIONS },
      { to: '/staff/purchase-orders', label: 'Purchase Orders', permission: 'inventory.transfer' },
      { to: '/staff/inventory', label: 'Inventory', permission: 'inventory.adjust' },
      { to: '/staff/cycle-counts', label: 'Cycle Count', permission: 'inventory.cycle_count' },
      { to: '/staff/transfers', label: 'Transfers', permission: 'inventory.transfer' },
      { to: '/staff/orders', label: 'Orders', permission: 'order.read' },
      { to: '/staff/returns', label: 'Returns', permission: 'refund.process' },
      { to: '/staff/invoices', label: 'Tax Invoices', permission: 'invoice.manage' },
      { to: '/staff/pos', label: 'POS', permission: 'pos.sell' },
      { to: '/staff/pos/shift-close', label: 'Shift Close', permission: 'pos.shift.manage' },
    ],
  },
  {
    label: 'Admin',
    links: [
      { to: '/admin/dashboard', label: 'Dashboard', permissions: ADMIN_DASHBOARD_PERMISSIONS },
      { to: '/staff/catalog', label: 'Catalog', permission: 'catalog.write' },
      { to: '/staff/discounts', label: 'Discounts', permission: 'promo.manage' },
      { to: '/staff/sales-report', label: 'Sales Report', permission: 'report.read' },
      { to: '/staff/marketing/instagram', label: 'Instagram Account', permission: 'social.connect' },
      { to: '/staff/marketing/drafts', label: 'Instagram Drafts', permission: 'social.compose' },
      { to: '/admin/users-roles', label: 'Users & Roles', permissions: IDENTITY_ADMIN_PERMISSIONS },
      { to: '/admin/locations', label: 'Locations', permissions: LOCATION_VIEW_PERMISSIONS },
      { to: '/admin/audit-logs', label: 'Audit Logs', permission: 'audit.read' },
      { to: '/admin/settings', label: 'Settings', permissions: ADMIN_SETTINGS_PERMISSIONS },
    ],
  },
] as const;

function RootLayoutContent() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const itemCount = useCartStore((state) => state.items.reduce((sum, item) => sum + item.qty, 0));
  
  const { locations, activeLocationId, setActiveLocationId } = useStaffLocation();

  function handleLogout() {
    logout();
    navigate('/');
  }

  const visibleNavGroups = navGroups
    .map((group) => ({
      ...group,
      links: group.links.filter((link) => {
        const permission = 'permission' in link ? link.permission : undefined;
        const permissions = 'permissions' in link ? link.permissions : undefined;
        return hasPermission(user, permission) && hasAnyPermission(user, permissions);
      }),
    }))
    .filter((group) => group.links.length > 0);

  return (
    <div className="min-h-screen bg-ink text-surface">
      <header className="sticky top-0 z-20 border-b border-primary/20 bg-ink/95 backdrop-blur">
        <div className="flex min-h-16 items-center justify-between gap-4 px-4 lg:px-6">
          <div className="flex items-center gap-3">
            <Button variant="secondary" className="px-3 lg:hidden" aria-label="Open navigation" icon={mobileOpen ? <X size={18} /> : <Menu size={18} />} onClick={() => setMobileOpen((open) => !open)} />
            <NavLink to="/" className="font-display text-lg font-semibold text-highlight">
              SeShop Vintage
            </NavLink>
          </div>

          <div className="flex items-center gap-3">
            {locations.length > 0 && (
              <select
                className="hidden sm:block rounded-md border border-primary/20 bg-surface px-3 py-1.5 text-sm text-ink shadow-sm focus:border-primary focus:outline-none"
                value={activeLocationId ?? ''}
                onChange={(e) => setActiveLocationId(Number(e.target.value))}
              >
                {locations.map(loc => (
                  <option key={loc.id} value={loc.id}>{loc.displayName}</option>
                ))}
              </select>
            )}
            <NavLink to="/cart" className="inline-flex items-center gap-2 rounded-md border border-primary/40 px-3 py-2 text-sm text-primary">
              <ShoppingCart size={16} />
              <span>{itemCount}</span>
            </NavLink>
            {token ? (
              <>
                <NavLink 
                  to={user?.userType === 'ADMIN' ? '/admin/profile' : user?.userType === 'STAFF' ? '/staff/profile' : '/profile'} 
                  className="hidden text-sm font-medium text-highlight hover:underline sm:inline"
                >
                  {user?.username ?? 'Signed in'}
                </NavLink>
                <Button variant="secondary" icon={<LogOut size={16} />} onClick={handleLogout}>
                  Logout
                </Button>
              </>
            ) : (
              <Button onClick={() => navigate('/auth/login')}>Sign in</Button>
            )}
          </div>
        </div>
      </header>

      <div className="grid lg:grid-cols-[260px_minmax(0,1fr)]">
        {mobileOpen ? (
          <aside className="border-b border-primary/20 bg-ink/95 p-4 lg:hidden">
            <nav className="grid gap-6">
              {visibleNavGroups.map((group) => (
                <section key={group.label}>
                  <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-surface/60">{group.label}</h2>
                  <div className="grid gap-1">
                    {group.links.map(({ to, label }) => (
                      <NavLink
                        key={to}
                        to={to}
                        onClick={() => setMobileOpen(false)}
                        className={({ isActive }) =>
                          `rounded-md px-3 py-2 text-sm transition ${
                            isActive ? 'bg-primary/15 font-medium text-highlight' : 'text-surface/80 hover:bg-primary/10'
                          }`
                        }
                      >
                        {label}
                      </NavLink>
                    ))}
                  </div>
                </section>
              ))}
            </nav>
          </aside>
        ) : null}
        <aside className="hidden border-r border-primary/20 bg-ink/90 p-4 lg:block">
          <nav className="grid gap-6">
            {visibleNavGroups.map((group) => (
              <section key={group.label}>
                <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-surface/60">{group.label}</h2>
                <div className="grid gap-1">
                  {group.links.map(({ to, label }) => (
                    <NavLink
                      key={to}
                      to={to}
                      className={({ isActive }) =>
                        `rounded-md px-3 py-2 text-sm transition ${
                          isActive
                            ? 'bg-primary/15 font-medium text-highlight'
                            : 'text-surface/80 hover:bg-primary/10'
                        }`
                      }
                    >
                      {label}
                    </NavLink>
                  ))}
                </div>
              </section>
            ))}
          </nav>
        </aside>

        <main className="min-w-0 p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export function RootLayout() {
  return (
    <LocationProvider>
      <RootLayoutContent />
    </LocationProvider>
  );
}
