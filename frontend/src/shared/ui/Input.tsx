import { forwardRef, InputHTMLAttributes } from 'react';

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

export const Input = forwardRef<HTMLInputElement, InputProps>(
  function Input({ label, error, className = '', ...props }, ref) {
    return (
      <label className="grid gap-1 text-sm font-medium text-ink">
        <span>{label}</span>
        <input
          ref={ref}
          className={`min-h-10 rounded-md border border-primary/30 bg-surface px-3 text-sm text-ink outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/30 ${className}`}
          {...props}
        />
        {error ? <span className="text-xs font-normal text-danger">{error}</span> : null}
      </label>
    );
  },
);
