import { useEffect, useMemo, useState } from 'react';
import { ImagePlus, Pencil, Plus, Search, Shirt, Tags } from 'lucide-react';
import {
  createProduct,
  createProductVariants,
  getProducts,
  registerProductImage,
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

const emptyProductForm: ProductMutationRequest = {
  name: '',
  brand: '',
  description: '',
  status: 'PUBLISHED',
};

const emptyVariantForm: VariantMutationRequest = {
  skuCode: '',
  size: '',
  color: '',
  price: 0,
  status: 'ACTIVE',
};

export function CatalogManagement() {
  const [products, setProducts] = useState<Product[]>([]);
  const [search, setSearch] = useState('');
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null);
  const [productForm, setProductForm] = useState<ProductMutationRequest>(emptyProductForm);
  const [variantForm, setVariantForm] = useState<VariantMutationRequest>(emptyVariantForm);
  const [imageUrl, setImageUrl] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  const selectedProduct = products.find((product) => product.id === selectedProductId) ?? null;
  const filteredProducts = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    if (!keyword) return products;
    return products.filter((product) =>
      [product.name, product.brand, product.variants.map((variant) => variant.skuCode).join(' ')]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    );
  }, [products, search]);

  async function loadProducts() {
    setIsLoading(true);
    try {
      const page = await getProducts({ page: 1, size: 100 });
      setProducts(page.items);
    } catch {
      setProducts([]);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadProducts();
  }, []);

  function handleEdit(product: Product) {
    setSelectedProductId(product.id);
    setProductForm({
      name: product.name,
      brand: product.brand ?? '',
      description: product.description ?? '',
      status: product.status,
    });
    setMessage('');
  }

  function handleNewProduct() {
    setSelectedProductId(null);
    setProductForm(emptyProductForm);
    setVariantForm(emptyVariantForm);
    setImageUrl('');
    setMessage('');
  }

  async function handleSaveProduct(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const saved = selectedProductId
        ? await updateProduct(selectedProductId, productForm)
        : await createProduct(productForm);
      setSelectedProductId(saved.id);
      setMessage(`${saved.name} saved.`);
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
      await createProductVariants(selectedProductId, [{ ...variantForm, price: Number(variantForm.price) }]);
      setVariantForm(emptyVariantForm);
      setMessage('Variant added.');
      await loadProducts();
    } catch {
      setMessage('Variant could not be added.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleAddImage(event: React.FormEvent) {
    event.preventDefault();
    if (!selectedProductId || !imageUrl.trim()) return;
    setIsSaving(true);
    setMessage('');
    try {
      await registerProductImage(selectedProductId, {
        url: imageUrl.trim(),
        sortOrder: selectedProduct?.images.length ?? 0,
        isInstagramReady: true,
      });
      setImageUrl('');
      setMessage('Image registered.');
      await loadProducts();
    } catch {
      setMessage('Image could not be registered.');
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
              <p className="mt-1 text-xs text-ink/50">{products.length} published catalog records loaded.</p>
            </div>
            <div className="flex w-full gap-2 sm:w-auto">
              <label className="relative flex-1 sm:w-72">
                <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink/40" />
                <input
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
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
                ) : filteredProducts.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-5 py-8">
                      <EmptyState title="No products found" description="Create a product or adjust the search term." />
                    </td>
                  </tr>
                ) : (
                  filteredProducts.map((product) => {
                    const minPrice = product.variants.length
                      ? Math.min(...product.variants.map((variant) => Number(variant.price)))
                      : 0;
                    return (
                      <tr key={product.id} className="text-ink/80">
                        <td className="px-5 py-4">
                          <div className="flex items-center gap-3">
                            <div className="flex size-12 items-center justify-center overflow-hidden rounded-md border border-primary/15 bg-ink/5 text-primary">
                              {product.thumbnailUrl ? (
                                <img src={product.thumbnailUrl} alt={product.name} className="h-full w-full object-cover" />
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
                        <td className="px-5 py-4">{product.variants.length}</td>
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
        </Card>

        <div className="grid gap-5">
          <Card className="border-primary/20 bg-surface/95 p-5">
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
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Variant Matrix</h2>
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
                <Input
                  label="Size"
                  value={variantForm.size}
                  onChange={(event) => setVariantForm((current) => ({ ...current, size: event.target.value }))}
                />
                <Input
                  label="Color"
                  value={variantForm.color}
                  onChange={(event) => setVariantForm((current) => ({ ...current, color: event.target.value }))}
                />
              </div>
              <Button type="submit" variant="secondary" icon={<Tags size={16} />} disabled={!selectedProductId} isLoading={isSaving}>
                Add Variant
              </Button>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Product Media</h2>
            <form onSubmit={handleAddImage} className="mt-4 grid gap-4">
              <Input
                label="Image URL"
                value={imageUrl}
                onChange={(event) => setImageUrl(event.target.value)}
                placeholder="https://..."
              />
              <Button type="submit" variant="secondary" icon={<ImagePlus size={16} />} disabled={!selectedProductId} isLoading={isSaving}>
                Register Image
              </Button>
            </form>
            {message ? <p className="mt-4 text-sm text-ink/65">{message}</p> : null}
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
