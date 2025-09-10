import { useEffect, useCallback, useRef, type ChangeEvent } from 'react';
import ChatList from './chat/ChatList';
import { useState } from 'react';
import { ChatApi, Configuration, MessagesApi } from '../../../services';
import type { ChatResponse, Message, MessageRequest, MessageResponse, PageMessageResponse, Notification } from '../../../services';
import { useKeycloak } from '@react-keycloak/web';
import waBanner from '../../../assets/wa_banner.png';
import * as Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import ThemeToggle from '../../common/ThemeToggle';

const Home = () => {
    const [chats, setChats] = useState<ChatResponse[]>([]);
    const { keycloak, initialized } = useKeycloak();
    const [selectedChat, setSelectedChat] = useState<ChatResponse>();
    const inputFileRef = useRef<HTMLInputElement>(null);
    const [showEmojis, setShowEmojis] = useState(false);
    const [messageContent, setMessageContent] = useState('');
    const [chatMessages, setChatMessages] = useState<PageMessageResponse>();

    // Refs to hold current values for cleanup (avoid stale closure issues)
    const sockClientRef = useRef<Stomp.Client | null>(null);
    const notificationSubscriptionRef = useRef<Stomp.Subscription | null>(null);
    const getChats = useCallback(() => {
        if (!initialized || !keycloak.authenticated || !keycloak.token) {
            console.log('Keycloak not ready:', { initialized, authenticated: keycloak.authenticated, hasToken: !!keycloak.token });
            return;
        }

        console.log('Making authenticated API call with token:', keycloak.token ? 'Present' : 'Missing');

        const configuration = new Configuration({
            headers: {
                Authorization: `Bearer ${keycloak.token}`,
                'Content-Type': 'application/json',
            },
        });

        new ChatApi(configuration)
            .getChatsByReceiver()
            .then(response => {
                console.log('Chats fetched successfully:', response);
                setChats(response);
            })
            .catch(error => {
                console.error('Error fetching chats:', error);
                if (error.response?.status === 401) {
                    console.error('Token might be expired, attempting refresh...');
                    keycloak
                        .updateToken(30)
                        .then(refreshed => {
                            if (refreshed) {
                                console.log('Token refreshed, retrying...');
                                getChats();
                            } else {
                                console.log('Token still valid, but got 401. Check server configuration.');
                            }
                        })
                        .catch(err => {
                            console.error('Failed to refresh token:', err);
                            keycloak.login();
                        });
                }
            });
    }, [initialized, keycloak]);

    const handleNotification = useCallback(
        (notification: Notification) => {
            if (!notification) return;

            if (selectedChat && selectedChat.id === notification.chatId) {
                switch (notification.notificationType) {
                    case 'MESSAGE':
                    case 'IMAGE': {
                        const newMessage: MessageResponse = {
                            senderId: notification.senderId,
                            receiverId: notification.receiverId,
                            content: notification.content,
                            type: notification.messageType || 'TEXT',
                            media: Array.isArray(notification.media) ? notification.media[0] : notification.media,
                            createdAt: new Date(),
                        };
                        setSelectedChat(prevChat => ({
                            ...prevChat,
                            unreadCount: (prevChat?.unreadCount || 0) + 1,
                            lastMessage: notification.messageType === 'IMAGE' ? 'Attachment' : notification.content,
                        }));
                        setChatMessages(prevMessages => {
                            return {
                                ...prevMessages,
                                content: [newMessage, ...(prevMessages?.content || [])],
                            };
                        });
                        break;
                    }
                    case 'SEEN':
                        setChatMessages(prevMessages => ({
                            ...prevMessages,
                            content: prevMessages?.content && prevMessages.content.map(msg => (msg.state === 'SENT' ? { ...msg, state: 'SEEN' } : msg)),
                        }));
                        break;
                }
            } else {
                const existingChat = chats.find(chat => chat.id === notification.chatId);
                if (existingChat && notification.notificationType !== 'SEEN') {
                    if (notification.notificationType === 'MESSAGE') {
                        existingChat.lastMessage = notification.content;
                    } else if (notification.notificationType === 'IMAGE') {
                        existingChat.lastMessage = 'Attachment';
                    }
                    existingChat.lastMessageTime = new Date();
                    existingChat.unreadCount = (existingChat.unreadCount || 0) + 1;
                    setChats(prevChats => {
                        const updatedChats = prevChats.map(chat => {
                            if (chat.id === existingChat.id) {
                                return existingChat;
                            }
                            return chat;
                        });
                        return updatedChats;
                    });
                } else if (notification.notificationType === 'MESSAGE') {
                    const newChat: ChatResponse = {
                        id: notification.chatId,
                        senderId: notification.senderId,
                        receiverId: notification.receiverId,
                        lastMessage: notification.content,
                        name: notification.chatName,
                        unreadCount: 1,
                        lastMessageTime: new Date(),
                    };
                    setChats(prevChats => [newChat, ...prevChats]);
                }
            }
        },
        [selectedChat, chats]
    );

    const initWebSocket = useCallback(() => {
        if (!keycloak.authenticated || !keycloak.token) {
            console.log('WebSocket initialization skipped: Keycloak not authenticated or token missing');
            return;
        }

        const ws = new SockJS('http://localhost:9090/ws');
        const client = Stomp.over(ws);
        sockClientRef.current = client;

        const subUrl = `/user/${keycloak.tokenParsed?.sub}/chat`;
        client.connect({ Authorization: `Bearer ${keycloak.token}` }, frame => {
            console.log('Connected to WebSocket:', frame);
            const subscription = client.subscribe(
                subUrl,
                message => {
                    const notification: Notification = JSON.parse(message.body);
                    handleNotification(notification);
                },
                () => {
                    console.error('Failed to subscribe to notifications');
                }
            );
            notificationSubscriptionRef.current = subscription;
        });
    }, [keycloak, handleNotification]);

    useEffect(() => {
        initWebSocket();

        return () => {
            // Clean up subscription using ref
            if (notificationSubscriptionRef.current) {
                notificationSubscriptionRef.current.unsubscribe();
                console.log('WebSocket subscription unsubscribed');
                notificationSubscriptionRef.current = null;
            }
            // Clean up socket connection using ref
            if (sockClientRef.current && sockClientRef.current.connected) {
                sockClientRef.current.disconnect(() => {
                    console.log('WebSocket disconnected');
                });
                sockClientRef.current = null;
            }
        };
    }, [initWebSocket]);

    useEffect(() => {
        getChats();
    }, [getChats]);

    const logout = () => {
        keycloak.logout().catch(error => {
            console.error('Logout failed:', error);
        });
    };

    const userProfile = () => {
        keycloak.accountManagement().catch(error => {
            console.error('Failed to open account management:', error);
        });
    };

    const onChatClicked = (chat: ChatResponse) => {
        setSelectedChat(chat);
        getAllMessages(chat.id as string);
        setMessagesToSeen(chat);
    };

    const setMessagesToSeen = (chat: ChatResponse) => {
        if (!keycloak.authenticated || !keycloak.token) {
            console.error('User not authenticated');
            return;
        }

        const configuration = new Configuration({
            headers: {
                Authorization: `Bearer ${keycloak.token}`,
                'Content-Type': 'application/json',
            },
        });

        new MessagesApi(configuration)
            .setMessageToSeen({
                chatId: chat.id as string,
            })
            .then(() => {
                setChatMessages(prevMessages => ({
                    ...prevMessages,
                    content: prevMessages?.content ? prevMessages.content.map(msg => (msg.state !== 'SEEN' ? { ...msg, state: 'SEEN' } : msg)) : [],
                }));
            })
            .catch(error => {
                console.error('Error setting messages to seen:', error);
            });
    };

    const getAllMessages = (chatId: string) => {
        if (!keycloak.authenticated || !keycloak.token) {
            console.error('User not authenticated');
            return;
        }

        const configuration = new Configuration({
            headers: {
                Authorization: `Bearer ${keycloak.token}`,
                'Content-Type': 'application/json',
            },
        });

        new MessagesApi(configuration)
            .getMessages({ chatId, page: 0, size: 50 })
            .then(response => {
                setChatMessages(response);
            })
            .catch(error => {
                console.error('Error fetching messages for chat:', chatId, error);
            });
    };

    if (!initialized) {
        return (
            <div className='min-h-screen flex items-center justify-center bg-telegram-light-background dark:bg-telegram-dark-background'>
                <ThemeToggle />
                <div className='text-center'>
                    <div className='flex flex-col items-center gap-4'>
                        <div className='spinner'></div>
                        <h5 className='text-telegram-light-textSecondary dark:text-telegram-dark-textSecondary text-lg'>Loading authentication...</h5>
                    </div>
                </div>
            </div>
        );
    }

    if (!keycloak.authenticated) {
        return (
            <div className='min-h-screen flex items-center justify-center bg-telegram-light-background dark:bg-telegram-dark-background'>
                <ThemeToggle />
                <div className='text-center'>
                    <div className='card-telegram p-8 max-w-md mx-auto'>
                        <div className='flex flex-col items-center gap-4'>
                            <i className='fas fa-lock text-4xl text-telegram-light-primary dark:text-telegram-dark-primary'></i>
                            <h4 className='text-xl font-semibold text-telegram-light-text dark:text-telegram-dark-text'>Authentication Required</h4>
                            <p className='text-telegram-light-textSecondary dark:text-telegram-dark-textSecondary mb-4'>
                                Please log in to continue to WhatsApp Clone
                            </p>
                            <button className='btn-primary px-6 py-3 flex items-center gap-2' onClick={() => keycloak.login()}>
                                <i className='fas fa-sign-in-alt'></i>
                                Log In
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    const handleUploadMedia = (event: ChangeEvent<HTMLInputElement>): void => {
        const files = event.target.files;
        if (!files || files.length === 0) return;

        if (!keycloak.authenticated || !keycloak.token) {
            console.error('User not authenticated');
            return;
        }

        const formData = new FormData();
        Array.from(files).forEach(file => {
            formData.append('media', file);
        });

        const configuration = new Configuration({
            headers: {
                Authorization: `Bearer ${keycloak.token}`,
            },
        });

        new MessagesApi(configuration)
            .uploadMedia({
                chatId: selectedChat?.id as string,
                file: formData.get('media') as File,
            })
            .then(response => {
                getAllMessages(selectedChat?.id as string);
                console.log('Media uploaded successfully:', response);
            })
            .catch(error => {
                console.error('Error uploading media:', error);
            });
    };

    const handleInputClick = () => {};

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    const getSenderId = (): string | undefined => {
        if (selectedChat && selectedChat?.senderId === keycloak.tokenParsed?.sub) {
            return selectedChat.senderId;
        }
        return selectedChat?.receiverId;
    };

    const getReceiverId = (): string | undefined => {
        if (selectedChat && selectedChat.senderId === keycloak.tokenParsed?.sub) {
            return selectedChat.receiverId as string;
        }
        return selectedChat?.senderId;
    };

    const sendMessage = () => {
        if (!messageContent.trim() || !selectedChat?.id) return;

        if (!keycloak.authenticated || !keycloak.token) {
            console.error('User not authenticated');
            return;
        }

        const message: MessageRequest = {
            chatId: selectedChat.id,
            senderId: getSenderId(),
            receiverId: getReceiverId(),
            content: messageContent,
            type: 'TEXT',
        };

        const configuration = new Configuration({
            headers: {
                Authorization: `Bearer ${keycloak.token}`,
                'Content-Type': 'application/json',
            },
        });

        new MessagesApi(configuration)
            .saveMessage({ messageRequest: message })
            .then((response: Message) => {
                const messageResponse: MessageResponse = {
                    messageId: response.id,
                    senderId: getSenderId(),
                    receiverId: getReceiverId(),
                    content: messageContent,
                    type: 'TEXT',
                    state: 'SENT',
                    createdAt: new Date(),
                };
                setSelectedChat(prevChat => {
                    if (!prevChat) return prevChat;
                    return {
                        ...prevChat,
                        lastMessageTime: new Date(),
                        lastMessage: messageContent,
                    };
                });
                setChatMessages(prevMessages => ({ ...prevMessages, content: [messageResponse, ...(prevMessages?.content || [])] }));
                setMessageContent('');
                setShowEmojis(false);
            })
            .catch(error => {
                console.error('Error sending message:', error);
            });
    };

    function isSelfMessage(message: MessageResponse): boolean {
        return message.senderId === keycloak.tokenParsed?.sub;
    }

    return (
        <div className='min-h-screen bg-gradient-to-br from-telegram-light-primary/10 to-telegram-light-secondary/10 dark:from-telegram-dark-background dark:to-telegram-dark-surface p-4 relative'>
            <ThemeToggle />

            {/* Background decorative elements */}
            <div className='absolute top-0 left-0 w-full h-1/4 bg-gradient-to-r from-telegram-light-primary/20 to-telegram-light-secondary/20 dark:from-telegram-dark-primary/20 dark:to-telegram-dark-secondary/20'></div>

            <div className='relative w-full max-w-7xl mx-auto h-[calc(100vh-2rem)] card-telegram overflow-hidden backdrop-blur-telegram'>
                <div className='flex h-full'>
                    {/* Left Sidebar Navigation */}
                    <div className='sidebar-nav w-16 flex-shrink-0'>
                        <div className='flex flex-col h-full justify-between'>
                            <div className='pt-4'>
                                <i className='fas fa-message text-2xl text-telegram-light-primary dark:text-telegram-dark-primary'></i>
                            </div>
                            <div className='flex flex-col gap-3 pb-4'>
                                <button className='btn-icon btn-secondary' onClick={userProfile} title='Profile'>
                                    <i className='fas fa-user'></i>
                                </button>
                                <button className='btn-icon btn-secondary' onClick={logout} title='Logout'>
                                    <i className='fas fa-door-open'></i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className='flex flex-1'>
                        {/* Chat List */}
                        <div className='w-80 chat-container'>
                            <ChatList chats={chats} setChats={setChats} onChatClicked={onChatClicked}></ChatList>
                        </div>

                        {/* Main Chat Area */}
                        {selectedChat && selectedChat.id ? (
                            <div className='flex-1 flex flex-col'>
                                {/* Chat Header */}
                                <div className='chat-header'>
                                    <div className='flex items-center gap-3'>
                                        <div className='relative avatar-md'>
                                            <img src='user.png' alt='User Avatar' className='w-full h-full object-cover' />
                                            {selectedChat.receiverOnline && <div className='status-online'></div>}
                                        </div>
                                        <div className='flex flex-col'>
                                            <h6 className='font-semibold text-telegram-light-text dark:text-telegram-dark-text'>{selectedChat.name}</h6>
                                            <div className='flex items-center gap-2'>
                                                {selectedChat.receiverOnline ? (
                                                    <>
                                                        <span className='w-2 h-2 bg-telegram-light-success dark:bg-telegram-dark-success rounded-full'></span>
                                                        <small className='text-telegram-light-success dark:text-telegram-dark-success font-medium'>
                                                            Online
                                                        </small>
                                                    </>
                                                ) : (
                                                    <>
                                                        <span className='w-2 h-2 bg-telegram-light-textMuted dark:bg-telegram-dark-textMuted rounded-full'></span>
                                                        <small className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted'>Offline</small>
                                                    </>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {/* Messages Area */}
                                <div
                                    className='flex-1 bg-telegram-light-surface dark:bg-telegram-dark-surface p-4 overflow-y-auto scrollbar-hide relative'
                                    ref={el => {
                                        if (el && chatMessages && chatMessages.content && chatMessages.content.length > 0) {
                                            el.scrollTop = el.scrollHeight;
                                        }
                                    }}>
                                    {/* Background pattern */}
                                    <div className='absolute inset-0 opacity-5 pointer-events-none'></div>
                                    <div className='flex flex-col-reverse gap-3 relative z-10'>
                                        {chatMessages &&
                                            chatMessages.content &&
                                            chatMessages.content.map((message, index) =>
                                                isSelfMessage(message) ? (
                                                    <div className='flex justify-end animate-slide-in' key={message.messageId || index}>
                                                        <div className='message-bubble-out'>
                                                            <div className='mb-1'>
                                                                {message.type === 'TEXT' ? (
                                                                    <p className='text-sm mb-0'>{message.content}</p>
                                                                ) : message.media ? (
                                                                    <div className='mb-1'>
                                                                        <img
                                                                            className='max-w-48 h-auto rounded-lg cursor-pointer hover:scale-105 transition-transform'
                                                                            src={`data:image/jpg;base64, ${message.media}`}
                                                                            alt='Attachment'
                                                                        />
                                                                    </div>
                                                                ) : null}
                                                            </div>
                                                            <div className='flex items-center justify-end gap-1 text-xs'>
                                                                <span className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted'>
                                                                    {message.createdAt
                                                                        ? new Date(message.createdAt).toLocaleTimeString([], {
                                                                              hour: '2-digit',
                                                                              minute: '2-digit',
                                                                          })
                                                                        : ''}
                                                                </span>
                                                                <div className={`message-status ${message.state === 'SEEN' ? 'read' : 'delivered'}`}>
                                                                    {message.state === 'SENT' ? (
                                                                        <i className='fas fa-check'></i>
                                                                    ) : (
                                                                        <i className='fas fa-check-double'></i>
                                                                    )}
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                ) : (
                                                    <div className='flex justify-start animate-slide-in' key={message.messageId || index}>
                                                        <div className='message-bubble-in'>
                                                            <div className='mb-1'>
                                                                {message.type === 'TEXT' ? (
                                                                    <p className='text-sm mb-0'>{message.content}</p>
                                                                ) : message.media ? (
                                                                    <div className='mb-1'>
                                                                        <img
                                                                            className='max-w-48 h-auto rounded-lg cursor-pointer hover:scale-105 transition-transform'
                                                                            src={`data:image/jpg;base64,${message.media}`}
                                                                            alt='Attachment'
                                                                        />
                                                                    </div>
                                                                ) : null}
                                                            </div>
                                                            <div className='flex items-center justify-end'>
                                                                <span className='text-xs text-telegram-light-textMuted dark:text-telegram-dark-textMuted'>
                                                                    {message.createdAt
                                                                        ? new Date(message.createdAt).toLocaleTimeString([], {
                                                                              hour: '2-digit',
                                                                              minute: '2-digit',
                                                                          })
                                                                        : ''}
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                )
                                            )}
                                    </div>
                                </div>

                                {/* Chat Input */}
                                <div className='chat-input-container'>
                                    <button className='btn-icon btn-secondary' type='button' onClick={() => inputFileRef.current?.click()} title='Attach file'>
                                        <i className='fas fa-paperclip'></i>
                                    </button>
                                    <input ref={inputFileRef} type='file' hidden accept='.jpg, .jpeg,.png,.svg,.mp4,.mov,.mp3' onChange={handleUploadMedia} />
                                    <button className='btn-icon btn-secondary' type='button' onClick={() => setShowEmojis(v => !v)} title='Emoji'>
                                        <i className='far fa-smile'></i>
                                    </button>
                                    {showEmojis && (
                                        <div className='absolute bottom-16 left-20 bg-telegram-light-background dark:bg-telegram-dark-surface rounded-lg shadow-telegram-xl z-10 p-4'>
                                            {/* Emoji picker component would go here */}
                                            <p className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted'>Emoji picker placeholder</p>
                                        </div>
                                    )}
                                    <input
                                        type='text'
                                        className='input-telegram flex-1'
                                        placeholder='Type a message...'
                                        value={messageContent}
                                        onChange={e => setMessageContent(e.target.value)}
                                        onKeyDown={handleKeyDown}
                                        onClick={handleInputClick}
                                    />
                                    <button className='btn-primary px-4' type='button' onClick={sendMessage} disabled={!messageContent.trim()}>
                                        {messageContent ? <i className='fas fa-paper-plane'></i> : <i className='fas fa-microphone'></i>}
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className='flex-1 welcome-screen'>
                                <img
                                    width='300'
                                    src={waBanner}
                                    alt='WhatsApp Banner'
                                    className='mb-6 drop-shadow-lg hover:scale-105 transition-transform duration-300'
                                />
                                <h2 className='text-2xl font-light text-telegram-light-text dark:text-telegram-dark-text mb-4 tracking-wide'>
                                    WhatsApp Clone Application
                                </h2>
                                <p className='text-telegram-light-textSecondary dark:text-telegram-dark-textSecondary mb-2 leading-relaxed'>
                                    Send and receive messages without keeping your phone online.
                                </p>
                                <p className='text-telegram-light-textSecondary dark:text-telegram-dark-textSecondary leading-relaxed'>
                                    Use WhatsApp on up to 4 linked devices and 1 phone at the same time.
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Home;
