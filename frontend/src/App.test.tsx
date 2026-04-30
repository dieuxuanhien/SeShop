import { render, screen } from '@testing-library/react';
import { App } from './App';

describe('App', () => {
  it('renders the customer home shell', () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: /Homepage & Product Browsing/i })).toBeInTheDocument();
  });
});
