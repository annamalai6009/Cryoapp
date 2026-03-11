import { ReactNode } from 'react';
import { AppHeader } from './AppHeader';

interface DashboardLayoutProps {
  children: ReactNode;
}

export const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  return (
    <div className="min-h-screen">
      <AppHeader />
      <main className="container py-6">
        {children}
      </main>
    </div>
  );
};
