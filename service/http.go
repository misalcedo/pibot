package service

import (
	"fmt"
	"log"
	"net/http"
)

// HTTPService is a service implemented using the HTTP protocol
type HTTPService struct {
	server      *http.Server
	multiplexer *http.ServeMux
}

// New creates a HTTTP based service.
func New(domain string, port uint16) Service {
	multiplexer := http.NewServeMux()
	server := &http.Server{Addr: fmt.Sprintf("%v:%v", domain, port), Handler: multiplexer}
	return &HTTPService{server: server, multiplexer: multiplexer}
}

// RegisterAPI on this HTTP service.
func (service *HTTPService) RegisterAPI(path string, handler func(w http.ResponseWriter, r *http.Request)) {
	service.multiplexer.HandleFunc(path, handler)
}

// Start listening on the service's address for API requests.
func (service *HTTPService) Start() error {
	log.Printf("Now listening on %v.\n", service.server.Addr)

	return service.server.ListenAndServe()
}

// Close the HTTP service
func (service *HTTPService) Close() error {
	return service.server.Close()
}
