import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Montserrat', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        serif: ['Playfair Display', 'Times New Roman', 'serif'],
        display: ['Playfair Display', 'Times New Roman', 'serif'],
      },
      colors: {
        ink: '#0A0A0A', // Darker black for true luxury
        surface: '#FDFBF7', // Very subtle warm off-white
        surfaceMuted: '#F3EADC',
        primary: '#D4AF37', // True metallic gold
        primaryStrong: '#AA8822',
        highlight: '#F9F1D8',
        success: '#2F6F52',
        warning: '#B8892B',
        danger: '#B23B2D',
      },
      boxShadow: {
        soft: '0 14px 40px rgba(10, 10, 10, 0.25)',
      },
      borderRadius: {
        card: '8px',
      },
      animation: {
        'fade-in-up': 'fadeInUp 0.8s ease-out forwards',
        'fade-in': 'fadeIn 1s ease-out forwards',
      },
      keyframes: {
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
      },
    },
  },
  plugins: [],
} satisfies Config;
