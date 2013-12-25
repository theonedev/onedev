package com.pmease.gitop.web.page.test;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitop.model.Team;
import com.pmease.gitop.web.component.choice.TeamChoiceProvider;
import com.pmease.gitop.web.component.choice.TeamSingleChoice;
import com.pmease.gitop.web.page.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		add(new WebMarkupContainer("container") {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(CssHeaderItem.forReference(new CssResourceReference(TestPage.class, "test.css")));
			}
			
		});
		
		add(new TeamSingleChoice("chooser", new IModel<Team>() {

			@Override
			public void detach() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Team getObject() {
				return null;
			}

			@Override
			public void setObject(Team object) {
				
			}
			
		}, new TeamChoiceProvider(null)));
	}

}
