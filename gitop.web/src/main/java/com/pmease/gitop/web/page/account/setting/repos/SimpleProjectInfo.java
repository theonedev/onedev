package com.pmease.gitop.web.page.account.setting.repos;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.Constants;
import com.pmease.gitop.web.common.datatype.DataTypes;
import com.pmease.gitop.web.component.link.ProjectHomeLink;
import com.pmease.gitop.web.util.DateUtils;

public class SimpleProjectInfo extends Panel {

	private static final long serialVersionUID = 1L;

	public SimpleProjectInfo(String id, IModel<Project> model) {
		super(id, model);
	}

	@SuppressWarnings({ "unchecked", "serial" })
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ProjectHomeLink("project", (IModel<Project>) getDefaultModel()));
		add(new Label("age", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return DateUtils.formatAge(getProject().getCreatedAt());
			}

		}).add(AttributeModifier.replace("title",
				new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return DataTypes.DATE
								.asString(getProject().getCreatedAt(),
										Constants.DATETIME_FULL_FORMAT);
					}

				})));

		if (getProject().getForkedFrom() != null) {
			add(new ProjectHomeLink("forkedFrom",
					new LoadableDetachableModel<Project>() {

						@Override
						protected Project load() {
							return getProject().getForkedFrom();
						}
					}));
		} else {
			add(new WebMarkupContainer("forkedFrom")
					.setVisibilityAllowed(false));
		}
	}

	protected Project getProject() {
		return (Project) getDefaultModelObject();
	}
}