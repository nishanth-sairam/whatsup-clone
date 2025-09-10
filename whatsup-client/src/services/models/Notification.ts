export interface Notification {
  chatId?: string;
  content?: string;
  senderId?: string;
  receiverId?: string;
  messageType?: 'TEXT' | 'IMAGE' | 'VIDEO' | 'AUDIO';
  notificationType?: 'SEEN' | 'MESSAGE' | 'IMAGE' | 'VIDEO' | 'AUDIO';
  chatName?: string;
  media?: Array<string>;
}
