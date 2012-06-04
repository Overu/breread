package com.goodow.web.service.server.jpa;

import com.goodow.web.service.server.Sort;
import com.goodow.web.service.shared.SortPredicateProxy;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.persist.finder.Finder;
import com.google.inject.persist.finder.FirstResult;
import com.google.inject.persist.finder.MaxResults;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@Singleton
public class JpaFinderProxy implements MethodInterceptor {
  /**
   * A wrapper data class that caches information about a finder method.
   */
  private static class FinderDescriptor {
    private volatile boolean isKeyedQuery = false;
    volatile boolean isBindAsRawParameters = true;
    volatile int sortIdx = -1;
    // should we treat the query as having ? instead of :named params
    volatile JpaFinderProxy.ReturnType returnType;
    volatile Class<?> returnClass;
    volatile Class<? extends Collection> returnCollectionType;
    volatile Constructor returnCollectionTypeConstructor;
    volatile Object[] parameterAnnotations;
    // contract is: null = no bind, @Named = param, @FirstResult/@MaxResults for paging

    private String query;
    private String name;

    public boolean isKeyedQuery() {
      return isKeyedQuery;
    }

    Query createQuery(final EntityManager em, final Object[] arguments) {
      if (isKeyedQuery) {
        return em.createNamedQuery(name);
      }
      if (sortIdx == -1) {
        return em.createQuery(query);
      }

      // List<SortPredicateProxy> sorts = (List<SortPredicateProxy>) arguments[sortIdx];
      // StringBuilder sort = new StringBuilder(" order by");
      // for (SortPredicateProxy s : sorts) {
      // sort.append(s.getPropertyName());
      // sort.append(" ");
      // sort.append(SortDirection.ASCENDING == s.getDirection() ? " asc " : " desc ");
      // }
      String ql =
          query.replace(SortPredicateProxy.SORT, arguments[sortIdx] == null ? " " : " order by "
              + arguments[sortIdx]);
      return em.createQuery(ql);
    }

    void setNamedQuery(final String name) {
      this.name = name;
      isKeyedQuery = true;
    }

    void setQuery(final String query) {
      this.query = query;
    }
  }
  private static enum ReturnType {
    PLAIN, COLLECTION, ARRAY
  }

  private final Logger logger = Logger.getLogger(getClass().getName());

  private final Map<Method, FinderDescriptor> finderCache = new MapMaker().weakKeys().makeMap();

  @Inject
  private Provider<EntityManager> emProvider;

  @Override
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
    EntityManager em = emProvider.get();

    // obtain a cached finder descriptor (or create a new one)
    JpaFinderProxy.FinderDescriptor finderDescriptor = getFinderDescriptor(methodInvocation);

    Object result = null;

    // execute as query (named params or otherwise)
    Query jpaQuery = finderDescriptor.createQuery(em, methodInvocation.getArguments());
    if (finderDescriptor.isBindAsRawParameters) {
      bindQueryRawParameters(jpaQuery, finderDescriptor, methodInvocation.getArguments());
    } else {
      bindQueryNamedParameters(jpaQuery, finderDescriptor, methodInvocation.getArguments());
    }

    // depending upon return type, decorate or return the result as is
    if (JpaFinderProxy.ReturnType.PLAIN.equals(finderDescriptor.returnType)) {
      result = jpaQuery.getSingleResult();
    } else if (JpaFinderProxy.ReturnType.COLLECTION.equals(finderDescriptor.returnType)) {
      result = getAsCollection(finderDescriptor, jpaQuery.getResultList());
    } else if (JpaFinderProxy.ReturnType.ARRAY.equals(finderDescriptor.returnType)) {
      result = jpaQuery.getResultList().toArray();
    }

