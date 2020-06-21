package crawler

import (
	"net/url"
	"sync"
)

type Droid5 struct {
	Logger
}

var _ Droid = (*Droid5)(nil)

func (d Droid5) Name() string {
	return "Droid5"
}

func (d Droid5) Invoke(entryURI string, depth int) ([]string, error) {
	d.Logf("Preparing crawl...")
	var wg sync.WaitGroup
	var visited sync.Map
	var uris []string
	var crawl func(in *entry)

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
		go crawl(&entry{
			URI:   uri,
			Depth: depth,
		})
	}

	crawl = func(in *entry) {
		defer wg.Done()
		d.Logf("Crawling %s...", in.URI)
		if err := crawlAndQueue(in, addEntry); err != nil {
			d.Logf("ERROR: Couldn't fetch %q; %v", in.URI, err)
		}
	}

	uri, err := url.Parse(entryURI)
	if err != nil {
		return nil, err
	}

	d.Logf("Starting to crawl...")
	addEntry(uri, depth)
	wg.Wait()

	return uris, nil
}
