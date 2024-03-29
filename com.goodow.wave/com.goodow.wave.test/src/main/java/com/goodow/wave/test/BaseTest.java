/*
 * Copyright 2012 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.goodow.wave.test;

import com.goodow.wave.bootstrap.server.BootstrapModule;

import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Module;

import org.junit.Assert;
import org.junit.Rule;

public abstract class BaseTest extends Assert {

  public static class BaseTestModule extends GuiceBerryModule {
    @Override
    protected void configure() {
      super.configure();
      install(new TestModule());
      install(new BootstrapModule());
    }
  }

  @Rule
  public final GuiceBerryRule guiceBerry = new GuiceBerryRule(providesModule());

  protected Class<? extends Module> providesModule() {
    return BaseTestModule.class;
  }
}
