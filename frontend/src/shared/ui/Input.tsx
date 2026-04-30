import { InputHTMLAttributes } from 'react';

type InputProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

export function Input({ label, error, className = '', ...props }: InputProps) {
  return (
    <label className="grid gap-1 text-sm font-medium text-slate-700">
      <span>{label}</span>
      <input
        className={`min-h-10 rounded-md border border-slate-300 bg-white px-3 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-blue-100 ${className}`}
        {...props}
      />
      {error ? <span className="text-xs font-normal text-danger">{error}</span> : null}
    </label>
  );
}
