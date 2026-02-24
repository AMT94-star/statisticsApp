package util;

import java.util.Set;

public class TextNormalizer {

    //ord som alltid ska vara caps
    private static final Set<String> ALWAYS_UPPER = Set.of(
            "GO", "IV", "CP", "XP", "EX",
            "CD", "GBL",
            "XS", "S", "M", "L", "XL", "XXL"
    );

    //ord som ska ha specifik stavning
    private static final Set<String> SPECIAL_CASE = Set.of(
            "PVP",
            "SHADOW",
            "MEGA",
            "DYNAMAX",
            "GIGANTAMAX"
    );

    public static String smartTitle(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return "";

        String[] words = t.split("\\s+");
        StringBuilder out = new StringBuilder();

        for (String w : words) {
            if (w.isEmpty()) continue;
            out.append(formatWordWithDelimiters(w)).append(" ");
        }

        return out.toString().trim();
    }

    private static String formatWordWithDelimiters(String word) {
        StringBuilder sb = new StringBuilder();
        StringBuilder part = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            if (c == '-' || c == '\'') {
                sb.append(formatSingleToken(part.toString()));
                sb.append(c);
                part.setLength(0);
            } else {
                part.append(c);
            }
        }

        sb.append(formatSingleToken(part.toString()));
        return sb.toString();
    }

    private static String formatSingleToken(String token) {
        if (token == null) return "";
        String t = token.trim();
        if (t.isEmpty()) return "";

        String upper = t.toUpperCase();

        if (ALWAYS_UPPER.contains(upper)) {
            return upper;
        }

        if (SPECIAL_CASE.contains(upper)) {
            return switch (upper) {
                case "PVP" -> "PvP";
                case "SHADOW" -> "Shadow";
                case "MEGA" -> "Mega";
                case "DYNAMAX" -> "Dynamax";
                case "GIGANTAMAX" -> "Gigantamax";
                default -> upper;
            };
        }

        String lower = t.toLowerCase();
        return lower.substring(0, 1).toUpperCase() + lower.substring(1);
    }
}