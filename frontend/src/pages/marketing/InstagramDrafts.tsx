import { useEffect, useMemo, useState } from 'react';
import { ChevronLeft, ChevronRight, Images } from 'lucide-react';
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
  publishInstagramDraft,
  updateInstagramDraft,
  type InstagramDraft,
} from '@/features/marketing/api/marketingApi';
import { getProductById, getProducts, uploadProductImage } from '@/features/catalog/api/catalogApi';
import type { Product } from '@/entities/product/types';
import { env } from '@/shared/config/env';

function resolveMarketingMediaUrl(url?: string) {
  if (!url) return undefined;
  if (/^(https?:|data:|blob:)/i.test(url)) return url;
  if (!url.startsWith('/uploads/')) return url;

  try {
    return `${new URL(env.apiBaseUrl, window.location.origin).origin}${url}`;
  } catch {
    return url;
  }
}

function splitMediaOrder(value: string) {
  return value
    .split(/\r?\n|,/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function productMediaLinks(product: Product) {
  return [...(product.images ?? [])]
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map((image) => resolveMarketingMediaUrl(image.url))
    .filter((url): url is string => Boolean(url));
}

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
  const [selectedDraftStatus, setSelectedDraftStatus] = useState<string>('DRAFT');
  const [products, setProducts] = useState<Product[]>([]);
  const [productSearch, setProductSearch] = useState('');
  const [isProductSearchLoading, setIsProductSearchLoading] = useState(false);
  const [previewIndex, setPreviewIndex] = useState(0);

  const previewMedia = useMemo(
    () => splitMediaOrder(mediaOrder).map(resolveMarketingMediaUrl).filter((url): url is string => Boolean(url)),
    [mediaOrder],
  );

  function applyProductDefaults(product: Product, overwriteText = true) {
    setProductSearch(product.name);
    if (overwriteText) {
      setCaption(`${product.name}\n\n${product.description || ''}`);
      setHashtags(`#${(product.brand || 'SeShop').replace(/\s+/g, '')} #fashion`);
    }
    const links = productMediaLinks(product);
    if (links.length > 0) {
      setMediaOrder(links.join(', '));
      setPreviewIndex(0);
    }
  }

  useEffect(() => {
    let isCurrent = true;
    const timer = window.setTimeout(() => {
      setIsProductSearchLoading(true);
      getProducts({ page: 1, size: 20, search: productSearch.trim() || undefined })
        .then((page) => {
          if (isCurrent) {
            setProducts(page.items);
          }
        })
        .catch(() => {
          if (isCurrent) {
            setProducts([]);
          }
        })
        .finally(() => {
          if (isCurrent) {
            setIsProductSearchLoading(false);
          }
        });
    }, 250);

    return () => {
      isCurrent = false;
      window.clearTimeout(timer);
    };
  }, [productSearch]);

  useEffect(() => {
    if (!selectedDraftId && productId > 0) {
      getProductById(productId)
        .then((product) => {
          if (product) {
            applyProductDefaults(product);
          }
        })
        .catch(() => {});
    }
  }, [productId, selectedDraftId]);

  useEffect(() => {
    if (previewIndex >= previewMedia.length) {
      setPreviewIndex(0);
    }
  }, [previewIndex, previewMedia.length]);

  useEffect(() => {
    if (previewMedia.length <= 1) return undefined;
    const timer = window.setInterval(() => {
      setPreviewIndex((index) => (index + 1) % previewMedia.length);
    }, 3500);
    return () => window.clearInterval(timer);
  }, [previewMedia.length]);

  async function loadProductDetails() {
    if (!productId) return;
    try {
      const product = await getProductById(productId);
      if (product) {
        applyProductDefaults(product);
        setMessage('Product details loaded.');
      }
    } catch {
      setMessage('Could not load product details.');
    }
  }

  async function handleImageUpload(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file || !productId) return;
    setIsSaving(true);
    setMessage('');
    try {
      const product = await uploadProductImage(productId, file);
      const newImageUrl = product.images?.[product.images.length - 1]?.url;
      const resolvedUrl = resolveMarketingMediaUrl(newImageUrl);
      if (resolvedUrl) {
        const currentMedia = splitMediaOrder(mediaOrder);
        currentMedia.push(resolvedUrl);
        setMediaOrder(currentMedia.join(', '));
        setMessage('Image uploaded and URL added successfully.');
      } else {
        setMessage('Image uploaded but URL could not be retrieved.');
      }
    } catch {
      setMessage('Image could not be uploaded.');
    } finally {
      setIsSaving(false);
      event.target.value = '';
    }
  }

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
    setSelectedDraftStatus(draft.status);
    setProductId(draft.productId);
    const draftMedia = draft.mediaOrder ?? [];
    getProductById(draft.productId)
      .then((product) => {
        setProductSearch(product?.name ?? '');
        if (product && draftMedia.length === 0) {
          applyProductDefaults(product, false);
        }
      })
      .catch(() => setProductSearch(''));
    setCaption(draft.caption ?? '');
    setHashtags(draft.hashtags ?? '');
    setMediaOrder(draftMedia.map(resolveMarketingMediaUrl).filter(Boolean).join(', '));
    setPreviewIndex(0);
    setMessage('');
  }

  function handleNewDraft() {
    setSelectedDraftId(null);
    setSelectedDraftStatus('DRAFT');
    setProductId(0);
    setProductSearch('');
    setCaption('');
    setHashtags('');
    setMediaOrder('');
    setPreviewIndex(0);
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
      mediaOrder: splitMediaOrder(mediaOrder),
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
      setSelectedDraftStatus('APPROVED');
      setMessage('Draft approved.');
      loadDrafts();
    } catch {
      setMessage('Draft could not be approved.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handlePublish() {
    if (!selectedDraftId) return;
    setIsSaving(true);
    setMessage('');
    try {
      const published = await publishInstagramDraft(selectedDraftId);
      setSelectedDraftStatus(published.status);
      setMessage(
        published.instagramPermalink
          ? `Draft published to Instagram: ${published.instagramPermalink}`
          : 'Draft published to Instagram.',
      );
      loadDrafts();
    } catch {
      setMessage('Draft could not be published to Instagram.');
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
                        {draft.status === 'PUBLISHED' ? 'PUBLISHED' : draft.status.split('_').join(' ')}
                  </Badge>
                </div>
                <div className="mt-3 h-24 rounded-md border border-dashed border-primary/30 bg-surface overflow-hidden">
                  {draft.mediaOrder && draft.mediaOrder.length > 0 ? (
                    <img src={resolveMarketingMediaUrl(draft.mediaOrder[0])} alt="Draft media" className="h-full w-full object-cover" />
                  ) : (
                    <span className="flex h-full w-full items-center justify-center text-xs text-ink/40">No Media</span>
                  )}
                </div>
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
              <div className="grid gap-3 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_140px] md:items-end">
                <Input
                  label="Find Product"
                  value={productSearch}
                  onChange={(event) => setProductSearch(event.target.value)}
                  placeholder="Search by product name"
                />
                <Select
                  label={isProductSearchLoading ? 'Product (Loading)' : 'Product'}
                  value={productId ? String(productId) : ''}
                  onChange={(event) => setProductId(event.target.value ? Number(event.target.value) : 0)}
                  options={[
                    { label: products.length ? 'Select product' : 'No products loaded', value: '' },
                    ...products.map((product) => ({
                      label: `${product.name}${product.brand ? ` - ${product.brand}` : ''}`,
                      value: String(product.id),
                    })),
                  ]}
                  required
                />
                <Button type="button" variant="secondary" onClick={loadProductDetails} disabled={!productId}>
                  Load Details
                </Button>
              </div>
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
              <div className="grid gap-2">
                <label className="text-xs font-semibold uppercase tracking-wide text-ink/50">Upload Local Image</label>
                <div className="flex items-center gap-2">
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageUpload}
                    disabled={!productId}
                    className="block w-full text-sm text-ink/70 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-primary/10 file:text-primary hover:file:bg-primary/20 cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
                  />
                  {!productId && <span className="text-xs text-ink/40">Select a product first to enable upload</span>}
                </div>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button type="submit" variant="secondary" isLoading={isSaving}>Save Draft</Button>
                <Button type="button" variant="secondary" onClick={handleSubmitReview} disabled={!selectedDraftId} isLoading={isSaving}>Submit Review</Button>
                <Button type="button" variant="secondary" onClick={handleApprove} disabled={!selectedDraftId} isLoading={isSaving}>Approve</Button>
                <Button type="button" onClick={handlePublish} disabled={!selectedDraftId} isLoading={isSaving}>Publish to Instagram</Button>
              </div>
            </form>
            {message ? <p className="mt-4 text-sm text-ink/65">{message}</p> : null}
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Preview</h2>
            <div className="mt-4 rounded-md border border-dashed border-primary/30 bg-ink/5 p-4">
              <div className="relative mx-auto aspect-[4/5] w-full max-w-64 overflow-hidden rounded-xl border border-primary/20 bg-surface">
                {previewMedia.length > 0 ? (
                  <img
                    src={previewMedia[previewIndex]}
                    alt={`Instagram preview ${previewIndex + 1}`}
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-ink/40">
                    <Images size={28} />
                  </div>
                )}
                {previewMedia.length > 1 ? (
                  <>
                    <button
                      type="button"
                      aria-label="Previous media"
                      className="absolute left-2 top-1/2 flex size-8 -translate-y-1/2 items-center justify-center rounded-full bg-ink/70 text-surface"
                      onClick={() => setPreviewIndex((index) => (index - 1 + previewMedia.length) % previewMedia.length)}
                    >
                      <ChevronLeft size={16} />
                    </button>
                    <button
                      type="button"
                      aria-label="Next media"
                      className="absolute right-2 top-1/2 flex size-8 -translate-y-1/2 items-center justify-center rounded-full bg-ink/70 text-surface"
                      onClick={() => setPreviewIndex((index) => (index + 1) % previewMedia.length)}
                    >
                      <ChevronRight size={16} />
                    </button>
                    <div className="absolute bottom-2 left-0 right-0 flex justify-center gap-1">
                      {previewMedia.map((url, index) => (
                        <button
                          key={`${url}-${index}`}
                          type="button"
                          aria-label={`Preview media ${index + 1}`}
                          className={`size-2 rounded-full ${index === previewIndex ? 'bg-primary' : 'bg-surface/70'}`}
                          onClick={() => setPreviewIndex(index)}
                        />
                      ))}
                    </div>
                  </>
                ) : null}
              </div>
              {previewMedia.length > 0 ? (
                <p className="mt-2 text-center text-xs text-ink/45">
                  {previewIndex + 1} / {previewMedia.length}
                </p>
              ) : null}
              <p className="mt-3 text-sm text-ink/70">{caption || 'Caption preview appears here.'}</p>
              <p className="mt-1 text-xs text-primary">{hashtags}</p>
            </div>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
