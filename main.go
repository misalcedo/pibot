package main

import (
	"time"

	"github.com/misalcedo/pibot/cluster"
)

func main() {
	system := cluster.New("Raspberry Pi Robot (PiBot)")

	node := cluster.NewNode()
	peer := cluster.NewNode()

	system.Join(node)
	system.Join(peer)

	time.Sleep(3 * time.Second)
}
