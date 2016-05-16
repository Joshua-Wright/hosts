package com.company;

import java.util.ArrayList;
import java.util.HashMap;

public class ArgParser {
    private enum ArgumentType {
        KEY_VALUE,
        IF_PRESENT,
    }

    private HashMap<String, ArgumentType> argTypes;
    private HashMap<Integer, String> allArgs;
    private HashMap<String, Integer> flagToKey;
    private ArrayList<String> miscArgs;

    public ArgParser() {
        this.argTypes = new HashMap<>();
        this.allArgs = new HashMap<>();
        this.flagToKey = new HashMap<>();
        this.miscArgs = new ArrayList<>();
    }

    public ArgParser putKeyValue(String flag, Integer key) {
        argTypes.put(flag, ArgumentType.KEY_VALUE);
        flagToKey.put(flag, key);
        return this;
    }

    public ArgParser putDefault(Integer key, String defaultValue) {
        allArgs.put(key, defaultValue);
        return this;
    }

    public ArgParser putIfPresent(String flag, Integer key) {
        argTypes.put(flag, ArgumentType.IF_PRESENT);
        flagToKey.put(flag, key);
        return this;
    }

    public ArgParser parse(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                miscArgs.add(args[i]);
                continue;
            }
            /*todo: -flag=value argument types*/
            /*todo: detect double-dashes, e.g. --flag*/
            switch (argTypes.get(args[i])) {
                case KEY_VALUE:
                    allArgs.put(flagToKey.get(args[i]), args[i + 1]);
                    i++; /*advance i because the next one is a non-argument*/
                    break;
                case IF_PRESENT:
                    /*empty non-null value*/
                    allArgs.put(flagToKey.get(args[i]), "");
                    break;
            }
        }
        return this;
    }

    public String getValue(Integer key) {
        return allArgs.get(key);
    }

    public ArrayList<String> getMiscArgs() {
        return miscArgs;
    }

    public boolean isPresent(Integer key) {
        return allArgs.get(key) != null;
    }


    /* test case*/
    public static void main(String[] args) {
        String[] testCase = " asdf -a 4 -v".split(" ");
        int key_a = 1;
        int key_b = 2;
        int key_v = 3;
        int key_d = 4;
        ArgParser parser = new ArgParser()
                .putIfPresent("-v", key_v)
                .putKeyValue("-a", key_a)
                .putKeyValue("-b", key_b)
                .putKeyValue("-d", key_d)
                .putDefault(key_d, "5")
                .parse(testCase);
        test(parser.getValue(key_a).equals("4"), "key value");
        test(parser.isPresent(key_v), "is present v");
        test(!parser.isPresent(key_b), "is present b");
        test(parser.getValue(key_d).equals("5"), "key value");
        test(parser.getMiscArgs().get(0).equals("asdf"), "misc args");
    }

    private static void test(boolean expr, String message) {
        if (!expr) {
            System.out.println("Test failed: " + message);
        }
    }

}
