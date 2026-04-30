import { BrowserRouter } from 'react-router-dom';
import { AppProviders } from './app/providers/AppProviders';
import { AppRoutes } from './app/router/routes';

export function App() {
  return (
    <AppProviders>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AppProviders>
  );
}
