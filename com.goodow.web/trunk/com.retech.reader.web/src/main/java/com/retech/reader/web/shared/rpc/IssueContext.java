package com.retech.reader.web.shared.rpc;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.Service;

import com.retech.reader.web.server.service.IssueService;
import com.retech.reader.web.shared.proxy.CategoryProxy;
import com.retech.reader.web.shared.proxy.IssueProxy;
import com.retech.reader.web.shared.proxy.PageProxy;

import org.cloudlet.web.service.server.NoLocator;
import org.cloudlet.web.service.shared.rpc.BaseContext;

import java.util.List;

@Service(value = IssueService.class, locator = NoLocator.class)
public interface IssueContext extends BaseContext {
  @Deprecated
  Request<List<IssueProxy>> find(CategoryProxy categoryProxy);

  Request<List<IssueProxy>> find(int start, int length);

  Request<List<IssueProxy>> find(IssueProxy bookGroupProxy);

  @Deprecated
  Request<List<IssueProxy>> find(IssueProxy bookGroupProxy, boolean brevity);

  Request<List<IssueProxy>> findByCategory(CategoryProxy category, int start, int length);

  @Deprecated
  Request<IssueProxy> findForDonwload(final long id);

  @Deprecated
  Request<List<PageProxy>> findPages(final long id, final boolean brevity);

  Request<List<IssueProxy>> findRecommend(IssueProxy issue, int start, int length, boolean brevity);

}