package org.cloudlet.web.offline.client;

public class ApplicationCache {
  public interface Listener {
    void updateReady();
  }

  public static native void addEventListener(Listener listener) /*-{
                                                                $wnd.applicationCache.addEventListener('updateready', function(e) {
                                                                if ($wnd.applicationCache.status == $wnd.applicationCache.UPDATEREADY) {
                                                                $wnd.applicationCache.swapCache();
                                                                listener.@org.cloudlet.web.offline.client.ApplicationCache.Listener::updateReady()();
                                                                }
                                                                }, false);
                                                                }-*/;

  public static native void hideAddressBar() /*-{
                                             $wnd.scrollTo(0, 1);
                                             }-*/;

  public static native void swapCache() /*-{
                                             $wnd.applicationCache.swapCache();
                                             }-*/;
}
