package com.pmease.gitop.web.page.project.settings;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.permission.operation.GeneralOperation;

@SuppressWarnings("serial")
public class ProjectPermissionPage extends AbstractProjectSettingPage {

	public ProjectPermissionPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.PERMISSIONS;
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();
		
		AbstractLink link = new AjaxLink<Void>("anonymouslink") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Project project = getProject();
				project.setPubliclyAccessible(!project.isPubliclyAccessible());
				Gitop.getInstance(ProjectManager.class).save(project);
				
				target.add(this);
			}
		
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				Project project = getProject();
				setEnabled(!project.getOwner().isPubliclyAccessible());
			}
		};
		
		link.setOutputMarkupId(true);
		link.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Project project = getProject();
				if (project.isPubliclyAccessible() || project.getOwner().isPubliclyAccessible()) {
					return "checked";
				} else {
					return "";
				}
			}
			
		}));
		
		add(link);
		
		WebMarkupContainer loggedInPermissions = new WebMarkupContainer("loggedInPermissions");
		add(loggedInPermissions);
		loggedInPermissions.setOutputMarkupId(true);
		add(new Label("userlevelpermission", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getProject().getOwner().getDefaultAuthorizedOperation().name();
			}
		}));
		
		IModel<List<GeneralOperation>> permissionsModel = new AbstractReadOnlyModel<List<GeneralOperation>>() {

			@Override
			public List<GeneralOperation> getObject() {
//				Project project = getProject();
//				List<GeneralOperation> operations = Lists.newArrayList();
//				GeneralOperation accountLevel = project.getOwner().getDefaultAuthorizedOperation();
//				for (GeneralOperation each : GeneralOperation.values()) {
//					if (each.compareTo(accountLevel) >= 0 && !Objects.equal(each, GeneralOperation.ADMIN)) {
//						operations.add(each);
//					}
//				}
//				
//				return operations;
				return ImmutableList.<GeneralOperation>of(GeneralOperation.NO_ACCESS, GeneralOperation.READ, GeneralOperation.WRITE);
			}
		};
		
		loggedInPermissions.add(new ListView<GeneralOperation>("permissions", permissionsModel) {

					@Override
					protected void populateItem(ListItem<GeneralOperation> item) {
						AjaxLink<?> link = new PermissionLink("permission", item.getModelObject());
						item.add(link);
						link.add(new Label("name", item.getModelObject().toString()));
					}
			
		});
	}
	
	private GeneralOperation getLoggedInPermission() {
		Project project = getProject();
		GeneralOperation op = project.getOwner().getDefaultAuthorizedOperation();
		GeneralOperation op2 = project.getDefaultAuthorizedOperation();
		return op.compareTo(op2) > 0 ? op : op2;
	}
	
	class PermissionLink extends AjaxLink<Void> {
		final GeneralOperation permission;
		
		PermissionLink(String id, final GeneralOperation permission) {
			super(id);
			this.permission = permission;
			
			add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return Objects.equal(getLoggedInPermission(), permission) ?
							"btn-default active" : "btn-default";
				}
			}));
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			Project project = getProject();
			project.setDefaultAuthorizedOperation(permission);
			Gitop.getInstance(ProjectManager.class).save(project);
			
			target.add(ProjectPermissionPage.this.get("loggedInPermissions"));
		}
	}
}
