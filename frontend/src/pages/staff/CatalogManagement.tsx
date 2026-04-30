import { PageScaffold } from '@/shared/ui/PageScaffold';

export function CatalogManagement() {
  return (
    <PageScaffold
      title="Catalog Management"
      viewCode="STAFF_001"
      purpose="Product and SKU creation shell with category assignment, media upload, variant matrix, and publish workflow."
      endpoints={['POST /staff/products', 'PUT /staff/products/{productId}', 'POST /staff/products/{productId}/variants', 'POST /staff/products/{productId}/images']}
    />
  );
}
