import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { ChevronLeft, ChevronRight, ImagePlus, Link, Pencil, Plus, Search, Shirt, Tags, Trash2 } from 'lucide-react';
import {
  createProduct,
  createProductVariants,
  deleteProductVariant,
  getStaffProducts,
  registerProductImage,
  uploadProductImage,
  updateProduct,
  type ProductMutationRequest,
  type VariantMutationRequest,
} from '@/features/catalog/api/catalogApi';
import type { Product } from '@/entities/product/types';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { EmptyState } from '@/shared/ui/EmptyState';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';
import { env } from '@/shared/config/env';

const emptyProductForm: ProductMutationRequest = {
  name: '',
  brand: '',
  description: '',
  status: 'PUBLISHED',
};

const emptyVariantForm: VariantMutationRequest & { attributesStr?: string } = {
  skuCode: '',
  attributesStr: '{}',
  price: 0,
  status: 'ACTIVE',
};

const catalogPageSize = 12;

function resolveCatalogMediaUrl(url?: string) {
  if (!url) return undefined;
  if (/^(https?:|data:|blob:)/i.test(url)) return url;
  if (!url.startsWith('/uploads/')) return url;

  try {
    return `${new URL(env.apiBaseUrl, window.location.origin).origin}${url}`;
  } catch {
    return url;
  }
}

function messageFromError(error: unknown, fallback: string) {
  if (error && typeof error === 'object' && 'message' in error && typeof error.message === 'string') {
    return error.message;
  }
  return fallback;
}

