package cluster

type Connection chan Message

func (connection *Connection) Append(from Identifier, bytes []byte) {
	*connection <- Message{From: from, Value: bytes}
}

type Message struct {
	Value []byte
	From  Identifier
}

func newConnection() Connection {
	return make(Connection)
}
