import { LogOut, Menu, ShoppingCart, X } from 'lucide-react';
import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth';
import { useCartStore } from '@/features/cart/model/cartStore';
import { Button } from '@/shared/ui/Button';

const navGroups = [
  {
    label: 'Staff',
    links: [
      ['/staff/dashboard', 'Dashboard'],
      ['/staff/catalog', 'Catalog'],
      ['/staff/inventory', 'Inventory'],
      ['/staff/transfers', 'Transfers'],
      ['/staff/orders', 'Orders'],
      ['/staff/returns', 'Returns'],
      ['/staff/discounts', 'Discounts'],
      ['/staff/pos', 'POS'],
      ['/staff/pos/shift-close', 'Shift Close'],
      ['/staff/marketing/drafts', 'Instagram Drafts'],
    ],
  },
  {
    label: 'Admin',
    links: [
      ['/admin/dashboard', 'Dashboard'],
      ['/admin/users-roles', 'Users & Roles'],
      ['/admin/locations', 'Locations'],
      ['/admin/audit-logs', 'Audit Logs'],
      ['/admin/settings', 'Settings'],
    ],
  },
] as const;

export function RootLayout() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const itemCount = useCartStore((state) => state.items.reduce((sum, item) => sum + item.qty, 0));

  function handleLogout() {
    logout();
    navigate('/');
  }

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
            <NavLink to="/cart" className="inline-flex items-center gap-2 rounded-md border border-primary/40 px-3 py-2 text-sm text-primary">
              <ShoppingCart size={16} />
              <span>{itemCount}</span>
            </NavLink>
            {token ? (
              <>
                <span className="hidden text-sm text-surface/70 sm:inline">{user?.username ?? 'Signed in'}</span>
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
              {navGroups.map((group) => (
                <section key={group.label}>
                  <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-surface/60">{group.label}</h2>
                  <div className="grid gap-1">
                    {group.links.map(([to, label]) => (
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
            {navGroups.map((group) => (
              <section key={group.label}>
                <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-surface/60">{group.label}</h2>
                <div className="grid gap-1">
                  {group.links.map(([to, label]) => (
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
