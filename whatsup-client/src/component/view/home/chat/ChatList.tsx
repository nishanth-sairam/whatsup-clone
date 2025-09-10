import { useState } from 'react';
import { ChatApi, Configuration, UserApi, type ChatResponse, type UserResponse } from '../../../../services';
import { useKeycloak } from '@react-keycloak/web';
import userImg from '../../../../assets/user.png';
import { KeycloakMiddleware } from '../../../../services/KeycloakMiddleware';

interface ChatListProps {
    chats: Array<ChatResponse>;
    onChatClicked: (chat: ChatResponse) => void;
    setChats: React.Dispatch<React.SetStateAction<Array<ChatResponse>>>;
}

const ChatList = ({ chats, onChatClicked, setChats }: ChatListProps) => {
    const [searchNewContact, setSearchNewContact] = useState<boolean>(false);
    const [contacts, setContacts] = useState<UserResponse[]>([]);
    const { keycloak } = useKeycloak();
    const chatService = new ChatApi(
        new Configuration({
            middleware: [new KeycloakMiddleware(keycloak)],
        })
    );

    const searchContact = () => {
        new UserApi(
            new Configuration({
                middleware: [new KeycloakMiddleware(keycloak)],
            })
        )
            .getAllUsersExceptSelf()
            .then(response => {
                setContacts(response);
                setSearchNewContact(true);
            })
            .catch(error => {
                console.error('Error fetching contacts:', error);
            });
    };

    const selectContact = (contact: UserResponse) => {
        chatService.createChat({ receiverId: contact.userId as string, senderId: keycloak.tokenParsed?.sub as string }).then(res => {
            const chat: ChatResponse = {
                id: res.response,
                name: contact.firstName + ' ' + contact.lastName,
                receiverOnline: contact.online,
                lastMessageTime: contact.lastSeen,
                senderId: keycloak.tokenParsed?.sub as string,
                receiverId: contact.userId,
            };
            setChats(prevChats => [chat, ...prevChats]);
        });
    };

    const wrapMessage = (message: string | undefined) => {
        return message && message.length > 20 ? message.substring(0, 17) + '...' : message;
    };

    const formatDate = (dateString: Date | undefined) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-GB', {
            day: '2-digit',
            month: '2-digit',
            year: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    return (
        <div className='h-full flex flex-col'>
            {/* Chat Header */}
            <div className='chat-header'>
                <div className='flex justify-between items-center'>
                    <h4 className='text-lg font-bold text-telegram-light-text dark:text-telegram-dark-text'>Chats</h4>
                    {!searchNewContact ? (
                        <button className='btn-icon btn-secondary' onClick={searchContact} title='New Chat'>
                            <i className='fas fa-comment-medical'></i>
                        </button>
                    ) : (
                        <button className='btn-icon btn-secondary' onClick={() => setSearchNewContact(false)} title='Close'>
                            <i className='fas fa-times'></i>
                        </button>
                    )}
                </div>

                {/* Search and Filters */}
                <div className='mt-4 space-y-3'>
                    <div className='flex items-center bg-telegram-light-surfaceVariant dark:bg-telegram-dark-surfaceVariant rounded-lg px-3 py-2'>
                        <i className='fas fa-search text-telegram-light-textMuted dark:text-telegram-dark-textMuted mr-3'></i>
                        <input
                            type='text'
                            className='flex-1 bg-transparent border-none outline-none text-telegram-light-text dark:text-telegram-dark-text placeholder:text-telegram-light-textMuted dark:placeholder:text-telegram-dark-textMuted'
                            placeholder='Search conversations...'
                        />
                    </div>
                    <div className='flex gap-2 flex-wrap'>
                        <span className='px-3 py-1 bg-telegram-light-primary dark:bg-telegram-dark-primary text-white rounded-full text-sm cursor-pointer'>
                            All
                        </span>
                        <span className='px-3 py-1 bg-telegram-light-surfaceVariant dark:bg-telegram-dark-surfaceVariant text-telegram-light-text dark:text-telegram-dark-text rounded-full text-sm cursor-pointer hover:bg-telegram-light-outline dark:hover:bg-telegram-dark-outline transition-colors'>
                            Unread
                        </span>
                        <span className='px-3 py-1 bg-telegram-light-surfaceVariant dark:bg-telegram-dark-surfaceVariant text-telegram-light-text dark:text-telegram-dark-text rounded-full text-sm cursor-pointer hover:bg-telegram-light-outline dark:hover:bg-telegram-dark-outline transition-colors'>
                            Favorites
                        </span>
                    </div>
                </div>
            </div>

            {/* Chat List */}
            <div className='flex-1 overflow-y-auto scrollbar-hide'>
                {chats.length > 0 && !searchNewContact ? (
                    chats.map(chat => (
                        <div key={chat.id} className='chat-list-item' onClick={() => onChatClicked(chat)}>
                            <div className='flex items-center gap-3 flex-1'>
                                <div className='relative avatar-lg'>
                                    <img src={userImg} alt='User Avatar' className='w-full h-full object-cover' />
                                    {typeof chat.unreadCount === 'number' && chat.unreadCount > 0 && (
                                        <span className='absolute -top-1 -right-1 bg-telegram-light-success dark:bg-telegram-dark-success text-white text-xs rounded-full min-w-[20px] h-5 flex items-center justify-center px-1'>
                                            {chat.unreadCount}
                                        </span>
                                    )}
                                </div>
                                <div className='flex-1 min-w-0'>
                                    <h6 className='font-semibold text-telegram-light-text dark:text-telegram-dark-text mb-1 truncate'>{chat.name}</h6>
                                    <div className='flex items-center gap-1 text-telegram-light-textMuted dark:text-telegram-dark-textMuted'>
                                        {chat.lastMessage === 'Attachment' && <i className='fas fa-image'></i>}
                                        <small className='truncate'>{wrapMessage(chat?.lastMessage)}</small>
                                    </div>
                                </div>
                            </div>
                            <div className='flex flex-col items-end gap-1'>
                                <small
                                    className={`text-xs ${
                                        typeof chat.unreadCount === 'number' && chat.unreadCount > 0
                                            ? 'text-telegram-light-success dark:text-telegram-dark-success font-semibold'
                                            : 'text-telegram-light-textMuted dark:text-telegram-dark-textMuted'
                                    }`}>
                                    {formatDate(chat?.lastMessageTime)}
                                </small>
                            </div>
                        </div>
                    ))
                ) : searchNewContact ? (
                    <div>
                        <div className='px-4 py-3 bg-telegram-light-surfaceVariant dark:bg-telegram-dark-surfaceVariant border-b border-telegram-light-outline dark:border-telegram-dark-outline'>
                            <h6 className='text-sm font-medium text-telegram-light-textSecondary dark:text-telegram-dark-textSecondary'>
                                Select a contact to start chatting
                            </h6>
                        </div>
                        {contacts.map(contact => (
                            <div key={contact.userId} className='chat-list-item' onClick={() => selectContact(contact)}>
                                <div className='flex items-center gap-3 flex-1'>
                                    <div className='relative avatar-lg'>
                                        <img src={userImg} alt='Contact Avatar' className='w-full h-full object-cover' />
                                        {contact.online && <div className='status-online'></div>}
                                    </div>
                                    <div className='flex-1'>
                                        <h6 className='font-semibold text-telegram-light-text dark:text-telegram-dark-text mb-1'>
                                            {contact.firstName + ' ' + contact.lastName}
                                        </h6>
                                        {contact.online ? (
                                            <small className='text-telegram-light-success dark:text-telegram-dark-success font-medium'>Online</small>
                                        ) : (
                                            <small className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted'>
                                                Last seen {formatDate(contact.lastSeen)}
                                            </small>
                                        )}
                                    </div>
                                </div>
                                <div>
                                    <i className='fas fa-plus-circle text-telegram-light-primary dark:text-telegram-dark-primary text-xl'></i>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className='flex flex-col justify-center items-center p-8 text-center h-full'>
                        <i className='fas fa-comments text-4xl text-telegram-light-textMuted dark:text-telegram-dark-textMuted mb-4'></i>
                        <h6 className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted mb-2 font-medium'>No conversations yet</h6>
                        <p className='text-telegram-light-textMuted dark:text-telegram-dark-textMuted text-sm'>
                            Start a new conversation by clicking the + button above
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ChatList;
