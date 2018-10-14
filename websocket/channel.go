package websocket

// ChannelWebSocket is a WebSocket implementation that uses a channel.
type ChannelWebSocket struct {
	address    Address
	connection chan Message
}
