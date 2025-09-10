// import React, { createContext, useEffect, useState, type ReactNode } from 'react';
// import { KeycloakMiddleware, type KeycloakConfig } from './KeycloakMiddleware';
// import KeycloakSingleton from './KeycloakSingleton';
// import type { KeycloakProfile } from 'keycloak-js';
// import LoadingSpinner from '../component/common/LoadingSpinner';

// export interface AuthContextType {
//     isAuthenticated: boolean;
//     isLoading: boolean;
//     keycloak: KeycloakMiddleware | null;
//     user: KeycloakProfile | null;
//     login: () => Promise<void>;
//     logout: () => Promise<void>;
//     hasRole: (role: string) => boolean;
//     userId: string | null;
//     token: string | null;
//     initializationError: string | null;
//     retryInitialization: () => void;
// }

// const AuthContext = createContext<AuthContextType | undefined>(undefined);

// interface AuthProviderProps {
//     children: ReactNode;
//     keycloakConfig?: KeycloakConfig;
// }

// export const AuthProvider: React.FC<AuthProviderProps> = ({ children, keycloakConfig }) => {
//     const [isAuthenticated, setIsAuthenticated] = useState(false);
//     const [isLoading, setIsLoading] = useState(true);
//     const [keycloak, setKeycloak] = useState<KeycloakMiddleware | null>(null);
//     const [user, setUser] = useState<KeycloakProfile | null>(null);
//     const [userId, setUserId] = useState<string | null>(null);
//     const [token, setToken] = useState<string | null>(null);
//     const [isInitialized, setIsInitialized] = useState(false);
//     const [initializationError, setInitializationError] = useState<string | null>(null);

//     const retryInitialization = () => {
//         setIsInitialized(false);
//         setInitializationError(null);
//         // Reset the singleton to allow fresh initialization
//         KeycloakSingleton.reset();
//     };

//     useEffect(() => {
//         if (isInitialized) {
//             return;
//         }

//         const initKeycloak = async () => {
//             try {
//                 setIsLoading(true);
//                 setIsInitialized(true);
//                 setInitializationError(null);

//                 console.log('Initializing Keycloak...');
//                 const keycloakInstance = await KeycloakSingleton.getInstance(keycloakConfig);
//                 setKeycloak(keycloakInstance);

//                 const authenticated = keycloakInstance.isAuthenticated();
//                 if (authenticated) {
//                     console.log('User is authenticated');
//                     setIsAuthenticated(true);
//                     setUser(keycloakInstance.getUserInfo() || null);
//                     setUserId(keycloakInstance.userId || null);
//                     setToken(keycloakInstance.getToken() || null);
//                 } else {
//                     console.log('User is not authenticated');
//                     setIsAuthenticated(false);
//                     // Don't automatically redirect here - let the AuthorityGuard handle it
//                 }
//             } catch (error) {
//                 console.error('Failed to initialize Keycloak:', error);
//                 setIsAuthenticated(false);
//                 setInitializationError(error instanceof Error ? error.message : 'Unknown authentication error');
//                 // Don't reset isInitialized to prevent infinite loop
//                 // The user can retry manually if needed
//             } finally {
//                 setIsLoading(false);
//             }
//         };

//         initKeycloak();

//         // Cleanup function
//         return () => {
//             console.log('AuthProvider cleanup');
//         };
//     }, [isInitialized, keycloakConfig]);

//     const login = async () => {
//         if (keycloak) {
//             try {
//                 await keycloak.login();
//             } catch (error) {
//                 console.error('Login failed:', error);
//                 throw error;
//             }
//         }
//     };

//     const logout = async () => {
//         if (keycloak) {
//             try {
//                 await keycloak.logout();
//                 setIsAuthenticated(false);
//                 setUser(null);
//                 setUserId(null);
//                 setToken(null);
//             } catch (error) {
//                 console.error('Logout failed:', error);
//                 throw error;
//             }
//         }
//     };

//     const hasRole = (role: string): boolean => {
//         return keycloak?.hasRole(role) || false;
//     };

//     const contextValue: AuthContextType = {
//         isAuthenticated,
//         isLoading,
//         keycloak,
//         user,
//         login,
//         logout,
//         hasRole,
//         userId,
//         token,
//         initializationError,
//         retryInitialization,
//     };

//     // Show loading spinner while initializing
//     if (isLoading) {
//         return (
//             <LoadingSpinner message='Initializing authentication...'>
//                 <p>Please wait while we set up your session.</p>
//             </LoadingSpinner>
//         );
//     }

//     // Show error message if initialization failed
//     if (initializationError) {
//         return (
//             <div
//                 style={{
//                     display: 'flex',
//                     flexDirection: 'column',
//                     alignItems: 'center',
//                     justifyContent: 'center',
//                     minHeight: '100vh',
//                     padding: '20px',
//                     textAlign: 'center',
//                 }}>
//                 <h2>Authentication Error</h2>
//                 <p>Failed to initialize authentication system.</p>
//                 <p style={{ color: 'red', fontFamily: 'monospace', fontSize: '12px' }}>{initializationError}</p>
//                 <button
//                     onClick={retryInitialization}
//                     style={{
//                         padding: '10px 20px',
//                         backgroundColor: '#007bff',
//                         color: 'white',
//                         border: 'none',
//                         borderRadius: '4px',
//                         cursor: 'pointer',
//                         marginTop: '20px',
//                     }}>
//                     Retry
//                 </button>
//                 <p style={{ marginTop: '20px', fontSize: '12px', color: '#666' }}>Make sure your Keycloak server is running and accessible.</p>
//             </div>
//         );
//     }

//     return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
// };

// export default AuthContext;
