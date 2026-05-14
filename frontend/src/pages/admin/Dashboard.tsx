import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getDashboardMetrics, getSystemStatus, type DashboardMetrics, type SystemStatus } from '@/features/admin/api/adminDashboardApi';

export function AdminDashboard() {
  const navigate = useNavigate();
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [systemStatus, setSystemStatus] = useState<SystemStatus[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        const [metricsData, statusData] = await Promise.all([
          getDashboardMetrics(),
          getSystemStatus(),
        ]);
        setMetrics(metricsData);
        setSystemStatus(statusData);
      } catch (error) {
        console.error('Failed to load dashboard metrics:', error);
      } finally {
        setIsLoading(false);
      }
    };
    
    loadDashboardData();
  }, []);

  if (isLoading) {
    return (
      <PageScaffold
        title="Dashboard Overview"
        viewCode="ADMIN_001"
        purpose="Monitor revenue, order load, inventory alerts, and platform health."
      >
        <Card className="border-primary/20 bg-surface/95 p-5 text-sm text-ink/60">Loading dashboard...</Card>
      </PageScaffold>
    );
  }

  if (!metrics) {
    return (
      <PageScaffold
        title="Dashboard Overview"
        viewCode="ADMIN_001"
        purpose="Monitor revenue, order load, inventory alerts, and platform health."
      >
        <Card className="border-primary/20 bg-surface/95 p-5 text-sm text-danger">Failed to load dashboard metrics. Please try again.</Card>
      </PageScaffold>
    );
  }

  const kpis = [
    { label: 'Revenue (Today)', value: `${(metrics.todayRevenue / 1_000_000).toFixed(1)}M VND`, change: `${metrics.revenueChange > 0 ? '+' : ''}${metrics.revenueChange}%` },
    { label: 'Active Orders', value: metrics.activeOrdersCount.toString(), change: `${metrics.ordersChange > 0 ? '+' : ''}${metrics.ordersChange}` },
    { label: 'Low Stock Alerts', value: metrics.lowStockAlertsCount.toString(), change: `${metrics.alertsChange > 0 ? '+' : ''}${metrics.alertsChange}` },
    { label: 'Staff Online', value: metrics.staffOnlineCount.toString(), change: metrics.staffChange },
  ];

  const systemPulse = systemStatus.length > 0 ? systemStatus : [
    { service: 'Payments (Stripe)', status: 'Healthy' as const },
    { service: 'Shipping Gateway', status: 'Healthy' as const },
    { service: 'Instagram OAuth', status: 'Healthy' as const },
    { service: 'Audit Pipeline', status: 'Healthy' as const },
  ];

  return (
    <PageScaffold
      title="Dashboard Overview"
      viewCode="ADMIN_001"
      purpose="Monitor revenue, order load, inventory alerts, and platform health."
    >
      <div className="grid gap-6">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {kpis.map((kpi) => (
            <Card key={kpi.label} className="border border-primary/20 bg-surface/95 p-4">
              <p className="text-xs uppercase tracking-wide text-ink/60">{kpi.label}</p>
              <p className="mt-2 text-2xl font-semibold text-ink">{kpi.value}</p>
              <p className="mt-1 text-xs text-ink/50">{kpi.change}</p>
            </Card>
          ))}
        </div>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
          <Card className="border border-primary/20 bg-gradient-to-br from-surface to-surfaceMuted/60 p-5">
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Sales Pulse</h2>
              <Badge variant="sale">Live</Badge>
            </div>
            <div className="mt-4 h-48 rounded-md border border-dashed border-primary/30 bg-ink/5" />
            <div className="mt-4 grid gap-2 text-xs text-ink/60">
              <p>Top products: Linen Blazer, Silk Scarf, Vintage Denim</p>
              <p>Channel mix: 62% online, 38% POS</p>
            </div>
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">System Pulse</h2>
            <div className="mt-4 grid gap-3">
              {systemPulse.map((item) => {
                const variantMap: Record<string, 'success' | 'warning' | 'danger'> = {
                  'Healthy': 'success',
                  'Degraded': 'warning',
                  'Down': 'danger',
                };
                return (
                  <div key={item.service} className="flex items-center justify-between gap-2 text-sm text-ink/80">
                    <span>{item.service}</span>
                    <Badge variant={variantMap[item.status] || 'success'}>{item.status}</Badge>
                  </div>
                );
              })}
            </div>
          </Card>
        </div>

        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Quick Actions</h2>
              <p className="mt-1 text-xs text-ink/50">Jump to critical admin controls.</p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="secondary" onClick={() => navigate('/admin/users-roles')}>New Role</Button>
              <Button variant="secondary" onClick={() => navigate('/admin/users-roles')}>Invite Staff</Button>
              <Button variant="secondary" onClick={() => navigate('/admin/audit-logs')}>View Audit</Button>
            </div>
          </div>
          <div className="mt-4 grid gap-3 md:grid-cols-3">
            <div className="rounded-md border border-primary/15 bg-ink/5 p-4 text-sm text-ink/70">
              6 locations need weekly inventory review.
            </div>
            <div className="rounded-md border border-primary/15 bg-ink/5 p-4 text-sm text-ink/70">
              3 roles pending permission updates.
            </div>
            <div className="rounded-md border border-primary/15 bg-ink/5 p-4 text-sm text-ink/70">
              2 payment failures flagged in the last hour.
            </div>
          </div>
        </Card>
      </div>
    </PageScaffold>
  );
}
