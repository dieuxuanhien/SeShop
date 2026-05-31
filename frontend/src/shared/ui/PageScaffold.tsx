type PageScaffoldProps = {
  title: string;
  viewCode?: string;
  purpose?: string;
  endpoints?: string[];
  children?: React.ReactNode;
};

export function PageScaffold({ title, purpose, children }: PageScaffoldProps) {
  return (
    <div className="mx-auto grid w-full max-w-7xl gap-5">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="font-display mt-1 text-2xl font-semibold text-surface">{title}</h1>
          {purpose && (
            <p className="mt-2 text-sm text-surface/60">{purpose}</p>
          )}
        </div>
      </header>

      {children}
    </div>
  );
}
