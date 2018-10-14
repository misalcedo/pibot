package cluster

import (
	"fmt"
	"log"
	"time"

	"github.com/golang/protobuf/proto"
	"github.com/misalcedo/pibot/models"
)

type Node struct {
	ID          Identifier
	VectorClock VectorClock
	Connection  Connection
	Cluster     *Cluster
}

func NewNode() *Node {
	node := &Node{ID: newIdentifier(), VectorClock: newVectorClock(), Connection: newConnection()}
	node.VectorClock.Clocks[node.ID] = Clock{Time: 0}

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
	heartbeat := &models.HeartBeat{From: node.ID.String(), Clocks: node.VectorClock.Map()}

	// Write the new address book back to disk.
	out, err := proto.Marshal(heartbeat)
	if err != nil {
		log.Fatalln("Failed to encode heartbeat:", err)
	}

	peer.Connection.Append(node.ID, out)

}

func (node *Node) String() string {
	return fmt.Sprintf("Node{ID: %v, Peers: %v, VectorClock: %v}", node.ID, node.Cluster, node.VectorClock)
}

func (node *Node) listen() {
	for {
		select {
		case message := <-node.Connection:
			heartbeat := &models.HeartBeat{}

			err := proto.Unmarshal(message.Value, heartbeat)
			if err != nil {
				log.Fatalln("Failed to parse heartbeat:", err)
			}

			log.Printf("Node %v received message %v from peer %v.", node.ID, heartbeat.Clocks, heartbeat.From)
		}
	}
}
