package com.pmease.gitplex.web.common.wicket.component.datagrid.toolbar;

import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;

public class AjaxPagingNavigator extends org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator {
  private static final long serialVersionUID = 1L;
  
  public AjaxPagingNavigator(String id, IPageable pageable) {
    super(id, pageable);
  }

  public AjaxPagingNavigator(final String id, final IPageable pageable,
                             final IPagingLabelProvider labelProvider) {
    super(id, pageable, labelProvider);
  }
}
