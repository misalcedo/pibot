package websocket

import (
	"bytes"
	"strconv"
)

// Address is a combination of hostname and port
type Address struct {
	hostname string
	port     int
}

func (address *Address) String() string {
	buffer := bytes.Buffer{}

	buffer.WriteString(address.hostname)
	buffer.WriteString(strconv.Itoa(address.port))

	return buffer.String()
}

// WebSocket is a single connection that implements the ws protocol
type WebSocket interface {
	Close() error
	Open() error
}

// Message is either a byte or text message frame.
type Message interface {
	String() string
}
