package distributed

import "fmt"

type Clock int
type VectorClock map[Identifier]Clock

func (vectorClock *VectorClock) String() string {
	return fmt.Sprintf("%v", *vectorClock)
}

func newVectorClock() VectorClock {
	return make(VectorClock)
}
