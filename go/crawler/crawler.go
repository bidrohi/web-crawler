package crawler

import (
	"fmt"
	"github.com/PuerkitoBio/goquery"
	"log"
	"net/http"
	"net/url"
)

type Droid interface {
	Name() string
	Invoke(entryURI string, depth int) ([]string, error)
}

type entry struct {
	URI   *url.URL
	Depth int
}

func crawlAndQueue(stage *entry, addEntry func(uri *url.URL, depth int)) error {
	if stage.Depth == 0 {
		return nil
	}
	resp, err := http.Get(stage.URI.String())
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to fetch content; error %s", resp.Status)
	}
	doc, err := goquery.NewDocumentFromReader(resp.Body)
	if err != nil {
		return err
	}
	doc.
		Find("a").
		Each(func(_ int, sel *goquery.Selection) {
			val, ok := sel.Attr("href")
			if !ok {
				return
			}
			newURI, err := stage.URI.Parse(val)
			if err != nil {
				// invalid URI
				return
			}
			addEntry(newURI, stage.Depth-1)
		})
	return nil
}

type Logger struct {
	IsDebugLogEnabled bool
}

func (l Logger) Logf(fmt string, args ...interface{}) {
	if l.IsDebugLogEnabled {
		log.Printf(fmt, args...)
	}
}
