package com.emobile.springtodo.configuration;

import org.springframework.data.domain.Sort;

/**
 * @author Alexei Shvariov
 */

public final class AppDefaults {
    public static final Sort SORT = Sort.by(Sort.Direction.ASC, "ID");
    public static final String PAGE_SIZE = "10";
    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_DIRECTION = "ASC";
    public static final String PAGE_SORT_PROP = "ID";
}
