package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.configuration.AppDefaults;
import com.emobile.springtodo.entity.ServiceUser;
import com.emobile.springtodo.exception.EntityNotFoundException;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.repository.utils.SqlQueryUtils;
import com.emobile.springtodo.security.SecurityRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class UserRepositoryImpl extends EntityRepositoryImpl<ServiceUser, Long> implements UserRepository {

    public UserRepositoryImpl(@Autowired JdbcTemplate jdbcTemplate,
                              @Autowired RowMapper<ServiceUser> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    @Override
    public List<ServiceUser> findAll() {
        final String sqlQuery = "SELECT su.id, su.username, su.email, su.password, sr.security_role FROM "
                + getTableName() + " su INNER JOIN " + ServiceUser.getRoleTableName() + " sr ON su.id = sr.user_id";
        return jdbcTemplate.query(sqlQuery, new ServiceUserResultSetExtractor());
    }


    @Override
    public Page<ServiceUser> findAll(Pageable pageable) {
        final long total = count();
        final Sort sort = pageable.getSortOr(AppDefaults.SORT);
        final String getEntitiesPageQuery = "SELECT su.id, su.username, su.email, su.password, sr.security_role FROM "
                + "(SELECT * FROM "+ getTableName()
                + " ORDER BY " + SqlQueryUtils.sortToSqlString(sort)
                + " OFFSET ? ROWS "
                + " FETCH NEXT ? ROWS ONLY ) AS su"
                + " LEFT JOIN " + ServiceUser.getRoleTableName() + " sr ON su.id = sr.user_id";
        List<ServiceUser> content = jdbcTemplate.query(getEntitiesPageQuery, new ServiceUserResultSetExtractor(), pageable.getOffset(), pageable.getPageSize());
        content = content == null ? new LinkedList<>() : content;
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<ServiceUser> findById(Long id) {
        ServiceUser user;
        try {
            String findUserSql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
            user = jdbcTemplate.queryForObject(findUserSql, rowMapper,id);
            if (user != null) {
                String findUserRolesSql = "SELECT security_role FROM " + ServiceUser.getRoleTableName() + " WHERE user_id = ?";
                List<String> roles = jdbcTemplate.queryForList(findUserRolesSql, String.class, id);
                user.setRoles(roles.stream().map(SecurityRole::valueOf).collect(Collectors.toSet()));
            }
        } catch (DataAccessException ex) {
            log.error("При попытке найти информацию о пользователе по id {} получено сообщение об ошибке {}", id, ex.getMessage());
            return Optional.empty();
        }
        return Optional.ofNullable(user);
    }

    @Override
    public ServiceUser save(ServiceUser user) {
        LinkedHashMap<String, Object> userFields = buildUserFields(user);
        if (user.getId() == null) {
            int insertResult = saveUserTable(userFields);
            assert (insertResult > 0);
            ServiceUser userFromDb = findByParameters(userFields).getFirst();
            user.setId(userFromDb.getId());
            int[] saveResults = saveRoleTable(user);
            assert Arrays.stream(saveResults).allMatch(value -> value > 0);
            return user;
        } else {
            int updateResult = updateUserTable(user, userFields);
            assert updateResult > 0;
            int[] updateResults = updateRoleTable(user);
            assert Arrays.stream(updateResults).allMatch(value -> value > 0);
            return findById(user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь с id " + user.getId() + " не найден"));
        }

    }

    private LinkedHashMap<String, Object> buildUserFields(ServiceUser user) {
        LinkedHashMap<String, Object> userFields = new LinkedHashMap<>();
        if (user.getId() != null) {
            userFields.put("id", user.getId());
        }
        userFields.put("email", user.getEmail());
        userFields.put("username", user.getUsername());
        userFields.put("password", user.getPassword());
        return userFields;
    }

    private int saveUserTable(LinkedHashMap<String, Object> userFields) {
        String insertUserQuery = "INSERT INTO " + getTableName()
                + " (" + SqlQueryUtils.buildInsertQueryParameters(userFields)
                + ") values (" + SqlQueryUtils.addQuestionMarks(userFields.size()) + ")";
        return jdbcTemplate.update(insertUserQuery, userFields.values().toArray());
    }

    private int[] saveRoleTable(ServiceUser user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return new int[0];
        }
        List<RoleEntity> roles = new ArrayList<>();
        for (SecurityRole role : user.getRoles()) {
            roles.add(new RoleEntity(role.name(), user.getId()));
        }
        String insertRoleQuery = "INSERT INTO " + ServiceUser.getRoleTableName() + " (security_role, user_id) VALUES (?, ?)";
        return jdbcTemplate.batchUpdate(insertRoleQuery, new RoleBatchSetter(roles));
    }

    private int updateUserTable(ServiceUser user, LinkedHashMap<String, Object> userFields) {
        String updateQuery = "UPDATE " + getTableName()
                + " SET " + SqlQueryUtils.buildUpdateQueryParameters(userFields) + " WHERE id = ?";
        List<Object> values = new ArrayList<>(userFields.values());
        values.add(user.getId());
        return jdbcTemplate.update(updateQuery, values.toArray());
    }

    private int[] updateRoleTable(ServiceUser user) {
        String deleteUserRoles = "DELETE FROM " + ServiceUser.getRoleTableName() + " WHERE user_id = " + user.getId();
        jdbcTemplate.execute(deleteUserRoles);
        return saveRoleTable(user);
    }

    @Override
    public Optional<ServiceUser> findByUsernameOrEmail(String username, String email) {
        ServiceUser user;
        try {
            String findUserSql = "SELECT * FROM " + getTableName() + " WHERE username = ? or email = ?";
            user = jdbcTemplate.queryForObject(findUserSql, rowMapper, username, email);
            if (user != null) {
                String findUserRolesSql = "SELECT security_role FROM " + ServiceUser.getRoleTableName() + " WHERE user_id = ?";
                List<String> roles = jdbcTemplate.queryForList(findUserRolesSql, String.class, user.getId());
                user.setRoles(roles.stream().map(SecurityRole::valueOf).collect(Collectors.toSet()));
            }
        } catch (DataAccessException ex) {
            log.debug("При попытке найти информацию о пользователе по username {} " +
                    "и email {} получено сообщение об ошибке {}", username, email, ex.getMessage());
            return Optional.empty();
        }
        return Optional.ofNullable(user);
    }

    @Override
    public boolean existByEmail(String email) {
        final String getCountSqlQuery = "SELECT EXISTS (SELECT * FROM " + getTableName() + " WHERE email = ?)";
        try {
            return jdbcTemplate.queryForObject(getCountSqlQuery, Boolean.class, email);
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка выполнения sql" + ex.getMessage());
        }

    }

    @Override
    public boolean existByUsername(String username) {
        final String getCountSqlQuery = "SELECT EXISTS (SELECT * FROM " + getTableName() + " WHERE username = ?)";
        try {
            return jdbcTemplate.queryForObject(getCountSqlQuery, Boolean.class, username);
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка выполнения sql" + ex.getMessage());
        }
    }


    private record RoleEntity(String role, Long userId) {
    }

    private record RoleBatchSetter(List<RoleEntity> roles) implements BatchPreparedStatementSetter {

        @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RoleEntity roleEntity = roles.get(i);
                ps.setString(1, roleEntity.role);
                ps.setLong(2, roleEntity.userId);
            }

            @Override
            public int getBatchSize() {
                return roles.size();
            }
    }

    private static class ServiceUserResultSetExtractor implements ResultSetExtractor<List<ServiceUser>> {
        @Override
        public List<ServiceUser> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            Map<Long, ServiceUser> usersMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                Long userId = resultSet.getLong("id");
                ServiceUser user = usersMap.get(userId);
                String role = resultSet.getString("security_role");
                if (user == null) {
                    Set<SecurityRole> roles =  role == null ? new HashSet<>() : new HashSet<>(Set.of(SecurityRole.valueOf(role)));
                    user = ServiceUser.builder()
                            .id(userId)
                            .username(resultSet.getString("username"))
                            .email(resultSet.getString("email"))
                            .password(resultSet.getString("password"))
                            .roles(roles)
                            .build();
                    usersMap.put(userId, user);
                }
                if (role != null) {
                    user.getRoles().add(SecurityRole.valueOf(role));
                }
            }
            return usersMap.values().stream().toList();
        }
    }
}
