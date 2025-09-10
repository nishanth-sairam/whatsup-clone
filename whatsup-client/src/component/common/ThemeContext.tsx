import React, { createContext, useState, useEffect } from 'react';

export type Theme = 'dark' | 'light';

export interface ThemeContextType {
    theme: Theme;
    toggleTheme: () => void;
    setTheme: (theme: Theme) => void;
}

export const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

interface ThemeProviderProps {
    children: React.ReactNode;
}

export const ThemeProvider: React.FC<ThemeProviderProps> = ({ children }) => {
    const [theme, setThemeState] = useState<Theme>(() => {
        // Check localStorage for saved theme preference
        const savedTheme = localStorage.getItem('whatsup-theme') as Theme;
        return savedTheme || 'dark';
    });

    useEffect(() => {
        // Apply theme to document for Tailwind dark mode
        if (theme === 'dark') {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }

        // Save theme preference to localStorage
        localStorage.setItem('whatsup-theme', theme);
    }, [theme]);

    const toggleTheme = () => {
        setThemeState(prev => (prev === 'dark' ? 'light' : 'dark'));
    };

    const setTheme = (newTheme: Theme) => {
        setThemeState(newTheme);
    };

    const value = {
        theme,
        toggleTheme,
        setTheme,
    };

    return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
};
