import React from 'react';
import type { AuthRoute } from './authRoute';

export const appRoutes: Array<AuthRoute> = [
    {
        key: 'app.home',
        path: '/home',
        component: React.lazy(() => import('../../view/home/Home')),
        authority: ['admin', 'user'],
    },
];
