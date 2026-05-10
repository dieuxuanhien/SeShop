import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';
import {
  approveInstagramDraft,
  createInstagramDraft,
  getInstagramDrafts,
  submitInstagramDraftForReview,
  updateInstagramDraft,
  type InstagramDraft,
} from '@/features/marketing/api/marketingApi';

export function InstagramDrafts() {
  const [drafts, setDrafts] = useState<InstagramDraft[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [selectedDraftId, setSelectedDraftId] = useState<number | null>(null);
  const [productId, setProductId] = useState(0);
  const [caption, setCaption] = useState('');
  const [hashtags, setHashtags] = useState('');
  const [mediaOrder, setMediaOrder] = useState('');
  const [message, setMessage] = useState('');

  function loadDrafts() {
    setIsLoading(true);
    getInstagramDrafts()
      .then(setDrafts)
      .catch(() => setDrafts([]))
      .finally(() => setIsLoading(false));
  }

  useEffect(() => {
    loadDrafts();
  }, []);

  function handleSelectDraft(draft: InstagramDraft) {
    setSelectedDraftId(draft.id);
    setProductId(draft.productId);
    setCaption(draft.caption ?? '');
    setHashtags(draft.hashtags ?? '');
    setMediaOrder((draft.mediaOrder ?? []).join(', '));
    setMessage('');
  }

  function handleNewDraft() {
    setSelectedDraftId(null);
    setProductId(0);
    setCaption('');
    setHashtags('');
    setMediaOrder('');
    setMessage('');
  }

  async function handleSaveDraft(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    const payload = {
      productId,
      caption,
      hashtags,
      mediaOrder: mediaOrder.split(',').map((item) => item.trim()).filter(Boolean),
      status: 'DRAFT',
    };
    try {
      const saved = selectedDraftId
        ? await updateInstagramDraft(selectedDraftId, payload)
        : await createInstagramDraft(payload);
      setSelectedDraftId(saved.id);
      setMessage(`Draft ${saved.id} saved.`);
      loadDrafts();
    } catch {
      setMessage('Draft could not be saved.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleSubmitReview() {
    if (!selectedDraftId) return;
    setIsSaving(true);
    setMessage('');
    try {
      await submitInstagramDraftForReview(selectedDraftId);
      setMessage('Draft submitted for review.');
      loadDrafts();
    } catch {
      setMessage('Draft could not be submitted.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleApprove() {
    if (!selectedDraftId) return;
    setIsSaving(true);
    setMessage('');
    try {
      await approveInstagramDraft(selectedDraftId);
      setMessage('Draft approved.');
      loadDrafts();
    } catch {
      setMessage('Draft could not be approved.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <PageScaffold
      title="Instagram Compose & Draft Management"
      viewCode="STAFF_009"
      purpose="Create product-linked captions, prepare media order, and move drafts through review."
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Draft Gallery</h2>
              <p className="mt-1 text-xs text-ink/50">Filter by status, preview drafts, and push for approval.</p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={handleNewDraft}>New Draft</Button>
              <Button variant="secondary" onClick={loadDrafts}>Refresh</Button>
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
              <p className="text-sm text-ink/60">No Instagram drafts yet.</p>
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
                  <Button variant="secondary" onClick={() => handleSelectDraft(draft)}>Edit</Button>
                  <Button variant="secondary" onClick={() => handleSelectDraft(draft)}>Preview</Button>
                </div>
              </div>
            ))}
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_320px]">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">{selectedDraftId ? `Edit Draft ${selectedDraftId}` : 'Compose Draft'}</h2>
            <form onSubmit={handleSaveDraft} className="mt-4 grid gap-4">
              <Input label="Product ID" type="number" min={1} value={productId || ''} onChange={(event) => setProductId(Number(event.target.value))} required />
              <div className="grid gap-2">
                <label className="text-xs font-semibold uppercase tracking-wide text-ink/50">Caption</label>
                <textarea
                  value={caption}
                  onChange={(event) => setCaption(event.target.value)}
                  className="min-h-28 rounded-md border border-primary/30 bg-surface px-3 py-2 text-sm text-ink outline-none"
                  placeholder="Write caption with hashtags, emojis, and call-to-action."
                />
              </div>
              <Input label="Hashtags" placeholder="#seshop #newarrival" value={hashtags} onChange={(event) => setHashtags(event.target.value)} />
              <Input label="Media URLs" placeholder="https://image-1, https://image-2" value={mediaOrder} onChange={(event) => setMediaOrder(event.target.value)} />
              <div className="flex flex-wrap gap-2">
                <Button type="submit" variant="secondary" isLoading={isSaving}>Save Draft</Button>
                <Button type="button" variant="secondary" onClick={handleSubmitReview} disabled={!selectedDraftId} isLoading={isSaving}>Submit Review</Button>
                <Button type="button" variant="secondary" onClick={handleApprove} disabled={!selectedDraftId} isLoading={isSaving}>Approve</Button>
              </div>
            </form>
            {message ? <p className="mt-4 text-sm text-ink/65">{message}</p> : null}
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Preview</h2>
            <div className="mt-4 rounded-md border border-dashed border-primary/30 bg-ink/5 p-4">
              <div className="mx-auto h-56 w-40 rounded-xl border border-primary/20 bg-surface" />
              <p className="mt-3 text-sm text-ink/70">{caption || 'Caption preview appears here.'}</p>
              <p className="mt-1 text-xs text-primary">{hashtags}</p>
            </div>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
