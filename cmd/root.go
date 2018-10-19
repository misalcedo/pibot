package cmd

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"runtime/pprof"
	"syscall"

	homedir "github.com/mitchellh/go-homedir"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

var cfgFile string
var profilerFile string

// rootCmd represents the base command when called without any subcommands
var rootCmd = &cobra.Command{
	Use:   "pibot",
	Short: "A Raspberry Pi-based robot written in Go.",
	Long: `A Raspberry Pi-based robot written in Go. 
Uses Python to access hat libraries and the camera module.
The robot is implemented as a distributed system where each device is a service.
`,
	Run: all,
}

func all(cmd *cobra.Command, args []string) {}

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	if err := rootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

func init() {
	cobra.OnInitialize(initConfig)
	cobra.OnInitialize(initProfiler)

	// Here you will define your flags and configuration settings.
	// Cobra supports persistent flags, which, if defined here,
	// will be global for your application.
	rootCmd.PersistentFlags().StringVar(&cfgFile, "config", "", "config file (default is $HOME/.pibot.yaml)")
	rootCmd.PersistentFlags().StringVar(&profilerFile, "profile", "./profile.prof", "porfiler file (default is ./profile.prof)")
}

func initProfiler() {
	if profilerFile != "" {
		f, err := os.Create(profilerFile)
		if err != nil {
			log.Fatal(err)
		}

		pprof.StartCPUProfile(f)

		sigs := make(chan os.Signal, 1)
		signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)

		go func() {
			<-sigs
			log.Printf("Flushing profiler output to %v.", profilerFile)
			pprof.StopCPUProfile()
			os.Exit(0)
		}()

		log.Printf("Writing profiler output to %v.", profilerFile)
	}
}

// initConfig reads in config file and ENV variables if set.
func initConfig() {
	if cfgFile != "" {
		// Use config file from the flag.
		viper.SetConfigFile(cfgFile)
	} else {
		// Find home directory.
		home, err := homedir.Dir()
		if err != nil {
			fmt.Println(err)
			os.Exit(1)
		}

		// Search config in home directory with name ".cmd" (without extension).
		viper.AddConfigPath(home)
		viper.SetConfigName(".pibot")
	}

	viper.AutomaticEnv() // read in environment variables that match

	// If a config file is found, read it in.
	if err := viper.ReadInConfig(); err == nil {
		fmt.Println("Using config file:", viper.ConfigFileUsed())
	}
}
