package main

import (
	"log"
	"os"
	"time"

	"tech.bidyut.com/crawler"

	"github.com/akamensky/argparse"
)

const baseURL = "https://www.engadget.com"

func main() {
	parser := argparse.NewParser("crawler", "A simple web crawler that just that collects URLs")
	depth := parser.Int("d", "depth", &argparse.Options{
		Default:  2,
		Required: false,
		Help:     "Number of depth to follow",
	})
	uri := parser.String("u", "uri", &argparse.Options{
		Default:  baseURL,
		Required: false,
		Help:     "Base url to start crawling from",
	})
	if err := parser.Parse(os.Args); err != nil {
		panic(err)
	}

	for _, droid := range []crawler.Droid{
		crawler.Droid1{},
		crawler.Droid2{},
		crawler.Droid3{},
	} {
		if err := crawlAndTime(droid, *uri, *depth); err != nil {
			panic(err)
		}
	}
}

func crawlAndTime(d crawler.Droid, uri string, depth int) error {
	start := time.Now()
	defer func() {
		log.Printf("=> took %v", time.Since(start))
	}()

	log.Printf("Invoking %s", d.Name())
	urls, err := d.Invoke(uri, depth)
	if err != nil {
		return err
	}
	log.Printf("=> found %d distinct URLs", len(urls))
	return nil
}
