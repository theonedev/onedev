package com.pmease.gitplex.web.page.depot.setting.general;

import java.io.Serializable;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteRepoModal;
import com.pmease.gitplex.web.page.account.depots.AccountDepotsPage;
import com.pmease.gitplex.web.page.depot.setting.DepotSettingPage;

@SuppressWarnings("serial")
public class GeneralSettingPage extends DepotSettingPage {

	private BeanEditor<?> editor;
	
	public GeneralSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		editor = BeanContext.editModel("editor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getDepot();
			}

			@Override
			public void setObject(Serializable object) {
				editor.getBeanDescriptor().copyProperties(object, getDepot());
			}
			
		});
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Depot depot = getDepot();
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				Depot depotWithSameName = depotManager.findBy(getAccount(), depot.getName());
				if (depotWithSameName != null && !depotWithSameName.equals(depot)) {
					String errorMessage = "This name has already been used by another repository in account " 
							+ getAccount().getName() + "."; 
					editor.getErrorContext(new PathSegment.Property("name")).addError(errorMessage);
				} else {
					depotManager.save(depot);
					Session.get().success("General setting has been updated");
					setResponsePage(GeneralSettingPage.class, paramsOf(depot));
				}
			}
			
		};
		form.add(editor);
		form.add(new SubmitLink("save"));

		form.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ConfirmDeleteRepoModal(target) {
					
					@Override
					protected void onDeleted(AjaxRequestTarget target) {
						setResponsePage(AccountDepotsPage.class, paramsOf(getAccount()));						
					}
					
					@Override
					protected Depot getDepot() {
						return GeneralSettingPage.this.getDepot();
					}
				};
			}
			
		});
		
		add(form);
	}

	@Override
	protected String getPageTitle() {
		return "General Setting - " + getDepot();
	}

}
