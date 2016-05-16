package com.company;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static String hostnamePath = "/etc/hostname";
    private static String headder = "# hosts file made by j0sh\n" +
            "127.0.0.1 %s # current hostname for this machine\n" +
            "::1 %s # current hostname for this machine\n" +
            "127.0.0.1 localhost # IPv4 localhost\n" +
            "::1 localhost #IPv6 localhost\n";
    private static int KEY_NO_HEADDER = 1;
    private static int KEY_WHITELIST_PATH = 2;

    public static void main(String[] args) {
        ArgParser arglist = new ArgParser()
                .putIfPresent("-nh", KEY_NO_HEADDER)
                .putKeyValue("-w", KEY_WHITELIST_PATH)
                .parse(args);
        if (arglist.getMiscArgs().size() < 1) {
            System.out.println("Usage: java -jar <this .jar file> <path to URL file>");
            System.out.println("URL file should be a file with one remote url per line");
            System.out.println("Optional: ");
            System.out.println(" -nh           do not print the header");
            System.out.println(" -w <file>     ignore the hosts specified in file");
            return;
        }

        /*read the whitelist file if we specify one*/
        final Set<String> whitelist = new HashSet<>();
        if (arglist.isPresent(KEY_WHITELIST_PATH)) {
            /*addAll instead of assignment so that the variable may be final*/
            readFileByLines(arglist.getValue(KEY_WHITELIST_PATH))
                    .stream()
                    .forEach(whitelist::add);
        }

        /*print the header and hostname*/
        if (!arglist.isPresent(KEY_NO_HEADDER)) {
            String hostname = "localhost";
            try {
                hostname = new BufferedReader(new FileReader(hostnamePath)).readLine();
            } catch (IOException e) {
                System.err.println("Could not read hostname, defaulting to localhost");
                e.printStackTrace();
            }
            System.out.println(String.format(headder, hostname, hostname));
        }

        /*download and build the host list and then format and print it*/
        Set<String> hostUrls = readFileByLines(arglist.getMiscArgs().get(0));
        System.out.println(hostUrls.parallelStream()
                .map(Main::downloadToSet)
                .reduce((strings, strings2) -> {
                    strings.addAll(strings2);
                    return strings;
                })
                .get()
                .parallelStream()
                .filter(s -> !whitelist.contains(s))
                .collect(Collectors.joining("\n"))
        );
    }

    private static boolean isNotComment(String s) {
        /*Allow C and Python style comments*/
        return !s.startsWith("#") && !s.startsWith("//") && !s.isEmpty();
    }

    private static Set<String> readFileByLines(String path) {
        try {
            return new BufferedReader(new FileReader(path))
                    .lines()
                    .filter(Main::isNotComment)
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (FileNotFoundException e) {
            System.err.println("Could not read input file");
            return new HashSet<>();
        }
    }

    private static Set<String> downloadToSet(String url) {
        BufferedInputStream in;
        try {
            in = new BufferedInputStream(new URL(url).openStream());
        } catch (IOException e) {
            /*if we could not download, ignore*/
            System.err.println("Failed to download: " + url);
            return new HashSet<>();
        }
        Scanner scanner = new Scanner(in);
        ArrayList<String> lines = new ArrayList<>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        return lines.parallelStream()
                /*Allow C and Python style comments*/
                .filter(Main::isNotComment)
                .map(line -> "0.0.0.0 " + line
                        .replace("127.0.0.1", "")
                        .replace("0.0.0.0", "")
                        .replace("\t", "")
                        .replace(" ", ""))
                .collect(Collectors.toSet());
    }
}
