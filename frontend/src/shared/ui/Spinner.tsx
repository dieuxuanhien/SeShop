type SpinnerProps = {
  size?: 'sm' | 'md' | 'lg';
};

const sizeClasses = {
  sm: 'size-3 border-2',
  md: 'size-4 border-2',
  lg: 'size-6 border-[3px]',
};

export function Spinner({ size = 'md' }: SpinnerProps) {
  return (
    <span
      className={`inline-block animate-spin rounded-full border-primary/30 border-t-primary ${sizeClasses[size]}`}
      aria-label="Loading"
    />
  );
}
