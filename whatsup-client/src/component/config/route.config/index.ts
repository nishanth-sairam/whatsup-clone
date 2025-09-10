import { appRoutes } from './appRoute';
import { authRoutes } from './authRoute';

export const allRoutes = [...appRoutes, ...authRoutes];
