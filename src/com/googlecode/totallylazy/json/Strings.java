package com.googlecode.totallylazy.json;

import com.googlecode.totallylazy.Mapper;

import static com.googlecode.totallylazy.Sequences.characters;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class Strings {
    public static String toString(CharSequence value) {
        return quote(escape(value));
    }

    public static String escape(CharSequence value) {
        return characters(value).map(functions.escape).toString("");
    }

    public static String escape(Character character) {
        switch (character) {
            case '"': return "\\\"";
            case '\\': return "\\\\";
            case '\b': return "\\b";
            case '\n': return "\\n";
            case '\r': return "\\r";
            case '\t': return "\\t";
            default: return character.toString();
        }
    }

    public static String quote(CharSequence value) {
        return format("\"%s\"", value);
    }

    public static String unescape(CharSequence escaped) {
        switch (escaped.charAt(0)) {
            case '"': return "\"";
            case '\\': return "\\";
            case '/': return "/";
            case 'b': return "\b";
            case 'n': return "\n";
            case 'r': return "\r";
            case 't': return "\t";
            case 'f': return "\f";
            case 'u': return Character.toString((char) parseInt(escaped.subSequence(2, escaped.length()).toString(), 16));
            default: throw new UnsupportedOperationException();
        }
    }

    public static class functions {
        public static Mapper<Character, String> escape = new Mapper<Character, String>() {
            public String call(Character character) throws Exception {
                return Strings.escape(character);
            }
        };

        public static Mapper<String, String> unescape = new Mapper<String, String>() {
            public String call(String escaped) throws Exception {
                return Strings.unescape(escaped);
            }
        };
    }
}

