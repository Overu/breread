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
package com.goodow.web.view.wave.client;

import com.goodow.web.view.wave.client.panel.WavePanel;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.ui.HTMLPanel;

import java.util.logging.Logger;

public class WaveTest extends WavePanel {

  private static final Logger logger = Logger.getLogger(WaveTest.class.getName());
  private boolean start = false;
  private int x1;
  private int x2;
  JsArray<Touch> touches = null;
  private boolean scheduled;
  private boolean isStart = false;

  public WaveTest() {
    // getWaveTitle().setText("test title");
    // wave.setHeader(new Label("test header"));
    // setWaveContent(new Label("test content"));
    // ToolBarClickButton btn = addWaveToolBar().addClickButton();
    // btn.setText("test");
    // wave.setFooter(new Label("test footer"));
    // ToolBarClickButton clickButton = wave.toolbar().addClickButton();
    // clickButton.setText("test button");
    HTMLPanel hp = new HTMLPanel("");
    hp.setWidth("100%");
    hp.setHeight("700px");
    setWaveContent(hp);

    // hp.addDomHandler(new TouchStartHandler() {
    //
    // @Override
    // public void onTouchStart(final TouchStartEvent event) {
    // event.preventDefault();
    // JsArray<Touch> touches = event.getTouches();
    // if (touches.length() == 2) {
    // Touch touch1 = touches.get(0);
    // Touch touch2 = touches.get(1);
    // x1 = touch1.getPageX();
    // x2 = touch2.getPageX();
    // start = true;
    // Window.alert("sdf");
    // }
    // }
    // }, TouchStartEvent.getType());
    //
    // hp.addDomHandler(new TouchMoveHandler() {
    //
    // @Override
    // public void onTouchMove(final TouchMoveEvent event) {
    // event.preventDefault();
    // if (start) {
    // JsArray<Touch> touches = event.getTouches();
    // Touch touch1 = touches.get(0);
    // Touch touch2 = touches.get(1);
    // int touch1PageX = touch1.getPageX();
    // int touch2PageX = touch2.getPageX();
    // int left = touch1PageX - x1;
    // int right = touch2PageX - x2;
    // if (left > 0 && right > 0 && (left > 100 || right > 100)) {
    // History.forward();
    // start = false;
    // return;
    // } else if (left < 0 && right < 0 && (left < -100 || right < -100)) {
    // History.back();
    // start = false;
    // return;
    // }
    // }
    // }
    // }, TouchMoveEvent.getType());
    //
    // hp.addDomHandler(new TouchEndHandler() {
    //
    // @Override
    // public void onTouchEnd(final TouchEndEvent event) {
    // event.preventDefault();
    // start = false;
    // }
    // }, TouchEndEvent.getType());

    hp.addDomHandler(new TouchStartHandler() {

      @Override
      public void onTouchStart(final TouchStartEvent event) {
        JsArray<Touch> toucheStart = event.getTouches();
        if (toucheStart.length() >= 2) {
          logger.info("touch start:" + toucheStart.length());
          isStart = true;
          Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            @Override
            public boolean execute() {
              if (touches != null && isStart && !scheduled) {
                scheduled = true;
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                  @Override
                  public void execute() {
                    scheduled = false;
                    printLog(touches);
                  }
                });
              }
              if (!isStart) {
                logger.info("Scheduler end:" + isStart);
              }
              return isStart;
            }
          }, 15);
        }
      }
    }, TouchStartEvent.getType());

    hp.addDomHandler(new TouchMoveHandler() {

      @Override
      public void onTouchMove(final TouchMoveEvent event) {
        touches = event.getTouches();
      }
    }, TouchMoveEvent.getType());

    hp.addDomHandler(new TouchEndHandler() {

      @Override
      public void onTouchEnd(final TouchEndEvent event) {
        touches = null;
        logger.info("strat end:");
        isStart = false;
      }
    }, TouchEndEvent.getType());

  }

  private void printLog(final JsArray<Touch> touches) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < touches.length(); i++) {
      Touch touch = touches.get(i);
      sb.append("{touch-id:" + touch.getIdentifier());
      sb.append(";touch-pageX:" + touch.getPageX());
      sb.append(";touch-pageY:" + touch.getPageY());
      sb.append(";touch-clientX:" + touch.getClientX());
      sb.append(";touch-clientY:" + touch.getClientY());
      sb.append(";touch-screenX:" + touch.getScreenX());
      sb.append(";touch-screenY:" + touch.getScreenY());
      sb.append("}");
    }
    logger.info(sb.toString());
  }
}