import { Card } from './Card';

type PageScaffoldProps = {
  title: string;
  viewCode?: string;
  purpose: string;
  endpoints?: string[];
  children?: React.ReactNode;
};

export function PageScaffold({ title, viewCode, purpose, endpoints = [], children }: PageScaffoldProps) {
  return (
    <div className="grid gap-5">
      <header className="flex flex-wrap items-start justify-between gap-3">
        <div>
          {viewCode ? <p className="text-xs font-semibold uppercase tracking-wide text-primary">{viewCode}</p> : null}
          <h1 className="mt-1 text-2xl font-semibold text-ink">{title}</h1>
          <p className="mt-2 max-w-3xl text-sm text-slate-600">{purpose}</p>
        </div>
      </header>

      <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_320px]">
        <Card className="min-h-64 p-5">{children ?? <div className="h-full rounded-md bg-surface" />}</Card>
        <Card className="p-5">
          <h2 className="text-sm font-semibold text-ink">API surface</h2>
          <div className="mt-3 grid gap-2">
            {endpoints.length ? (
              endpoints.map((endpoint) => (
                <code key={endpoint} className="rounded bg-slate-100 px-2 py-1 text-xs text-slate-700">
                  {endpoint}
                </code>
              ))
            ) : (
              <p className="text-sm text-slate-600">No direct endpoint yet.</p>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
