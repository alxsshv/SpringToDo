package com.emobile.springtodo.repository;

import com.emobile.springtodo.exception.RepositoryOperationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/** Интерфейс, описывающий методы базового репозитория для получения записей о сущностях из БД. */

public interface EntityRepository<T, ID> {

    /** Метод получения всех записей о сущности, содержащихся в указанной таблице.
     * @return возвращает список экземпляров сущности, котрой параметризован репозиторий. */
    List<T> findAll();

    /** Метод постраничного получения всех записей о сущности, содержащихся в указанной таблице.
     * @param pageable - параметры пагинации в виде {@link Pageable}.
     * @return возвращает страницу экземпляров сущности, котрой параметризован репозиторий. */
    Page<T> findAll(Pageable pageable);


    List<T> findByParameters(LinkedHashMap<String, Object> entityFields);
    Page<T> findByParameters(LinkedHashMap<String, Object> entityFields, Pageable pageable);

    /** Метод получения записи о сущности по уникальному идентификатору.
     * @param id - уникальный идентификатор записи об объекте в базе данных.
     * @return возвращает {@link Optional} с экземпляром сущности, котрой параметризован репозиторий,
     * уникальный идентификатор которого соответствует параметру метода id или пустой Optional, если объект не найден.
     * */
    Optional<T> findById(ID id);

    /** Метод сохранения записи о переданном объекте в БД
     * или обновляет его значения, если он ранее был сохранен в БД и ему присвоен уникальный идентификатор.
     * @param entityFields - карта (мапа) полей объекта с ключом, соответствующим заголовку столбца таблицы БД
     * @return возвращает сохранённый в базе данных объект с уникальным идентификатором */
    T save(LinkedHashMap<String, Object> entityFields);

    /** Удаление записи по уникальному идентификатору.
     * @param id - уникальный идентификатор объекта, который необходимо удалить*/
    void deleteById(ID id);

    /** Метод удаления всех записей об объектах (очистка таблицы).*/
    void deleteAll();

    /** Счетчик количества записей в таблице.
     * @return возвращает количество записей в таблице.
     * @throws RepositoryOperationException будет выброшено,
     * в случае возникновения исключений при выполнении запроса к БД с использованием {@link JdbcTemplate}
     * */
    Long count();
}
