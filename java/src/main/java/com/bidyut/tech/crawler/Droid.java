package com.bidyut.tech.crawler;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class Droid {

    private final @NotNull ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final @NotNull HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .executor(executorService)
            .build();
    private boolean debugLogEnabled;

    public @NotNull Droid enableDebugLog(boolean enable) {
        debugLogEnabled = enable;
        return this;
    }

    public abstract @NotNull String getName();

    public abstract @NotNull List<String> invoke(@NotNull String uri, int depth);

    protected void debugLog(
            @NotNull String msg
    ) {
        if (debugLogEnabled) {
            System.out.println(msg);
        }
    }

    @Blocking
    protected CompletableFuture<Boolean> crawlAndQueue(
            @NotNull Entry entry,
            @NotNull Entry.Adder addEntry
    ) {
        debugLog("Crawling " + entry.uri + "...");
        final int nextDepth = entry.depth - 1;
        final HttpRequest request = HttpRequest.newBuilder(entry.uri).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApplyAsync(response -> {
                    final int statusCode = response.statusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        final InputStream body = response.body();
                        final Document doc;
                        try {
                            doc = Jsoup.parse(body, StandardCharsets.UTF_8.name(), response.uri().toString());
                        } catch (IOException e) {
                            debugLog("-> !Error: " + e);
                            return false;
                        }
                        final Elements anchors = doc.getElementsByTag("a");
                        for (Element anchor : anchors) {
                            final String newUri = anchor.absUrl("href");
                            debugLog("-> Found " + newUri);
                            addEntry.call(new Entry(newUri, nextDepth));
                        }
                    } else {
                        debugLog("-> !Error: status code " + statusCode);
                    }
                    return true;
                });
    }

    protected void shutdown() {
        executorService.shutdownNow();
    }
}
