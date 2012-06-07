package com.goodow.web.core.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.goodow.web.core.shared.CorePackage;
import com.goodow.web.core.shared.Entity;
import com.goodow.web.core.shared.EntityId;
import com.goodow.web.core.shared.EntityType;
import com.goodow.web.core.shared.Operation;
import com.goodow.web.core.shared.Parameter;
import com.goodow.web.core.shared.Property;
import com.goodow.web.core.shared.Request;
import com.goodow.web.core.shared.Response;
import com.goodow.web.core.shared.Type;
import com.goodow.web.core.shared.ValueType;
import com.goodow.web.core.shared.WebPlatform;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class JsonBuilder {

  @Inject
  private Provider<Request> requestProvider;

  @Inject
  private Provider<Response> responseProvider;

  @Inject
  WebPlatform platform;

  public Request parse(final String payload) {
    Request request = requestProvider.get();
    JSONObject json;
    try {
      json = new JSONObject(payload);
      String operationName = json.getString("operation");
      Operation operation = platform.getOperation(operationName);
      request.setOperation(operation);

      JSONArray entities = json.getJSONArray("entities");

      if (entities != null) {
        for (int i = 0; i < entities.length(); i++) {
          JSONObject jsonObj = entities.getJSONObject(i);
          parseEntity(request, jsonObj);
        }
      }
      Object[] args = new Object[operation.getParameters().size()];
      int i = 0;
      for (Parameter param : operation.getParameters().values()) {
        JSONArray values = json.getJSONArray("parameters");
        Type type = param.getType();
        Object obj = values.get(i);
        args[i] = parse(request, type, obj);
        i++;
      }
      request.setArgs(args);
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return request;
  }

  public String serialize(final Request<?> request, final Response<?> response)
      throws JSONException {
    JSONObject jsonObject = new JSONObject();
    Object obj = response.getResult();
    Type type = request.getOperation().getType();
    serialize(request, jsonObject, "result", obj, type);
    jsonObject.put("success", true);
    return jsonObject.toString();
  }

  private Object parse(final Request<?> request, final Type type, final Object obj)
      throws JSONException {
    if (obj == null || obj.equals(JSONObject.NULL)) {
      return null;
    } else if (obj instanceof JSONObject) {
      JSONObject jsonObject = (JSONObject) obj;
      return parseEntity(request, jsonObject);
    } else if (type instanceof ValueType) {
      if (type == CorePackage.Boolean.as() || type == CorePackage.BOOLEAN.as()) {
        return (Boolean) obj;
      } else if (type == CorePackage.INT.as() || type == CorePackage.Integer.as()) {
        return ((Number) obj).intValue();
      } else if (type == CorePackage.LONG.as() || type == CorePackage.Long.as()) {
        return ((Number) obj).longValue();
      } else {
        return (String) obj;
      }
    } else {
      String eId = (String) obj;
      EntityId id = EntityId.parseId(eId);
      return request.getEntity(id);
    }
  }

  private Object parseEntity(final Request<?> request, final JSONObject jsonObject)
      throws JSONException {
    if (jsonObject == null) {
      return null;
    } else {
      String eId = jsonObject.getString("e_id");
      EntityId id = EntityId.parseId(eId);
      Entity entity = request.getEntity(id);
      for (Property prop : entity.type().getAllProperties().values()) {
        if (jsonObject.has(prop.getName())) {
          Object jsonValue = jsonObject.get(prop.getName());
          Object value = parse(request, prop.getType(), jsonValue);
          entity.set(prop, value);
        }
      }
      return entity;
    }
  }

  private void serialize(final Request<?> request, final JSONObject parent, final String key,
      final Object value, final Type type) throws JSONException {
    if (value == null) {
      parent.put(key, JSONObject.NULL);
    } else if (type instanceof ValueType) {
      parent.put(key, value);
    } else {
      Entity entity = (Entity) value;
      EntityId eid = request.getEntityId(entity);
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("e_id", eid.toString());
      for (Property prop : entity.type().getAllProperties().values()) {
        Object propValue = entity.get(prop);
        Type propType = prop.getType();
        if (propType instanceof EntityType) {
          Entity entityValue = (Entity) propValue;
          if (entityValue == null) {
            jsonObject.put(prop.getName(), JSONObject.NULL);
          } else if (prop.isContainment()) {
            serialize(request, jsonObject, prop.getName(), propValue, prop.getType());
          } else {
            EntityId id = request.getEntityId(entityValue);
            jsonObject.put(prop.getName(), id.toString());
          }
        } else {
          jsonObject.put(prop.getName(), propValue);
        }
      }
      parent.put(key, jsonObject);
    }
  }
}