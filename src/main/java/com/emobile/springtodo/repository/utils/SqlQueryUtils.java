package com.emobile.springtodo.repository.utils;

import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * @author Alexei Shvariov
 */
public class SqlQueryUtils {
    public static String sortToSqlString(Sort sort) {
       Optional<String>  stringOpt = sort.get()
                .filter(order -> !order.getProperty().isEmpty())
                .map(order -> {
                    return order.getProperty() + " " + order.getDirection().name().toUpperCase();
                })
                .reduce((sortParam1, sortParam2) -> {
                    return sortParam1 + ", " + sortParam2;
                });
       return stringOpt.orElseThrow(() -> new IllegalArgumentException("Переданный в качестве параметра объект Sort неверно настроен"));
    }

    public static String buildUpdateQueryParameters(LinkedHashMap<String, Object> entityFields) {
        String queryParams =  String.join(" = ?, ", entityFields.keySet());
        queryParams = queryParams.concat(" = ?");
        return queryParams;
    }

    public static String buildFindQueryParameters(LinkedHashMap<String, Object> entityFields) {
       List<String> params =  entityFields.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .toList();
        String queryParams = String.join(" = ? AND ", params);
        queryParams = queryParams.concat(" = ?");
        return queryParams;
    }

    public static String buildInsertQueryParameters(LinkedHashMap<String, Object> entityFields) {
        return String.join(", ", entityFields.keySet());
    }

    public static String addQuestionMarks(int parametersSize) {
        String[] questArray = new String[parametersSize];
        Arrays.fill(questArray, "?");
        return String.join(", ", questArray);
    }
}
