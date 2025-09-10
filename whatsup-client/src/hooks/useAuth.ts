import { useKeycloak } from '@react-keycloak/web';
import type { KeycloakInstance } from 'keycloak-js';

export interface AuthContextType {
    keycloak: KeycloakInstance;
    initialized: boolean;
    isAuthenticated: boolean;
    isLoading: boolean;
    hasRole: (role: string) => boolean;
}

export const useAuth = (): AuthContextType => {
    const { keycloak, initialized } = useKeycloak();

    const hasRole = (role: string): boolean => {
        if (!keycloak.authenticated || !keycloak.realmAccess?.roles) {
            return false;
        }
        return keycloak.realmAccess.roles.includes(role);
    };

    return {
        keycloak,
        initialized,
        isAuthenticated: keycloak.authenticated || false,
        isLoading: !initialized,
        hasRole
    };
};