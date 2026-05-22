import { useState, useEffect } from 'react';
import { Clock } from 'lucide-react';
import { getReceiptHistory, type ReceiptDto } from '@/features/staff/api/staffPosApi';
import { PrintableReceipt } from '@/features/staff/ui/PrintableReceipt';
import { Button } from '@/shared/ui/Button';

export function PosHistory() {
  const [historyList, setHistoryList] = useState<ReceiptDto[]>([]);
  const [selectedHistoryReceipt, setSelectedHistoryReceipt] = useState<ReceiptDto | null>(null);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyPage, setHistoryPage] = useState(0);
  const [historyTotalPages, setHistoryTotalPages] = useState(0);

  const fetchHistory = async (page = 0) => {
    setHistoryLoading(true);
    try {
      const data = await getReceiptHistory(page, 10);
      setHistoryList(data.items || []);
      setHistoryPage(data.page);
      setHistoryTotalPages(data.totalPages);
      if (data.items && data.items.length > 0) {
        setSelectedHistoryReceipt(data.items[0]);
      } else {
        setSelectedHistoryReceipt(null);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  return (
    <div className="flex flex-1 overflow-hidden bg-surfaceMuted/20 print:block">
      {/* History List Side */}
      <div className="flex flex-1 flex-col border-r border-primary/15 bg-surface print:hidden">
        <div className="border-b border-primary/15 p-4 flex items-center justify-between">
          <h2 className="text-lg font-bold flex items-center gap-2">
            <Clock size={20} className="text-primary" /> Past Receipts
          </h2>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="secondary"
              disabled={historyPage === 0 || historyLoading}
              onClick={() => fetchHistory(historyPage - 1)}
            >
              Prev
            </Button>
            <span className="text-xs text-ink/60 font-semibold">
              Page {historyPage + 1} of {Math.max(1, historyTotalPages)}
            </span>
            <Button
              size="sm"
              variant="secondary"
              disabled={historyPage >= historyTotalPages - 1 || historyLoading}
              onClick={() => fetchHistory(historyPage + 1)}
            >
              Next
            </Button>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto p-4 space-y-3">
          {historyLoading ? (
            <div className="flex h-full items-center justify-center text-ink/40">
              <span className="animate-pulse">Loading history...</span>
            </div>
          ) : historyList.length === 0 ? (
            <div className="flex h-full items-center justify-center text-ink/40">
              No receipt history found for this store.
            </div>
          ) : (
            historyList.map((hist) => (
              <div
                key={hist.id}
                onClick={() => setSelectedHistoryReceipt(hist)}
                className={`p-4 rounded-md border cursor-pointer transition-all duration-200 ${
                  selectedHistoryReceipt?.id === hist.id
                    ? 'border-primary bg-primary/5 shadow-sm'
                    : 'border-primary/10 bg-surface hover:border-primary/30 hover:bg-ink/[0.01]'
                }`}
              >
                <div className="flex justify-between items-start mb-2">
                  <span className="font-bold text-ink">{hist.receiptNumber}</span>
                  <span className="text-[10px] bg-primary/10 text-primary px-2 py-0.5 rounded-full font-semibold">
                    {hist.paymentMethod}
                  </span>
                </div>
                <div className="flex justify-between text-xs text-ink/65">
                  <span>{new Date(hist.issuedAt).toLocaleString()}</span>
                  <span className="font-bold text-ink">
                    {hist.totalAmount?.toLocaleString() || '0'} VND
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Receipt Details and Action Side */}
      <div className="flex w-[480px] flex-col bg-surfaceMuted/30 overflow-y-auto print:w-full print:bg-white print:p-0">
        {selectedHistoryReceipt ? (
          <div className="p-6 space-y-6 print:p-0">
            <PrintableReceipt receipt={selectedHistoryReceipt} printOnly={true} />
            <div className="rounded-md border border-primary/20 bg-surface p-6 shadow-soft print:hidden">
              <PrintableReceipt receipt={selectedHistoryReceipt} className="mb-6" />
              <Button className="w-full h-12 text-sm" onClick={() => window.print()}>
                Reprint Receipt
              </Button>
            </div>
          </div>
        ) : (
          <div className="flex h-full items-center justify-center text-ink/40 p-6 text-center">
            Select a receipt from the history list to view details and print.
          </div>
        )}
      </div>
    </div>
  );
}
