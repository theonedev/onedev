package com.pmease.gitop.web.common.wicket.component.datagrid.event;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.wicket.event.AjaxEvent;

public class SearchStringChanged extends AjaxEvent {

  private final String pattern;
  
  public SearchStringChanged(AjaxRequestTarget target, String pattern) {
    super(target);
    this.pattern = pattern;
  }

  public String getPattern() {
    return pattern;
  }
}
