package service

import "net/http"

// Service is a server with a defined API.
type Service interface {
	// RegisterAPI on this service.
	RegisterAPI(path string, handler func(w http.ResponseWriter, r *http.Request))

	// Close the HTTP service
	Close() error

	// Start listening on the service's address for API requests.
	Start() error
}
