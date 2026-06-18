/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#1d4ed8',
          light: '#2563eb',
          dark: '#1e40af',
          50: '#eff6ff',
          100: '#dbeafe',
          500: '#2563eb',
          600: '#1d4ed8',
          700: '#1e40af',
          900: '#1e3a8a'
        },
        ink: {
          DEFAULT: '#0f172a',
          muted: '#475569',
          soft: '#64748b',
          light: '#94a3b8'
        },
        surface: {
          DEFAULT: '#ffffff',
          soft: '#f8fafc',
          gray: '#f1f5f9'
        },
        success: '#059669',
        warning: '#ea580c',
        danger: '#dc2626',
        paid: '#16a34a'
      },
      borderRadius: {
        'card': '20px',
        'xl2': '20px',
        'pill': '9999px'
      },
      boxShadow: {
        'card': '0 2px 8px rgba(15, 23, 42, 0.04), 0 1px 3px rgba(15, 23, 42, 0.03)',
        'card-hover': '0 8px 24px rgba(15, 23, 42, 0.08)',
        'nav': '0 -4px 20px rgba(15, 23, 42, 0.06)'
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'sans-serif']
      }
    },
  },
  plugins: [],
}
