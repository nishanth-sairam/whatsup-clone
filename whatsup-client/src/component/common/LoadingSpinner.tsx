import { type ReactNode } from 'react';

interface LoadingSpinnerProps {
    message?: string;
    children?: ReactNode;
}

const LoadingSpinner = ({ message = 'Loading...', children }: LoadingSpinnerProps) => {
    return (
        <div className='flex flex-col justify-center items-center min-h-screen bg-telegram-light-background dark:bg-telegram-dark-background'>
            <div className='spinner mb-4'></div>
            <h5 className='text-telegram-light-primary dark:text-telegram-dark-primary text-lg font-medium'>{message}</h5>
            {children && <div className='mt-3 text-center text-telegram-light-textMuted dark:text-telegram-dark-textMuted'>{children}</div>}
        </div>
    );
};

export default LoadingSpinner;
