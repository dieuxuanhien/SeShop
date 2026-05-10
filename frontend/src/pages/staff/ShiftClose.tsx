import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { closeShift, getCurrentShift, openShift, type ShiftData } from '@/features/staff/api/staffPosApi';

export function ShiftClose() {
  const navigate = useNavigate();
  const [shift, setShift] = useState<ShiftData | null>(null);
  const [actualCash, setActualCash] = useState<number | ''>('');
  const [isLoading, setIsLoading] = useState(false);
  const [shiftLoading, setShiftLoading] = useState(true);
  const [locationId, setLocationId] = useState(1);
  const [startingCash, setStartingCash] = useState(0);

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
      <div className="mx-auto max-w-3xl">
        <Card className="border-primary/20 bg-surface/95 p-5 text-sm text-ink/60">Loading shift data...</Card>
      </div>
    );
  }

  if (!shift) {
    return (
      <div className="mx-auto max-w-3xl">
        <h1 className="mb-8 font-display text-3xl font-bold text-surface">Open Register Shift</h1>
        <Card className="border-primary/20 bg-surface/95 p-6">
          <p className="mb-5 text-sm text-ink/60">No active shift was found. Open a register shift before selling or closing cash.</p>
          <form
            className="grid gap-4"
            onSubmit={async (event) => {
              event.preventDefault();
              setIsLoading(true);
              try {
                setShift(await openShift(locationId, startingCash));
              } finally {
                setIsLoading(false);
              }
            }}
          >
            <Input label="Location ID" type="number" min={1} value={locationId} onChange={(event) => setLocationId(Number(event.target.value))} />
            <Input label="Starting Cash" type="number" min={0} value={startingCash} onChange={(event) => setStartingCash(Number(event.target.value))} />
            <Button type="submit" isLoading={isLoading}>Open Shift</Button>
          </form>
        </Card>
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
    <div className="mx-auto max-w-3xl">
      <h1 className="mb-8 font-display text-3xl font-bold text-surface">Close Register Shift</h1>
      
      <Card className="border-primary/20 bg-surface/95">
        <div className="p-6">
          <div className="mb-8 rounded-md bg-ink/[0.03] p-4">
            <h3 className="mb-4 text-sm font-medium uppercase tracking-wide text-ink/55">Shift Summary</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-ink/55">Register</p>
                <p className="font-medium text-ink">{shift.registerName}</p>
              </div>
              <div>
                <p className="text-sm text-ink/55">Opened At</p>
                <p className="font-medium text-ink">{new Date(shift.openedAt).toLocaleString()}</p>
              </div>
              <div>
                <p className="text-sm text-ink/55">Total Transactions</p>
                <p className="font-medium text-ink">{shift.transactionCount}</p>
              </div>
              <div>
                <p className="text-sm text-ink/55">Total Card Payments</p>
                <p className="font-medium text-ink">{shift.cardPaymentsTotal.toLocaleString()} VND</p>
              </div>
            </div>
          </div>

          <div className="mb-8">
            <h3 className="mb-4 text-lg font-medium text-ink">Cash Reconciliation</h3>
            <div className="flex items-center justify-between border-b border-primary/15 py-3">
              <span className="text-ink/60">System Expected Cash</span>
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
              <div className={`mt-4 flex items-center justify-between rounded-md p-4 font-medium ${variance === 0 ? 'bg-success/10 text-success' : 'bg-danger/10 text-danger'}`}>
                <span>Variance</span>
                <span>{variance > 0 ? '+' : ''}{variance.toLocaleString()} VND</span>
              </div>
            )}
          </div>

          <div className="flex justify-end gap-4 border-t border-primary/15 pt-6">
            <Button variant="secondary" onClick={() => navigate('/staff/dashboard')}>Cancel</Button>
            <Button 
              onClick={handleCloseShift} 
              isLoading={isLoading}
              disabled={actualCash === ''}
              variant={variance !== 0 ? 'danger' : 'primary'}
            >
              {variance !== 0 ? 'Close Shift with Variance' : 'Close Shift Successfully'}
            </Button>
          </div>
        </div>
      </Card>
    </div>
  );
}
