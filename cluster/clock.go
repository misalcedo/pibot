package cluster

import "fmt"

type Clock struct {
	Time int32
}

type VectorClock struct {
	Clocks map[Identifier]Clock
}

func (vectorClock *VectorClock) String() string {
	return fmt.Sprintf("%v", *vectorClock)
}

func (vectorClock *VectorClock) Start(node *Node) Clock {
	clock, clockStarted := vectorClock.Clocks[node.ID]

	if !clockStarted {
		clock = Clock{Time: 0}
		vectorClock.Clocks[node.ID] = clock
	}

	return clock
}

func (vectorClock *VectorClock) Map() map[string]int32 {
	clock := make(map[string]int32)

	for id, v := range vectorClock.Clocks {
		clock[id.String()] = v.Time
	}
	return clock
}

func newVectorClock() VectorClock {
	return VectorClock{Clocks: make(map[Identifier]Clock)}
}
