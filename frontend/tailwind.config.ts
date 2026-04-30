import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#172033',
        surface: '#F7F8FA',
        primary: '#2563EB',
        success: '#0F9F6E',
        warning: '#B7791F',
        danger: '#DC2626',
      },
      boxShadow: {
        soft: '0 8px 24px rgba(15, 23, 42, 0.08)',
      },
      borderRadius: {
        card: '8px',
      },
    },
  },
  plugins: [],
} satisfies Config;
