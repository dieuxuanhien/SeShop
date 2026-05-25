import { ButtonHTMLAttributes, ReactNode } from 'react';

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'danger' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  icon?: ReactNode;
};

const variants = {
  primary: 'bg-gradient-to-r from-primary to-primaryStrong text-surface font-semibold shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/30 hover:-translate-y-0.5 border-none',
  secondary: 'border border-primary/20 bg-surface/50 backdrop-blur-sm text-ink font-medium hover:bg-primary/5 hover:border-primary/40 shadow-sm hover:shadow-md hover:-translate-y-0.5',
  danger: 'bg-gradient-to-r from-danger to-red-800 text-surface font-semibold shadow-md shadow-danger/20 hover:shadow-lg hover:shadow-danger/30 hover:-translate-y-0.5 border-none',
  outline: 'border-2 border-primary/20 bg-transparent text-primary font-medium hover:border-primary/50 hover:bg-primary/5 hover:-translate-y-0.5',
};

const sizes = {
  sm: 'min-h-9 px-4 py-1.5 text-xs rounded-lg',
  md: 'min-h-11 px-6 py-2 text-sm rounded-xl',
  lg: 'min-h-12 px-8 py-2.5 text-base rounded-xl',
};

export function Button({ variant = 'primary', size = 'md', icon, children, className = '', isLoading = false, disabled, ...props }: ButtonProps) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:translate-y-0 disabled:hover:shadow-none ${variants[variant]} ${sizes[size]} ${className}`}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? <span className="inline-block size-4 animate-spin rounded-full border-2 border-current border-t-transparent" aria-hidden="true" /> : icon}
      {children}
    </button>
  );
}
