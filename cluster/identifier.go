package cluster

import "github.com/google/uuid"

type Identifier struct {
	UUID uuid.UUID
}

func (identifier *Identifier) String() string {
	return uuid.UUID.String(identifier.UUID)
}

func newIdentifier() Identifier {
	return Identifier{UUID: uuid.New()}
}
