import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { closeShift, getCurrentShift, type ShiftData } from '@/features/staff/api/staffPosApi';

export function ShiftClose() {
  const navigate = useNavigate();
  const [shift, setShift] = useState<ShiftData | null>(null);
  const [actualCash, setActualCash] = useState<number | ''>('');
  const [isLoading, setIsLoading] = useState(false);
  const [shiftLoading, setShiftLoading] = useState(true);

  useEffect(() => {
    // Fetch current shift data to get expected cash from backend
    const loadShift = async () => {
      try {
        const currentShift = await getCurrentShift();
        setShift(currentShift);
      } catch (e) {
        console.error('Failed to load shift data:', e);
      } finally {
        setShiftLoading(false);
      }
    };
    loadShift();
  }, []);

  if (shiftLoading) {
    return (
      <div className="max-w-3xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <p className="text-gray-600">Loading shift data...</p>
      </div>
    );
  }

  if (!shift) {
    return (
      <div className="max-w-3xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <p className="text-red-600">No active shift found. Start a new shift first.</p>
      </div>
    );
  }

  const expectedCash = shift.expectedCash;
  const variance = actualCash === '' ? 0 : Number(actualCash) - expectedCash;

  const handleCloseShift = async () => {
    if (actualCash === '') return;
    setIsLoading(true);
    try {
      await closeShift(shift.shiftId, expectedCash, Number(actualCash));
      navigate('/staff/dashboard');
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Close Register Shift</h1>
      
      <Card>
        <div className="p-6">
          <div className="bg-gray-50 p-4 rounded-md mb-8">
            <h3 className="text-sm font-medium text-gray-500 uppercase tracking-wide mb-4">Shift Summary</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-500">Register</p>
                <p className="font-medium text-gray-900">{shift.registerName}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Opened At</p>
                <p className="font-medium text-gray-900">{new Date(shift.openedAt).toLocaleString()}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Total Transactions</p>
                <p className="font-medium text-gray-900">{shift.transactionCount}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Total Card Payments</p>
                <p className="font-medium text-gray-900">{shift.cardPaymentsTotal.toLocaleString()} VND</p>
              </div>
            </div>
          </div>

          <div className="mb-8">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Cash Reconciliation</h3>
            <div className="flex justify-between items-center py-3 border-b border-gray-200">
              <span className="text-gray-600">System Expected Cash</span>
              <span className="font-bold text-lg">{expectedCash.toLocaleString()} VND</span>
            </div>
            
            <div className="mt-6">
              <Input
                label="Actual Cash in Drawer"
                type="number"
                value={actualCash}
                onChange={(e) => setActualCash(Number(e.target.value))}
                placeholder="Enter counted amount"
              />
            </div>

            {actualCash !== '' && (
              <div className={`mt-4 p-4 rounded flex justify-between items-center font-medium ${variance === 0 ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'}`}>
                <span>Variance</span>
                <span>{variance > 0 ? '+' : ''}{variance.toLocaleString()} VND</span>
              </div>
            )}
          </div>

          <div className="flex justify-end gap-4 border-t border-gray-200 pt-6">
            <Button variant="secondary" onClick={() => navigate('/staff/dashboard')}>Cancel</Button>
            <Button 
              onClick={handleCloseShift} 
              isLoading={isLoading}
              disabled={actualCash === ''}
              className={variance !== 0 ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500' : ''}
            >
              {variance !== 0 ? 'Close Shift with Variance' : 'Close Shift Successfully'}
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
}
