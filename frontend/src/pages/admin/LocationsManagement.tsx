import { PageScaffold } from '@/shared/ui/PageScaffold';

export function LocationsManagement() {
  return (
    <PageScaffold
      title="Locations Management"
      viewCode="ADMIN_003"
      purpose="Store and storage location configuration shell for inventory operations and location status."
      endpoints={['GET /staff/inventory/balances']}
    />
  );
}
