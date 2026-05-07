import { useState, useEffect } from 'react';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Badge } from '@/shared/ui/Badge';
import { Spinner } from '@/shared/ui/Spinner';
import { getStockTransfers, type StockTransfer } from '@/features/staff/api/staffInventoryApi';

export function StockTransfer() {
  const [transfers, setTransfers] = useState<StockTransfer[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchTransfers = async () => {
    setIsLoading(true);
    try {
      const res = await getStockTransfers();
      setTransfers(res.items);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTransfers();
  }, []);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
      <div className="flex justify-between items-end mb-8">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Stock Transfers</h1>
          <p className="mt-1 text-sm text-gray-500">Manage inventory moving between locations.</p>
        </div>
        <Button>New Transfer</Button>
      </div>

      <Card>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">From</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">To</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Items</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Created At</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {transfers.map((transfer) => (
                <tr key={transfer.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">TRN-{transfer.id}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{transfer.sourceLocationName}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{transfer.destinationLocationName}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <Badge variant={transfer.status === 'COMPLETED' ? 'success' : transfer.status === 'IN_TRANSIT' ? 'warning' : 'default'}>
                      {transfer.status}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-500">{transfer.itemCount}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm text-gray-500">{new Date(transfer.createdAt).toLocaleDateString()}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    {transfer.status === 'IN_TRANSIT' && (
                      <Button size="sm">Receive</Button>
                    )}
                    {transfer.status === 'DRAFT' && (
                      <Button size="sm" variant="secondary">Ship</Button>
                    )}
                  </td>
                </tr>
              ))}
              {transfers.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-4 text-center text-sm text-gray-500">
                    No transfers found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
