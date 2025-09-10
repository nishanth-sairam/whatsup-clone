import React from 'react';

export interface AuthRoute {
    key: string;
    path: string;
    component: React.ComponentType<unknown>;
    authority?: Array<'admin' | 'user' | 'guest'>;
}

export const authRoutes: Array<AuthRoute> = [
    {
        key: 'auth.login',
        path: '/login',
        component: React.lazy(() => import('../../view/auth/login')),
        authority: [],
    },
    {
        key: 'auth.register',
        path: '/register',
        component: React.lazy(() => import('../../view/auth/register')),
        authority: [],
    },
    {
        key: 'auth.logout',
        path: '/logout',
        component: React.lazy(() => import('../../view/auth/logout')),
        authority: [],
    },
];
