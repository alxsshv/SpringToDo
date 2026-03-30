package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.configuration.AppDefaults;
import com.emobile.springtodo.entity.Identifiable;
import com.emobile.springtodo.exception.EntityIllegalStateException;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.exception.RepositoryOperationException;
import com.emobile.springtodo.repository.EntityRepository;
import com.emobile.springtodo.repository.annotation.TableName;
import com.emobile.springtodo.repository.utils.SqlQueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/** Абстрактный класс - репозиторий, описывающий методы выполнения CRUD-операций над сущностями,
 *  реализующими интерфейс {@link Identifiable}.
 *  В классе, описывающем сущность должна быть установлена аннотация
 *  {@link TableName}  со значением параметра value, соответствующем названию таблицы в БД.
 */

@Slf4j
public abstract class EntityRepositoryImpl<T extends Identifiable<ID>, ID> implements EntityRepository<T, ID> {

    /** Тип сущности с котрой работает репозиторий. */
    private final Type entityType;

    /** Класс, для организации взаимодействия с базой данных */
    protected final JdbcTemplate jdbcTemplate;

    /** Интерфейс для преобразования данных, полученных из базы данных в сущность.
     *  Для преобразования необходимо предоставить реализацию интерфейса для соответствующей сущности */
    protected final RowMapper<T> rowMapper;

    /** Имя таблицы, в которой хранятся записи об объектах соответствующей сущности в БД.*/
    private String tableName;

