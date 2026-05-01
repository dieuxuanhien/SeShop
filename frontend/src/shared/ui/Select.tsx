import { forwardRef } from 'react';
import { ChevronDown } from 'lucide-react';

type SelectOption = {
  label: string;
  value: string;
};

type SelectProps = {
  label?: string;
  options: SelectOption[];
  error?: string;
  className?: string;
} & Omit<React.SelectHTMLAttributes<HTMLSelectElement>, 'children'>;

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, options, error, className = '', ...rest }, ref) => {
    return (
      <div className={`grid gap-1.5 ${className}`}>
        {label ? (
          <label className="text-xs font-semibold uppercase tracking-wide text-surface/70">
            {label}
          </label>
        ) : null}
        <div className="relative">
          <select
            ref={ref}
            className={`w-full appearance-none rounded-md border bg-ink/80 px-3 py-2.5 pr-9 text-sm text-surface outline-none transition ${
              error
                ? 'border-danger focus:ring-1 focus:ring-danger'
                : 'border-primary/20 focus:border-primary focus:ring-1 focus:ring-primary/30'
            }`}
            {...rest}
          >
            {options.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          <ChevronDown
            size={14}
            className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-surface/50"
          />
        </div>
        {error ? <p className="text-xs text-danger">{error}</p> : null}
      </div>
    );
  },
);

Select.displayName = 'Select';
