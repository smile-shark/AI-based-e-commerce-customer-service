// WebSocket工具类

export interface ChatMessage {
  sessionId?: number
  goodsId?: number
  ctId?: number
  type: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL' | 'COMMERCIAL_TENANT'
  message: string
  state?: 'SUCCESS' | 'ERROR' | 'SURE' | 'END'
}

export interface WebSocketMessage extends ChatMessage {
  timestamp?: string
}

export class WebSocketClient {
  private ws: WebSocket | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectInterval = 3000
  private url: string = ''
  private messageHandlers: ((message: WebSocketMessage) => void)[] = []
  private onOpenHandlers: (() => void)[] = []
  private onCloseHandlers: (() => void)[] = []
  private onErrorHandlers: ((error: Event) => void)[] = []

  constructor(url: string) {
    this.url = url
  }

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        // 如果是HTTP协议，替换为WebSocket协议
        const wsUrl = this.url.replace(/^http/, 'ws')
        this.ws = new WebSocket(wsUrl)

        this.ws.onopen = () => {
          console.log('WebSocket connected')
          this.reconnectAttempts = 0
          this.onOpenHandlers.forEach(handler => handler())
          resolve()
        }

        this.ws.onmessage = (event) => {
          try {
            const message: WebSocketMessage = JSON.parse(event.data)
            this.messageHandlers.forEach(handler => handler(message))
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error)
          }
        }

        this.ws.onclose = () => {
          console.log('WebSocket closed')
          this.onCloseHandlers.forEach(handler => handler())
          this.attemptReconnect()
        }

        this.ws.onerror = (error) => {
          console.error('WebSocket error:', error)
          this.onErrorHandlers.forEach(handler => handler(error))
          reject(error)
        }
      } catch (error) {
        reject(error)
      }
    })
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  sendMessage(message: ChatMessage) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.error('WebSocket is not connected')
    }
  }

  onMessage(handler: (message: WebSocketMessage) => void) {
    this.messageHandlers.push(handler)
  }

  onOpen(handler: () => void) {
    this.onOpenHandlers.push(handler)
  }

  onClose(handler: () => void) {
    this.onCloseHandlers.push(handler)
  }

  onError(handler: (error: Event) => void) {
    this.onErrorHandlers.push(handler)
  }

  private attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)

      setTimeout(() => {
        this.connect().catch(() => {
          console.error('Reconnection failed')
        })
      }, this.reconnectInterval)
    } else {
      console.error('Max reconnection attempts reached')
    }
  }

  get isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN
  }
}

// 创建WebSocket客户端实例
export const createWebSocketClient = (url: string): WebSocketClient => {
  return new WebSocketClient(url)
}

// 用户WebSocket客户端管理
let userWebSocketClient: WebSocketClient | null = null

export const getUserWebSocketClient = (userId: number): WebSocketClient => {
  if (!userWebSocketClient) {
    // 使用后端WebSocket端口8080
    userWebSocketClient = createWebSocketClient(`ws://localhost:8080/user/chat/${userId}`)
  }
  return userWebSocketClient
}

// 商户WebSocket客户端管理
let ctWebSocketClient: WebSocketClient | null = null

export const getCtWebSocketClient = (ctId: number): WebSocketClient => {
  if (!ctWebSocketClient) {
    // 使用后端WebSocket端口8080
    ctWebSocketClient = createWebSocketClient(`ws://localhost:8080/commercialTenant/chat/${ctId}`)
  }
  return ctWebSocketClient
}

// 清理所有连接
export const cleanupWebSocket = () => {
  if (userWebSocketClient) {
    userWebSocketClient.disconnect()
    userWebSocketClient = null
  }
  if (ctWebSocketClient) {
    ctWebSocketClient.disconnect()
    ctWebSocketClient = null
  }
}
