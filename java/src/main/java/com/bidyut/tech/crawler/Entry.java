package com.bidyut.tech.crawler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Objects;

class Entry {
    @NotNull
    public final URI uri;
    public final int depth;

    Entry(
            @NotNull URI uri,
            int depth
    ) {
        this.uri = uri;
        this.depth = depth;
    }

    Entry(
            @NotNull String uri,
            int depth
    ) {
        this(URI.create(uri), depth);
    }

    @Override
    public boolean equals(
            @Nullable Object o
    ) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return depth == entry.depth && uri.equals(entry.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, depth);
    }

    interface Adder {
        void call(@NotNull Entry entry);
    }
}
