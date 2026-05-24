import { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Leaflet default icon fix
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

type MapPickerProps = {
  lat?: number;
  lng?: number;
  onChange: (lat: number, lng: number) => void;
  className?: string;
};

function LocationMarker({ position, setPosition }: { position: L.LatLng | null; setPosition: (p: L.LatLng) => void }) {
  useMapEvents({
    click(e) {
      setPosition(e.latlng);
    },
  });

  return position === null ? null : (
    <Marker position={position} draggable={true} eventHandlers={{ dragend: (e) => setPosition(e.target.getLatLng()) }} />
  );
}

export function MapPicker({ lat, lng, onChange, className = 'h-64 w-full rounded-md' }: MapPickerProps) {
  const [position, setPosition] = useState<L.LatLng | null>(
    lat && lng ? new L.LatLng(lat, lng) : null
  );

  useEffect(() => {
    if (position) {
      onChange(position.lat, position.lng);
    }
  }, [position, onChange]);
  
  const GOONG_MAP_TILES_KEY = 'f9Xhe392Of8PGBV9BJdesNJlwvqmjgNLuvP1iSHV';
  // Center is somewhere in Vietnam (e.g. Ho Chi Minh City)
  const defaultCenter = new L.LatLng(10.8231, 106.6297);

  return (
    <div className={className}>
      <MapContainer
        center={position || defaultCenter}
        zoom={13}
        scrollWheelZoom={true}
        style={{ height: '100%', width: '100%', zIndex: 10 }}
      >
        <TileLayer
          url={`https://rsapi.goong.io/tile/a/{z}/{x}/{y}.png?api_key=${GOONG_MAP_TILES_KEY}`}
          attribution='&copy; <a href="https://goong.io">Goong</a> contributors'
        />
        <LocationMarker position={position} setPosition={setPosition} />
      </MapContainer>
    </div>
  );
}
