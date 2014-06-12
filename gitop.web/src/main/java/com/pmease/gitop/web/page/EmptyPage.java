package com.pmease.gitop.web.page;

import java.util.Iterator;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.assets.PageResourceReference;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.page.init.ServerInitPage;
import com.pmease.gitop.web.shiro.LoginPage;

@SuppressWarnings("serial")
public abstract class EmptyPage extends WebPage {

	private WebMarkupContainer body;

	public EmptyPage() {
		commonInit();
	}

	public EmptyPage(IModel<?> model) {
		super(model);
		commonInit();
	}

	public EmptyPage(PageParameters params) {
		super(params);
		commonInit();
	}

	private void commonInit() {
		body = new TransparentWebMarkupContainer("body");
		body.setOutputMarkupId(true);
		add(body);
		body.add(AttributeAppender.append("class",
				new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						String css = getPageCssClass();
						return Strings.isNullOrEmpty(css) ? "" : css;
					}
				}));

		add(new BookmarkablePageLink<Void>("home-link", Application.get().getHomePage()));
		
		if (!Gitop.getInstance().isReady()
				&& getClass() != ServerInitPage.class) {
			throw new RestartResponseAtInterceptPageException(ServerInitPage.class);
		}
	}

	@Override
	public IMarkupFragment getMarkup(Component child) {
		if (child != null) {
			IMarkupFragment markup = super.getMarkup(child);
			if (markup != null)
				return markup;
			else if (body != null)
				return body.getMarkup(child);
			else
				return null;
		} else {
			final IMarkupFragment markup = super.getMarkup(child);
			return new IMarkupFragment() {

				@Override
				public String toString(boolean markupOnly) {
					return markup.toString();
				}

				@Override
				public int size() {
					return markup.size();
				}

				@Override
				public MarkupResourceStream getMarkupResourceStream() {
					return markup.getMarkupResourceStream();
				}

				@Override
				public MarkupElement get(int index) {
					return markup.get(index);
				}

				@Override
				public IMarkupFragment find(String id) {
					IMarkupFragment found = markup.find(id);
					if (found != null)
						return found;
					else if (body != null)
						return body.getMarkup().find(id);
					else
						return null;
				}

				@Override
				public Iterator<MarkupElement> iterator() {
					return markup.iterator();
				}
			};
		}
	}

	protected String getPageCssClass() {
		String name = getClass().getSimpleName();
		return StringUtils.camelCaseToLowerCaseWithHyphen(name);
	}

	protected boolean isPermitted() {
		return true;
	}
	
	protected Optional<User> currentUser() {
		return Optional.fromNullable(Gitop.getInstance(UserManager.class).getCurrent());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (!isPermitted()) {
			if (currentUser().isPresent()) {
				throw new AccessDeniedException("Access denied");
			} else {
				throw new RestartResponseAtInterceptPageException(LoginPage.class);
			}
		}

		add(new Label("title", getPageTitle()));

		add(new WebMarkupContainer("refresh") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("content", getPageRefreshInterval());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPageRefreshInterval() != 0);
			}

		});

	}
	
	protected String getPageTitle() {
		return "Gitop - Enterprise Git Management System";
	};

	protected int getPageRefreshInterval() {
		return 0;
	}
	
	public WebMarkupContainer getBody() {
		return body;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(PageResourceReference.get()));
	}
}
