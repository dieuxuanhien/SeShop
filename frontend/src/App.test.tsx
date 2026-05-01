import { render, screen } from '@testing-library/react';
import { App } from './App';

describe('App', () => {
  it('renders the customer home page', () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: /Elegance\s+Redefined/i })).toBeInTheDocument();
  });
});
