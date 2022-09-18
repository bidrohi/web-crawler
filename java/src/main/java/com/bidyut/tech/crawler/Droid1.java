package com.bidyut.tech.crawler;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.*;

public class Droid1 extends Droid {
    @Override
    public @NotNull String getName() {
        return "Droid1";
    }

    @Override
    public @NotNull List<String> invoke(@NotNull String uri, int depth) {
        debugLog("Starting to crawl...");
        final Queue<Entry> queue = new LinkedList<>();
        queue.add(new Entry(uri, depth));
        final List<String> uris = new LinkedList<>();
        final Set<URI> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            final Entry entry = queue.poll();
            uris.add(entry.uri.toString());
            if (entry.depth > 0 && !visited.contains(entry.uri)) {
                visited.add(entry.uri);
                crawlAndQueue(entry, queue::add).join();
            }
        }
        shutdown();
        return uris;
    }
}
