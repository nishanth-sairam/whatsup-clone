import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { allRoutes } from './component/config/route.config';
import { useMemo } from 'react';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import Keycloak from 'keycloak-js';
import { ThemeProvider } from './component/common/ThemeContext';

function App() {
    const keycloakConfig = useMemo(
        () => ({
            url: import.meta.env?.VITE_KEYCLOAK_URL,
            realm: import.meta.env?.VITE_KEYCLOAK_REALM,
            clientId: import.meta.env?.VITE_KEYCLOAK_CLIENT_ID,
        }),
        []
    );

    const keycloak = useMemo(() => {
        // Validate configuration before creating Keycloak instance
        if (!keycloakConfig.url || !keycloakConfig.realm || !keycloakConfig.clientId) {
            console.error('Keycloak configuration is incomplete:', keycloakConfig);
            return null;
        }
        return new Keycloak(keycloakConfig);
    }, [keycloakConfig]);

    console.log('Keycloak Config:', keycloakConfig);

    // Show error if keycloak configuration is incomplete
    if (!keycloak) {
        return (
            <div className='flex items-center justify-center min-h-screen bg-red-50'>
                <div className='text-center p-6 bg-white rounded-lg shadow-lg'>
                    <h2 className='text-xl font-bold text-red-600 mb-2'>Configuration Error</h2>
                    <p className='text-gray-700 mb-4'>Keycloak configuration is missing or incomplete.</p>
                    <div className='text-sm text-gray-600'>
                        <p>Please ensure the following environment variables are set:</p>
                        <ul className='mt-2 text-left'>
                            <li>• VITE_KEYCLOAK_URL: {keycloakConfig.url || 'Missing'}</li>
                            <li>• VITE_KEYCLOAK_REALM: {keycloakConfig.realm || 'Missing'}</li>
                            <li>• VITE_KEYCLOAK_CLIENT_ID: {keycloakConfig.clientId || 'Missing'}</li>
                        </ul>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <ThemeProvider>
            <ReactKeycloakProvider
                authClient={keycloak}
                initOptions={{
                    onLoad: 'login-required',
                    checkLoginIframe: false,
                    enableLogging: true,
                }}
                onEvent={(event, error) => {
                    console.log('Keycloak event:', event, error);
                }}
                onTokens={tokens => {
                    console.log('Keycloak tokens:', tokens);
                }}>
                <BrowserRouter>
                    <Routes>
                        <Route path='/' element={<Navigate to='/home' replace />} />
                        {allRoutes.map(route => {
                            const { key, path, component: Component } = route;
                            return <Route key={key} path={path} element={<Component />} />;
                        })}
                    </Routes>
                </BrowserRouter>
            </ReactKeycloakProvider>
        </ThemeProvider>
    );
}

export default App;
