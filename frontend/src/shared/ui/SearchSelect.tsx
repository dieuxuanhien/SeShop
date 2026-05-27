import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Loader2 } from 'lucide-react';

interface Option {
  value: string;
  label: string;
}

interface SearchSelectProps {
  label?: string;
  value: string;
  onChange: (value: string) => void;
  options: Option[];
  placeholder?: string;
  isLoading?: boolean;
  required?: boolean;
  disabled?: boolean;
  className?: string;
}

export function SearchSelect({
  label,
  value,
  onChange,
  options,
  placeholder = 'Select an option...',
  isLoading = false,
  required = false,
  disabled = false,
  className = '',
}: SearchSelectProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [search, setSearch] = useState('');
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const selectedOption = options.find((o) => o.value === value);

  const filteredOptions = options.filter((o) =>
    o.label.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className={`flex flex-col gap-1.5 ${className}`} ref={wrapperRef}>
      {label && (
        <label className="text-sm font-medium text-ink">
          {label}
          {required && <span className="ml-1 text-danger">*</span>}
        </label>
      )}
      <div className="relative">
        <button
          type="button"
          className="flex h-10 w-full items-center justify-between rounded-md border border-primary/20 bg-surface px-3 py-2 text-sm text-ink outline-none transition-colors hover:border-primary focus:border-primary disabled:cursor-not-allowed disabled:opacity-50"
          onClick={() => !disabled && setIsOpen(!isOpen)}
          disabled={disabled}
        >
          <span className="truncate">
            {selectedOption ? selectedOption.label : placeholder}
          </span>
          {isLoading ? (
            <Loader2 size={16} className="animate-spin text-ink/50" />
          ) : (
            <ChevronDown size={16} className="text-ink/50" />
          )}
        </button>
        {isOpen && (
          <div className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md border border-primary/20 bg-surface py-1 shadow-lg">
            <div className="sticky top-0 bg-surface px-2 py-1.5">
              <input
                type="text"
                className="w-full rounded-md border border-primary/20 bg-surface px-2 py-1.5 text-sm outline-none focus:border-primary"
                placeholder="Search..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                onClick={(e) => e.stopPropagation()}
              />
            </div>
            {filteredOptions.length === 0 ? (
              <div className="px-3 py-2 text-sm text-ink/50">No results found</div>
            ) : (
              filteredOptions.map((option) => (
                <button
                  key={option.value}
                  type="button"
                  className={`w-full px-3 py-2 text-left text-sm hover:bg-primary/5 ${
                    option.value === value ? 'bg-primary/10 font-medium text-primary' : 'text-ink'
                  }`}
                  onClick={() => {
                    onChange(option.value);
                    setIsOpen(false);
                    setSearch('');
                  }}
                >
                  {option.label}
                </button>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
}
