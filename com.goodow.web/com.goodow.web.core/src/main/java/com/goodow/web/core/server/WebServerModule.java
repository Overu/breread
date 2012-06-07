package com.goodow.web.core.server;

import com.goodow.web.core.shared.Message;

import com.google.inject.AbstractModule;

import java.util.logging.Logger;

public class WebServerModule extends AbstractModule {

  private final Logger logger = Logger.getLogger(getClass().getName());

  @Override
  protected void configure() {
    logger.finest("Install JpaServiceModule begin");

    bind(Message.class).toProvider(MessageProvider.class);

    // bind(RemoteServiceLocator.class); // TODO why bind RemoteServiceLocator?

    // requestStaticInjection(InjectionListener.class);

    // MethodInterceptor finderInterceptor = new JpaFinderProxy();
    // requestInjection(finderInterceptor);
    // bindInterceptor(any(), annotatedWith(Finder.class), finderInterceptor);
    logger.finest("Install JpaServiceModule end");
  }

}