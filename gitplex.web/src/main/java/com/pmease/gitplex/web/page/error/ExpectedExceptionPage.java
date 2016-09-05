package com.pmease.gitplex.web.page.error;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.util.string.Strings;

import com.pmease.commons.util.WordUtils;
import com.pmease.commons.wicket.component.MultilineLabel;
import com.pmease.gitplex.web.page.home.DashboardPage;

@SuppressWarnings("serial")
public class ExpectedExceptionPage extends BaseErrorPage {
	
	private final Exception exception;
	
	public ExpectedExceptionPage(Exception exception) {
		this.exception = exception;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String title = exception.getClass().getSimpleName();
		String suffixToRemove = "Exception";
		if (title.endsWith(suffixToRemove))
			title = title.substring(0, title.length()-suffixToRemove.length());
		
		title = WordUtils.capitalizeFully(WordUtils.uncamel(title));
		
		WebMarkupContainer container = new WebMarkupContainer("error");
		container.setOutputMarkupId(true);
		add(container);
		
		container.add(new Label("title", title));
		container.add(new Label("description", exception.getMessage()));
		
		container.add(new BookmarkablePageLink<Void>("dashboard", DashboardPage.class));
		
		container.add(new AjaxLink<Void>("showDetail") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Fragment fragment = new Fragment("detail", "detailFrag", ExpectedExceptionPage.this);
				fragment.add(new MultilineLabel("body", Strings.toString(exception)));				
				container.replace(fragment);
				target.add(container);
				setVisible(false);
			}

		});
		container.add(new WebMarkupContainer("detail"));
	}
	
}
