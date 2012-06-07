package com.goodow.web.core.shared;

import java.io.Serializable;
import java.util.Stack;

public abstract class Message implements Serializable {

  public static final String JSON_CONTENT_TYPE_UTF8 = "application/json; charset=utf-8";

  protected Stack<Request<?>> requests = new Stack<Request<?>>();

  protected Request<?> activeRequest;

  public void addRequest(final Request<?> request) {
    requests.add(request);
  }

  public abstract Entity find(EntityId eId);

  public boolean isActive() {
    return activeRequest != null;
  }

  public abstract Response send();
}