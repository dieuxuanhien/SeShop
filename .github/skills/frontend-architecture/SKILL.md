---
description: "Frontend architecture and design patterns for SeShop. Use when: understanding React component structure, designing state management, integrating APIs, or querying view specifications."
name: frontend-architecture
---

# SeShop Frontend Architecture Skill

This skill provides architectural knowledge for SeShop frontend implementation using React, TypeScript, and Vite.

## When to Use

- Understanding component hierarchy and composition
- Designing state management patterns
- Integrating backend APIs with proper error handling
- Implementing role-based UI routing
- Optimizing component performance

## Architecture Overview

**Framework**: React 18+ with TypeScript  
**Build Tool**: Vite  
**State Management**: Redux Toolkit or Context API  
**HTTP Client**: Axios with interceptors  
**Routing**: React Router v6

### Directory Structure

```
src/
├── pages/ → Top-level pages (route destinations)
│   ├── Admin/
│   │   ├── Dashboard.tsx → ADMIN_001
│   │   ├── UserManagement.tsx → ADMIN_002
│   │   ├── LocationsManagement.tsx → ADMIN_003
│   │   └── ...
│   ├── Staff/
│   │   ├── CatalogManagement.tsx → STAFF_001
│   │   ├── InventoryAdjustment.tsx → STAFF_002
│   │   └── ...
│   └── Customer/
│       ├── Browse.tsx → CUST_001
│       ├── ProductDetail.tsx → CUST_002
│       └── ...
│
├── components/ → Reusable components
│   ├── common/
│   │   ├── Header.tsx
│   │   ├── Sidebar.tsx
│   │   ├── Footer.tsx
│   │   └── ErrorBoundary.tsx
│   ├── forms/
│   │   ├── LoginForm.tsx
│   │   ├── ProductForm.tsx
│   │   └── CheckoutForm.tsx
│   ├── tables/
│   │   ├── ProductsTable.tsx
│   │   ├── OrdersTable.tsx
│   │   └── InventoryTable.tsx
│   └── widgets/
│       ├── StockWidget.tsx
│       ├── PriceDisplay.tsx
│       └── AIRecommendationChat.tsx
│
├── hooks/ → Custom React hooks
│   ├── useAuth.ts → Auth state and methods
│   ├── useApi.ts → API calls with loading/error
│   ├── useLocalStorage.ts → Persist state
│   └── useDebounce.ts → Debounce values
│
├── store/ → Redux slices (if using Redux)
│   ├── authSlice.ts → User, permissions, token
│   ├── cartSlice.ts → Cart items, total
│   ├── orderSlice.ts → Current/recent orders
│   └── inventorySlice.ts → Stock levels
│
├── api/ → API client and endpoints
│   ├── client.ts → Axios instance with interceptors
│   ├── auth.ts → /auth/* endpoints
│   ├── products.ts → /products/* endpoints
│   ├── orders.ts → /orders/* endpoints
│   └── inventory.ts → /inventory/* endpoints
│
├── types/ → TypeScript interfaces
│   ├── models.ts → Domain models (User, Product, Order, etc.)
│   ├── api.ts → API request/response types
│   └── store.ts → Redux state types
│
├── utils/ → Helper functions
│   ├── validators.ts → Form validation
│   ├── formatters.ts → Currency, dates, etc.
│   ├── constants.ts → App constants
│   └── permissions.ts → Permission checking
│
├── styles/ → Global styles
│   ├── index.css → Global styles
│   ├── variables.css → CSS variables (colors, spacing)
│   └── breakpoints.ts → Responsive breakpoints
│
├── App.tsx → Root component with routing
└── main.tsx → Entry point
```

## Component Design Patterns

### 1. Page Components (Route-Level)

```typescript
// pages/Admin/Dashboard.tsx
import React, { useEffect, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { dashboardAPI } from '@/api/dashboard';
import { Header } from '@/components/common/Header';
import { ErrorAlert } from '@/components/common/ErrorAlert';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user?.permissions.includes('DASHBOARD:VIEW')) {
      setError('You do not have permission to view this page');
      return;
    }

    dashboardAPI.getMetrics()
      .then(setMetrics)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, [user]);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorAlert message={error} />;

  return (
    <>
      <Header />
      <main className="dashboard">
        {/* Dashboard content */}
      </main>
    </>
  );
};
```

### 2. Reusable Components

```typescript
// components/forms/ProductForm.tsx
interface ProductFormProps {
  product?: Product;
  onSubmit: (data: ProductFormData) => Promise<void>;
  onCancel: () => void;
}

export const ProductForm: React.FC<ProductFormProps> = ({
  product,
  onSubmit,
  onCancel,
}) => {
  const [formData, setFormData] = useState<ProductFormData>(
    product ? toFormData(product) : emptyFormData
  );
  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    setLoading(true);

    try {
      const validationErrors = validateProductForm(formData);
      if (Object.keys(validationErrors).length > 0) {
        setErrors(validationErrors);
        return;
      }

      await onSubmit(formData);
    } catch (err) {
      setErrors({ submit: err.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields with error display */}
      <button type="submit" disabled={loading}>
        {loading ? 'Saving...' : 'Save'}
      </button>
      <button type="button" onClick={onCancel}>
        Cancel
      </button>
    </form>
  );
};
```

### 3. Custom Hooks for API Calls