    /** Конструктор с параметрами, который должен быть вызван в классе наследнике
     * @param jdbcTemplate  - экземпляр класса {@link JdbcTemplate}.
     * @param rowMapper - должен быть передан интерфейс RowMapper параметризованный сущностью с которой работает репозиторий
     */
    public EntityRepositoryImpl(JdbcTemplate jdbcTemplate, RowMapper<T> rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.entityType = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /** Метод получения имени таблицы БД, в которой хранятся записи сущности.
     *  Если имя таблицы отсутствует в свойствах репозитория,
     *  будет выполнена попытка получения имени таблицы из аннотации {@link TableName},
     *  которая должна быть установлена в классе описывающем сущность с указанием имени соответствующей таблицы.
     * @return возвращает имя таблицы БД, в которой хранятся записи сущности.
     * @throws EntityIllegalStateException будет выброшено, если Класс, описывающий сущность,
     * содержит аннотацию {@link TableName}, но имя таблицы (параметр value) не указано.
     * @throws IllegalStateException будет выброшено, если класс-наследник не передал в конструкцию интерфейс RowMapper,
     * параметризованный типом сущности.
     */
    protected String getTableName() {
        if (tableName == null) {
            final String tableNameAnnotationValue = getTableNameAnnotationValue();
            if (tableNameAnnotationValue.isEmpty()) {
                throw new EntityIllegalStateException("Класс, описывающий сущность, содержит аннотацию @TableName," +
                        " но имя таблицы (параметр value) не указано");
            }
            tableName = tableNameAnnotationValue;
        }
        return tableName;
    }

    /** Получение значения имени таблицы из параметра value аннотации {@link TableName}, которой помечена сущность,
     *  с котрой работает репозиторий
     * @return возвращает строку, содержащую имя таблицы, из значения value аннотации {@link TableName}.
     * @throws EntityIllegalStateException будет выброшено, если класс,
     * описывающий сущность не помечена аннотацией {@link TableName}.
     * @throws RepositoryOperationException будет выброшено в случае возникновения прочих
     * ошибок при получении имени таблицы из значения аннотации.
     */
    private String getTableNameAnnotationValue() {
        try {
            final Class<?> entityClass = Class.forName(entityType.getTypeName());
            if (!entityClass.isAnnotationPresent(TableName.class)) {
                throw new EntityIllegalStateException("Класс, описывающий сущность, не содержит обязательной аннотации @TableName");
            }
            return entityClass.getAnnotation(TableName.class).value();
        } catch (ClassNotFoundException  ex) {
            String errorMessage = "Ошибка при чтении имени таблицы из аннотации @TableName: " + ex.getMessage();
            log.error(errorMessage);
            throw new RepositoryOperationException(errorMessage);
        }
    }

    /** Метод получения всех записей о сущности, содержащихся в указанной таблице.
     * @return возвращает список экземпляров сущности, котрой параметризован репозиторий. */
    @Override
    public List<T> findAll() {
        final String sqlQuery = "SELECT * FROM " + getTableName();
        return jdbcTemplate.query(sqlQuery, rowMapper);
    }

    /** Метод постраничного получения всех записей о сущности, содержащихся в указанной таблице.
     * @param pageable - параметры пагинации в виде {@link Pageable}.
     * @return возвращает страницу экземпляров сущности, котрой параметризован репозиторий. */
    @Override
    public Page<T> findAll(Pageable pageable) {
        final long total = count();
        final Sort sort = pageable.getSortOr(AppDefaults.SORT);
        final String getEntitiesPageQuery = "SELECT * FROM " + getTableName() + " ORDER BY "
                + SqlQueryUtils.sortToSqlString(sort)
                + " OFFSET ? ROWS "
                + " FETCH NEXT ? ROWS ONLY" ;
        List<T> content = jdbcTemplate.query(getEntitiesPageQuery, rowMapper, pageable.getOffset(), pageable.getPageSize());
        return new PageImpl<>(content, pageable, total);
    }

    /** Метод получения записи о сущности по уникальному идентификатору.
     * @param id - уникальный идентификатор записи об объекте в базе данных.
     * @return возвращает {@link Optional} с экземпляром сущности, котрой параметризован репозиторий,
     * уникальный идентификатор которого соответствует параметру метода id или пустой Optional, если объект не найден.
     * */
    public Optional<T> findById(ID id) {
        T entity;
        try {
            entity = jdbcTemplate.queryForObject("SELECT * FROM " + getTableName() + " WHERE id = ?", rowMapper, id);
        } catch (DataAccessException ex) {
            log.error("when find user by id {} has been thrown exception with message {}", id, ex.getMessage());
            return Optional.empty();
        }
        return Optional.ofNullable(entity);
    }


    /** Метод сохранения записи о переданном объекте в БД
     * или обновляет его значения, если он ранее был сохранен в БД и ему присвоен уникальный идентификатор.
     * @param entityFields - карта (мапа) полей объекта с ключом, соответствующим заголовку столбца таблицы БД
     * @return возвращает сохранённый в базе данных объект с уникальным идентификатором */
    @Override
    public T save(LinkedHashMap<String, Object> entityFields) {
        if (!entityFields.containsKey("id") || entityFields.get("id") == null) {
            String insertQuery = "INSERT INTO " + getTableName() + " (" + SqlQueryUtils.buildInsertQueryParameters(entityFields)
                    + ") values (" + SqlQueryUtils.addQuestionMarks(entityFields.size()) + ")";
            int updateResult = jdbcTemplate.update(insertQuery, entityFields.values().toArray());
            assert updateResult > 0;
            return findByParameters(entityFields).getFirst();
        } else {
            String updateQuery = "UPDATE " + getTableName() + " SET " + SqlQueryUtils.buildUpdateQueryParameters(entityFields) + " WHERE id = ?";
            List<Object> values = new LinkedList<>(entityFields.values());
            values.add(entityFields.get("id"));
            int updateResult = jdbcTemplate.update(updateQuery, values.toArray());
            assert updateResult > 0;
            return findById((ID) entityFields.get("id"))
                    .orElseThrow(() -> new EntityNotFoundException("Сущность с указанным id " + entityFields.get("id") + " не найдена"));
        }
    }

    /** Поиск объектов, соответствующих определённым параметрам.
     * @param entityFields - карта (мапа) полей объекта с ключом, соответствующим заголовку столбца таблицы БД.
     * @return список объектов, записи о которых в БД соответствуют каждому из указанных параметров.
     * */
    @Override
    public List<T> findByParameters(LinkedHashMap<String, Object> entityFields) {
        String findQuery = "SELECT * FROM " + getTableName() + " WHERE " + SqlQueryUtils.buildFindQueryParameters(entityFields);
        return jdbcTemplate.query(findQuery, rowMapper, entityFields.values().stream().filter(Objects::nonNull).toArray());
    }

    /** Постраничный поиск объектов, соответствующих определённым параметрам.
     * @param entityFields - карта (мапа) полей объекта с ключом, соответствующим заголовку столбца таблицы БД.
     * @param pageable - параметры пагинации в виде {@link Pageable}.
     * @return страницу с объектами, записи о которых в БД соответствуют каждому из указанных параметров.
     */
    @Override
    public Page<T> findByParameters(LinkedHashMap<String, Object> entityFields, Pageable pageable) {
        final String totalSqlQuery = "SELECT count(1) FROM " + getTableName()
                + " WHERE " + SqlQueryUtils.buildFindQueryParameters(entityFields);
        final Object[] args = entityFields.values().stream().filter(Objects::nonNull).toArray();
        final long total = jdbcTemplate.queryForObject(totalSqlQuery, Long.class, args);

        final Sort sort = pageable.getSortOr(AppDefaults.SORT);
        final String getEntitiesPageQuery = "SELECT * FROM " + getTableName()
                + " WHERE " + SqlQueryUtils.buildFindQueryParameters(entityFields)
                + " ORDER BY " + SqlQueryUtils.sortToSqlString(sort)
                + " OFFSET ? ROWS "
                + " FETCH NEXT ? ROWS ONLY" ;
        List<Object> fieldValues = new ArrayList<>(entityFields.values().stream().filter(Objects::nonNull).toList());
        fieldValues.addAll(List.of(pageable.getOffset(), pageable.getPageSize()));
        final List<T> content = jdbcTemplate.query(getEntitiesPageQuery, rowMapper, fieldValues.toArray());
        return new PageImpl<>(content, pageable, total);
    }

    /** Метод удаления записи об объекте по его уникальному идентификатору
     * @param id - уникальный идентификатор объекта.
     * */
    @Override
    public void deleteById(ID id) {
        final String deleteQuery = "DELETE FROM " + getTableName() + " WHERE id = ?";
        jdbcTemplate.update(deleteQuery, id);
    }

    /** Метод удаления всех записей об объектах (очистка таблицы).*/
    @Override
    public void deleteAll() {
        final String clearTableQuery = "DELETE FROM " + getTableName();
        jdbcTemplate.update(clearTableQuery);
    }


    /** Счетчик количества записей в таблице.
     * @return возвращает количество записей в таблице.
     * @throws RepositoryOperationException будет выброшено,
     * в случае возникновения исключений при выполнении запроса к БД с использованием {@link JdbcTemplate}
     * */
    @Override
    public Long count() {
        final String getCountSqlQuery = "SELECT count(1) FROM " + getTableName();
        try {
            return jdbcTemplate.queryForObject(getCountSqlQuery, Long.class);
        } catch (Exception ex) {
            throw new RepositoryOperationException("Ошибка выполнения sql" + ex.getMessage());
        }
    }
}
