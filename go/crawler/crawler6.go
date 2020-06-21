package crawler

import (
	"net/url"
	"sync"
)

type Droid6 struct {
	Logger

	wg      sync.WaitGroup
	visited sync.Map
	uris    []string
}

var _ Droid = (*Droid6)(nil)

func (d *Droid6) Name() string {
	return "Droid6"
}

func (d *Droid6) addEntry(uri *url.URL, depth int) {
	key := uri.Host + uri.Path
	if _, ok := d.visited.Load(key); ok {
		return
	}
	//d.Logf("Remaining depth %d: Found %s", depth, uri)
	d.uris = append(d.uris, uri.String())
	if depth == 0 {
		return
	}
	d.wg.Add(1)
	d.visited.Store(key, &struct{}{})
	go d.crawl(uri, depth)
}

func (d *Droid6) crawl(uri *url.URL, depth int) {
	defer d.wg.Done()
	d.Logf("Crawling %s...", uri)
	if err := crawlAndQueue(&entry{
		URI:   uri,
		Depth: depth,
	}, d.addEntry); err != nil {
		d.Logf("ERROR: Couldn't fetch %q; %v", uri, err)
	}
}

func (d *Droid6) Invoke(entryURI string, depth int) ([]string, error) {
	d.Logf("Preparing crawl...")

	uri, err := url.Parse(entryURI)
	if err != nil {
		return nil, err
	}

	d.Logf("Starting to crawl...")
	d.addEntry(uri, depth)
	d.wg.Wait()

	return d.uris, nil
}