```typescript
// hooks/useApi.ts
export function useApi<T>(apiCall: () => Promise<T>) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    apiCall()
      .then(result => {
        if (isMounted) setData(result);
      })
      .catch(err => {
        if (isMounted) setError(err.message);
      })
      .finally(() => {
        if (isMounted) setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  return { data, loading, error, refetch: apiCall };
}

// Usage in component
const { data: orders, loading, error } = useApi(() => 
  ordersAPI.getMyOrders()
);
```

### 4. State Management (Redux)

```typescript
// store/cartSlice.ts
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface CartItem {
  skuId: number;
  quantity: number;
  price: number;
}

interface CartState {
  items: CartItem[];
  total: number;
}

const initialState: CartState = {
  items: [],
  total: 0,
};

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    addItem: (state, action: PayloadAction<CartItem>) => {
      const existing = state.items.find(i => i.skuId === action.payload.skuId);
      if (existing) {
        existing.quantity += action.payload.quantity;
      } else {
        state.items.push(action.payload);
      }
      state.total = state.items.reduce(
        (sum, item) => sum + item.price * item.quantity,
        0
      );
    },
    removeItem: (state, action: PayloadAction<number>) => {
      state.items = state.items.filter(i => i.skuId !== action.payload);
      state.total = state.items.reduce(
        (sum, item) => sum + item.price * item.quantity,
        0
      );
    },
  },
});

export const { addItem, removeItem } = cartSlice.actions;
export default cartSlice.reducer;
```

## API Integration Pattern

```typescript
// api/client.ts
import axios, { AxiosInstance } from 'axios';
import { useAuth } from '@/hooks/useAuth';

export const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000,
});

// Add token to every request
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle errors globally
apiClient.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // Token expired, redirect to login
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error.response?.data || error);
  }
);

// api/products.ts
export const productsAPI = {
  list: (params?: { page?: number; limit?: number }) =>
    apiClient.get('/products', { params }),
  
  getById: (id: number) =>
    apiClient.get(`/products/${id}`),
  
  create: (data: ProductFormData) =>
    apiClient.post('/products', data),
  
  update: (id: number, data: ProductFormData) =>
    apiClient.put(`/products/${id}`, data),
};
```

## Routing with Role-Based Access

```typescript
// App.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { LoginPage } from '@/pages/LoginPage';
import { DashboardPage } from '@/pages/Admin/Dashboard';
import { Unauthorized } from '@/pages/Unauthorized';

const ProtectedRoute: React.FC<{
  component: React.ReactNode;
  requiredPermission?: string;
}> = ({ component, requiredPermission }) => {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" />;
  }

  if (requiredPermission && !user.permissions.includes(requiredPermission)) {
    return <Unauthorized />;
  }

  return component;
};

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/admin/dashboard"
          element={
            <ProtectedRoute
              component={<DashboardPage />}
              requiredPermission="DASHBOARD:VIEW"
            />
          }
        />
        {/* More routes */}
      </Routes>
    </BrowserRouter>
  );
}
```

## Performance Optimization

### 1. Code Splitting by Route
```typescript
const Dashboard = lazy(() => import('@/pages/Admin/Dashboard'));
const Orders = lazy(() => import('@/pages/Staff/Orders'));

<Suspense fallback={<LoadingSpinner />}>
  <Routes>
    <Route path="/dashboard" element={<Dashboard />} />
    <Route path="/orders" element={<Orders />} />
  </Routes>
</Suspense>
```

### 2. Memoization for Expensive Components
```typescript
export const ProductList = memo(({ products }) => (
  <ul>
    {products.map(p => (
      <ProductItem key={p.id} product={p} />
    ))}
  </ul>
));
```

### 3. React Query for Caching
```typescript
import { useQuery } from '@tanstack/react-query';

const { data, isLoading } = useQuery({
  queryKey: ['orders', userId],
  queryFn: () => ordersAPI.getByUser(userId),
  staleTime: 5 * 60 * 1000, // Cache for 5 minutes
});
```

## Error Handling

```typescript
interface ApiError {
  errorCode: string;
  message: string;
  details?: Record<string, string>;
}

// Helper to display user-friendly errors
function getErrorMessage(error: ApiError): string {
  const messages: Record<string, string> = {
    'INVENTORY_INSUFFICIENT': 'Not enough stock available',
    'PAYMENT_FAILED': 'Payment processing failed',
    'UNAUTHORIZED': 'You do not have permission to perform this action',
  };
  return messages[error.errorCode] || error.message;
}
```

## Responsive Design Breakpoints

```typescript
// utils/breakpoints.ts
export const breakpoints = {
  mobile: '640px',    // Small phones
  tablet: '1024px',   // Tablets
  desktop: '1280px',  // Desktops
  wide: '1920px',     // Wide screens
};

// Usage in styled-components or CSS
const Container = styled.div`
  padding: 1rem;
  
  @media (min-width: ${breakpoints.tablet}) {
    padding: 2rem;
  }
  
  @media (min-width: ${breakpoints.desktop}) {
    padding: 3rem;
  }
`;
```

## References

- [SeShop Views Description](docs/4.%20View%20descriptions/SeShop%20Views%20Desc.md)
- [SESHOP API Spec](docs/3.Design/SESHOP%20API%20Spec.md)
- [SESHOP SRS](docs/10.SRS/SESHOP%20SRS.md)
- [Frontend Implementation Agent](.github/agents/seshop-frontend-impl.agent.md)

