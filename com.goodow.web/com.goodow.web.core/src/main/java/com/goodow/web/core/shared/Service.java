package com.goodow.web.core.shared;

public interface Service<E extends Entity> {

  E find(String id);

  Class<E> getEntityClass();

  <T> T invoke(final Wrapper<Operation> operation, final Object... args);
}
