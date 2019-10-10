package io.onedev.server.web.page.error;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Throwables;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.web.ExpectedExceptionContribution;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.ProjectListPage;

@SuppressWarnings("serial")
public class ErrorPage extends BasePage {
	
	private static final int MAX_TITLE_LEN = 240;
	
	private String title;
	
	private String detailMessage;
	
	public ErrorPage(Exception exception) {
		super(new PageParameters());
		
		for (ExpectedExceptionContribution contribution: OneDev.getExtensions(ExpectedExceptionContribution.class)) {
			for (Class<? extends Exception> expectedExceptionClass: contribution.getExpectedExceptionClasses()) {
				Exception expectedException = ExceptionUtils.find(exception, expectedExceptionClass);
				if (expectedException != null)
					title = expectedException.getMessage();
				if (title != null)
					break;
			}
		}
		if (title == null)
			title = "An unexpected exception occurred";
		detailMessage = Throwables.getStackTraceAsString(exception);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("error");
		container.setOutputMarkupId(true);
		add(container);
		
		container.add(new Label("title", StringUtils.abbreviate(title, MAX_TITLE_LEN)));
		
		container.add(new ViewStateAwarePageLink<Void>("home", ProjectListPage.class));
		
		container.add(new AjaxLink<Void>("showDetail") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("detail", "detailFrag", ErrorPage.this);
				fragment.add(new MultilineLabel("body", detailMessage));				
				container.replace(fragment);
				target.add(container);
				setVisible(false);
			}

		});
		container.add(new WebMarkupContainer("detail"));
	}
	
	@Override
	public boolean isErrorPage() {
		return true;
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		target.appendJavaScript("location.reload();");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ErrorPageResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("$('html,body').addClass('error');"));
	}
	
}
