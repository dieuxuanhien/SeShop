type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'sale' | 'info';

const variantClasses: Record<BadgeVariant, string> = {
  default: 'bg-surface/10 text-surface/80',
  success: 'bg-success/15 text-success',
  warning: 'bg-warning/15 text-warning',
  danger: 'bg-danger/15 text-danger',
  sale: 'bg-primary/15 text-primary',
  info: 'bg-surface/10 text-surface/80',
};

type BadgeProps = {
  children: React.ReactNode;
  variant?: BadgeVariant;
  className?: string;
};

export function Badge({ children, variant = 'default', className = '' }: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wider ${variantClasses[variant]} ${className}`}
    >
      {children}
    </span>
  );
}
