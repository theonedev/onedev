package com.pmease.gitop.web.page.account.setting.repos;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.core.model.Project;

public class SimpleProjectInfo extends Panel {

	private static final long serialVersionUID = 1L;

	public SimpleProjectInfo(String id, IModel<Project> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
//		add(new UserProjectLink("project", (IModel<Project>) getDefaultModel()));
//		add(new Label("age", new AbstractReadOnlyModel<String>() {
//
//			@Override
//			public String getObject() {
//				return DateUtils.formatAge(getProject().getCreatedAt());
//			}
//
//		}).add(AttributeModifier.replace("title",
//				new AbstractReadOnlyModel<String>() {
//
//					@Override
//					public String getObject() {
//						return DataTypes.DATE
//								.asString(getProject().getCreatedAt(),
//										Constants.DATETIME_FULL_FORMAT);
//					}
//
//				})));
//
//		if (getProject().isForked()) {
//			add(new UserProjectLink("forkedFrom",
//					new LoadableDetachableModel<Project>() {
//
//						@Override
//						protected Project load() {
//							return getProject().getParentFork().getForkedFrom();
//						}
//					}));
//		} else {
//			add(new WebMarkupContainer("forkedFrom")
//					.setVisibilityAllowed(false));
//		}
	}

	protected Project getProject() {
		return (Project) getDefaultModelObject();
	}
}