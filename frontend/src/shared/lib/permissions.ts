import type { AuthUser } from '@/entities/user/types';

export function hasPermission(user: AuthUser | null, permission?: string) {
  if (!permission) {
    return true;
  }
  return user?.permissions.includes(permission) ?? false;
}

export function hasRole(user: AuthUser | null, role?: string) {
  if (!role) {
    return true;
  }
  return user?.roles.includes(role) || user?.userType === role;
}
