import React, { useEffect, useState } from 'react';

interface ToastProps {
    message: string;
    type: 'success' | 'error' | 'warning' | 'info';
    show: boolean;
    onClose: () => void;
    duration?: number;
}

const ToastNotification: React.FC<ToastProps> = ({ message, type, show, onClose, duration = 3000 }) => {
    const [visible, setVisible] = useState(show);

    useEffect(() => {
        setVisible(show);
        if (show && duration > 0) {
            const timer = setTimeout(() => {
                setVisible(false);
                onClose();
            }, duration);
            return () => clearTimeout(timer);
        }
    }, [show, duration, onClose]);

    if (!visible) return null;

    const getToastClass = () => {
        switch (type) {
            case 'success':
                return 'bg-success';
            case 'error':
                return 'bg-danger';
            case 'warning':
                return 'bg-warning';
            case 'info':
                return 'bg-info';
            default:
                return 'bg-primary';
        }
    };

    const getIcon = () => {
        switch (type) {
            case 'success':
                return 'fas fa-check-circle';
            case 'error':
                return 'fas fa-exclamation-circle';
            case 'warning':
                return 'fas fa-exclamation-triangle';
            case 'info':
                return 'fas fa-info-circle';
            default:
                return 'fas fa-bell';
        }
    };

    return (
        <div className='position-fixed top-0 end-0 p-3' style={{ zIndex: 1055 }}>
            <div className={`toast show ${getToastClass()} text-white`} role='alert'>
                <div className='d-flex'>
                    <div className='toast-body d-flex align-items-center'>
                        <i className={`${getIcon()} me-2`}></i>
                        {message}
                    </div>
                    <button
                        type='button'
                        className='btn-close btn-close-white me-2 m-auto'
                        onClick={() => {
                            setVisible(false);
                            onClose();
                        }}></button>
                </div>
            </div>
        </div>
    );
};

export default ToastNotification;
