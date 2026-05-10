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
          <label className="text-xs font-semibold uppercase tracking-wide text-ink/55">
            {label}
          </label>
        ) : null}
        <div className="relative">
          <select
            ref={ref}
            className={`w-full appearance-none rounded-md border bg-surface px-3 py-2.5 pr-9 text-sm text-ink outline-none transition ${
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
            className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-ink/45"
          />
        </div>
        {error ? <p className="text-xs text-danger">{error}</p> : null}
      </div>
    );
  },
);

Select.displayName = 'Select';
