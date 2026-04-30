export type OrderStatus =
  | 'PENDING'
  | 'ALLOCATED'
  | 'PICKING'
  | 'PACKED'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'RETURNED';

export type OrderSummary = {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  currency: string;
};
