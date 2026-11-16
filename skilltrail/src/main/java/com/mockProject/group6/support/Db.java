package com.mockProject.group6.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;

public final class Db {

    private Db() {
    }

    /** Runs an INSERT and returns the generated identity value. */
    public static int insert(JdbcTemplate jdbc, String sql, Object... args) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int k = 0; k < args.length; k++) ps.setObject(k + 1, args[k]);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key == null) throw new IllegalStateException("No generated key");
        return key.intValue();
    }
}
