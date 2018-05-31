package io.onedev.server.web.page.error;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;

import com.google.common.base.Throwables;

import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectListPage;

@SuppressWarnings("serial")
public class ExpectedExceptionPage extends BaseErrorPage {
	
	private final String title;
	
	private final String detailMessage;
	
	public ExpectedExceptionPage(Exception exception) {
		title = exception.getMessage();
		detailMessage = Throwables.getStackTraceAsString(exception);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("error");
		container.setOutputMarkupId(true);
		add(container);
		
		container.add(new Label("title", StringUtils.abbreviate(title, 80)));
		
		container.add(new ViewStateAwarePageLink<Void>("home", ProjectListPage.class));
		
		container.add(new AjaxLink<Void>("showDetail") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("detail", "detailFrag", ExpectedExceptionPage.this);
				fragment.add(new MultilineLabel("body", detailMessage));				
				container.replace(fragment);
				target.add(container);
				setVisible(false);
			}

		});
		container.add(new WebMarkupContainer("detail"));
	}
	
}
