package io.onedev.server.web.page.error;

import static io.onedev.server.web.translation.Translation._T;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.exception.ServerNotFoundException;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.HomePage;
import io.onedev.server.web.page.simple.SimplePage;

public class GeneralErrorPage extends SimplePage {

	private static final Logger logger = LoggerFactory.getLogger(GeneralErrorPage.class);
	
	private static final int MAX_TITLE_LEN = 240;
	
	private boolean serverNotFound;
	
	private String title;

	private String detailMessage;

	private int statusCode;

	public GeneralErrorPage(Exception exception) {
		super(new PageParameters());
				
		serverNotFound = ExceptionUtils.find(exception, ServerNotFoundException.class) != null;
		
		var response = ExceptionUtils.buildResponse(exception);
		if (response != null) {
			title = response.getBody() != null? response.getBody().getText(): HttpStatus.getMessage(response.getStatus());
			statusCode = response.getStatus();
		} else {
			title = _T("An unexpected exception occurred");
			detailMessage = Throwables.getStackTraceAsString(exception);
			statusCode = SC_INTERNAL_SERVER_ERROR;
		}
		if (statusCode >= SC_INTERNAL_SERVER_ERROR)
			logger.error("Error processing wicket request", exception);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer container = new WebMarkupContainer("error");
		container.setOutputMarkupId(true);
		add(container);

		if (serverNotFound) {
			container.add(new Link<Void>("home") {

				@Override
				public void onClick() {
					OneDev.getInstance(ProjectService.class).updateActiveServers();
					setResponsePage(HomePage.class);
				}
				
			}.setBody(Model.of(_T("Sync Replica Status and Back to Home"))));
		} else {
			container.add(new ViewStateAwarePageLink<Void>("home", HomePage.class));
		}

		container.add(new AjaxLink<Void>("showDetail") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("errorDetail", "errorDetailFrag", GeneralErrorPage.this);
				fragment.add(new MultilineLabel("content", detailMessage));
				fragment.add(new CopyToClipboardLink("copy", Model.of(detailMessage)));
				container.replace(fragment);
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
	protected void setHeaders(final WebResponse response) {
		response.setStatus(statusCode);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		target.appendJavaScript("location.reload();");
	}

	@Override
	protected String getTitle() {
		return _T("OOPS! There Is An Error");
	}

	@Override
	protected String getSubTitle() {
		return StringUtils.abbreviate(title, MAX_TITLE_LEN);
	}

	@Override
	protected WebComponent newPageLogo(String componentId) {
		return new SpriteImage(componentId, "sad-panda");
	}

}
