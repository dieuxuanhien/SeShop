import { HTMLAttributes } from 'react';

export function Card({ className = '', ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={`rounded-card border border-primary/20 bg-surface shadow-soft ${className}`} {...props} />;
}
