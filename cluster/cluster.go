package cluster

import (
	"fmt"
	"sync"

	"github.com/pkg/errors"
)

type Cluster struct {
	Members Members
	Name    string
	Mutex   sync.Mutex
}

func New(name string) *Cluster {
	clusters := Cluster{Members: make(Members), Name: name}
	return &clusters
}

func (cluster *Cluster) Peers(node *Node) Peers {
	member, inCluster := cluster.member(node)

	if !inCluster {
		return make(Peers, 0)
	}

	peers := make(Peers, 0, len(cluster.Members)-1)

	for id, peer := range cluster.Members {
		if id != member.ID {
			peers = append(peers, peer)
		}
	}

	return peers
}

func (cluster *Cluster) member(node *Node) (*Member, bool) {
	cluster.Mutex.Lock()
	defer cluster.Mutex.Unlock()

	member, inCluster := cluster.Members[node.ID]

	return member, inCluster
}

func (cluster *Cluster) Join(node *Node) error {
	cluster.Mutex.Lock()
	defer cluster.Mutex.Unlock()

	if node.Cluster == nil {
		cluster.Members[node.ID] = &Member{ID: node.ID, Connection: node.Connection}
		node.Cluster = cluster
		go node.gossip()
	} else if node.Cluster != cluster {
		return errors.New(fmt.Sprintf(
			"Node %v cannot join cluster %v because it already belongs to cluster %v.",
			node.ID,
			cluster.Name,
			node.Cluster.Name))
	}

	return nil
}

func (cluster *Cluster) String() string {
	return fmt.Sprintf("Cluster{Name: %v, Members: %v}", cluster.Name, cluster.Members)
}
