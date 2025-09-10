import React from 'react';

interface UserProfileModalProps {
    show: boolean;
    onHide: () => void;
    userName?: string;
    userEmail?: string;
    lastSeen?: string;
    isOnline?: boolean;
}

const UserProfileModal: React.FC<UserProfileModalProps> = ({
    show,
    onHide,
    userName = 'John Doe',
    userEmail = 'john.doe@example.com',
    lastSeen = 'Last seen recently',
    isOnline = false,
}) => {
    if (!show) return null;

    return (
        <>
            <div className='modal-backdrop fade show' onClick={onHide}></div>
            <div className='modal fade show d-block' tabIndex={-1}>
                <div className='modal-dialog modal-dialog-centered'>
                    <div className='modal-content'>
                        <div className='modal-header bg-primary text-white'>
                            <h5 className='modal-title'>
                                <i className='fas fa-user me-2'></i>
                                User Profile
                            </h5>
                            <button type='button' className='btn-close btn-close-white' onClick={onHide}></button>
                        </div>
                        <div className='modal-body'>
                            <div className='text-center mb-4'>
                                <div className='position-relative d-inline-block'>
                                    <img src='/user.png' alt='Profile' className='rounded-circle mb-3' style={{ width: '100px', height: '100px' }} />
                                    {isOnline && (
                                        <span
                                            className='position-absolute bottom-0 end-0 badge bg-success rounded-pill'
                                            style={{ width: '20px', height: '20px' }}></span>
                                    )}
                                </div>
                                <h4 className='mb-1'>{userName}</h4>
                                <p className='text-muted mb-0'>{userEmail}</p>
                                <small className='text-muted'>
                                    {isOnline ? (
                                        <span className='text-success'>
                                            <i className='fas fa-circle me-1' style={{ fontSize: '8px' }}></i>
                                            Online
                                        </span>
                                    ) : (
                                        lastSeen
                                    )}
                                </small>
                            </div>

                            <div className='row'>
                                <div className='col-12'>
                                    <div className='card bg-light'>
                                        <div className='card-body'>
                                            <h6 className='card-title'>
                                                <i className='fas fa-info-circle me-2 text-primary'></i>
                                                About
                                            </h6>
                                            <p className='card-text text-muted small mb-0'>Available for messaging and calls. Let's stay connected!</p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div className='row mt-3'>
                                <div className='col-6'>
                                    <div className='d-grid'>
                                        <button className='btn btn-outline-primary btn-sm'>
                                            <i className='fas fa-phone me-2'></i>
                                            Call
                                        </button>
                                    </div>
                                </div>
                                <div className='col-6'>
                                    <div className='d-grid'>
                                        <button className='btn btn-outline-primary btn-sm'>
                                            <i className='fas fa-video me-2'></i>
                                            Video Call
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className='modal-footer'>
                            <button type='button' className='btn btn-secondary' onClick={onHide}>
                                Close
                            </button>
                            <button type='button' className='btn btn-primary'>
                                <i className='fas fa-edit me-2'></i>
                                Edit Profile
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default UserProfileModal;
