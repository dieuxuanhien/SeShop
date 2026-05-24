import { useEffect, useState } from 'react';
import { UserRound, KeyRound, ShieldCheck } from 'lucide-react';
import { useAuth } from '@/features/auth';
import { getMe, updateProfile, updatePassword } from '@/features/auth/api/authApi';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function StaffAdminProfile() {
  const { user, setAuth } = useAuth();
  const [isSaved, setIsSaved] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [pwdMsg, setPwdMsg] = useState('');

  useEffect(() => {
    getMe().then((data) => {
      setDisplayName(data.username);
      setEmail(data.email || '');
      setPhone(data.phoneNumber || '');
      setAuth(localStorage.getItem('seshop.accessToken')!, data);
    }).catch(console.error);
  }, [setAuth]);

  const handleProfileSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      const data = await updateProfile({ username: displayName, email, phoneNumber: phone });
      setAuth(localStorage.getItem('seshop.accessToken')!, data);
      setIsSaved(true);
      setTimeout(() => setIsSaved(false), 3000);
    } catch (e) {
      console.error('Failed to update profile', e);
    }
  };

  const handlePasswordSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      await updatePassword({ currentPassword, newPassword });
      setPwdMsg('Password updated successfully');
      setCurrentPassword('');
      setNewPassword('');
      setTimeout(() => setPwdMsg(''), 3000);
    } catch (e) {
      setPwdMsg('Failed to update password');
      setTimeout(() => setPwdMsg(''), 3000);
    }
  };

  return (
    <PageScaffold
      title={`${user?.userType === 'ADMIN' ? 'Admin' : 'Staff'} Account & Security`}
      purpose="Manage your internal account details, view permissions, and configure security settings."
    >
      <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_340px]">
        <div className="flex flex-col gap-5">
          <Card className="border-primary/20 bg-surface/95 p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="flex size-12 items-center justify-center rounded-full bg-primary/15 text-primary">
                  <UserRound size={22} />
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-ink">{user?.username ?? 'Internal User'}</h2>
                  <p className="text-sm text-ink/55">{user?.userType ?? 'STAFF'}</p>
                </div>
              </div>
              <Badge variant="success">Active</Badge>
            </div>

            <form
              className="mt-6 grid gap-4 md:grid-cols-2"
              onSubmit={handleProfileSubmit}
            >
              <Input label="Display Name" value={displayName} onChange={(event) => setDisplayName(event.target.value)} required />
              <Input label="Email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
              <Input label="Phone" value={phone} onChange={(event) => setPhone(event.target.value)} required />
              <div className="md:col-span-2 flex flex-wrap items-center justify-between gap-3 border-t border-primary/15 pt-4">
                <span className="text-sm text-ink/55">{isSaved ? 'Profile saved successfully.' : ''}</span>
                <Button type="submit">Save Profile</Button>
              </div>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
             <div className="flex items-center gap-3 mb-4">
                <div className="flex size-10 items-center justify-center rounded-full bg-primary/15 text-primary">
                  <KeyRound size={18} />
                </div>
                <div>
                  <h2 className="text-md font-semibold text-ink">Account Security</h2>
                </div>
              </div>
              <form
                className="grid gap-4 md:grid-cols-2"
                onSubmit={handlePasswordSubmit}
              >
                <Input label="Current Password" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
                <Input label="New Password" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required minLength={8} />
                <div className="md:col-span-2 flex flex-wrap items-center justify-between gap-3 border-t border-primary/15 pt-4">
                  <span className={`text-sm ${pwdMsg.includes('Failed') ? 'text-danger' : 'text-success'}`}>{pwdMsg}</span>
                  <Button type="submit" variant="secondary">Change Password</Button>
                </div>
              </form>
          </Card>
        </div>

        <div className="flex flex-col gap-5">
          <Card className="border-primary/20 bg-surface/95 p-5">
            <div className="flex items-center gap-3 mb-4">
              <div className="flex size-10 items-center justify-center rounded-full bg-primary/15 text-primary">
                <ShieldCheck size={18} />
              </div>
              <div>
                <h2 className="text-md font-semibold text-ink">Roles & Permissions</h2>
              </div>
            </div>
            <div className="mt-4 grid gap-3 text-sm text-ink/70">
              <div>
                <span className="font-semibold text-ink/90">Roles:</span>
                <div className="flex flex-wrap gap-2 mt-2">
                  {user?.roles?.map((role) => (
                    <Badge key={role} variant="info">{role}</Badge>
                  ))}
                  {(!user?.roles || user.roles.length === 0) && 'No specific roles'}
                </div>
              </div>
              <div className="mt-3">
                <span className="font-semibold text-ink/90">Permissions:</span>
                <div className="flex flex-wrap gap-2 mt-2">
                  {user?.permissions?.map((perm) => (
                    <span key={perm} className="text-xs bg-surface border border-primary/20 px-2 py-1 rounded text-ink/70">
                      {perm}
                    </span>
                  ))}
                  {(!user?.permissions || user.permissions.length === 0) && 'No specific permissions'}
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
