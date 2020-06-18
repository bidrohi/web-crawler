package crawler

import (
	"net/url"
	"sync"
)

type Droid3 struct {
	Logger
}

var _ Droid = (*Droid3)(nil)

func (d Droid3) Name() string {
	return "Droid3"
}

func (d Droid3) Invoke(entryURI string, depth int) ([]string, error) {
	d.Logf("Preparing crawl...")
	var wg sync.WaitGroup
	inChan := make(chan *entry, 50)
	closeOnComplete := func() {
		defer close(inChan)
		wg.Wait()
	}

	addEntry := func(uri *url.URL, depth int) {
		//d.Logf("Depth %d: Found %s", depth, uri)
		wg.Add(1)
		inChan <- &entry{
			URI:   uri,
			Depth: depth,
		}
	}

	cleanEntry := func(in *entry) {
		wg.Done()
	}

	crawl := func(in *entry) {
		defer cleanEntry(in)
		d.Logf("Crawling %s...", in.URI)
		if err := crawlAndQueue(in, addEntry); err != nil {
			d.Logf("ERROR: Couldn't fetch %q; %v", in.URI, err)
		}
	}

	uri, err := url.Parse(entryURI)
	if err != nil {
		return nil, err
	}
	addEntry(uri, depth)
	go closeOnComplete()

	d.Logf("Starting to crawl...")
	visited := make(map[string]*struct{})
	for in := range inChan {
		//d.Logf("==> %d found; %d in queue", len(visited), len(inChan))
		key := in.URI.Host + in.URI.Path
		if _, ok := visited[key]; !ok {
			visited[key] = &struct{}{}
			if in.Depth > 0 {
				go crawl(in)
			} else {
				cleanEntry(in)
			}
		} else {
			cleanEntry(in)
		}
	}

	return getMapKeys(visited), nil
}
