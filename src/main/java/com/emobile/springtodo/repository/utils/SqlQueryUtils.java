package com.emobile.springtodo.repository.utils;

import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * Утилитарный класс, содержащий вспомогательный методы для формирования Sql запросов.
 * @author Alexei Shvariov
 */
public class SqlQueryUtils {

    /** Метод преобразования объекта {@link Sort} в строку для применения в параметрах sql запроса.
     * @param sort - направление и свойство сортировки в виде объекта класса {@link Sort}
     * @return возвращает строку с параметрами сортировки данных в sql запросе.*/
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

    /** Метод формирование параметров запроса на обновление записи.
     * @param entityFields - коллекция ключ-значение, где в качестве ключа используется заголовок столбца таблицы,
     *                    а в качестве значения - требуемое значение записи в данном столбце.
     * @return возвращает строку с параметрами запроса для обновления записи в таблице БД. */
    public static String buildUpdateQueryParameters(LinkedHashMap<String, Object> entityFields) {
        String queryParams =  String.join(" = ?, ", entityFields.keySet());
        queryParams = queryParams.concat(" = ?");
        return queryParams;
    }

    /** Метод формирования строки с параметрами запроса на поиск записей в таблице на основе значений свойств сущности.
     * @param entityFields - коллекция ключ-значение, где в качестве ключа используется заголовок столбца таблицы,
     *                    а в качестве значения - требуемое значение записи в данном столбце.
     * @return возвращает строку с параметрами запроса для поиска записей в таблице БД.
     * */
    public static String buildFindQueryParameters(LinkedHashMap<String, Object> entityFields) {
       List<String> params =  entityFields.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .toList();
        String queryParams = String.join(" = ? AND ", params);
        queryParams = queryParams.concat(" = ?");
        return queryParams;
    }

    /** Метод формирования строки с параметрами запроса на добавление записи в таблицу на основе значений свойств сущности.
     * @param entityFields - коллекция ключ-значение, где в качестве ключа используется заголовок столбца таблицы,
     *                    а в качестве значения - требуемое значение записи в данном столбце.
     * @return возвращает строку с параметрами запроса для добавления записи в таблицу БД.
     * */
    public static String buildInsertQueryParameters(LinkedHashMap<String, Object> entityFields) {
        return String.join(", ", entityFields.keySet());
    }

    /** Метод формирования строки с требуемым количеством знаков вопроса для использования при построении запроса в БД.
     * @param parametersSize - количество параметров в запросе.
     * @return возвращает строку со знаками вопроса и запятой в качестве разделителя.
     * */
    public static String addQuestionMarks(int parametersSize) {
        String[] questArray = new String[parametersSize];
        Arrays.fill(questArray, "?");
        return String.join(", ", questArray);
    }
}
