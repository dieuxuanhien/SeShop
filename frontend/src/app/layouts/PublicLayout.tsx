import { LogOut, Search, ShoppingBag, User } from 'lucide-react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '@/features/auth';
import { useCartStore } from '@/features/cart/model/cartStore';

export function PublicLayout() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const itemCount = useCartStore((state) => state.items.reduce((sum, item) => sum + item.qty, 0));

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <div className="min-h-screen bg-ink text-surface font-sans selection:bg-primary/30 selection:text-highlight">
      {/* Sleek, sticky header */}
      <header className="sticky top-0 z-50 w-full glass border-b-0 transition-all duration-300">
        <div className="mx-auto flex max-w-7xl h-20 items-center justify-between px-6 lg:px-12">
          
          {/* Left Navigation */}
          <nav className="hidden md:flex items-center gap-8 text-sm uppercase tracking-widest text-surface/80">
            <NavLink to="/products" className={({isActive}) => isActive ? 'text-primary' : 'hover:text-primary transition-colors'}>Collection</NavLink>
            <NavLink to="/products?category=accessories" className="hover:text-primary transition-colors">Accessories</NavLink>
            <NavLink to="/ai-chat" className="hover:text-primary transition-colors">AI Stylist</NavLink>
          </nav>

          {/* Center Logo */}
          <NavLink to="/" className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 font-display text-2xl tracking-widest text-highlight">
            SESHOP
          </NavLink>

          {/* Right Icons */}
          <div className="flex items-center gap-6 text-surface">
            <button className="hover:text-primary transition-colors" aria-label="Search">
              <Search size={20} strokeWidth={1.5} />
            </button>
            
            <div className="relative group">
              <button className="hover:text-primary transition-colors flex items-center gap-2" aria-label="Account" onClick={() => !token && navigate('/auth/login')}>
                <User size={20} strokeWidth={1.5} />
              </button>
              
              {token && (
                <div className="absolute right-0 mt-4 w-48 rounded-md glass opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 py-2">
                  <div className="px-4 py-2 border-b border-surface/10 text-xs text-surface/50">{user?.username}</div>
                  <NavLink to="/profile" className="block px-4 py-2 text-sm hover:bg-surface/5">Profile</NavLink>
                  <NavLink to="/orders" className="block px-4 py-2 text-sm hover:bg-surface/5">Orders</NavLink>
                  {user?.userType === 'ADMIN' || user?.userType === 'STAFF' ? (
                    <NavLink to={user.userType === 'ADMIN' ? '/admin/dashboard' : '/staff/dashboard'} className="block px-4 py-2 text-sm text-primary hover:bg-surface/5">Dashboard</NavLink>
                  ) : null}
                  <button onClick={handleLogout} className="w-full text-left px-4 py-2 text-sm text-danger hover:bg-surface/5">Logout</button>
                </div>
              )}
            </div>

            <NavLink to="/cart" className="hover:text-primary transition-colors relative" aria-label="Cart">
              <ShoppingBag size={20} strokeWidth={1.5} />
              {itemCount > 0 && (
                <span className="absolute -top-1.5 -right-2 flex h-4 w-4 items-center justify-center rounded-full bg-primary text-[10px] font-bold text-ink">
                  {itemCount}
                </span>
              )}
            </NavLink>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="min-h-[calc(100vh-80px-200px)]">
        <Outlet />
      </main>

      {/* Elegant Footer */}
      <footer className="border-t border-surface/10 bg-ink mt-20">
        <div className="mx-auto max-w-7xl px-6 lg:px-12 py-16 grid grid-cols-1 md:grid-cols-4 gap-12">
          <div className="md:col-span-2">
            <h3 className="font-display text-xl text-highlight mb-4 tracking-widest">SESHOP</h3>
            <p className="text-sm text-surface/60 leading-relaxed max-w-sm">
              Discover curated luxury vintage clothing and accessories. Every piece tells a story of elegance and timeless style.
            </p>
          </div>
          <div>
            <h4 className="text-xs uppercase tracking-widest text-surface/80 mb-6">Explore</h4>
            <ul className="space-y-4 text-sm text-surface/60">
              <li><NavLink to="/products" className="hover:text-primary transition-colors">New Arrivals</NavLink></li>
              <li><NavLink to="/products" className="hover:text-primary transition-colors">Clothing</NavLink></li>
              <li><NavLink to="/products" className="hover:text-primary transition-colors">Accessories</NavLink></li>
            </ul>
          </div>
          <div>
            <h4 className="text-xs uppercase tracking-widest text-surface/80 mb-6">Client Services</h4>
            <ul className="space-y-4 text-sm text-surface/60">
              <li><NavLink to="/contact" className="hover:text-primary transition-colors">Contact Us</NavLink></li>
              <li><NavLink to="/shipping" className="hover:text-primary transition-colors">Shipping & Returns</NavLink></li>
              <li><NavLink to="/faq" className="hover:text-primary transition-colors">FAQ</NavLink></li>
            </ul>
          </div>
        </div>
        <div className="border-t border-surface/5 py-6 text-center text-xs text-surface/40">
          <p>&copy; {new Date().getFullYear()} SeShop Vintage. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}
