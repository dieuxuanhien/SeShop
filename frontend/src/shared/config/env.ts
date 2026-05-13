export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  stripePublishableKey: import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY ?? '',
};
