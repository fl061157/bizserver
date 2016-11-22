package com.handwin.entity;

import info.archinnov.achilles.codec.Codec;
import info.archinnov.achilles.exception.AchillesTranscodingException;

import java.sql.Timestamp;

/**
 * Created by yangwei on 14-12-1.
 */
public class TimestampToString implements Codec<Timestamp, String> {
    @Override
    public Class<Timestamp> sourceType() {
        return Timestamp.class;
    }

    @Override
    public Class<String> targetType() {
        return String.class;
    }

    @Override
    public String encode(Timestamp timestamp) throws AchillesTranscodingException {
        return String.valueOf(timestamp.getTime());
    }

    @Override
    public Timestamp decode(String s) throws AchillesTranscodingException {
        try {
            return new Timestamp(Long.parseLong(s));
        } catch (Exception e) {
            return null;
        }
    }
}
