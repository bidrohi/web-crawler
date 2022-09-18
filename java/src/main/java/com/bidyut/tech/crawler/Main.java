package com.bidyut.tech.crawler;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final ArgumentParser parser = ArgumentParsers.newFor("crawler").build()
                .defaultHelp(true)
                .description("Crawl a webpage and collect links to a depth");
        parser.addArgument("-d", "--depth")
                .type(Integer.class)
                .setDefault(2)
                .help("Number of depth to follow");
        parser.addArgument("-u", "--uri")
                .type(String.class)
                .setDefault("https://www.engadget.com")
                .help("Base url to start crawling from");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        final String uri = ns.getString("uri");
        final Integer depth = ns.getInt("depth");

        System.out.println("Crawl benchmarking...");
        System.out.println("Crawling " + uri + " up to " + depth + " levels deep...");
        final List<Droid> droids = List.of(
                new Droid1().enableDebugLog(true)
        );
        for (Droid d : droids) {
            crawlAndTime(d, uri, depth);
        }
    }

    private static void crawlAndTime(
            @NotNull Droid d,
            @NotNull String uri,
            int depth
    ) {
        System.out.println("Invoking " + d.getName());
        final Instant start = Instant.now();
        final List<String> urls = d.invoke(uri, depth);
        final Instant end = Instant.now();
        System.out.println("=> found " + urls.size() + " distinct URLs");
        System.out.println("=> took " + Duration.between(start, end).toMillis() + "ms");
    }
}
