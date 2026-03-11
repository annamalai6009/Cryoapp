import { useEffect, useState, useCallback } from 'react';
import { DashboardLayout } from '@/components/DashboardLayout';
import { SummaryCards } from '@/components/dashboard/SummaryCards';
import { FreezerGrid } from '@/components/dashboard/FreezerGrid';
import { RegisterFreezerModal } from '@/components/dashboard/RegisterFreezerModal';
import { WhatsAppWidget } from '@/components/dashboard/WhatsAppWidget'; // ✅ NEW IMPORT
import { freezerService, FreezerResponse, FreezerSummary } from '@/services/freezerService';

import { authService, SignupRequest } from '@/services/authService';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { 
  ShieldCheck, 
  PackagePlus, 
  UserPlus, 
  UserX,
  Loader2,
  Database,
  Lock,
  Link as LinkIcon
} from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription
} from "@/components/ui/card"
import toast from 'react-hot-toast';

const Dashboard = () => {
  const { userId, isAdmin, isRoot } = useAuth(); 
  const [freezers, setFreezers] = useState<FreezerResponse[]>([]);
  const [summary, setSummary] = useState<FreezerSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // --- MODAL STATES (Admin) ---
  const [isInventoryOpen, setIsInventoryOpen] = useState(false);
  const [isAdminCreateOpen, setIsAdminCreateOpen] = useState(false);
  const [isSuspendOpen, setIsSuspendOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // --- FORM DATA ---
  const [inventoryData, setInventoryData] = useState({ poNumber: '', s3Url: '' });
  const [adminData, setAdminData] = useState<SignupRequest>({ name: '', email: '', mobileNumber: '', password: '' });
  const [suspendEmail, setSuspendEmail] = useState('');
// Find your fetchData function and replace it with this:
// Inside Dashboard.tsx - replace your fetchData function:
  const fetchData = useCallback(async () => {
    if (!userId) return;

    try {
      setIsLoading(true);
      if (isAdmin || isRoot) {
        // ADMIN/ROOT: Fetch full system summary
        const summaryRes = await freezerService.getFreezerSummary();
        setSummary(summaryRes.data); // ✅ UNWRAPPING FIXED
        setIsLoading(false);
      } else {
        // CUSTOMER: Fetch User-specific freezers and summary
        const [freezersData, summaryRes] = await Promise.all([
          freezerService.getDashboardData(userId),
          freezerService.getFreezerSummary(),
        ]);
        
        setFreezers(freezersData || []);
        setSummary(summaryRes.data); // ✅ UNWRAPPING FIXED
        setIsLoading(false);
      }
    } catch (error) {
      console.error("Dashboard fetch error:", error);
      setIsLoading(false);
    }
  }, [userId, isAdmin, isRoot]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // --- HANDLERS ---

  const handleAddInventory = async () => {
    if (!inventoryData.poNumber || !inventoryData.s3Url) {
      toast.error("Please enter both PO Number and S3 URL");
      return;
    }

    setIsSubmitting(true);
    try {
      await freezerService.addInventory([{ 
        poNumber: inventoryData.poNumber, 
        s3Url: inventoryData.s3Url 
      }]);
      
      toast.success('Inventory Added Successfully');
      setIsInventoryOpen(false);
      setInventoryData({ poNumber: '', s3Url: '' });
    } catch (error) {
      toast.error('Failed to add inventory');
    } finally { 
      setIsSubmitting(false); 
    }
  };

  const handleCreateAdmin = async () => {
    setIsSubmitting(true);
    try {
      await authService.createStaff(adminData);
      toast.success('New Admin Created');
      setIsAdminCreateOpen(false);
      setAdminData({ name: '', email: '', mobileNumber: '', password: '' });
    } catch (error: unknown) {
      const errorMessage = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed';
      toast.error(errorMessage);
    } finally { setIsSubmitting(false); }
  };

  const handleSuspendUser = async () => {
    setIsSubmitting(true);
    try {
      await authService.deactivateStaff(suspendEmail);
      toast.success('User Suspended');
      setIsSuspendOpen(false);
      setSuspendEmail('');
    } catch (error: unknown) {
      const errorMessage = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed';
      toast.error(errorMessage);
    } finally { setIsSubmitting(false); }
  };

  return (
    <DashboardLayout>
      
      {/* --- ADMIN DASHBOARD --- */}
      {isAdmin ? (
        <div className="space-y-8">
          
          {/* Header */}
          <div className="flex items-center gap-4 border-b pb-6">
             <div className={`p-3 rounded-lg ${isRoot ? 'bg-red-100 text-red-600' : 'bg-blue-100 text-blue-600'}`}>
               <ShieldCheck className="w-8 h-8" />
             </div>
             <div>
               <h1 className="text-3xl font-bold text-gray-900">System Administration</h1>
               <p className="text-muted-foreground">
                 {isRoot ? 'Root Access Granted' : 'Staff Access Granted'}
               </p>
             </div>
          </div>

          {/* Management Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              
              {/* 1. INVENTORY CARD */}
              <Card className="hover:shadow-md transition-shadow">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Database className="w-5 h-5 text-blue-500" />
                    Inventory
                  </CardTitle>
                  <CardDescription>Register new freezer units.</CardDescription>
                </CardHeader>
                <CardContent>
                  <Dialog open={isInventoryOpen} onOpenChange={setIsInventoryOpen}>
                     <DialogTrigger asChild>
                       <Button className="w-full bg-slate-900">
                         <PackagePlus className="w-4 h-4 mr-2" /> Add Stock
                       </Button>
                     </DialogTrigger>
                     <DialogContent>
                       <DialogHeader>
                         <DialogTitle>Add Inventory</DialogTitle>
                         <DialogDescription>
                           Attach purchase order and asset link for a new freezer unit.
                         </DialogDescription>
                       </DialogHeader>
                       <div className="space-y-4 py-4">
                         <div className="space-y-2">
                           <Label>PO Number</Label>
                           <Input 
                             placeholder="e.g. PO-12345"
                             value={inventoryData.poNumber} 
                             onChange={(e) => setInventoryData({...inventoryData, poNumber: e.target.value})} 
                           />
                         </div>
                         <div className="space-y-2">
                           <Label>S3 URL (Asset Link)</Label>
                           <div className="relative">
                             <LinkIcon className="absolute left-2.5 top-2.5 h-4 w-4 text-gray-500" />
                             <Input 
                               className="pl-9"
                               placeholder="s3://bucket/path"
                               value={inventoryData.s3Url} 
                               onChange={(e) => setInventoryData({...inventoryData, s3Url: e.target.value})} 
                             />
                           </div>
                         </div>
                         <Button onClick={handleAddInventory} disabled={isSubmitting} className="w-full">
                           {isSubmitting ? <Loader2 className="animate-spin" /> : 'Confirm Add'}
                         </Button>
                       </div>
                     </DialogContent>
                   </Dialog>
                </CardContent>
              </Card>

              {/* 2. STAFF CARD */}
              {isRoot ? (
                <Card className="hover:shadow-md transition-shadow border-red-100">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-red-900">
                      <UserPlus className="w-5 h-5 text-red-500" />
                      Staff Team
                    </CardTitle>
                    <CardDescription>Create new admin staff.</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Dialog open={isAdminCreateOpen} onOpenChange={setIsAdminCreateOpen}>
                       <DialogTrigger asChild>
                         <Button variant="outline" className="w-full border-red-200 text-red-700 hover:bg-red-50">
                           Create Admin
                         </Button>
                       </DialogTrigger>
                       <DialogContent>
                         <DialogHeader>
                           <DialogTitle>Create New Admin</DialogTitle>
                           <DialogDescription>
                             Provide credentials to grant staff administration access.
                           </DialogDescription>
                         </DialogHeader>
                         <div className="space-y-3 py-4">
                           <Input placeholder="Name" value={adminData.name} onChange={e => setAdminData({...adminData, name: e.target.value})} />
                           <Input placeholder="Email" value={adminData.email} onChange={e => setAdminData({...adminData, email: e.target.value})} />
                           <Input placeholder="Mobile" value={adminData.mobileNumber} onChange={e => setAdminData({...adminData, mobileNumber: e.target.value})} />
                           <Input type="password" placeholder="Password" value={adminData.password} onChange={e => setAdminData({...adminData, password: e.target.value})} />
                           <Button onClick={handleCreateAdmin} disabled={isSubmitting} className="w-full bg-red-600 hover:bg-red-700">Create</Button>
                         </div>
                       </DialogContent>
                    </Dialog>
                  </CardContent>
                </Card>
              ) : (
                <Card className="bg-gray-50 opacity-60">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-gray-400">
                      <Lock className="w-5 h-5" /> Staff Team
                    </CardTitle>
                    <CardDescription>Root access required.</CardDescription>
                  </CardHeader>
                </Card>
              )}

              {/* 3. SECURITY CARD */}
              {isRoot ? (
                <Card className="hover:shadow-md transition-shadow border-red-100">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2 text-red-900">
                      <UserX className="w-5 h-5 text-red-500" />
                      Security
                    </CardTitle>
                    <CardDescription>Suspend user access.</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <Dialog open={isSuspendOpen} onOpenChange={setIsSuspendOpen}>
                       <DialogTrigger asChild>
                         <Button variant="destructive" className="w-full">
                           Suspend User
                         </Button>
                       </DialogTrigger>
                       <DialogContent>
                         <DialogHeader>
                           <DialogTitle>Suspend Account</DialogTitle>
                           <DialogDescription>
                             Enter the user&apos;s email to temporarily revoke access.
                           </DialogDescription>
                         </DialogHeader>
                         <div className="space-y-4 py-4">
                           <Label>Email to Suspend</Label>
                           <Input value={suspendEmail} onChange={(e) => setSuspendEmail(e.target.value)} />
                           <Button variant="destructive" onClick={handleSuspendUser} disabled={isSubmitting} className="w-full">
                             Confirm Suspension
                           </Button>
                         </div>
                       </DialogContent>
                    </Dialog>
                  </CardContent>
                </Card>
              ) : (
                 <Card className="bg-gray-50 opacity-60">
                   <CardHeader>
                     <CardTitle className="flex items-center gap-2 text-gray-400">
                       <Lock className="w-5 h-5" /> Security
                     </CardTitle>
                     <CardDescription>Root access required.</CardDescription>
                   </CardHeader>
                 </Card>
              )}

          </div>
        </div>
      ) : (
        /* --- CUSTOMER DASHBOARD --- */
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold">My Dashboard</h1>
              <p className="text-muted-foreground mt-1">
                Monitor your freezer fleet in real-time
              </p>
            </div>
            <RegisterFreezerModal onSuccess={fetchData} />
          </div>

          <SummaryCards summary={summary} isLoading={isLoading} />
          
          {/* ✅ NEW: WhatsApp Widget Placed Here */}
          <WhatsAppWidget />
          
          <div className="mb-4">
            <h2 className="text-xl font-semibold">Your Freezers</h2>
          </div>
          
          <FreezerGrid freezers={freezers} isLoading={isLoading} />
        </div>
      )}
    </DashboardLayout>
  );
};

export default Dashboard;