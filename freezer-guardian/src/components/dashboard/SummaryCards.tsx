import { FreezerSummary } from '@/services/freezerService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Thermometer, AlertTriangle, CheckCircle } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';

interface SummaryCardsProps {
  summary: FreezerSummary | null;
  isLoading: boolean;
}

export const SummaryCards = ({ summary, isLoading }: SummaryCardsProps) => {
  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[...Array(4)].map((_, i) => (
          <Skeleton key={i} className="h-[120px] w-full rounded-xl" />
        ))}
      </div>
    );
  }

  const totalDevices =
    (summary?.totalFreezers ?? 0) + (summary?.totalDataLoggers ?? 0);

  // Use the exact field names from your JSON
  const cards = [
    {
      title: "Total Devices",
      value: totalDevices,
      icon: Thermometer,
      description: "Registered Units",
      color: "text-blue-600"
    },
    {
      title: "Active Monitoring",
      value: summary?.activeDevicesCount ?? summary?.activeFreezersCount ?? 0,
      icon: CheckCircle,
      description: "Sensors Online",
      color: "text-green-600"
    },
    {
      title: "Active Alerts",
      value: summary?.redAlertFreezersCount ?? 0,
      icon: AlertTriangle,
      description: "Critical Temperatures",
      color: "text-red-600"
    }
  ];

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {cards.map((card, index) => (
        <Card key={index}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {card.title}
            </CardTitle>
            <card.icon className={`h-4 w-4 ${card.color}`} />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{card.value}</div>
            <p className="text-xs text-muted-foreground">
              {card.description}
            </p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};