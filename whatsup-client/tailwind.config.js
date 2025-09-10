/** @type {import('tailwindcss').Config} */
export default {
    content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
    theme: {
        extend: {
            colors: {
                // Telegram-inspired color palette
                telegram: {
                    // Light theme colors
                    light: {
                        primary: '#0088cc',
                        secondary: '#40a7e3',
                        background: '#ffffff',
                        surface: '#f4f4f5',
                        surfaceVariant: '#e4e4e7',
                        outline: '#d4d4d8',
                        text: '#000000',
                        textSecondary: '#6b7280',
                        textMuted: '#9ca3af',
                        accent: '#0088cc',
                        success: '#00c851',
                        warning: '#ffbb33',
                        error: '#ff4444',
                        messageOut: '#dcf8c6',
                        messageIn: '#ffffff',
                        bubble: '#ffffff',
                    },
                    // Dark theme colors
                    dark: {
                        primary: '#8774e1',
                        secondary: '#7b68ee',
                        background: '#0e1621',
                        surface: '#151e2b',
                        surfaceVariant: '#1e2732',
                        outline: '#2e3a47',
                        text: '#ffffff',
                        textSecondary: '#a8b2c1',
                        textMuted: '#6b7785',
                        accent: '#8774e1',
                        success: '#00d4aa',
                        warning: '#ffa726',
                        error: '#f44336',
                        messageOut: '#2b5278',
                        messageIn: '#182533',
                        bubble: '#182533',
                    },
                },
            },
            fontFamily: {
                sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'Helvetica Neue', 'Arial', 'sans-serif'],
            },
            fontSize: {
                xs: ['11px', '16px'],
                sm: ['12px', '16px'],
                base: ['14px', '20px'],
                lg: ['16px', '24px'],
                xl: ['18px', '28px'],
                '2xl': ['20px', '28px'],
            },
            borderRadius: {
                xs: '4px',
                sm: '6px',
                md: '8px',
                lg: '12px',
                xl: '16px',
                '2xl': '20px',
                message: '18px',
            },
            spacing: {
                18: '4.5rem',
                88: '22rem',
            },
            animation: {
                'fade-in': 'fadeIn 0.3s ease-in',
                'slide-in': 'slideIn 0.3s ease-out',
                'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
            },
            keyframes: {
                fadeIn: {
                    '0%': { opacity: '0', transform: 'translateY(10px)' },
                    '100%': { opacity: '1', transform: 'translateY(0)' },
                },
                slideIn: {
                    '0%': { opacity: '0', transform: 'translateY(20px) scale(0.95)' },
                    '100%': { opacity: '1', transform: 'translateY(0) scale(1)' },
                },
            },
            boxShadow: {
                telegram: '0 1px 2px 0 rgba(16, 24, 40, 0.05)',
                'telegram-lg': '0 4px 6px -1px rgba(16, 24, 40, 0.1), 0 2px 4px -1px rgba(16, 24, 40, 0.06)',
                'telegram-xl': '0 10px 15px -3px rgba(16, 24, 40, 0.1), 0 4px 6px -2px rgba(16, 24, 40, 0.05)',
                message: '0 1px 2px rgba(0, 0, 0, 0.1)',
                'message-dark': '0 1px 2px rgba(0, 0, 0, 0.3)',
            },
        },
    },
};
