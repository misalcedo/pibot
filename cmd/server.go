package cmd

import (
	"io"
	"log"
	"net/http"

	"github.com/spf13/cobra"
)

// serverCmd represents the server command
var serverCmd = &cobra.Command{
	Use:   "server",
	Short: "An HTTP ECHO server.",
	Long:  `An HTTP server that ECHOs the body of all requests.`,
	Run: func(cmd *cobra.Command, args []string) {
		port := cmd.Flags().Lookup("port").Value.String()
		Serve(port)
	},
}

func Serve(port string) {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		if r.Body != nil {
			if _, err := io.Copy(w, r.Body); err != nil {
				log.Printf("Encountered an error while echoing the body: %v\n.", err)
			}
		}
	})

	log.Printf("Now listening on port %v.\n", port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}

func init() {
	rootCmd.AddCommand(serverCmd)

	// Here you will define your flags and configuration settings.

	// Cobra supports Persistent Flags which will work for this command
	// and all subcommands, e.g.:
	serverCmd.Flags().Uint16("port", 8080, "The port to listen on.")
}
