import { HTMLAttributes } from 'react';

export function Card({ className = '', ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={`rounded-2xl border border-primary/15 bg-surface/90 backdrop-blur-md shadow-xl shadow-black/5 transition-all duration-300 hover:shadow-black/10 ${className}`} {...props} />;
}
