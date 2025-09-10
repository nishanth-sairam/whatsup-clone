import type keycloak from 'keycloak-js';
import type { Middleware } from './runtime';
import type { RequestContext, ResponseContext, ErrorContext, FetchParams } from './runtime';

export class KeycloakMiddleware implements Middleware {

    private keycloak: keycloak;

    constructor(keycloak: keycloak) {
        this.keycloak = keycloak;
    }

    async pre(context: RequestContext): Promise<FetchParams | void> {
        // Ensure Keycloak is authenticated
        if (!this.keycloak.authenticated) {
            console.warn('Keycloak not authenticated, attempting login...');
            try {
                await this.keycloak.login();
            } catch (error) {
                console.error('Failed to authenticate with Keycloak:', error);
                throw new Error('Authentication required');
            }
        }

        // Update token if needed
        try {
            await this.keycloak.updateToken(30); // Refresh if expires within 30 seconds
        } catch (error) {
            console.error('Failed to refresh token:', error);
            try {
                await this.keycloak.login();
            } catch (loginError) {
                console.error('Failed to re-authenticate:', loginError);
                throw new Error('Authentication failed');
            }
        }

        // Add Authorization header with Bearer token
        const headers = new Headers(context.init.headers);
        if (this.keycloak.token) {
            headers.set('Authorization', `Bearer ${this.keycloak.token}`);
        }

        return {
            url: context.url,
            init: {
                ...context.init,
                headers
            }
        };
    }

    async post(context: ResponseContext): Promise<Response | void> {
        // Handle successful responses
        if (context.response.status === 200 || context.response.status === 201) {
            return context.response;
        }

        // Log non-success responses for debugging
        if (context.response.status >= 400) {
            console.warn(`API call failed with status ${context.response.status}:`, context.url);
        }

        return context.response;
    }

    async onError(context: ErrorContext): Promise<Response | void> {
        // Handle authentication errors
        if (context.response && (context.response.status === 401 || context.response.status === 403)) {
            console.warn('Authentication error detected, attempting to refresh token...');

            try {
                // Try to refresh the token
                await this.keycloak.updateToken(-1); // Force refresh

                // Retry the original request with new token
                const headers = new Headers(context.init.headers);
                if (this.keycloak.token) {
                    headers.set('Authorization', `Bearer ${this.keycloak.token}`);
                }

                const retryInit = {
                    ...context.init,
                    headers
                };

                return await context.fetch(context.url, retryInit);
            } catch (refreshError) {
                console.error('Failed to refresh token, redirecting to login:', refreshError);
                try {
                    await this.keycloak.login();
                } catch (loginError) {
                    console.error('Failed to initiate login:', loginError);
                }
            }
        }

        // Handle other errors
        console.error('API request failed:', {
            url: context.url,
            error: context.error,
            response: context.response
        });

        // Return the original response or re-throw the error
        if (context.response) {
            return context.response;
        }

        throw context.error;
    }

    /**
     * Get the current user's information from Keycloak
     */
    getCurrentUser() {
        if (!this.keycloak.authenticated) {
            return null;
        }
        return this.keycloak.tokenParsed;
    }

    /**
     * Check if the user has a specific role
     */
    hasRole(role: string): boolean {
        if (!this.keycloak.authenticated || !this.keycloak.tokenParsed) {
            return false;
        }

        const realmRoles = this.keycloak.tokenParsed.realm_access?.roles || [];
        return realmRoles.includes(role);
    }

    /**
     * Check if the user has a specific client role
     */
    hasClientRole(clientId: string, role: string): boolean {
        if (!this.keycloak.authenticated || !this.keycloak.tokenParsed) {
            return false;
        }

        const clientRoles = this.keycloak.tokenParsed.resource_access?.[clientId]?.roles || [];
        return clientRoles.includes(role);
    }

    /**
     * Logout the user
     */
    async logout(): Promise<void> {
        try {
            await this.keycloak.logout();
        } catch (error) {
            console.error('Failed to logout:', error);
            throw error;
        }
    }

    /**
     * Get the current access token
     */
    getToken(): string | undefined {
        return this.keycloak.token;
    }

    /**
     * Check if the user is authenticated
     */
    isAuthenticated(): boolean {
        return this.keycloak.authenticated || false;
    }
}