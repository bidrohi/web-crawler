package crawler

import (
	"net/url"
	"sync"
)

type Droid2 struct {
	Logger
}

var _ Droid = (*Droid2)(nil)

func (d Droid2) Name() string {
	return "Droid2"
}

func (d Droid2) Invoke(entryURI string, depth int) ([]string, error) {
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

	crawl := func(in *entry) {
		defer wg.Done()
		if in.Depth > 0 {
			d.Logf("Crawling %s...", in.URI)
			if err := crawlAndQueue(in, addEntry); err != nil {
				d.Logf("ERROR: Couldn't fetch %q; %v", in.URI, err)
			}
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
	var uris []string
	for in := range inChan {
		//d.Logf("==> %d found; %d in queue", len(visited), len(inChan))
		key := in.URI.Host + in.URI.Path
		if _, ok := visited[key]; !ok {
			uris = append(uris, uri.String())
			visited[key] = &struct{}{}
			go crawl(in)
		} else {
			wg.Done()
		}
	}

	return uris, nil
}
