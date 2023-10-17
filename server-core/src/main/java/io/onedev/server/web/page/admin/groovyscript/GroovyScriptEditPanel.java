package io.onedev.server.web.page.admin.groovyscript;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
abstract class GroovyScriptEditPanel extends Panel {

	private final int scriptIndex;
	
	public GroovyScriptEditPanel(String id, int scriptIndex) {
		super(id);
	
		this.scriptIndex = scriptIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		GroovyScript script;
		if (scriptIndex != -1)
			script = SerializationUtils.clone(getScripts().get(scriptIndex));
		else
			script = new GroovyScript();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(GroovyScriptEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.edit("editor", script);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (scriptIndex != -1) { 
					GroovyScript oldScript = getScripts().get(scriptIndex);
					if (!script.getName().equals(oldScript.getName()) && getScript(script.getName()) != null) {
						editor.error(new Path(new PathNode.Named("name")),
								"This name has already been used by another script");
					}
				} else if (getScript(script.getName()) != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another script");
				}

				if (editor.isValid()) {
					if (scriptIndex != -1) 
						getScripts().set(scriptIndex, script);
					else 
						getScripts().add(script);
					OneDev.getInstance(SettingManager.class).saveGroovyScripts(getScripts());
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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(GroovyScriptEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}
	
	@Nullable
	private GroovyScript getScript(String name) {
		for (GroovyScript script: getScripts()) {
			if (script.getName().equals(name))
				return script;
		}
		return null;
	}

	protected abstract List<GroovyScript> getScripts();
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

}
