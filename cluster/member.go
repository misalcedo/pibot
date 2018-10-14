package cluster

import "fmt"

type Member struct {
	ID         Identifier
	Connection Connection
}

type Members map[Identifier]*Member
type Peers []*Member

func (member *Member) String() string {
	return fmt.Sprintf("Member{ID: %v}", member.ID)
}
