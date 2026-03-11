import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { authService } from '@/services/authService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  LogOut,
  ShieldCheck,
  UserCircle,
  Mail,
  Phone,
  Hash,
  Loader2,
  Pencil,
  Save,
  X,
  Smartphone,
  MessageSquare
} from 'lucide-react';
import logo from '@/assets/company-logo.png';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Separator } from '@/components/ui/separator';
import toast from 'react-hot-toast';
import { BackButton } from '@/components/BackButton';

/* ===========================
   ✅ TYPE SAFE PROFILE
=========================== */

interface UserProfile {
  ownerUserId: string;
  name: string;
  email: string;
  mobileNumber?: string;
  alternativeMobileNumber?: string;
  notifyWhatsapp?: boolean;
  notifySms?: boolean;
  notifyEmail?: boolean;
  roles: string | string[];
}

export const AppHeader = () => {

  const { isAuthenticated, isAdmin, isRoot, logout } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const [formData, setFormData] = useState({
    mobileNumber: '',
    alternativeMobileNumber: '',
    notifyWhatsapp: true,
    notifySms: false,
    notifyEmail: false
  });

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleOpenProfile = async () => {
    setIsLoading(true);
    try {
      const res = await authService.getProfile();
      setProfile(res.data);

      setFormData({
        mobileNumber: res.data.mobileNumber || '',
        alternativeMobileNumber: res.data.alternativeMobileNumber || '',
        notifyWhatsapp: true,
        notifySms: Boolean(res.data.notifySms),
        notifyEmail: Boolean(res.data.notifyEmail)
      });
    } catch {
      console.error("Failed to load profile");
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveProfile = async () => {
    setIsSaving(true);
    try {
      const res = await authService.updateProfile({
        mobileNumber: formData.mobileNumber,
        alternativeMobileNumber: formData.alternativeMobileNumber,
        notifyWhatsapp: (!isAdmin && !isRoot) ? true : undefined,
        notifySms: (!isAdmin && !isRoot) ? formData.notifySms : undefined,
        notifyEmail: (!isAdmin && !isRoot) ? formData.notifyEmail : undefined
      });

      setProfile(res.data);
      setIsEditing(false);
      toast.success("Profile updated successfully!");

    } catch {
      toast.error("Failed to update profile");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border/50 bg-background/95 backdrop-blur">
      <div className="container flex h-16 items-center justify-between px-4">

        {/* LOGO */}
        <Link to={isAuthenticated ? '/dashboard' : '/'} className="flex items-center gap-2">
          <div className="flex items-center justify-center">
            <img
              src={logo}
              alt="Freezer Monitoring logo"
              className="h-9 w-auto"
            />
          </div>
          <span className="font-bold text-xl">Freezer Monitoring</span>
        </Link>

        {isAuthenticated && (
          <div className="flex items-center gap-3">

            {(isAdmin || isRoot) && (
              <div className="hidden sm:flex items-center gap-1 bg-red-100 text-red-600 px-3 py-1 rounded-full text-xs font-bold border border-red-200">
                <ShieldCheck className="w-3 h-3" />
                {isRoot ? 'ROOT ADMIN' : 'STAFF ADMIN'}
              </div>
            )}

            {/* PROFILE */}
            <Dialog onOpenChange={(open) => !open && setIsEditing(false)}>
              <DialogTrigger asChild>
                <Button variant="ghost" size="icon" onClick={handleOpenProfile}>
                  <UserCircle className="w-6 h-6" />
                </Button>
              </DialogTrigger>

              <DialogContent className="sm:max-w-md">
                <DialogHeader>
                  <DialogTitle>User Profile</DialogTitle>
                  <DialogDescription>
                    View and update your profile details and notification preferences.
                  </DialogDescription>
                </DialogHeader>

                {isLoading ? (
                  <div className="flex justify-center py-8">
                    <Loader2 className="w-8 h-8 animate-spin" />
                  </div>
                ) : profile ? (
                  <div className="space-y-4">

                    <div className="text-center">
                      <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto">
                        <span className="text-2xl font-bold text-blue-600">
                          {profile.name?.charAt(0).toUpperCase()}
                        </span>
                      </div>
                      <h3 className="font-bold">{profile.name}</h3>
                      <p className="text-sm text-muted-foreground">{profile.roles}</p>
                    </div>

                    <Separator />

                    {!isEditing ? (
                      <>
                        <p>{profile.email}</p>
                        <p>{profile.mobileNumber}</p>
                        <p>{profile.ownerUserId}</p>

                        <Button size="sm" onClick={() => setIsEditing(true)}>
                          <Pencil className="w-4 h-4 mr-2" /> Edit
                        </Button>
                      </>
                    ) : (
                      <div className="space-y-3">

                        <div>
                          <Label htmlFor="mobileNumber">Mobile Number</Label>
                          <Input
                            id="mobileNumber"
                            value={formData.mobileNumber}
                            onChange={(e) => setFormData({...formData, mobileNumber:e.target.value})}
                          />
                        </div>

                        <div>
                          <Label htmlFor="altMobile">Alternative Mobile</Label>
                          <Input
                            id="altMobile"
                            placeholder="Enter alternative number"
                            value={formData.alternativeMobileNumber}
                            onChange={(e)=>setFormData({...formData, alternativeMobileNumber:e.target.value})}
                          />
                        </div>

                        <div className="flex justify-between">
                          <span>Email Alerts</span>
                          <input
                            type="checkbox"
                            aria-label="Email Alerts"
                            checked={formData.notifyEmail}
                            onChange={(e)=>setFormData({...formData, notifyEmail:e.target.checked})}
                          />
                        </div>

                        <div className="flex justify-between">
                          <span>SMS Alerts</span>
                          <input
                            type="checkbox"
                            aria-label="SMS Alerts"
                            checked={formData.notifySms}
                            onChange={(e)=>setFormData({...formData, notifySms:e.target.checked})}
                          />
                        </div>

                        <div className="flex gap-2">
                          <Button onClick={handleSaveProfile} disabled={isSaving}>
                            {isSaving ? <Loader2 className="animate-spin w-4 h-4" /> : <Save className="w-4 h-4" />}
                            Save
                          </Button>

                          <Button variant="outline" onClick={()=>setIsEditing(false)}>
                            <X className="w-4 h-4" /> Cancel
                          </Button>
                        </div>

                      </div>
                    )}

                  </div>
                ) : (
                  <div>Failed to load profile</div>
                )}
              </DialogContent>
            </Dialog>

            {/* BACK BUTTON */}
            <BackButton />

            <Button variant="ghost" size="icon" onClick={handleLogout}>
              <LogOut className="w-5 h-5" />
            </Button>

          </div>
        )}
      </div>
    </header>
  );
};