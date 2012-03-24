package org.cloudlet.web.dev.client.ioc;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.cloudlet.web.boot.shared.MapBinder;
import org.cloudlet.web.dev.client.ui.ShellUiDev;
import org.cloudlet.web.dev.client.ui.TreeNodeEditor;
import org.cloudlet.web.dev.client.ui.TreeNodeListView;
import org.cloudlet.web.mvp.shared.tree.TreeNodeProxy;

import java.util.logging.Logger;

public final class DevGinModule extends AbstractGinModule {

  @Singleton
  public static class Binder {
  }
  @Singleton
  public static class BinderProvider implements Provider<Binder> {
    private final Logger logger = Logger.getLogger(getClass().getName());
    @Inject
    private MapBinder<String, IsWidget> isWidgetMapBinder;
    @Inject
    private AsyncProvider<TreeNodeEditor> treeNodeEditor;
    @Inject
    private AsyncProvider<TreeNodeListView> treeNodeListView;

    @Override
    public Binder get() {
      logger.finest("EagerSingleton begin");

      isWidgetMapBinder.addBinding(TreeNodeListView.class.getName()).toAsyncProvider(
          treeNodeListView);
      isWidgetMapBinder.addBinding(TreeNodeProxy.class.getName()).toAsyncProvider(treeNodeEditor);

      logger.finest("EagerSingleton end");
      return null;
    }
  }
  public static interface CellListResources extends CellList.Resources {
    @Override
    @Source({CellList.Style.DEFAULT_CSS, "MobileCellList.css"})
    CellList.Style cellListStyle();
  }

  private final Logger logger = Logger.getLogger(getClass().getName());

  @Override
  protected void configure() {
    bind(Binder.class).toProvider(BinderProvider.class).asEagerSingleton();
    bind(CellList.Resources.class).to(CellListResources.class).in(Singleton.class);
    bind(ShellUiDev.class).asEagerSingleton();
  }

}
