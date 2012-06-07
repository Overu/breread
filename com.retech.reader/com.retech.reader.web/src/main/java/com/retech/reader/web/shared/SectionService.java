package com.retech.reader.web.shared;

import com.goodow.web.security.shared.ContentService;

import com.google.inject.persist.finder.FirstResult;
import com.google.inject.persist.finder.MaxResults;

import java.util.List;

public interface SectionService extends ContentService<Section> {

  List<Section> findByBook(final Issue book, @FirstResult final int start,
      @MaxResults final int length);

  Page findFirstPage(final Section section);

  List<Section> findSectionByPage(final Page page);
}
