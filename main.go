package main

import (
	"github.com/misalcedo/pibot/distributed"
	"time"
)

func main() {
	cluster := distributed.New("Raspberry Pi Robot (PiBot)")

	node := distributed.NewNode()
	peer := distributed.NewNode()

	cluster.Join(node)
	cluster.Join(peer)

	time.Sleep(3 * time.Second)
}
