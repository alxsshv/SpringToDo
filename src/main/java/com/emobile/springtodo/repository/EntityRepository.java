package com.emobile.springtodo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public interface EntityRepository<T, ID> {
    List<T> findAll();
    Page<T> findAll(Pageable pageable);
    List<T> findByParameters(LinkedHashMap<String, Object> entityFields);
    Page<T> findByParameters(LinkedHashMap<String, Object> entityFields, Pageable pageable);
    Optional<T> findById(ID id);
    T save(T entity);
    void deleteById(ID id);
    void deleteAll();
    Long count();
}
