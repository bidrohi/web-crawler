package crawler

import (
	"net/url"
)

type Droid1 struct {
	Logger
}

var _ Droid = (*Droid1)(nil)

func (d Droid1) Name() string {
	return "Droid1"
}

func (d Droid1) Invoke(entryURI string, depth int) ([]string, error) {
	d.Logf("Preparing crawl...")
	var queue []*entry

	addEntry := func(uri *url.URL, depth int) {
		queue = append(queue, &entry{
			URI:   uri,
			Depth: depth,
		})
	}

	crawl := func(in *entry) {
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

	d.Logf("Starting to crawl...")
	visited := make(map[string]*struct{})
	for len(queue) > 0 {
		in := queue[0]
		queue = queue[1:]
		//d.Logf("==> %d found; %d in queue", len(visited), len(inChan))
		key := in.URI.Host + in.URI.Path
		if _, ok := visited[key]; !ok {
			visited[key] = &struct{}{}
			if in.Depth > 0 {
				crawl(in)
			}
		}
	}

	return getMapKeys(visited), nil
}
