import { forwardRef, InputHTMLAttributes } from 'react';

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
  error?: string;
};

export const Input = forwardRef<HTMLInputElement, InputProps>(
  function Input({ label, error, className = '', ...props }, ref) {
    return (
      <label className="grid gap-1.5 text-sm font-semibold text-ink/80">
        {label ? <span>{label}</span> : null}
        <input
          ref={ref}
          className={`w-full min-h-11 rounded-xl border border-primary/20 bg-surface/80 px-4 text-sm text-ink outline-none transition-all duration-300 placeholder:text-ink/30 hover:border-primary/40 focus:border-primary focus:bg-surface focus:ring-4 focus:ring-primary/10 shadow-inner ${className}`}
          {...props}
        />
        {error ? <span className="text-xs font-medium text-danger animate-fade-in">{error}</span> : null}
      </label>
    );
  },
);
