import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getLocations, locationsFromBalances, type LocationSummary, type AdminLocation, createLocation, updateLocation, type LocationMutationRequest } from '@/features/admin/api/adminApi';
import { getInventoryBalances, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';
import { Modal } from '@/shared/ui/Modal';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { MapPicker } from '@/shared/ui/MapPicker';
import { getProvinces, getDistricts, getWards, type Province, type District, type Ward } from '@/features/commerce/api/checkoutApi';

export function LocationsManagement() {
  const [locations, setLocations] = useState<LocationSummary[]>([]);
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState<LocationMutationRequest>({
    code: '',
    displayName: '',
    locationType: 'STORE',
    status: 'ACTIVE',
    latitude: undefined,
    longitude: undefined,
    addressText: '',
    provinceId: undefined,
    districtId: undefined,
    wardCode: '',
  });

  const [provinces, setProvinces] = useState<Province[]>([]);
  const [districts, setDistricts] = useState<District[]>([]);
  const [wards, setWards] = useState<Ward[]>([]);

  useEffect(() => {
    getProvinces().then(setProvinces).catch(() => {});
  }, []);

  useEffect(() => {
    if (formData.provinceId) {
      getDistricts(formData.provinceId).then(setDistricts).catch(() => {});
    } else {
      setDistricts([]);
    }
  }, [formData.provinceId]);

  useEffect(() => {
    if (formData.districtId) {
      getWards(formData.districtId).then(setWards).catch(() => {});
    } else {
      setWards([]);
    }
  }, [formData.districtId]);

  const resetForm = () => {
    setFormData({
      code: '',
      displayName: '',
      locationType: 'STORE',
      status: 'ACTIVE',
      latitude: undefined,
      longitude: undefined,
      addressText: '',
      provinceId: undefined,
      districtId: undefined,
      wardCode: '',
    });
    setEditingId(null);
  };

  function loadLocations() {
    setIsLoading(true);
    Promise.all([getLocations(), getInventoryBalances(1, 100)])
      .then(([locationRows, page]) => {
        setBalances(page.items);
        const stockLocations = locationsFromBalances(page.items);
        setLocations(locationRows.map((location) => {
          const stock = stockLocations.find((row) => row.id === location.id);
          return {
            id: location.id,
            code: location.code,
            name: location.displayName,
            type: location.locationType,
            status: location.status,
            latitude: location.latitude,
            longitude: location.longitude,
            addressText: location.addressText,
            skus: stock?.skus ?? 0,
          };
        }));
      })
      .catch(() => {
        setBalances([]);
        setLocations([]);
      })
      .finally(() => setIsLoading(false));
  }

  useEffect(() => {
    loadLocations();
  }, []);

  const totalsByLocation = locations.map((location) => {
    const locationBalances = balances.filter((balance) => balance.locationId === location.id);
    const available = locationBalances.reduce((sum, balance) => sum + Number(balance.availableQty ?? 0), 0);
    const reserved = locationBalances.reduce((sum, balance) => sum + Number(balance.reservedQty ?? 0), 0);
    return { ...location, available, reserved };
  });

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingId) {
        await updateLocation(editingId, formData);
      } else {
        await createLocation(formData);
      }
      setIsModalOpen(false);
      resetForm();
      loadLocations();
    } catch (err) {
      console.error(err);
    }
  };

  const handleEdit = (location: any) => {
    setEditingId(location.id);
    setFormData({
      code: location.code || '',
      displayName: location.name,
      locationType: location.type || 'STORE',
      status: location.status || 'ACTIVE',
      latitude: location.latitude,
      longitude: location.longitude,
      addressText: location.addressText || '',
      provinceId: location.provinceId,
      districtId: location.districtId,
      wardCode: location.wardCode || '',
    });
    setIsModalOpen(true);
  };

  return (
    <PageScaffold
      title="Locations Management"
      viewCode="ADMIN_003"
      purpose="Review store and storage locations through current inventory balances."
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Location Directory</h2>
              <p className="mt-1 text-xs text-ink/50">Monitor store and storage hubs with inventory snapshots.</p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={loadLocations}>Refresh</Button>
              <Button onClick={() => { resetForm(); setIsModalOpen(true); }}>Add Location</Button>
            </div>
          </div>
          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="text-xs uppercase text-ink/50">
	                <tr>
	                  <th className="px-3 py-2">Code</th>
	                  <th className="px-3 py-2">Location</th>
	                  <th className="px-3 py-2">Type</th>
	                  <th className="px-3 py-2 text-right">SKU Count</th>
	                  <th className="px-3 py-2 text-right">Action</th>
	                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10">
                {isLoading ? (
                  <tr>
	                    <td colSpan={5} className="px-3 py-6 text-center text-sm text-ink/60">Loading locations...</td>
	                  </tr>
	                ) : totalsByLocation.length === 0 ? (
	                  <tr>
	                    <td colSpan={5} className="px-3 py-6 text-center text-sm text-ink/60">No locations returned.</td>
	                  </tr>
	                ) : totalsByLocation.map((location) => (
	                  <tr key={location.id} className="text-ink/80">
	                    <td className="px-3 py-3 font-semibold text-ink">{location.code ?? `LOC-${location.id}`}</td>
	                    <td className="px-3 py-3">{location.name}</td>
	                    <td className="px-3 py-3">{location.type ?? 'STORE'}</td>
	                    <td className="px-3 py-3 text-right">{location.skus} SKUs / {location.available} units</td>
                    <td className="px-3 py-3 text-right">
                      <Button variant="secondary" size="sm" onClick={() => handleEdit(location)}>Edit</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-2">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Inventory Heatmap</h2>
            <div className="mt-4 grid gap-3">
              {totalsByLocation.map((location) => {
                const max = Math.max(...totalsByLocation.map((item) => item.available), 1);
                return (
                  <div key={location.id}>
                    <div className="flex justify-between text-xs text-ink/55">
                      <span>{location.name}</span>
                      <span>{location.available} available</span>
                    </div>
                    <div className="mt-1 h-2 rounded-full bg-ink/10">
                      <div className="h-full rounded-full bg-primary" style={{ width: `${Math.max(8, (location.available / max) * 100)}%` }} />
                    </div>
                  </div>
                );
              })}
            </div>
          </Card>
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Transfer Queue</h2>
            <ul className="mt-4 grid gap-2 text-sm text-ink/70">
              {totalsByLocation.slice(0, 4).map((location) => (
                <li key={location.id} className="flex justify-between rounded-md border border-primary/15 bg-ink/5 p-3">
                  <span>{location.name}</span>
                  <span>{location.reserved} reserved</span>
                </li>
              ))}
            </ul>
          </Card>
        </div>
      </div>

      <Modal open={isModalOpen} onClose={() => setIsModalOpen(false)} title={editingId ? 'Edit Location' : 'New Location'}>
        <form onSubmit={handleSave} className="p-6 grid gap-4 w-full max-w-lg min-w-[320px] md:min-w-[500px]">
          <div className="grid grid-cols-2 gap-4">
            <Input label="Code" value={formData.code} onChange={e => setFormData({...formData, code: e.target.value})} required />
            <Input label="Name" value={formData.displayName} onChange={e => setFormData({...formData, displayName: e.target.value})} required />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Select 
              label="Type" 
              value={formData.locationType} 
              onChange={e => setFormData({...formData, locationType: e.target.value as any})}
              options={[
                { value: 'STORE', label: 'Store' },
                { value: 'STORAGE', label: 'Storage' }
              ]}
            />
            <Select 
              label="Status" 
              value={formData.status} 
              onChange={e => setFormData({...formData, status: e.target.value as any})}
              options={[
                { value: 'ACTIVE', label: 'Active' },
                { value: 'INACTIVE', label: 'Inactive' }
              ]}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Select
              label="Province / City"
              value={formData.provinceId?.toString() || ''}
              onChange={(e) => setFormData({ ...formData, provinceId: Number(e.target.value) || undefined, districtId: undefined, wardCode: '' })}
              options={[
                { value: '', label: 'Select Province' },
                ...provinces.map((p) => ({ value: p.ProvinceID.toString(), label: p.ProvinceName }))
              ]}
            />
            <Select
              label="District"
              value={formData.districtId?.toString() || ''}
              onChange={(e) => setFormData({ ...formData, districtId: Number(e.target.value) || undefined, wardCode: '' })}
              disabled={!formData.provinceId}
              options={[
                { value: '', label: 'Select District' },
                ...districts.map((d) => ({ value: d.DistrictID.toString(), label: d.DistrictName }))
              ]}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Select
              label="Ward"
              value={formData.wardCode || ''}
              onChange={(e) => setFormData({ ...formData, wardCode: e.target.value })}
              disabled={!formData.districtId}
              options={[
                { value: '', label: 'Select Ward' },
                ...wards.map((w) => ({ value: w.WardCode, label: w.WardName }))
              ]}
            />
            <Input label="Address Details (Street, House No.)" value={formData.addressText} onChange={e => setFormData({...formData, addressText: e.target.value})} />
          </div>
          <div>
            <label className="text-sm font-medium mb-2 block">Pin Location</label>
            <MapPicker 
              lat={formData.latitude} 
              lng={formData.longitude} 
              onChange={(lat, lng) => setFormData({...formData, latitude: lat, longitude: lng})} 
            />
          </div>
          <div className="flex justify-end gap-2 mt-4">
            <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button type="submit">Save Location</Button>
          </div>
        </form>
      </Modal>
    </PageScaffold>
  );
}
