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

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java -jar <this .jar file> <path to URL file>");
            System.out.println("URL file should be a file with one remote url per line");
            return;
        }

        String hostUrlsFilePath = args[0];
        ArrayList<String> hostUrls = new ArrayList<>();
        try {
            hostUrls = new BufferedReader(new FileReader(hostUrlsFilePath))
                    .lines()
                    /*Allow C and Python style comments*/
                    .filter(s -> (!s.startsWith("#") && !s.startsWith("//")))
                    .filter(s -> (!s.isEmpty()))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (FileNotFoundException e) {
//            e.printStackTrace();
            System.err.println("Could not read input file");
            return;
        }

        /*print the header and hostname*/
        String hostname = "localhost";
        try {
            hostname = new BufferedReader(new FileReader(hostnamePath)).readLine();
        } catch (IOException e) {
            System.err.println("Could not read hostname, defaulting to localhost");
            e.printStackTrace();
        }
        System.out.println(String.format(headder, hostname, hostname));

        /*download and build the host list and then format and print it*/
        StringBuilder builder = new StringBuilder();
        hostUrls.parallelStream()
                .map(Main::downloadToSet)
                .reduce((strings, strings2) -> {
                    strings.addAll(strings2);
                    return strings;
                })
                .get()
                .forEach(builder::append);
        System.out.println(builder.toString());
    }

    private static Set<String> downloadToSet(String url) {
        BufferedInputStream in;
        try {
            URL source = new URL(url);
            in = new BufferedInputStream(source.openStream());
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
                .filter(s -> (!s.startsWith("#") && !s.startsWith("//")))
                .filter(s -> (!s.isEmpty()))
                .map(line -> "0.0.0.0 " + line
                        .replace("127.0.0.1", "")
                        .replace("0.0.0.0", "")
                        .replace("\t", "")
                        .replace(" ", "")
                        + "\n")
                .collect(Collectors.toSet());
    }
}
