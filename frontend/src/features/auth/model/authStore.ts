import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthUser } from '@/entities/user/types';

type AuthState = {
  token: string | null;
  user: AuthUser | null;
  setAuth: (token: string, user: AuthUser) => void;
  logout: () => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: localStorage.getItem('seshop.accessToken'),
      user: null,
      setAuth: (token, user) => {
        localStorage.setItem('seshop.accessToken', token);
        set({ token, user });
      },
      logout: () => {
        localStorage.removeItem('seshop.accessToken');
        set({ token: null, user: null });
      },
    }),
    {
      name: 'seshop.auth',
      partialize: (state) => ({ token: state.token, user: state.user }),
    },
  ),
);
