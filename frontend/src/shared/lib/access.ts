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
  'order.ship',
  'refund.process',
  'promo.manage',
  'pos.sell',
  'pos.shift.manage',
  'invoice.manage',
  'report.read',
  'social.compose',
  'social.connect',
  'inventory.cycle_count',
] as const;

export const ADMIN_DASHBOARD_PERMISSIONS = [
  'audit.read',
  'location.scope.all',
] as const;

export const LOCATION_VIEW_PERMISSIONS = [
  'staff.location.assign',
  'location.scope.all',
] as const;

export const ADMIN_SETTINGS_PERMISSIONS = [
  'audit.read',
  'location.scope.all',
] as const;

export function dashboardPathFor(user: AuthUser | null) {
  if (user?.userType === 'STAFF' && hasAnyPermission(user, STAFF_OPERATION_PERMISSIONS)) {
    return '/staff/dashboard';
  }
  if (user?.userType === 'ADMIN' && hasAnyPermission(user, ADMIN_DASHBOARD_PERMISSIONS)) {
    return '/admin/dashboard';
  }
  if (hasAnyPermission(user, STAFF_OPERATION_PERMISSIONS)) {
    return '/staff/dashboard';
  }
  if (hasAnyPermission(user, ADMIN_DASHBOARD_PERMISSIONS)) {
    return '/admin/dashboard';
  }
  return '/';
}
