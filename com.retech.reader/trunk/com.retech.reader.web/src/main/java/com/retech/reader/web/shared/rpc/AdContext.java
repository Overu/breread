package com.retech.reader.web.shared.rpc;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;

import com.retech.reader.web.server.service.AdService;
import com.retech.reader.web.shared.proxy.IssueProxy;

import org.cloudlet.web.service.server.NoLocator;

import java.util.List;

@Service(value = AdService.class, locator = NoLocator.class)
public interface AdContext extends RequestContext {

  Request<List<IssueProxy>> findRecommend(int start, int length, boolean brevity);

  Request<List<IssueProxy>> findRecommend(IssueProxy issue, int start, int length);

  Request<List<IssueProxy>> findRecommend(IssueProxy issue, int start, int length, boolean brevity);

}