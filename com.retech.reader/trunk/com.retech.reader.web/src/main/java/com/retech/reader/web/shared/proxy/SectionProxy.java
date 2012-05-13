package com.retech.reader.web.shared.proxy;

import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;

import com.retech.reader.web.server.domain.Section;

import org.cloudlet.web.service.server.NoLocator;
import org.cloudlet.web.service.shared.rpc.BaseEntityProxy;

import java.util.List;

@ProxyFor(value = Section.class, locator = NoLocator.class)
public interface SectionProxy extends BaseEntityProxy {

  String WITH = "issue";

  IssueProxy getIssue();

  int getPageCount();

  @Deprecated
  List<PageProxy> getPages();

  int getSequence();

  String getTitle();

  @Override
  EntityProxyId<SectionProxy> stableId();
}