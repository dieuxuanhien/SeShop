import { ArrowRight, ShoppingBag } from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';

export function Home() {
  return (
    <div className="flex flex-col w-full -mt-20"> {/* Negative margin to underlap the transparent header */}
      
      {/* Hero Section */}
      <section className="relative h-screen w-full flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0 z-0">
          <img 
            src="https://images.unsplash.com/photo-1490481651871-ab68de25d43d?q=80&w=2070&auto=format&fit=crop" 
            alt="Luxury Fashion" 
            className="w-full h-full object-cover object-top opacity-80"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-ink via-ink/20 to-transparent"></div>
        </div>
        
        <div className="relative z-10 text-center px-4 max-w-4xl mx-auto mt-20 animate-fade-in-up">
          <span className="text-primary text-sm uppercase tracking-[0.3em] font-semibold mb-6 block">New Collection 2026</span>
          <h1 className="font-display text-5xl md:text-7xl lg:text-8xl text-surface mb-8 leading-tight">
            Elegance <br className="hidden md:block"/> Redefined
          </h1>
          <p className="text-surface/80 text-lg md:text-xl max-w-2xl mx-auto mb-10 font-light">
            Discover our curated selection of vintage luxury pieces. Timeless fashion for the modern aesthete.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-6">
            <NavLink to="/products" className="group flex items-center gap-3 bg-primary text-ink px-8 py-4 uppercase tracking-widest text-sm font-semibold hover:bg-primaryStrong transition-colors">
              Shop Now <ArrowRight size={16} className="group-hover:translate-x-1 transition-transform" />
            </NavLink>
            <NavLink to="/products?q=accessories" className="group flex items-center gap-3 bg-transparent border border-surface/30 text-surface px-8 py-4 uppercase tracking-widest text-sm font-semibold hover:bg-surface/10 transition-colors">
              Accessories
            </NavLink>
          </div>
        </div>
      </section>

      {/* Featured Categories */}
      <section className="py-24 px-6 lg:px-12 max-w-7xl mx-auto w-full">
        <div className="text-center mb-16">
          <h2 className="font-display text-3xl md:text-4xl text-highlight mb-4">Curated Categories</h2>
          <div className="w-12 h-0.5 bg-primary mx-auto"></div>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            { title: 'Outerwear', img: 'https://images.unsplash.com/photo-1445205170230-053b83016050?q=80&w=2071&auto=format&fit=crop' },
            { title: 'Accessories', img: 'https://images.unsplash.com/photo-1509319117193-57bab727e09d?q=80&w=1974&auto=format&fit=crop' },
            { title: 'Vintage Denim', img: 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?q=80&w=1974&auto=format&fit=crop' }
          ].map((cat, idx) => (
            <NavLink key={idx} to="/products" className="group relative aspect-[3/4] overflow-hidden bg-surfaceMuted/10">
              <img src={cat.img} alt={cat.title} className="w-full h-full object-cover opacity-80 group-hover:scale-105 group-hover:opacity-100 transition-all duration-700" />
              <div className="absolute inset-0 bg-gradient-to-t from-ink/90 via-ink/20 to-transparent"></div>
              <div className="absolute bottom-0 left-0 p-8 w-full flex justify-between items-end">
                <h3 className="font-display text-2xl text-surface">{cat.title}</h3>
                <span className="w-10 h-10 rounded-full border border-surface/30 flex items-center justify-center text-surface group-hover:bg-primary group-hover:text-ink group-hover:border-primary transition-all duration-300">
                  <ArrowRight size={18} />
                </span>
              </div>
            </NavLink>
          ))}
        </div>
      </section>

      {/* Brand Story / Statement */}
      <section className="py-24 bg-surfaceMuted/5 border-y border-primary/10">
        <div className="max-w-4xl mx-auto px-6 text-center">
          <ShoppingBag className="mx-auto text-primary mb-8 opacity-50" size={40} strokeWidth={1} />
          <h2 className="font-display text-3xl md:text-5xl text-highlight mb-8 leading-tight">
            "True luxury is understanding that every piece has a story. We simply provide the next chapter."
          </h2>
          <p className="text-primary tracking-[0.2em] text-sm uppercase">The SeShop Philosophy</p>
        </div>
      </section>

    </div>
  );
}
