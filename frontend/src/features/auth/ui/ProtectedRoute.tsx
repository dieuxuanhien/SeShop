import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../model/useAuth';
import { hasPermission, hasRole } from '@/shared/lib/permissions';

type ProtectedRouteProps = {
  role?: string;
  permission?: string;
};

export function ProtectedRoute({ role, permission }: ProtectedRouteProps) {
  const location = useLocation();
  const { token, user } = useAuth();

  if (!token) {
    return <Navigate to="/auth/login" state={{ from: location }} replace />;
  }

  if (!hasRole(user, role) || !hasPermission(user, permission)) {
    return <Navigate to="/access-denied" replace />;
  }

  return <Outlet />;
}
