package distributed

import "github.com/google/uuid"

type Identifier struct {
	UUID uuid.UUID
}

func (identifier *Identifier) String() string {
	return uuid.UUID.String(identifier.UUID)[1:9]
}

func newIdentifier() Identifier {
	return Identifier{UUID: uuid.New()}
}
