package com.pmease.gitplex.web.page.test;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.PropertyModel;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.web.component.refmatch.RefMatchInput;
import com.pmease.gitplex.web.page.base.BasePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	private String refMatch;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				System.out.println(refMatch);
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		
		form.add(new RefMatchInput("input", new AbstractReadOnlyModel<Depot>() {

			@Override
			public Depot getObject() {
				return GitPlex.getInstance(Dao.class).load(Depot.class, 1L);
			}
			
		}, new PropertyModel<String>(this, "refMatch")));
		add(form);
	}

}
