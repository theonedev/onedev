package io.onedev.server.web.component.job;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.git.GitUtils;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
abstract class BuildOptionContentPanel extends Panel {

	private final List<String> refNames;
	
	private final Serializable paramBean;
	
	private final Collection<String> selectedRefNames = new HashSet<>();
	
	public BuildOptionContentPanel(String id, List<String> refNames, Serializable paramBean) {
		super(id);
		this.refNames = refNames;
		this.paramBean = paramBean;
		
		if (refNames.size() == 1)
			selectedRefNames.addAll(refNames);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		form.setOutputMarkupId(true);
		
		ListView<String> refNamesView;
		form.add(refNamesView = new ListView<String>("refNames", refNames) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String refName = item.getModelObject();
				item.add(new CheckBox("check", new IModel<Boolean>() {

					@Override
					public void detach() {
					}

					@Override
					public Boolean getObject() {
						return selectedRefNames.contains(refName);
					}

					@Override
					public void setObject(Boolean object) {
						if (object)
							selectedRefNames.add(refName);
						else
							selectedRefNames.remove(refName);
					}
					
				}));
				
				String branch = GitUtils.ref2branch(refName);
				if (branch != null)
					item.add(new Label("label", branch));
				else
					item.add(new Label("label", GitUtils.ref2tag(refName)));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(refNames.size() > 1);
			}
			
		});
		
		form.add(new FencedFeedbackPanel("refNamesError", refNamesView));
		
		form.add(BeanContext.edit("paramEditor", paramBean));
		
		form.add(new AjaxButton("ok") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (selectedRefNames.isEmpty()) {
					refNamesView.error("At least one branch or tag should be selected");
					target.add(form);
				} else {
					onSave(target, selectedRefNames, paramBean);
				}
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				BuildOptionContentPanel.this.onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				BuildOptionContentPanel.this.onCancel(target);
			}
			
		});
		add(form);
	}
	
	protected abstract void onSave(AjaxRequestTarget target, Collection<String> selectedRefNames, 
			Serializable populatedParamBean);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
