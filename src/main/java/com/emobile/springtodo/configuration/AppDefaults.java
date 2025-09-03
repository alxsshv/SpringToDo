package com.emobile.springtodo.configuration;

import org.springframework.data.domain.Sort;

/**
 * Класс для хранения значений по умолчанию, используемых в приложении
 * @author Alexei Shvariov
 */

public final class AppDefaults {

    /** Параметры сортировки по умолчанию */
    public static final Sort SORT = Sort.by(Sort.Direction.ASC, "id");

    /** Размер страницы с данными по умолчанию (для пагинации) */
    public static final String PAGE_SIZE = "10";

    /** Номер страницы с данными по умолчанию (для пагинации) */
    public static final String PAGE_NUMBER = "0";

    /** Направление сортировки страницы по умолчанию (для пагинации) */
    public static final String PAGE_DIRECTION = "ASC";

    /** Свойство по которому выполняется сортировка по умолчанию (для пагинации) */
    public static final String PAGE_SORT_PROP = "id";
}
