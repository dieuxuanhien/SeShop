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
          <label className="text-sm font-semibold text-ink/80">
            {label}
          </label>
        ) : null}
        <div className="relative">
          <select
            ref={ref}
            className={`w-full appearance-none min-h-11 rounded-xl border bg-surface/80 px-4 py-2.5 pr-10 text-sm text-ink outline-none transition-all duration-300 hover:border-primary/40 focus:bg-surface focus:ring-4 shadow-inner ${
              error
                ? 'border-danger focus:border-danger focus:ring-danger/10'
                : 'border-primary/20 focus:border-primary focus:ring-primary/10'
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
            size={16}
            className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-primary/70 transition-transform group-focus-within:rotate-180"
          />
        </div>
        {error ? <p className="text-xs font-medium text-danger animate-fade-in">{error}</p> : null}
      </div>
    );
  },
);

Select.displayName = 'Select';
