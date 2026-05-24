import { PackageOpen, type LucideIcon } from 'lucide-react';

type EmptyStateProps = {
  title: string;
  description?: string;
  icon?: LucideIcon;
};

export function EmptyState({ title, description, icon: Icon = PackageOpen }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center rounded-card border border-dashed border-primary/40 bg-surface p-12 text-center transition-all duration-300 hover:border-primary/60">
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-primary/10 mb-6">
        <Icon className="h-8 w-8 text-primary" strokeWidth={1.5} />
      </div>
      <h2 className="font-display text-lg font-medium text-ink">{title}</h2>
      {description ? <p className="mt-2 text-sm text-ink/70 max-w-sm">{description}</p> : null}
    </div>
  );
}
