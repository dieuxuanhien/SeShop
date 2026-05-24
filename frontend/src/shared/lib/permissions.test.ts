import { describe, expect, it } from 'vitest';
import type { AuthUser } from '@/entities/user/types';
import { dashboardPathFor } from './access';
import { hasAnyPermission, hasPermission, hasRole } from './permissions';

describe('permissions helpers', () => {
  const customer: AuthUser = {
    id: 1,
    username: 'demo.customer',
    userType: 'CUSTOMER',
    roles: ['CUSTOMER'],
    permissions: ['order.read'],
  };

  const staff: AuthUser = {
    id: 2,
    username: 'staff.manager',
    userType: 'STAFF',
    roles: ['STORE_MANAGER'],
    permissions: ['catalog.write', 'inventory.adjust', 'order.read'],
  };

  const admin: AuthUser = {
    id: 3,
    username: 'super.admin',
    userType: 'ADMIN',
    roles: ['SUPER_ADMIN'],
    permissions: ['audit.read'],
  };

  it('checks direct permissions', () => {
    expect(hasPermission(staff, 'catalog.write')).toBe(true);
    expect(hasPermission(staff, 'refund.process')).toBe(false);
    expect(hasPermission(customer, undefined)).toBe(true);
    expect(hasAnyPermission(staff, ['refund.process', 'order.read'])).toBe(true);
    expect(hasAnyPermission(staff, ['refund.process', 'audit.read'])).toBe(false);
  });

  it('checks explicit roles without admin or seed-role fallbacks', () => {
    expect(hasRole(staff, 'STORE_MANAGER')).toBe(true);
    expect(hasRole(admin, 'STORE_MANAGER')).toBe(false);
    expect(hasRole(customer, 'STORE_MANAGER')).toBe(false);
    expect(hasRole(null, 'STORE_MANAGER')).toBe(false);
    expect(hasRole(customer, undefined)).toBe(true);
  });

  it('routes staff dashboard links to staff space even when staff has broad permissions', () => {
    const staffWithAdminLikePermissions: AuthUser = {
      ...staff,
      permissions: ['catalog.write', 'order.read', 'audit.read', 'location.scope.all'],
    };

    expect(dashboardPathFor(staffWithAdminLikePermissions)).toBe('/staff/dashboard');
    expect(dashboardPathFor(admin)).toBe('/admin/dashboard');
  });
});
