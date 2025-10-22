package com.finanzas.backend_gestor_finanzas.service;

import java.util.List;
import java.util.Optional;

public interface CrudService<T, ID> {
    T create(T entity);
    Optional<T> getById(ID id);
    List<T> getAll();
    T update(ID id, T entity);
    void delete(ID id);
}
