import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';
import { getInstagramDrafts, type InstagramDraft } from '@/features/marketing/api/marketingApi';

export function InstagramDrafts() {
  const [drafts, setDrafts] = useState<InstagramDraft[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getInstagramDrafts()
      .then(setDrafts)
      .catch(() => setDrafts([]))
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <PageScaffold
      title="Instagram Compose & Draft Management"
      viewCode="STAFF_009"
      purpose="Manual-review Instagram draft workflow with product media, caption, hashtags, media ordering, and approval."
      endpoints={['POST /marketing/drafts', 'PUT /marketing/drafts/{draftId}', 'POST /marketing/drafts/{draftId}/submit-review', 'POST /marketing/drafts/{draftId}/approve']}
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Draft Gallery</h2>
              <p className="mt-1 text-xs text-ink/50">Filter by status, preview drafts, and push for approval.</p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary">New Draft</Button>
              <Button variant="secondary">Bulk Actions</Button>
            </div>
          </div>
          <div className="mt-4 grid gap-4 md:grid-cols-[repeat(3,minmax(0,1fr))_140px]">
            <Input label="Search" placeholder="Caption keywords" />
            <Select
              label="Status"
              options={[
                { label: 'All', value: 'ALL' },
                { label: 'Editing', value: 'EDITING' },
                { label: 'Review', value: 'READY_FOR_REVIEW' },
                { label: 'Approved', value: 'APPROVED' },
              ]}
            />
            <Select
              label="Sort"
              options={[
                { label: 'Newest', value: 'NEWEST' },
                { label: 'Last Edited', value: 'UPDATED' },
              ]}
            />
            <div className="flex items-end">
              <Button variant="secondary" className="w-full">Apply</Button>
            </div>
          </div>
          <div className="mt-4 grid gap-4 md:grid-cols-3">
            {isLoading ? (
              <p className="text-sm text-ink/60">Loading drafts...</p>
            ) : drafts.length === 0 ? (
              <p className="text-sm text-ink/60">No Instagram drafts returned by the API.</p>
            ) : drafts.map((draft) => (
              <div key={draft.id} className="rounded-md border border-primary/15 bg-ink/5 p-4">
                <div className="flex items-center justify-between">
                  <p className="text-xs font-semibold uppercase text-ink/50">DRF-{draft.id}</p>
                  <Badge variant={draft.status === 'APPROVED' ? 'success' : draft.status === 'READY_FOR_REVIEW' ? 'warning' : 'default'}>
                    {draft.status.split('_').join(' ')}
                  </Badge>
                </div>
                <div className="mt-3 h-24 rounded-md border border-dashed border-primary/30 bg-surface" />
                <p className="mt-3 text-sm font-semibold text-ink">{draft.caption ?? 'No caption'}</p>
                <p className="mt-1 text-xs text-ink/50">{draft.createdAt ? `Created ${new Date(draft.createdAt).toLocaleString()}` : 'No timestamp'}</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  <Button variant="secondary">Edit</Button>
                  <Button variant="secondary">Preview</Button>
                </div>
              </div>
            ))}
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_320px]">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Compose Draft</h2>
            <div className="mt-4 grid gap-4">
              <div className="grid gap-2">
                <label className="text-xs font-semibold uppercase tracking-wide text-ink/50">Caption</label>
                <textarea
                  className="min-h-28 rounded-md border border-primary/30 bg-surface px-3 py-2 text-sm text-ink outline-none"
                  placeholder="Write caption with hashtags, emojis, and call-to-action."
                />
              </div>
              <Input label="Hashtags" placeholder="#seshop #newarrival" />
              <Input label="Product Tag" placeholder="Search SKU or product name" />
              <div className="rounded-md border border-dashed border-primary/30 bg-ink/5 p-4 text-sm text-ink/60">
                Drag & drop media here (carousel supported)
              </div>
              <div className="flex flex-wrap gap-2">
                <Button variant="secondary">Save Draft</Button>
                <Button variant="secondary">Submit Review</Button>
                <Button variant="secondary">Approve</Button>
              </div>
            </div>
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Preview</h2>
            <div className="mt-4 rounded-md border border-dashed border-primary/30 bg-ink/5 p-4">
              <div className="mx-auto h-56 w-40 rounded-xl border border-primary/20 bg-surface" />
              <p className="mt-3 text-xs text-ink/50">Mobile preview with caption and tags.</p>
            </div>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
