package distributed

import (
	"fmt"
	"log"
	"time"
)

type Node struct {
	ID          Identifier
	VectorClock VectorClock
	Connection  Connection
	Cluster     *Cluster
}

func NewNode() *Node {
	node := &Node{ID: newIdentifier(), VectorClock: newVectorClock(), Connection: newConnection()}
	node.VectorClock[node.ID] = 0

	go node.listen()

	return node
}

func (node *Node) gossip() {
	for {
		peers := node.Cluster.Peers(node)

		for peer := range peers {
			go node.heartBeat(peers[peer])
		}

		time.Sleep(50 * time.Millisecond)
	}
}

func (node *Node) heartBeat(peer *Member) {
	log.Printf("Node %v sending heartbeet to peer %v.", node.ID, peer.ID)
	peer.Connection.Append(node.ID, []byte(node.VectorClock.String()))

}

func (node *Node) String() string {
	return fmt.Sprintf("Node{ID: %v, Peers: %v, VectorClock: %v}", node.ID, node.Cluster, node.VectorClock)
}
func (node *Node) listen() {
	for {
		select {
		case message := <-node.Connection:
			log.Printf("Node %v received message %v from peer %v.", node.ID, string(message.Value), message.From)
		}
	}
}
