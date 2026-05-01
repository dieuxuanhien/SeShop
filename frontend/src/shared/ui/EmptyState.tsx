type EmptyStateProps = {
  title: string;
  description?: string;
};

export function EmptyState({ title, description }: EmptyStateProps) {
  return (
    <div className="rounded-card border border-dashed border-primary/40 bg-surface p-8 text-center">
      <h2 className="font-display text-base font-semibold text-ink">{title}</h2>
      {description ? <p className="mt-2 text-sm text-ink/70">{description}</p> : null}
    </div>
  );
}
