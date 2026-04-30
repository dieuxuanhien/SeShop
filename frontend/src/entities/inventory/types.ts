export type LocationType = 'STORE' | 'STORAGE';
export type LocationStatus = 'ACTIVE' | 'INACTIVE';

export type StockAvailability = {
  locationId: number;
  locationName: string;
  availableQty: number;
  updatedAt: string;
};
