package com.goodow.web.core.shared;

import com.google.inject.Singleton;

@Singleton
public class OperationInfo<T> implements Wrapper<Operation> {

  private final Operation operation;

  public OperationInfo(final String name) {
    operation = new Operation();
    operation.setName(name);
  }

  @Override
  public Operation as() {
    return operation;
  }
}