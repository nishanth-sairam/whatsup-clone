import React, { useState, useEffect, type ReactNode } from 'react';

interface Props {
    children: ReactNode;
}

interface ErrorInfo {
    hasError: boolean;
    error?: Error;
}

const ErrorBoundary: React.FC<Props> = ({ children }) => {
    const [errorInfo, setErrorInfo] = useState<ErrorInfo>({ hasError: false });

    useEffect(() => {
        const handleError = (error: ErrorEvent) => {
            console.error('ErrorBoundary caught an error:', error.error);
            setErrorInfo({ hasError: true, error: error.error });
        };

        const handleUnhandledRejection = (event: PromiseRejectionEvent) => {
            console.error('ErrorBoundary caught an unhandled promise rejection:', event.reason);
            setErrorInfo({ hasError: true, error: new Error(event.reason) });
        };

        window.addEventListener('error', handleError);
        window.addEventListener('unhandledrejection', handleUnhandledRejection);

        return () => {
            window.removeEventListener('error', handleError);
            window.removeEventListener('unhandledrejection', handleUnhandledRejection);
        };
    }, []);

    const handleRefresh = () => {
        window.location.reload();
    };

    if (errorInfo.hasError) {
        return (
            <div
                style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    minHeight: '100vh',
                    padding: '20px',
                    textAlign: 'center',
                }}>
                <h2>Something went wrong</h2>
                <p>The application encountered an unexpected error.</p>
                {errorInfo.error && (
                    <details style={{ marginTop: '20px' }}>
                        <summary>Error details</summary>
                        <pre
                            style={{
                                color: 'red',
                                fontFamily: 'monospace',
                                fontSize: '12px',
                                textAlign: 'left',
                                backgroundColor: '#f5f5f5',
                                padding: '10px',
                                borderRadius: '4px',
                                marginTop: '10px',
                            }}>
                            {errorInfo.error.message}
                            {'\n'}
                            {errorInfo.error.stack}
                        </pre>
                    </details>
                )}
                <button
                    onClick={handleRefresh}
                    style={{
                        padding: '10px 20px',
                        backgroundColor: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer',
                        marginTop: '20px',
                    }}>
                    Refresh Page
                </button>
            </div>
        );
    }

    return <>{children}</>;
};

export default ErrorBoundary;
