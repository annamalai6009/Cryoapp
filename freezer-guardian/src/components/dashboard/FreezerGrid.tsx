import { useNavigate } from 'react-router-dom';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { FreezerResponse } from '@/services/freezerService';
import { Eye, Trash2, ShieldCheck } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';

interface FreezerGridProps {
  freezers: FreezerResponse[];
  isLoading: boolean;
}

export const FreezerGrid = ({ freezers, isLoading }: FreezerGridProps) => {
  const navigate = useNavigate();

  if (isLoading) {
    return (
      <Card className="mt-4 p-4">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-emerald-500" />
            <span className="text-sm font-semibold text-slate-700">Verified Device List</span>
          </div>
          <Skeleton className="h-4 w-24" />
        </div>
        <Skeleton className="h-[56px] w-full rounded-xl" />
      </Card>
    );
  }

  if (!freezers || freezers.length === 0) {
    return (
      <Card className="mt-4 p-6 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-emerald-500" />
          <div>
            <p className="text-sm font-semibold text-slate-700">Verified Device List</p>
            <p className="text-xs text-muted-foreground">No devices registered yet. Add one to get started.</p>
          </div>
        </div>
        <Badge variant="outline" className="text-[11px] px-3 py-1 rounded-full">
          0 TOTAL REGISTERED
        </Badge>
      </Card>
    );
  }

  return (
    <Card className="mt-4 overflow-hidden border-0 shadow-sm bg-gradient-to-b from-slate-50 to-white">
      <div className="flex items-center justify-between px-4 pt-4 pb-2">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-emerald-500" />
          <span className="text-sm font-semibold text-slate-800">Verified Device List</span>
        </div>
        <Badge variant="outline" className="text-[11px] px-3 py-1 rounded-full">
          {freezers.length} TOTAL REGISTERED
        </Badge>
      </div>

      <div className="px-4 pb-4">
        <div className="overflow-x-auto rounded-2xl border border-slate-100 bg-white">
          <table className="min-w-full text-xs sm:text-sm">
            <thead className="bg-slate-50 text-slate-500 uppercase tracking-[0.08em] text-[10px]">
              <tr>
                <th className="px-4 py-3 text-left">S. NO</th>
                <th className="px-4 py-3 text-left">Name</th>
                <th className="px-4 py-3 text-left">PO</th>
                <th className="px-4 py-3 text-left">Topic / ID</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {freezers.map((freezer, index) => (
                <tr
                  key={freezer.id}
                  className="border-t border-slate-100 hover:bg-slate-50/80 transition-colors"
                >
                  <td className="px-4 py-3 text-slate-500">
                    {String(index + 1).padStart(2, '0')}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col">
                      <span className="font-medium text-slate-800">{freezer.name}</span>
                      <span className="text-[11px] text-slate-400">
                        ID:{' '}
                        {freezer.hasTopic && freezer.topicId
                          ? freezer.topicId
                          : 'Awaiting first data'}
                      </span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <Badge
                      variant="outline"
                      className="text-[11px] px-2 py-0.5 rounded-full bg-slate-50"
                    >
                      {freezer.poNumber || '—'}
                    </Badge>
                  </td>
                  <td className="px-4 py-3 text-slate-700 font-mono text-[11px]">
                    {freezer.hasTopic && freezer.topicId
                      ? freezer.topicId
                      : '—'}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        type="button"
                        onClick={() => {
                          if (!freezer.hasTopic || !freezer.topicId) {
                            return;
                          }
                          freezer.topicId.startsWith('DL')
                            ? navigate(`/datalogger/${freezer.topicId}`)
                            : navigate(`/freezer/${freezer.topicId}`);
                        }}
                        className="inline-flex h-7 w-7 items-center justify-center rounded-full border border-slate-200 text-slate-500 hover:text-slate-800 hover:border-slate-300 bg-white shadow-sm"
                        aria-label="View details"
                      >
                        <Eye className="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        className="inline-flex h-7 w-7 items-center justify-center rounded-full border border-rose-100 text-rose-500 hover:text-rose-700 hover:border-rose-200 bg-rose-50/60"
                        aria-label="Remove (coming soon)"
                        disabled
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </Card>
  );
};