package com.goodow.web.core.shared;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class WebPlatform {

  @Inject
  private static WebPlatform instance;

  public static WebPlatform getInstance() {
    return instance;
  }

  public @Inject
  Provider<Message> message;

  Map<String, Package> packages = new HashMap<String, Package>();

  public ObjectType getObjectType(final String fullName) {
    WebType type = getType(fullName);
    return (ObjectType) type;
  }

  public Operation getOperation(final String fullName) {
    int index = fullName.lastIndexOf(".");
    String typeName = fullName.substring(0, index);
    ObjectType pkg = getObjectType(typeName);
    if (pkg != null) {
      String simpleName = fullName.substring(index + 1);
      return pkg.getOperation(simpleName);
    }
    return null;

  }

  public WebEntity getOrCreateEntity(final EntityId id) {
    ObjectType objectType = WebPlatform.getInstance().getObjectType(id.getType());
    if (id.getStableId() != null) {
      return message.get().find(objectType, id.getStableId());
    } else {
      return (WebEntity) objectType.create();
    }
  }

  public Package getPackage(final String name) {
    return packages.get(name);
  }

  public Map<String, Package> getPackages() {
    return packages;
  }

  public WebType getType(final String fullName) {
    int index = fullName.lastIndexOf(".");
    String pkgName = fullName.substring(0, index);
    Package pkg = getPackage(pkgName);
    if (pkg != null) {
      String simpleName = fullName.substring(index + 1);
      return pkg.getType(simpleName);
    }
    return null;
  }

}
