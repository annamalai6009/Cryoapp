import { useState } from 'react';
import { DashboardLayout } from '@/components/DashboardLayout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { authService } from '@/services/authService';
import { freezerService, DeviceType } from '@/services/freezerService';
import { alertService, AlertEvaluateRequest } from '@/services/alertService';
import { Package, Users, Shield, Bug, Loader2, UserPlus, UserX, AlertTriangle } from 'lucide-react';
import toast from 'react-hot-toast';

const AdminDashboard = () => {
  // Inventory State
  const [inventoryLoading, setInventoryLoading] = useState(false);
  const [inventoryData, setInventoryData] = useState<{
    poNumber: string;
    s3Url: string;
    deviceType: DeviceType;
  }>({
    poNumber: '',
    s3Url: '',
    deviceType: 'NORMAL_FREEZER',
  });

  // Staff State
  const [staffLoading, setStaffLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [staffData, setStaffData] = useState({
    name: '',
    email: '',
    mobileNumber: '',
    password: '',
  });
  const [deactivateEmail, setDeactivateEmail] = useState('');

  // Alert Debug State
  const [alertLoading, setAlertLoading] = useState(false);
  const [alertData, setAlertData] = useState<AlertEvaluateRequest>({
    freezerId: '',
    temperature: -20,
    minThreshold: -25,
    maxThreshold: -15,
  });
  const [alertResult, setAlertResult] = useState<{
    isAlert: boolean;
    alertType: string;
    message: string;
  } | null>(null);

  const handleAddInventory = async (e: React.FormEvent) => {
    e.preventDefault();
    setInventoryLoading(true);

    try {
      await freezerService.addInventory([inventoryData]);
      toast.success('Inventory added successfully!');
      setInventoryData({
        poNumber: '',
        s3Url: '',
        deviceType: 'NORMAL_FREEZER',
      });
    } catch (error) {
      // Handled by interceptor
    } finally {
      setInventoryLoading(false);
    }
  };

  const handleCreateStaff = async (e: React.FormEvent) => {
    e.preventDefault();
    setStaffLoading(true);

    try {
      await authService.createStaff(staffData);
      toast.success('Staff member created successfully!');
      setStaffData({ name: '', email: '', mobileNumber: '', password: '' });
    } catch (error) {
      // Handled by interceptor
    } finally {
      setStaffLoading(false);
    }
  };

  const handleDeactivateStaff = async (e: React.FormEvent) => {
    e.preventDefault();
    setDeactivateLoading(true);

    try {
      await authService.deactivateStaff(deactivateEmail);
      toast.success('Staff member deactivated!');
      setDeactivateEmail('');
    } catch (error) {
      // Handled by interceptor
    } finally {
      setDeactivateLoading(false);
    }
  };

  const handleTestAlert = async (e: React.FormEvent) => {
    e.preventDefault();
    setAlertLoading(true);
    setAlertResult(null);

    try {
      const response = await alertService.evaluateAlert(alertData);
      setAlertResult(response.data);
      toast.success('Alert evaluation complete!');
    } catch (error) {
      // Handled by interceptor
    } finally {
      setAlertLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="mb-8 flex items-start justify-between">
        <div>
          <div className="flex items-center gap-3 mb-2">
          <Shield className="w-8 h-8 text-primary" />
          <h1 className="text-3xl font-bold">Admin Dashboard</h1>
          </div>
          <p className="text-muted-foreground">Manage inventory, staff, and system diagnostics</p>
        </div>
      </div>

      <Tabs defaultValue="inventory" className="space-y-6">
        <TabsList className="grid w-full grid-cols-3 max-w-md">
          <TabsTrigger value="inventory" className="flex items-center gap-2">
            <Package className="w-4 h-4" />
            Inventory
          </TabsTrigger>
          <TabsTrigger value="staff" className="flex items-center gap-2">
            <Users className="w-4 h-4" />
            Staff
          </TabsTrigger>
          <TabsTrigger value="debug" className="flex items-center gap-2">
            <Bug className="w-4 h-4" />
            Debug
          </TabsTrigger>
        </TabsList>

        {/* Inventory Tab */}
        <TabsContent value="inventory">
          <Card className="max-w-xl">
            <CardHeader>
              <CardTitle>Add to Inventory</CardTitle>
              <CardDescription>Register a new freezer unit in the system inventory.</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleAddInventory} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="poNumber">PO Number</Label>
                  <Input
                    id="poNumber"
                    placeholder="PO-12345"
                    value={inventoryData.poNumber}
                    onChange={(e) => setInventoryData({ ...inventoryData, poNumber: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="s3Url">S3 URL</Label>
                  <Input
                    id="s3Url"
                    placeholder="s3://bucket/path/to/asset"
                    value={inventoryData.s3Url}
                    onChange={(e) => setInventoryData({ ...inventoryData, s3Url: e.target.value })}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="deviceType">Device Type</Label>
                  <select
                    id="deviceType"
                    className="block w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                    value={inventoryData.deviceType}
                    onChange={(e) =>
                      setInventoryData({
                        ...inventoryData,
                        deviceType: e.target.value as DeviceType,
                      })
                    }
                  >
                    <option value="NORMAL_FREEZER">Normal Freezer</option>
                    <option value="DATA_LOGGER">Data Logger</option>
                  </select>
                </div>
                <Button type="submit" disabled={inventoryLoading}>
                  {inventoryLoading ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Adding...
                    </>
                  ) : (
                    <>
                      <Package className="w-4 h-4 mr-2" />
                      Add to Inventory
                    </>
                  )}
                </Button>
              </form>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Staff Tab */}
        <TabsContent value="staff">
          <div className="grid gap-6 md:grid-cols-2">
            {/* Create Staff */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <UserPlus className="w-5 h-5" />
                  Create Staff Member
                </CardTitle>
                <CardDescription>Add a new staff member to the system.</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleCreateStaff} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="staffName">Name</Label>
                    <Input
                      id="staffName"
                      placeholder="John Doe"
                      value={staffData.name}
                      onChange={(e) => setStaffData({ ...staffData, name: e.target.value })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="staffEmail">Email</Label>
                    <Input
                      id="staffEmail"
                      type="email"
                      placeholder="john@example.com"
                      value={staffData.email}
                      onChange={(e) => setStaffData({ ...staffData, email: e.target.value })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="staffMobile">Mobile Number</Label>
                    <Input
                      id="staffMobile"
                      placeholder="+1 234 567 8900"
                      value={staffData.mobileNumber}
                      onChange={(e) => setStaffData({ ...staffData, mobileNumber: e.target.value })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="staffPassword">Password</Label>
                    <Input
                      id="staffPassword"
                      type="password"
                      placeholder="••••••••"
                      value={staffData.password}
                      onChange={(e) => setStaffData({ ...staffData, password: e.target.value })}
                      required
                    />
                  </div>
                  <Button type="submit" disabled={staffLoading}>
                    {staffLoading ? (
                      <>
                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                        Creating...
                      </>
                    ) : (
                      'Create Staff'
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Suspend Staff */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <UserX className="w-5 h-5" />
                  Suspend Staff
                </CardTitle>
                <CardDescription>Deactivate a staff member's account.</CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleDeactivateStaff} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="deactivateEmail">Staff Email</Label>
                    <Input
                      id="deactivateEmail"
                      type="email"
                      placeholder="staff@example.com"
                      value={deactivateEmail}
                      onChange={(e) => setDeactivateEmail(e.target.value)}
                      required
                    />
                  </div>
                  <Button type="submit" variant="destructive" disabled={deactivateLoading}>
                    {deactivateLoading ? (
                      <>
                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                        Deactivating...
                      </>
                    ) : (
                      'Deactivate Staff'
                    )}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Debug Tab */}
        <TabsContent value="debug">
          <Card className="max-w-xl">
            <CardHeader>
              <CardTitle>Test Alert Evaluation</CardTitle>
              <CardDescription>
                Manually trigger the alert evaluation logic for testing purposes.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleTestAlert} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="freezerId">Freezer ID</Label>
                  <Input
                    id="freezerId"
                    placeholder="freezer-uuid"
                    value={alertData.freezerId}
                    onChange={(e) => setAlertData({ ...alertData, freezerId: e.target.value })}
                    required
                  />
                </div>
                <div className="grid grid-cols-3 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="temperature">Temperature (°C)</Label>
                    <Input
                      id="temperature"
                      type="number"
                      step="0.1"
                      value={alertData.temperature}
                      onChange={(e) => setAlertData({ ...alertData, temperature: parseFloat(e.target.value) })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="minThreshold">Min Threshold</Label>
                    <Input
                      id="minThreshold"
                      type="number"
                      step="0.1"
                      value={alertData.minThreshold}
                      onChange={(e) => setAlertData({ ...alertData, minThreshold: parseFloat(e.target.value) })}
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="maxThreshold">Max Threshold</Label>
                    <Input
                      id="maxThreshold"
                      type="number"
                      step="0.1"
                      value={alertData.maxThreshold}
                      onChange={(e) => setAlertData({ ...alertData, maxThreshold: parseFloat(e.target.value) })}
                      required
                    />
                  </div>
                </div>
                <Button type="submit" disabled={alertLoading}>
                  {alertLoading ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Evaluating...
                    </>
                  ) : (
                    <>
                      <AlertTriangle className="w-4 h-4 mr-2" />
                      Test Alert
                    </>
                  )}
                </Button>
              </form>

              {alertResult && (
                <div className="mt-6 p-4 rounded-lg bg-muted border">
                  <h4 className="font-semibold mb-3">Result:</h4>
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <span className="text-muted-foreground">Alert Triggered:</span>
                      <Badge variant={alertResult.isAlert ? 'destructive' : 'default'}>
                        {alertResult.isAlert ? 'YES' : 'NO'}
                      </Badge>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-muted-foreground">Type:</span>
                      <span className="font-mono">{alertResult.alertType}</span>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Message:</span>
                      <p className="mt-1">{alertResult.message}</p>
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </DashboardLayout>
  );
};

export default AdminDashboard;