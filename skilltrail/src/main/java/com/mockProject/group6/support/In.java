package com.mockProject.group6.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Safe readers for JSON request bodies (Map&lt;String,Object&gt;). */
public final class In {

    private In() {
    }

    public static Map<String, Object> body(Map<String, Object> b) {
        return b == null ? Collections.emptyMap() : b;
    }

    /** The value as text, or null when missing. */
    public static String str(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? null : v.toString();
    }

    /** Trimmed text, "" when missing. */
    public static String trim(Map<String, Object> m, String k) {
        String v = str(m, k);
        return v == null ? "" : v.trim();
    }

    /** A number (also accepts numeric text); null when missing or not a number. */
    public static Double num(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String && !((String) v).isBlank()) {
            try {
                return Double.valueOf(((String) v).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static Integer intv(Map<String, Object> m, String k) {
        Double d = num(m, k);
        return d == null ? null : (int) Math.round(d);
    }

    public static boolean bool(Map<String, Object> m, String k) {
        Object v = m.get(k);
        if (v instanceof Boolean) return (Boolean) v;
        return v != null && "true".equalsIgnoreCase(v.toString());
    }

    public static List<?> list(Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v instanceof List ? (List<?>) v : Collections.emptyList();
    }

    /** Integer from any object (Number or numeric text); null when not possible. */
    public static Integer toInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) {
            try {
                return Integer.valueOf(((String) v).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** Cuts text to a maximum length (database column sizes). */
    public static String cut(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
