import type { Product, Category } from '@/entities/product/types';
import type { StockAvailability } from '@/entities/inventory/types';

// ── Categories ──────────────────────────────────────────────
export const mockCategories: Category[] = [
  { id: 1, name: 'Outerwear', slug: 'outerwear', productCount: 4 },
  { id: 2, name: 'Dresses', slug: 'dresses', productCount: 3 },
  { id: 3, name: 'Accessories', slug: 'accessories', productCount: 3 },
  { id: 4, name: 'Denim', slug: 'denim', productCount: 3 },
  { id: 5, name: 'Tops', slug: 'tops', productCount: 3 },
];

const sizes = ['XS', 'S', 'M', 'L', 'XL'];
const colors: { name: string; hex: string }[] = [
  { name: 'Noir', hex: '#1A1A1A' },
  { name: 'Ivory', hex: '#FFFFF0' },
  { name: 'Camel', hex: '#C19A6B' },
  { name: 'Burgundy', hex: '#722F37' },
  { name: 'Navy', hex: '#1B2A4A' },
  { name: 'Sage', hex: '#9CAF88' },
];

function makeVariants(basePrice: number, compareAt?: number) {
  let id = 0;
  return sizes.flatMap((size) =>
    colors.slice(0, 2).map((color) => ({
      id: ++id,
      skuCode: `SKU-${id.toString().padStart(4, '0')}`,
      size,
      color: color.name,
      colorHex: color.hex,
      price: basePrice,
      compareAtPrice: compareAt,
      status: 'ACTIVE' as const,
    })),
  );
}

