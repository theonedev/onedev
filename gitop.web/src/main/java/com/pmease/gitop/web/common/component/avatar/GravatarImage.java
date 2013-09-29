package com.pmease.gitop.web.common.component.avatar;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.IModel;

import com.google.common.base.Preconditions;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.web.GitopWebApp;
import com.pmease.gitop.web.util.Gravatar;

public class GravatarImage extends NonCachingImage {

  private static final long serialVersionUID = 1L;

  public GravatarImage(String id, IModel<User> model) {
    super(id, model);
  }

  @Override
  protected void onComponentTag(ComponentTag tag) {
    User user = (User) getDefaultModelObject();
    Preconditions.checkNotNull(user);
    if (GitopWebApp.get().isGravatarEnabled()) {
      tag.put("src", Gravatar.getURL(user.getEmail(), 256));
    } else {
      tag.put("src", "assets/img/empty-avatar.jpg");
    }
  }
  
  @Override
  protected boolean getStatelessHint() {
    return true;
  }
}
