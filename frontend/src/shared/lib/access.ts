import type { AuthUser } from '@/entities/user/types';
import { hasAnyPermission } from './permissions';

export const IDENTITY_ADMIN_PERMISSIONS = [
  'role.create',
  'role.update',
  'role.delete',
  'role.permission.assign',
  'staff.role.assign',
  'staff.user.read',
  'staff.user.create',
  'staff.user.update',
  'staff.user.delete',
] as const;

export const STAFF_OPERATION_PERMISSIONS = [
  'catalog.write',
  'inventory.adjust',
  'inventory.transfer',
  'order.read',
  'refund.process',
  'promo.manage',
  'pos.sell',
  'pos.shift.manage',
  'report.read',
  'social.compose',
  'social.connect',
] as const;

export const ADMIN_DASHBOARD_PERMISSIONS = [
  ...IDENTITY_ADMIN_PERMISSIONS,
  'audit.read',
  'report.read',
  'inventory.adjust',
  'order.read',
] as const;

export const LOCATION_VIEW_PERMISSIONS = [
  'inventory.adjust',
  'inventory.transfer',
  'report.read',
] as const;

export const ADMIN_SETTINGS_PERMISSIONS = [
  'social.connect',
  'invoice.manage',
  'audit.read',
] as const;

export function dashboardPathFor(user: AuthUser | null) {
  if (hasAnyPermission(user, ADMIN_DASHBOARD_PERMISSIONS)) {
    return '/admin/dashboard';
  }
  if (hasAnyPermission(user, STAFF_OPERATION_PERMISSIONS)) {
    return '/staff/dashboard';
  }
  return '/';
}
