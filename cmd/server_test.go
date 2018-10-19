package cmd

import (
	"io/ioutil"
	"net/http"
	"strings"
	"testing"
)

func BenchmarkEcho(t *testing.B) {
	for n := 0; n < t.N; n++ {
		callEcho(t)
	}
}

func callEcho(b *testing.B) {
	expected := "foobar"
	resp, err := http.Post("http://localhost:44444/", "text/plain", strings.NewReader(expected))
	if err != nil {
		b.Errorf("Unable to receive a response from server: %v.", err)
	}

	defer resp.Body.Close()

	actual := readBody(resp, b)

	if actual != expected {
		b.Errorf("Invalid response form server. Expected: '%v', Received: '%v'.", expected, actual)
	}
}

func readBody(resp *http.Response, b *testing.B) string {
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		b.Errorf("Unable to receive a response from server: %v.", err)
	}

	return string(body)
}