// ── Products ────────────────────────────────────────────────
export const mockProducts: Product[] = [
  {
    id: 1,
    name: 'Cashmere Overcoat',
    brand: 'Maison Sé',
    description: 'An impeccably tailored overcoat in pure Italian cashmere. The clean silhouette and hand-stitched details make this an investment piece for generations. Features a notch lapel, single-breasted closure, and fully lined interior.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=600&q=80',
    images: [
      { id: 1, url: 'https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=1200&q=80', altText: 'Cashmere overcoat front', sortOrder: 0 },
      { id: 2, url: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=1200&q=80', altText: 'Cashmere overcoat styled', sortOrder: 1 },
    ],
    variants: makeVariants(1890000),
    categories: [mockCategories[0]],
    reviewSummary: { averageRating: 4.8, reviewCount: 24 },
    createdAt: '2026-04-01T10:00:00Z',
  },
  {
    id: 2,
    name: 'Vintage Leather Jacket',
    brand: 'Rebel Heritage',
    description: 'Hand-distressed Italian lambskin jacket with antique brass hardware. Each piece is unique — the patina tells a different story. Asymmetric zip closure with three exterior pockets.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600&q=80',
    images: [
      { id: 3, url: 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=1200&q=80', altText: 'Leather jacket front', sortOrder: 0 },
      { id: 4, url: 'https://images.unsplash.com/photo-1520975954732-35dd22299614?w=1200&q=80', altText: 'Leather jacket detail', sortOrder: 1 },
    ],
    variants: makeVariants(2450000, 2990000),
    categories: [mockCategories[0]],
    reviewSummary: { averageRating: 4.9, reviewCount: 41 },
    createdAt: '2026-03-20T10:00:00Z',
  },
  {
    id: 3,
    name: 'Silk Evening Dress',
    brand: 'Atelier Noir',
    description: 'A floor-length evening dress in lustrous silk charmeuse. The draped cowl neckline and open back create an effortlessly glamorous silhouette. Dry clean only.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=600&q=80',
    images: [
      { id: 5, url: 'https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=1200&q=80', altText: 'Silk evening dress', sortOrder: 0 },
    ],
    variants: makeVariants(3200000),
    categories: [mockCategories[1]],
    reviewSummary: { averageRating: 4.7, reviewCount: 18 },
    createdAt: '2026-04-10T10:00:00Z',
  },
  {
    id: 4,
    name: 'Heritage Wool Blazer',
    brand: 'Maison Sé',
    description: 'Single-breasted blazer in British heritage wool with a subtle houndstooth pattern. Notch lapels, two-button closure, and patch pockets. Fully canvassed construction for a superior drape.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&q=80',
    images: [
      { id: 6, url: 'https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=1200&q=80', altText: 'Wool blazer', sortOrder: 0 },
    ],
    variants: makeVariants(1650000),
    categories: [mockCategories[0]],
    reviewSummary: { averageRating: 4.6, reviewCount: 32 },
    createdAt: '2026-03-15T10:00:00Z',
  },
  {
    id: 5,
    name: 'Italian Leather Belt',
    brand: 'Atelier Noir',
    description: 'Full-grain Italian calf leather with a hand-burnished finish. The solid brass buckle features an engraved monogram. Width: 3.5cm.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=600&q=80',
    images: [
      { id: 7, url: 'https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=1200&q=80', altText: 'Leather belt', sortOrder: 0 },
    ],
    variants: makeVariants(590000),
    categories: [mockCategories[2]],
    reviewSummary: { averageRating: 4.5, reviewCount: 56 },
    createdAt: '2026-02-28T10:00:00Z',
  },
  {
    id: 6,
    name: 'Selvedge Denim Jeans',
    brand: 'Rebel Heritage',
    description: 'Japanese selvedge denim with a slim-straight fit. 14oz raw indigo fabric that develops a unique fade pattern over time. Chain-stitched hems and hidden rivets.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&q=80',
    images: [
      { id: 8, url: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=1200&q=80', altText: 'Selvedge denim', sortOrder: 0 },
    ],
    variants: makeVariants(890000),
    categories: [mockCategories[3]],
    reviewSummary: { averageRating: 4.8, reviewCount: 67 },
    createdAt: '2026-04-05T10:00:00Z',
  },
  {
    id: 7,
    name: 'Linen Oversized Shirt',
    brand: 'Maison Sé',
    description: 'Relaxed oversized shirt in European linen. The natural texture and breathability make it ideal for warm seasons. Mother-of-pearl buttons and a mandarin collar.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&q=80',
    images: [
      { id: 9, url: 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=1200&q=80', altText: 'Linen shirt', sortOrder: 0 },
    ],
    variants: makeVariants(750000),
    categories: [mockCategories[4]],
    reviewSummary: { averageRating: 4.4, reviewCount: 29 },
    createdAt: '2026-04-12T10:00:00Z',
  },
  {
    id: 8,
    name: 'Wrap Midi Dress',
    brand: 'Atelier Noir',
    description: 'A versatile wrap dress in crepe de chine with a flattering midi length. Self-tie waist and flutter sleeves add a feminine touch. Suitable for day-to-evening transitions.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=600&q=80',
    images: [
      { id: 10, url: 'https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=1200&q=80', altText: 'Wrap midi dress', sortOrder: 0 },
    ],
    variants: makeVariants(1250000),
    categories: [mockCategories[1]],
    reviewSummary: { averageRating: 4.6, reviewCount: 22 },
    createdAt: '2026-03-25T10:00:00Z',
  },
  {
    id: 9,
    name: 'Cashmere Scarf',
    brand: 'Maison Sé',
    description: 'Lightweight cashmere scarf with hand-rolled edges. The subtle herringbone weave adds dimension without being overstated. 200cm x 70cm.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1601924994987-69e26d50dc64?w=600&q=80',
    images: [
      { id: 11, url: 'https://images.unsplash.com/photo-1601924994987-69e26d50dc64?w=1200&q=80', altText: 'Cashmere scarf', sortOrder: 0 },
    ],
    variants: makeVariants(420000),
    categories: [mockCategories[2]],
    reviewSummary: { averageRating: 4.9, reviewCount: 44 },
    createdAt: '2026-04-08T10:00:00Z',
  },
  {
    id: 10,
    name: 'Vintage Denim Jacket',
    brand: 'Rebel Heritage',
    description: 'Stone-washed denim jacket with authentic vintage distressing. Brass buttons, adjustable waist tabs, and blanket-lined interior for warmth.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=600&q=80',
    images: [
      { id: 12, url: 'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=1200&q=80', altText: 'Denim jacket', sortOrder: 0 },
    ],
    variants: makeVariants(980000, 1200000),
    categories: [mockCategories[3]],
    reviewSummary: { averageRating: 4.7, reviewCount: 38 },
    createdAt: '2026-03-18T10:00:00Z',
  },
  {
    id: 11,
    name: 'Merino Wool Turtleneck',
    brand: 'Maison Sé',
    description: 'Extra-fine merino wool turtleneck in a relaxed fit. The seamless construction and ribbed cuffs provide a clean, contemporary look. Machine washable.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1638643391904-9b551ba91eaa?w=600&q=80',
    images: [
      { id: 13, url: 'https://images.unsplash.com/photo-1638643391904-9b551ba91eaa?w=1200&q=80', altText: 'Merino turtleneck', sortOrder: 0 },
    ],
    variants: makeVariants(680000),
    categories: [mockCategories[4]],
    reviewSummary: { averageRating: 4.5, reviewCount: 51 },
    createdAt: '2026-04-15T10:00:00Z',
  },
  {
    id: 12,
    name: 'Leather Crossbody Bag',
    brand: 'Atelier Noir',
    description: 'Compact crossbody bag in pebbled Italian leather. Adjustable strap, magnetic closure, and three interior compartments. Fits essentials without bulk.',
    status: 'PUBLISHED',
    thumbnailUrl: 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600&q=80',
    images: [
      { id: 14, url: 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=1200&q=80', altText: 'Crossbody bag', sortOrder: 0 },
    ],
    variants: makeVariants(1150000),
    categories: [mockCategories[2]],
    reviewSummary: { averageRating: 4.8, reviewCount: 33 },
    createdAt: '2026-04-02T10:00:00Z',
  },
];

// ── Stock Availability ──────────────────────────────────────
export function getMockAvailability(productId: number): StockAvailability[] {
  const locations = [
    { locationId: 1, locationName: 'SeShop Boutique — District 1' },
    { locationId: 2, locationName: 'SeShop Atelier — District 3' },
    { locationId: 3, locationName: 'Central Warehouse' },
  ];
  return locations.map((loc) => ({
    ...loc,
    availableQty: Math.max(0, Math.floor(Math.random() * 15) - 2 + productId),
    updatedAt: new Date().toISOString(),
  }));
}
