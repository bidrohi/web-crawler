package crawler

import (
	"net/url"
	"sync"
)

type Droid4 struct {
	Logger
}

var _ Droid = (*Droid4)(nil)

func (d Droid4) Name() string {
	return "Droid4"
}

func (d Droid4) Invoke(entryURI string, depth int) ([]string, error) {
	d.Logf("Preparing crawl...")
	var wg sync.WaitGroup
	inChan := make(chan *entry, 50)
	closeOnComplete := func() {
		defer close(inChan)
		wg.Wait()
	}

	var visited sync.Map
	var uris []string
	addEntry := func(uri *url.URL, depth int) {
		key := uri.Host + uri.Path
		if _, ok := visited.Load(key); ok {
			return
		}
		//d.Logf("Remaining depth %d: Found %s", depth, uri)
		uris = append(uris, uri.String())
		if depth == 0 {
			return
		}
		wg.Add(1)
		visited.Store(key, &struct{}{})
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
	for in := range inChan {
		//d.Logf("==> %d found; %d in queue", len(visited), len(inChan))
		go crawl(in)
	}

	return uris, nil
}
