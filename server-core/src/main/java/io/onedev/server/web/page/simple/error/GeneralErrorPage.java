package io.onedev.server.web.page.simple.error;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Throwables;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.ExpectedExceptionContribution;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class GeneralErrorPage extends SimplePage {
	
	private static final int MAX_TITLE_LEN = 240;
	
	private String title;
	
	private String detailMessage;
	
	public GeneralErrorPage(Exception exception) {
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
		if (title == null) {
			title = "An unexpected exception occurred";
			detailMessage = Throwables.getStackTraceAsString(exception);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("error");
		container.setOutputMarkupId(true);
		add(container);
		
		container.add(new ViewStateAwarePageLink<Void>("home", ProjectListPage.class));
		
		container.add(new AjaxLink<Void>("showDetail") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.replace(new MultilineLabel("errorDetail", detailMessage));
				target.add(container);
				setVisible(false);
			}

		}.setVisible(SecurityUtils.isAdministrator() && detailMessage != null));
		
		container.add(new WebMarkupContainer("errorDetail").setVisible(false));
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
	protected String getTitle() {
		return "OOPS! There Is An Error";
	}

	@Override
	protected String getSubTitle() {
		return StringUtils.abbreviate(title, MAX_TITLE_LEN);
	}

	@Override
	protected String getLogoHref() {
		return "sad-panda";
	}
	
}
