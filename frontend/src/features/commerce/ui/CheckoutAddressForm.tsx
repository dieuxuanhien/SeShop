import { Input } from '@/shared/ui/Input';
import type { Province, District, Ward } from '@/features/commerce/api/checkoutApi';

type AddressState = {
  fullName: string;
  phoneNumber: string;
  line1: string;
  ward: string;
  district: string;
  city: string;
};

type CheckoutAddressFormProps = {
  address: AddressState;
  setAddress: (addr: AddressState) => void;
  provinces: Province[];
  districts: District[];
  wards: Ward[];
  selectedProvinceId: number | null;
  setSelectedProvinceId: (id: number) => void;
  selectedDistrictId: number | null;
  setSelectedDistrictId: (id: number) => void;
  selectedWardCode: string;
  handleWardChange: (code: string) => void;
};

export function CheckoutAddressForm({
  address,
  setAddress,
  provinces,
  districts,
  wards,
  selectedProvinceId,
  setSelectedProvinceId,
  selectedDistrictId,
  setSelectedDistrictId,
  selectedWardCode,
  handleWardChange,
}: CheckoutAddressFormProps) {
  return (
    <div className="grid grid-cols-1 gap-4 rounded-md border border-primary/20 bg-surface p-5 md:grid-cols-2">
      <Input
        label="Full Name"
        value={address.fullName}
        onChange={(e) => setAddress({ ...address, fullName: e.target.value })}
        required
      />
      <Input
        label="Phone Number"
        value={address.phoneNumber}
        onChange={(e) => setAddress({ ...address, phoneNumber: e.target.value })}
        required
      />
      <div className="md:col-span-2">
        <Input
          label="Address Line 1 (Street, House Number)"
          value={address.line1}
          onChange={(e) => setAddress({ ...address, line1: e.target.value })}
          required
        />
      </div>

      {provinces.length === 0 ? (
        <>
          <Input
            label="Province / City"
            value={address.city}
            onChange={(e) => setAddress({ ...address, city: e.target.value })}
            required
          />
          <Input
            label="District"
            value={address.district}
            onChange={(e) => setAddress({ ...address, district: e.target.value })}
            required
          />
          <div className="md:col-span-2">
            <Input
              label="Ward / Commune"
              value={address.ward}
              onChange={(e) => setAddress({ ...address, ward: e.target.value })}
              required
            />
          </div>
        </>
      ) : (
        <>
          <div className="flex flex-col gap-1">
            <label htmlFor="checkout-province" className="text-xs font-medium text-surface">Province / City</label>
            <select
              id="checkout-province"
              value={selectedProvinceId ?? ''}
              onChange={(e) => setSelectedProvinceId(Number(e.target.value))}
              className="w-full rounded-md border border-primary/20 bg-surface p-2.5 text-sm text-ink focus:border-primary focus:outline-none"
              required
            >
              <option value="">Select Province / City</option>
              {provinces.map((p) => (
                <option key={p.ProvinceID} value={p.ProvinceID}>{p.ProvinceName}</option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="checkout-district" className="text-xs font-medium text-surface">District</label>
            <select
              id="checkout-district"
              value={selectedDistrictId ?? ''}
              onChange={(e) => setSelectedDistrictId(Number(e.target.value))}
              disabled={!selectedProvinceId}
              className="w-full rounded-md border border-primary/20 bg-surface p-2.5 text-sm text-ink focus:border-primary focus:outline-none disabled:opacity-50"
              required
            >
              <option value="">Select District</option>
              {districts.map((d) => (
                <option key={d.DistrictID} value={d.DistrictID}>{d.DistrictName}</option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-1 md:col-span-2">
            <label htmlFor="checkout-ward" className="text-xs font-medium text-surface">Ward / Commune</label>
            <select
              id="checkout-ward"
              value={selectedWardCode}
              onChange={(e) => handleWardChange(e.target.value)}
              disabled={!selectedDistrictId}
              className="w-full rounded-md border border-primary/20 bg-surface p-2.5 text-sm text-ink focus:border-primary focus:outline-none disabled:opacity-50"
              required
            >
              <option value="">Select Ward / Commune</option>
              {wards.map((w) => (
                <option key={w.WardCode} value={w.WardCode}>{w.WardName}</option>
              ))}
            </select>
          </div>
        </>
      )}
    </div>
  );
}
