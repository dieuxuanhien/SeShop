import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Settings() {
  return (
    <PageScaffold
      title="System Configuration"
      viewCode="ADMIN_005"
      purpose="Global settings shell for payment, shipping, email, Instagram integration, and business metadata."
      endpoints={['GET /marketing/instagram/status']}
    />
  );
}
