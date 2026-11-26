import { Client, type IMessage } from '@stomp/stompjs';

export type NotificationConnectionState = 'connecting' | 'connected' | 'disconnected' | 'error';

export interface NotificationConnectionOptions {
  url: string;
  token: string;
  onStateChange: (state: NotificationConnectionState) => void;
  onMessage: (message: string) => void;
}

export function connectNotifications(options: NotificationConnectionOptions): () => void {
  const client = new Client({
    webSocketFactory: () => new WebSocket(options.url),
    connectHeaders: { Authorization: `Bearer ${options.token}` },
    reconnectDelay: 5_000,
    onConnect: () => {
      options.onStateChange('connected');
      client.subscribe('/user/queue/notifications', (message: IMessage) => {
        options.onMessage(message.body);
      });
    },
    onStompError: () => options.onStateChange('error'),
    onWebSocketClose: () => options.onStateChange('disconnected'),
    onWebSocketError: () => options.onStateChange('error'),
  });

  options.onStateChange('connecting');
  client.activate();
  return () => {
    void client.deactivate();
  };
}
