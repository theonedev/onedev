package com.pmease.gitop.web.page;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.web.assets.BaseResourcesBehavior;
import com.pmease.gitop.web.assets.PageResourcesBehavior;
import com.pmease.gitop.web.common.component.messenger.MessengerResourcesBehavior;
import com.pmease.gitop.web.common.component.modal.Modal;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.page.init.ServerInitPage;

@SuppressWarnings("serial")
public abstract class BasePage extends WebPage {

	private WebMarkupContainer body;

	private Modal modal;
	
	public BasePage() {
		commonInit();
	}

	public BasePage(IModel<?> model) {
		super(model);
		commonInit();
	}

	public BasePage(PageParameters params) {
		super(params);
		commonInit();
	}

	private void commonInit() {
		body = new TransparentWebMarkupContainer("body");
		add(body);
		body.add(AttributeAppender.append("class",
				new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						String css = getPageCssClass();
						return Strings.isNullOrEmpty(css) ? "" : css;
					}
				}));

		modal = new Modal("modal");
		add(modal);
		
		if (!Gitop.getInstance().isReady()
				&& getClass() != ServerInitPage.class) {
			throw new RestartResponseAtInterceptPageException(
					ServerInitPage.class);
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

	public final void redirect(final Class<? extends Page> clazz) {
		throw new RestartResponseException(clazz);
	}

	public final void redirect(final Class<? extends Page> clazz,
			PageParameters parameters) {
		throw new RestartResponseException(clazz, parameters);
	}

	public final void redirect(final Page page) {
		throw new RestartResponseException(page);
	}

	public final void redirect(String url) {
		throw new RedirectToUrlException(url);
	}

	protected String getPageCssClass() {
		String name = getClass().getSimpleName();
		return StringUtils.camelCaseToLowerCaseWithHyphen(name);
	}

	protected boolean isPermitted() {
		return true;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (!isPermitted()) {
			throw new AccessDeniedException();
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

		/*
		 * Bind global resources here so that they can appear in page header
		 * before any other resources. Simply rendering the resource in
		 * renderHead method of base page will not work as renderHead method of
		 * container will be called after contained components, and this will
		 * cause components with resources using global resources not working
		 * properly.
		 */
		add(new BaseResourcesBehavior());
		add(new WebMarkupContainer("globalResourceBinder"));
//				.add(new BaseResourcesBehavior())
				add(MessengerResourcesBehavior.get());
				add(PageResourcesBehavior.get());
	}

	protected abstract String getPageTitle();

	protected int getPageRefreshInterval() {
		return 0;
	}
	
	public Modal getModal() {
		return modal;
	}
}
