type PageScaffoldProps = {
  title: string;
  viewCode?: string;
  purpose?: string;
  endpoints?: string[];
  children?: React.ReactNode;
};

export function PageScaffold({ title, viewCode, purpose, endpoints, children }: PageScaffoldProps) {
  return (
    <div className="mx-auto grid w-full max-w-7xl gap-5">
      <header className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="font-display mt-1 text-2xl font-semibold text-surface">{title}</h1>
          {(viewCode || purpose) && (
            <div className="mt-2 text-sm text-surface/80">
              {viewCode && <span className="font-medium mr-2">[{viewCode}]</span>}
              {purpose && <span>{purpose}</span>}
            </div>
          )}
          {endpoints && endpoints.length > 0 && (
            <div className="mt-1 text-xs text-surface/60">
              Endpoints: {endpoints.join(', ')}
            </div>
          )}
        </div>
      </header>

      {children}
    </div>
  );
}
