package com.mockProject.group6.support;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Typed readers for JdbcTemplate rows (column names are case-insensitive). */
public final class Rows {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private Rows() {
    }

    public static int i(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v instanceof Number ? ((Number) v).intValue() : 0;
    }

    /** Nullable integer. */
    public static Integer in(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v instanceof Number ? Integer.valueOf(((Number) v).intValue()) : null;
    }

    public static double d(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v instanceof Number ? ((Number) v).doubleValue() : 0d;
    }

    public static String s(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v == null ? null : v.toString();
    }

    /** Text, "" instead of null. */
    public static String sn(Map<String, Object> r, String k) {
        Object v = r.get(k);
        return v == null ? "" : v.toString();
    }

    public static boolean b(Map<String, Object> r, String k) {
        Object v = r.get(k);
        if (v instanceof Boolean) return (Boolean) v;
        return v instanceof Number && ((Number) v).intValue() != 0;
    }

    /** Timestamp column as "yyyy-MM-ddTHH:mm:ss" (what the front-end mock uses), null when empty. */
    public static String ts(Map<String, Object> r, String k) {
        Object v = r.get(k);
        if (v == null) return null;
        if (v instanceof Timestamp) return ((Timestamp) v).toLocalDateTime().format(ISO);
        if (v instanceof java.util.Date) return new Timestamp(((java.util.Date) v).getTime()).toLocalDateTime().format(ISO);
        return v.toString();
    }
}