export function CatalogManagement() {
  const [products, setProducts] = useState<Product[]>([]);
  const [search, setSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [pageInfo, setPageInfo] = useState({
    page: 1,
    size: catalogPageSize,
    totalElements: 0,
    totalPages: 1,
  });
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null);
  const [productForm, setProductForm] = useState<ProductMutationRequest>(emptyProductForm);
  const [variantForm, setVariantForm] = useState<VariantMutationRequest & { attributesStr?: string }>(emptyVariantForm);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [imageUrl, setImageUrl] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');
  const uploadInputRef = useRef<HTMLInputElement>(null);

  const selectedProduct = products.find((product) => product.id === selectedProductId) ?? null;
  const selectedVariants = useMemo(
    () => (selectedProduct?.variants ?? []).filter((variant) => variant.status !== 'INACTIVE'),
    [selectedProduct?.variants],
  );
  const selectedImages = useMemo(
    () => [...(selectedProduct?.images ?? [])].sort((a, b) => a.sortOrder - b.sortOrder),
    [selectedProduct?.images],
  );

  const loadProducts = useCallback(async () => {
    setIsLoading(true);
    try {
      const page = await getStaffProducts({
        page: currentPage,
        size: catalogPageSize,
        search: search.trim() || undefined,
      });
      setProducts(page.items);
      setPageInfo({
        page: page.page,
        size: page.size,
        totalElements: page.totalElements,
        totalPages: Math.max(page.totalPages, 1),
      });
    } catch {
      setProducts([]);
      setPageInfo((current) => ({ ...current, totalElements: 0, totalPages: 1 }));
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, search]);

  function replaceProduct(updatedProduct: Product) {
    setProducts((current) => {
      const exists = current.some((product) => product.id === updatedProduct.id);
      return exists
        ? current.map((product) => (product.id === updatedProduct.id ? updatedProduct : product))
        : [updatedProduct, ...current];
    });
  }

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  function handleEdit(product: Product) {
    setSelectedProductId(product.id);
    setProductForm({
      name: product.name,
      brand: product.brand ?? '',
      description: product.description ?? '',
      status: product.status,
    });
    setMessage('');
    document.getElementById('product-form')?.scrollIntoView({ behavior: 'smooth' });
  }

  function handleNewProduct() {
    setSelectedProductId(null);
    setProductForm(emptyProductForm);
    setVariantForm(emptyVariantForm);
    setUploadFile(null);
    setImageUrl('');
    setMessage('');
    document.getElementById('product-form')?.scrollIntoView({ behavior: 'smooth' });
  }

  async function handleSaveProduct(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const saved = selectedProductId
        ? await updateProduct(selectedProductId, productForm)
        : await createProduct(productForm);
      replaceProduct(saved);
      setSelectedProductId(saved.id);
      setMessage(`${saved.name} saved.`);
      if (!selectedProductId) {
        setCurrentPage(1);
      }
      await loadProducts();
    } catch {
      setMessage('Product could not be saved. Check required fields and try again.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleAddVariant(event: React.FormEvent) {
    event.preventDefault();
    if (!selectedProductId) return;
    setIsSaving(true);
    setMessage('');
    try {
      let attrs = {};
      if (variantForm.attributesStr && variantForm.attributesStr.trim()) {
        attrs = JSON.parse(variantForm.attributesStr);
      }
      const updated = await createProductVariants(selectedProductId, [{ 
        ...variantForm, 
        attributes: attrs,
        price: Number(variantForm.price) 
      }]);
      replaceProduct(updated);
      setVariantForm(emptyVariantForm);
      setMessage('Variant added.');
      await loadProducts();
    } catch (error) {
      setMessage(messageFromError(error, 'Variant could not be added.'));
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDeleteVariant(variantId: number) {
    if (!selectedProductId) return;
    const variant = selectedVariants.find((item) => item.id === variantId);
    if (!window.confirm(`Delete variant ${variant?.skuCode ?? variantId}? Existing order history will be preserved.`)) {
      return;
    }
    setIsSaving(true);
    setMessage('');
    try {
      const updated = await deleteProductVariant(selectedProductId, variantId);
      replaceProduct(updated);
      setMessage('Variant deleted from active catalog.');
      await loadProducts();
    } catch (error) {
      setMessage(messageFromError(error, 'Variant could not be deleted.'));
    } finally {
      setIsSaving(false);
    }
  }

  async function handleAddImage(event: React.FormEvent) {
    event.preventDefault();
    if (!selectedProductId || !uploadFile) return;
    setIsSaving(true);
    setMessage('');
    try {
      const updated = await uploadProductImage(selectedProductId, uploadFile);
      replaceProduct(updated);
      setUploadFile(null);
      if (uploadInputRef.current) uploadInputRef.current.value = '';
      setMessage('Image uploaded successfully.');
      await loadProducts();
    } catch (error) {
      setMessage(messageFromError(error, 'Image could not be uploaded.'));
    } finally {
      setIsSaving(false);
    }
  }

  async function handleRegisterImage(event: React.FormEvent) {
    event.preventDefault();
    if (!selectedProductId || !imageUrl.trim()) return;
    setIsSaving(true);
    setMessage('');
    try {
      const updated = await registerProductImage(selectedProductId, {
        url: imageUrl.trim(),
        sortOrder: selectedImages.length,
        isInstagramReady: true,
      });
      replaceProduct(updated);
      setImageUrl('');
      setMessage('Image URL added.');
      await loadProducts();
    } catch {
      setMessage('Image URL could not be added.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <PageScaffold
      title="Catalog Management"
      viewCode="STAFF_001"
      purpose="Create products, maintain variants, and prepare media for the storefront."
    >
      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_380px]">
        <Card className="overflow-hidden border-primary/20 bg-surface/95">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-primary/15 p-5">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Product Directory</h2>
              <p className="mt-1 text-xs text-ink/50">{pageInfo.totalElements} catalog records found.</p>
            </div>
            <div className="flex w-full gap-2 sm:w-auto">
              <label className="relative flex-1 sm:w-72">
                <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink/40" />
                <input
                  value={search}
                  onChange={(event) => {
                    setSearch(event.target.value);
                    setCurrentPage(1);
                  }}
                  placeholder="Search products or SKUs"
                  className="min-h-10 w-full rounded-md border border-primary/25 bg-surface pl-9 pr-3 text-sm text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                />
              </label>
              <Button type="button" icon={<Plus size={16} />} onClick={handleNewProduct}>
                New
              </Button>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-ink/[0.03] text-xs uppercase text-ink/50">
                <tr>
                  <th className="px-5 py-3">Product</th>
                  <th className="px-5 py-3">Variants</th>
                  <th className="px-5 py-3">Media</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10">
                {isLoading ? (
                  <tr>
                    <td colSpan={5} className="px-5 py-10 text-center text-sm text-ink/55">
                      Loading catalog...
                    </td>
                  </tr>
                ) : products.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-5 py-8">
                      <EmptyState title="No products found" description="Create a product or adjust the search term." />
                    </td>
                  </tr>
                ) : (
                  products.map((product) => {
                    const activeVariants = product.variants.filter((variant) => variant.status !== 'INACTIVE');
                    const minPrice = activeVariants.length
                      ? Math.min(...activeVariants.map((variant) => Number(variant.price)))
                      : 0;
                    const thumbnailUrl = resolveCatalogMediaUrl(product.thumbnailUrl);
                    return (
                      <tr
                        key={product.id}
                        className={`text-ink/80 transition ${product.id === selectedProductId ? 'bg-primary/10' : 'hover:bg-ink/[0.02]'}`}
                      >
                        <td className="px-5 py-4">
                          <div className="flex items-center gap-3">
                            <div className="flex size-12 items-center justify-center overflow-hidden rounded-md border border-primary/15 bg-ink/5 text-primary">
                              {thumbnailUrl ? (
                                <img src={thumbnailUrl} alt={product.name} className="h-full w-full object-cover" />
                              ) : (
                                <Shirt size={18} />
                              )}
                            </div>
                            <div>
                              <p className="font-semibold text-ink">{product.name}</p>
                              <p className="text-xs text-ink/55">{product.brand || 'Unbranded'} | {formatCurrency(minPrice)}</p>
                            </div>
                          </div>
                        </td>
                        <td className="px-5 py-4">{activeVariants.length}</td>
                        <td className="px-5 py-4">{product.images.length}</td>
                        <td className="px-5 py-4">
                          <Badge variant={product.status === 'PUBLISHED' ? 'success' : product.status === 'ARCHIVED' ? 'danger' : 'warning'}>
                            {product.status}
                          </Badge>
                        </td>
                        <td className="px-5 py-4 text-right">
                          <Button variant="secondary" size="sm" icon={<Pencil size={14} />} onClick={() => handleEdit(product)}>
                            Edit
                          </Button>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
          <div className="flex flex-wrap items-center justify-between gap-3 border-t border-primary/10 px-5 py-4 text-sm text-ink/60">
            <span>
              Page {pageInfo.page} of {pageInfo.totalPages} · {pageInfo.totalElements} products
            </span>
            <div className="flex gap-2">
              <Button
                type="button"
                variant="secondary"
                size="sm"
                icon={<ChevronLeft size={14} />}
                disabled={isLoading || pageInfo.page <= 1}
                onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
              >
                Prev
              </Button>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                icon={<ChevronRight size={14} />}
                disabled={isLoading || pageInfo.page >= pageInfo.totalPages}
                onClick={() => setCurrentPage((page) => Math.min(pageInfo.totalPages, page + 1))}
              >
                Next
              </Button>
            </div>
          </div>
        </Card>

        <div className="grid gap-5">
          <Card id="product-form" className="border-primary/20 bg-surface/95 p-5">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">
                {selectedProductId ? 'Edit Product' : 'Create Product'}
              </h2>
              {selectedProductId ? <Badge variant="info">ID {selectedProductId}</Badge> : null}
            </div>
            <form onSubmit={handleSaveProduct} className="grid gap-4">
              <Input
                label="Name"
                value={productForm.name}
                onChange={(event) => setProductForm((current) => ({ ...current, name: event.target.value }))}
                required
              />
              <Input
                label="Brand"
                value={productForm.brand}
                onChange={(event) => setProductForm((current) => ({ ...current, brand: event.target.value }))}
              />
              <label className="grid gap-1 text-sm font-medium text-ink">
                <span>Description</span>
                <textarea
                  value={productForm.description}
                  onChange={(event) => setProductForm((current) => ({ ...current, description: event.target.value }))}
                  className="min-h-24 rounded-md border border-primary/30 bg-surface px-3 py-2 text-sm text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                />
              </label>
              <Select
                label="Status"
                value={productForm.status}
                onChange={(event) => setProductForm((current) => ({ ...current, status: event.target.value as ProductMutationRequest['status'] }))}
                options={[
                  { label: 'Published', value: 'PUBLISHED' },
                  { label: 'Draft', value: 'DRAFT' },
                  { label: 'Archived', value: 'ARCHIVED' },
                ]}
              />
              <Button type="submit" icon={<Plus size={16} />} isLoading={isSaving}>
                Save Product
              </Button>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Variant Matrix</h2>
              <Badge variant="info">{selectedVariants.length} active variants</Badge>
            </div>
            {selectedProduct ? (
              <div className="mt-4 overflow-hidden rounded-md border border-primary/15">
                <table className="min-w-full text-left text-xs">
                  <thead className="bg-ink/[0.03] uppercase text-ink/50">
                    <tr>
                      <th className="px-3 py-2">SKU</th>
                      <th className="px-3 py-2">Option</th>
                      <th className="px-3 py-2">Status</th>
                      <th className="px-3 py-2 text-right">Price</th>
                      <th className="px-3 py-2 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-primary/10">
                    {selectedVariants.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="px-3 py-4 text-center text-ink/55">
                          No variants yet.
                        </td>
                      </tr>
                    ) : (
                      selectedVariants.map((variant) => (
                        <tr key={variant.id}>
                          <td className="px-3 py-2 font-semibold text-ink">{variant.skuCode}</td>
                          <td className="px-3 py-2 text-ink/65">
                            {variant.attributes && Object.keys(variant.attributes).length > 0 
                              ? Object.entries(variant.attributes).map(([k, v]) => `${k}: ${v}`).join(', ') 
                              : 'Default'}
                          </td>
                          <td className="px-3 py-2">
                            <Badge variant={variant.status === 'ACTIVE' ? 'success' : 'default'}>{variant.status}</Badge>
                          </td>
                          <td className="px-3 py-2 text-right font-medium text-ink">{formatCurrency(variant.price)}</td>
                          <td className="px-3 py-2 text-right">
                            <Button
                              type="button"
                              variant="danger"
                              size="sm"
                              icon={<Trash2 size={13} />}
                              disabled={variant.status === 'INACTIVE' || isSaving}
                              onClick={() => handleDeleteVariant(variant.id)}
                            >
                              Delete
                            </Button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="mt-4 rounded-md border border-dashed border-primary/25 px-3 py-4 text-center text-sm text-ink/55">
                Select or save a product first.
              </p>
            )}
            <form onSubmit={handleAddVariant} className="mt-4 grid gap-4">
              <div className="grid gap-3 sm:grid-cols-2">
                <Input
                  label="SKU"
                  value={variantForm.skuCode}
                  onChange={(event) => setVariantForm((current) => ({ ...current, skuCode: event.target.value }))}
                  required
                />
                <Input
                  label="Price"
                  type="number"
                  min={1}
                  value={variantForm.price}
                  onChange={(event) => setVariantForm((current) => ({ ...current, price: Number(event.target.value) }))}
                  required
                />
                <label className="grid gap-1 text-sm font-medium text-ink sm:col-span-2">
                  <span>Attributes (JSON)</span>
                  <textarea
                    value={variantForm.attributesStr}
                    onChange={(event) => setVariantForm((current) => ({ ...current, attributesStr: event.target.value }))}
                    placeholder='{"size": "M", "color": "Red"}'
                    className="min-h-16 rounded-md border border-primary/30 bg-surface px-3 py-2 text-sm text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                  />
                </label>
                <Select
                  label="Status"
                  value={variantForm.status}
                  onChange={(event) => setVariantForm((current) => ({ ...current, status: event.target.value as VariantMutationRequest['status'] }))}
                  options={[
                    { label: 'Active', value: 'ACTIVE' },
                    { label: 'Inactive', value: 'INACTIVE' },
                  ]}
                  className="sm:col-span-2"
                />
              </div>
              <Button type="submit" variant="secondary" icon={<Tags size={16} />} disabled={!selectedProductId} isLoading={isSaving}>
                Add Variant
              </Button>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Product Media</h2>
              <Badge variant="info">{selectedImages.length} images</Badge>
            </div>
            {selectedProduct ? (
              <div className="mt-4 grid grid-cols-2 gap-3">
                {selectedImages.length === 0 ? (
                  <p className="col-span-2 rounded-md border border-dashed border-primary/25 px-3 py-4 text-center text-sm text-ink/55">
                    No media yet.
                  </p>
                ) : (
                  selectedImages.map((image, index) => {
                    const mediaUrl = resolveCatalogMediaUrl(image.url);
                    return (
                      <a
                        key={image.id}
                        href={mediaUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="group overflow-hidden rounded-md border border-primary/15 bg-ink/5"
                      >
                        {mediaUrl ? (
                          <img
                            src={mediaUrl}
                            alt={image.altText ?? `${selectedProduct.name} image ${index + 1}`}
                            className="aspect-square w-full object-cover transition group-hover:scale-105"
                          />
                        ) : (
                          <div className="flex aspect-square items-center justify-center text-xs text-ink/45">No preview</div>
                        )}
                        <div className="flex items-center justify-between px-2 py-1.5 text-xs text-ink/60">
                          <span>#{image.sortOrder + 1}</span>
                          {image.isInstagramReady ? <Badge variant="success">Ready</Badge> : null}
                        </div>
                      </a>
                    );
                  })
                )}
              </div>
            ) : (
              <p className="mt-4 rounded-md border border-dashed border-primary/25 px-3 py-4 text-center text-sm text-ink/55">
                Select or save a product first.
              </p>
            )}
            <form onSubmit={handleAddImage} className="mt-4 grid gap-4">
              <label className="grid gap-1 text-sm font-medium text-ink">
                <span>Upload Product Image</span>
                <input
                  ref={uploadInputRef}
                  type="file"
                  accept="image/*"
                  onChange={(event) => setUploadFile(event.target.files?.[0] || null)}
                  className="w-full rounded-md border border-primary/30 bg-surface px-3 py-2 text-sm text-ink outline-none file:mr-4 file:rounded-md file:border-0 file:bg-primary file:px-4 file:py-1 file:text-xs file:font-semibold file:text-surface"
                />
              </label>
              <Button type="submit" variant="secondary" icon={<ImagePlus size={16} />} disabled={!selectedProductId || !uploadFile} isLoading={isSaving}>
                Upload Image
              </Button>
            </form>
            <form onSubmit={handleRegisterImage} className="mt-4 grid gap-3 border-t border-primary/10 pt-4">
              <Input
                label="Image URL"
                value={imageUrl}
                onChange={(event) => setImageUrl(event.target.value)}
                placeholder="https://..."
              />
              <Button
                type="submit"
                variant="secondary"
                icon={<Link size={16} />}
                disabled={!selectedProductId || !imageUrl.trim()}
                isLoading={isSaving}
              >
                Add URL
              </Button>
            </form>
            {message ? <p className="mt-4 text-sm text-ink/65">{message}</p> : null}
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
