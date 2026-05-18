import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../model/useAuth';
import { hasAnyPermission, hasPermission, hasRole } from '@/shared/lib/permissions';

type ProtectedRouteProps = {
  role?: string;
  permission?: string;
  permissions?: readonly string[];
};

export function ProtectedRoute({ role, permission, permissions }: ProtectedRouteProps) {
  const location = useLocation();
  const { token, user } = useAuth();

  if (!token) {
    return <Navigate to="/auth/login" state={{ from: location }} replace />;
  }

  if (!hasRole(user, role) || !hasPermission(user, permission) || !hasAnyPermission(user, permissions)) {
    return <Navigate to="/access-denied" replace />;
  }

  return <Outlet />;
}