    return result;
  }

  private void bindQueryNamedParameters(final Query jpaQuery,
      final JpaFinderProxy.FinderDescriptor descriptor, final Object[] arguments) {
    for (int i = 0; i < arguments.length; i++) {
      Object argument = arguments[i];
      Object annotation = descriptor.parameterAnnotations[i];

      if (null == annotation)
      // noinspection UnnecessaryContinue
      {
        continue; // skip param as it's not bindable
      } else if (annotation instanceof Named) {
        Named named = (Named) annotation;
        jpaQuery.setParameter(named.value(), argument);
      } else if (annotation instanceof javax.inject.Named) {
        javax.inject.Named named = (javax.inject.Named) annotation;
        jpaQuery.setParameter(named.value(), argument);
      } else if (annotation instanceof FirstResult) {
        jpaQuery.setFirstResult((Integer) argument);
      } else if (annotation instanceof MaxResults) {
        jpaQuery.setMaxResults((Integer) argument);
      }
    }
  }

  private void bindQueryRawParameters(final Query jpaQuery,
      final JpaFinderProxy.FinderDescriptor descriptor, final Object[] arguments) {
    for (int i = 0, index = 1; i < arguments.length; i++) {
      Object argument = arguments[i];
      Object annotation = descriptor.parameterAnnotations[i];

      if (null == annotation) {
        // bind it as a raw param (1-based index, yes I know its different from Hibernate, blargh)
        jpaQuery.setParameter(index, argument);
        index++;
      } else if (annotation instanceof FirstResult) {
        jpaQuery.setFirstResult((Integer) argument);
      } else if (annotation instanceof MaxResults) {
        jpaQuery.setMaxResults((Integer) argument);
      }
    }
  }

  /**
   * writes to a chm (used to provide copy-on-write but this is bettah!)
   * 
   * @param method The key
   * @param finderDescriptor The descriptor to cache
   */
  private void cacheFinderDescriptor(final Method method, final FinderDescriptor finderDescriptor) {
    // write to concurrent map
    finderCache.put(method, finderDescriptor);
  }

  private JpaFinderProxy.ReturnType determineReturnType(final Class<?> returnClass) {
    if (Collection.class.isAssignableFrom(returnClass)) {
      return JpaFinderProxy.ReturnType.COLLECTION;
    } else if (returnClass.isArray()) {
      return JpaFinderProxy.ReturnType.ARRAY;
    }

    return JpaFinderProxy.ReturnType.PLAIN;
  }

  private Object getAsCollection(final JpaFinderProxy.FinderDescriptor finderDescriptor,
      final List results) {
    Collection<?> collection;
    try {
      collection = (Collection) finderDescriptor.returnCollectionTypeConstructor.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(
          "Specified collection class of Finder's returnAs could not be instantated: "
              + finderDescriptor.returnCollectionType, e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
          "Specified collection class of Finder's returnAs could not be instantated (do not have access privileges): "
              + finderDescriptor.returnCollectionType, e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(
          "Specified collection class of Finder's returnAs could not be instantated (it threw an exception): "
              + finderDescriptor.returnCollectionType, e);
    }

    collection.addAll(results);
    return collection;
  }

  private JpaFinderProxy.FinderDescriptor getFinderDescriptor(final MethodInvocation invocation) {
    Method method = invocation.getMethod();
    JpaFinderProxy.FinderDescriptor finderDescriptor = finderCache.get(method);
    if (null != finderDescriptor) {
      return finderDescriptor;
    }

    // otherwise reflect and cache finder info...
    finderDescriptor = new JpaFinderProxy.FinderDescriptor();

    // determine return type
    finderDescriptor.returnClass = invocation.getMethod().getReturnType();
    finderDescriptor.returnType = determineReturnType(finderDescriptor.returnClass);

    // determine finder query characteristics
    Finder finder = invocation.getMethod().getAnnotation(Finder.class);
    String query = finder.query();
    if (!"".equals(query.trim())) {
      finderDescriptor.setQuery(query);
    } else {
      finderDescriptor.setNamedQuery(finder.namedQuery());
    }

    // determine parameter annotations
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    Object[] discoveredAnnotations = new Object[parameterAnnotations.length];
    for (int i = 0; i < parameterAnnotations.length; i++) {
      Annotation[] annotations = parameterAnnotations[i];
      // each annotation per param
      for (Annotation annotation : annotations) {
        // discover the named, first or max annotations then break out
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (Named.class.equals(annotationType) || javax.inject.Named.class.equals(annotationType)) {
          discoveredAnnotations[i] = annotation;
          finderDescriptor.isBindAsRawParameters = false;
          break;
        } else if (FirstResult.class.equals(annotationType)) {
          discoveredAnnotations[i] = annotation;
          break;
        } else if (MaxResults.class.equals(annotationType)) {
          discoveredAnnotations[i] = annotation;
          break;
        } else if (Sort.class.equals(annotationType)) {
          discoveredAnnotations[i] = annotation;
          if (finderDescriptor.sortIdx != -1) {
            logger.warning("指定了重复的@" + Sort.class.getSimpleName());
          }
          if (finderDescriptor.query == null
              || !finderDescriptor.query.contains(SortPredicateProxy.SORT)) {
            logger.warning("缺少排序字符串" + SortPredicateProxy.SORT + ": " + finderDescriptor.query);
          }
          finderDescriptor.sortIdx = i;
          break;
        } // leave as null for no binding
      }
    }

    // set the discovered set to our finder cache object
    finderDescriptor.parameterAnnotations = discoveredAnnotations;

    // discover the returned collection implementation if this finder returns a collection
    if (JpaFinderProxy.ReturnType.COLLECTION.equals(finderDescriptor.returnType)
        && finderDescriptor.returnClass != Collection.class) {
      finderDescriptor.returnCollectionType = finder.returnAs();
      try {
        finderDescriptor.returnCollectionTypeConstructor =
            finderDescriptor.returnCollectionType.getConstructor();
        finderDescriptor.returnCollectionTypeConstructor.setAccessible(true); // UGH!
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(
            "Finder's collection return type specified has no default constructor! returnAs: "
                + finderDescriptor.returnCollectionType, e);
      }
    }

    // Type[] parameterTypes = method.getGenericParameterTypes();
    // for (int i = 0; i < parameterTypes.length; i++) {
    // Type type = parameterTypes[i];
    // Class<?> base = TypeUtils.ensureBaseType(type);
    // if (base == List.class) {
    // Type parameterization = TypeUtils.getSingleParameterization(base, type);
    // if (parameterization != null
    // && TypeUtils.ensureBaseType(parameterization) == SortPredicateProxy.class) {
    // if (finderDescriptor.sortIdx != -1) {
    // logger.warning("指定了重复的" + SortPredicateProxy.class.getSimpleName());
    // }
    // finderDescriptor.sortIdx = i;
    // }
    // }
    // }

    // cache it
    cacheFinderDescriptor(method, finderDescriptor);

    return finderDescriptor;
  }
}