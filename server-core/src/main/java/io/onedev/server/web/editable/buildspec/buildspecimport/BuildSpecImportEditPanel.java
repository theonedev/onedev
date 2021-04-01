package io.onedev.server.web.editable.buildspec.buildspecimport;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.buildspec.BuildSpecImport;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
abstract class BuildSpecImportEditPanel extends Panel {

	private final List<BuildSpecImport> imports;
	
	private final int importIndex;
	
	public BuildSpecImportEditPanel(String id, List<BuildSpecImport> imports, int importIndex) {
		super(id);
	
		this.imports = imports;
		this.importIndex = importIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BuildSpecImport buildSpecImport;
		if (importIndex != -1)
			buildSpecImport = imports.get(importIndex);
		else
			buildSpecImport = new BuildSpecImport();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(BuildSpecImportEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.edit("editor", buildSpecImport);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (importIndex != -1) { 
					BuildSpecImport oldImport = imports.get(importIndex);
					if (!buildSpecImport.getProjectName().equals(oldImport.getProjectName()) 
							&& getImport(buildSpecImport.getProjectName()) != null) {
						editor.error(new Path(new PathNode.Named("projectName")),
								"Build spec of this project is already imported");
					}
				} else if (getImport(buildSpecImport.getProjectName()) != null) {
					editor.error(new Path(new PathNode.Named("projectName")),
							"Build spec of this project is already imported");
				}

				if (editor.isValid()) {
					if (importIndex != -1) {
						imports.set(importIndex, buildSpecImport);
					} else {
						imports.add(buildSpecImport);
					}
					onSave(target);
				} else {
					target.add(form);
				}
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(BuildSpecImportEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	private BuildSpecImport getImport(String projectName) {
		for (BuildSpecImport buildSpecImport: imports) {
			if (projectName.equals(buildSpecImport.getProjectName()))
				return buildSpecImport;
		}
		return null;
	}
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

}
