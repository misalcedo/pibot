package motor

import (
	"net/http"

	"github.com/misalcedo/pibot/service"
)

// New creates a service for the Raspberry Pi motor hat.
func New(domain string, port uint16) service.Service {
	api := service.New(domain, port)

	api.RegisterAPI("ping", func(w http.ResponseWriter, r *http.Request) {
	})

	api.RegisterAPI("orient", func(w http.ResponseWriter, r *http.Request) {
	})

	return api
}
