import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { getStaffLocations, StaffLocationDto } from '@/features/staff/api/staffLocationApi';
import { useAuth } from '@/features/auth';

type LocationContextType = {
  locations: StaffLocationDto[];
  activeLocationId: number | null;
  setActiveLocationId: (id: number | null) => void;
  isLoading: boolean;
};

const LocationContext = createContext<LocationContextType | undefined>(undefined);

export function LocationProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [locations, setLocations] = useState<StaffLocationDto[]>([]);
  const [activeLocationId, setActiveLocationId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (user?.userType === 'STAFF' || user?.userType === 'ADMIN') {
      setIsLoading(true);
      getStaffLocations()
        .then((data) => {
          setLocations(data);
          const savedId = localStorage.getItem('seshop.activeLocationId');
          if (savedId && data.some(loc => loc.id === Number(savedId))) {
            setActiveLocationId(Number(savedId));
          } else if (data.length > 0) {
            setActiveLocationId(data[0].id);
          }
        })
        .catch(console.error)
        .finally(() => setIsLoading(false));
    }
  }, [user]);

  const handleSetActiveLocationId = (id: number | null) => {
    setActiveLocationId(id);
    if (id !== null) {
      localStorage.setItem('seshop.activeLocationId', String(id));
    } else {
      localStorage.removeItem('seshop.activeLocationId');
    }
  };

  return (
    <LocationContext.Provider
      value={{ locations, activeLocationId, setActiveLocationId: handleSetActiveLocationId, isLoading }}
    >
      {children}
    </LocationContext.Provider>
  );
}

export function useStaffLocation() {
  const context = useContext(LocationContext);
  if (context === undefined) {
    throw new Error('useStaffLocation must be used within a LocationProvider');
  }
  return context;
}
