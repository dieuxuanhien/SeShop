import { create } from 'zustand';

export type CartLine = {
  id?: number;
  variantId: number;
  skuCode: string;
  name: string;
  qty: number;
  unitPrice: number;
};

type CartState = {
  items: CartLine[];
  setItems: (items: CartLine[]) => void;
  addItem: (item: CartLine) => void;
  removeItem: (variantId: number) => void;
  clear: () => void;
};

export const useCartStore = create<CartState>((set) => ({
  items: [],
  setItems: (items) => set({ items }),
  addItem: (item) =>
    set((state) => {
      const existing = state.items.find((line) => line.variantId === item.variantId);
      if (existing) {
        return {
          items: state.items.map((line) =>
            line.variantId === item.variantId ? { ...line, qty: line.qty + item.qty } : line,
          ),
        };
      }
      return { items: [...state.items, item] };
    }),
  removeItem: (variantId) => set((state) => ({ items: state.items.filter((line) => line.variantId !== variantId) })),
  clear: () => set({ items: [] }),
}));
