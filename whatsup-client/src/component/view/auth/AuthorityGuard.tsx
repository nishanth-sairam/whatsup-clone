import { type ReactNode, useEffect } from 'react';
import { useAuth } from '../../../hooks/useAuth';
import LoadingSpinner from '../../common/LoadingSpinner';

interface AuthorityGuardProps {
    children: ReactNode;
    authority?: string[];
    userAuthority?: string[];
}

const AuthorityGuard = ({ children, authority }: AuthorityGuardProps) => {
    const { isAuthenticated, isLoading, hasRole, keycloak } = useAuth();

    useEffect(() => {
        // If not loading and not authenticated, redirect to login
        if (!isLoading && !isAuthenticated && keycloak) {
            console.log('User not authenticated, redirecting to login...');
            keycloak.login({
                redirectUri: window.location.origin + window.location.pathname,
            });
        }
    }, [isLoading, isAuthenticated, keycloak]);

    // Show loading state while authentication is being verified
    if (isLoading) {
        return <LoadingSpinner message='Verifying authentication...' />;
    }

    // If not authenticated, show loading message while redirecting
    if (!isAuthenticated) {
        return (
            <div className='flex justify-center items-center min-h-screen bg-telegram-light-background dark:bg-telegram-dark-background'>
                <div className='text-center'>
                    <div className='spinner mb-4'></div>
                    <h5 className='text-lg font-medium text-telegram-light-text dark:text-telegram-dark-text mb-2'>Redirecting to login...</h5>
                    <p className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted'>Please wait while we redirect you to the login page.</p>
                </div>
            </div>
        );
    }

    // Check if user has required authority/role
    if (authority && authority.length > 0) {
        const hasRequiredRole = authority.some(role => hasRole(role));
        if (!hasRequiredRole) {
            return (
                <div className='flex justify-center items-center min-h-screen bg-telegram-light-background dark:bg-telegram-dark-background'>
                    <div className='text-center card-telegram p-8 max-w-md'>
                        <h5 className='text-lg font-semibold text-telegram-light-error dark:text-telegram-dark-error mb-3'>Access Denied</h5>
                        <p className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted mb-2'>
                            You do not have the required permissions to access this page.
                        </p>
                        <p className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted text-sm'>Required roles: {authority.join(', ')}</p>
                    </div>
                </div>
            );
        }
    }

    return <>{children}</>;
};

export default AuthorityGuard;
