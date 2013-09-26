package com.pmease.gitop.web.common.component.zeroclipboard;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class ZeroClipboardBehavior extends Behavior {
  private static final long serialVersionUID = 1L;

  private static final ResourceReference JS_RESOURCE = new JavaScriptResourceReference(ZeroClipboardBehavior.class, "res/ZeroClipboard.min.js");
  
  @Override
  public void renderHead(Component component, IHeaderResponse resp) {
    super.renderHead(component, resp);
    
    resp.render(JavaScriptReferenceHeaderItem.forReference(JS_RESOURCE));
  }
}
