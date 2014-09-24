package com.pmease.commons.wicket;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Strings;
import com.pmease.commons.util.StringUtils;

@SuppressWarnings("serial")
public abstract class CommonPage extends WebPage {

	private FeedbackPanel sessionFeedback;
	
	private WebMarkupContainer body;

	public CommonPage() {
	}

	public CommonPage(IModel<?> model) {
		super(model);
	}

	public CommonPage(PageParameters params) {
		super(params);
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
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		body = new TransparentWebMarkupContainer("htmlBody");
		body.setOutputMarkupId(true);
		add(body);
		body.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String css = getPageCssClass();
				return Strings.isNullOrEmpty(css) ? "" : css;
			}
			
		}));

		sessionFeedback = new SessionFeedbackPanel("sessionFeedback");
		add(sessionFeedback);			
		sessionFeedback.setOutputMarkupId(true);
	}
	
	public FeedbackPanel getSessionFeedback() {
		return sessionFeedback;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(CommonResourceReference.get())));
	}

}
