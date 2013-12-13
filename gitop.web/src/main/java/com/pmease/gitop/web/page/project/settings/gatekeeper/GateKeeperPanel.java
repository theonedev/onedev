package com.pmease.gitop.web.page.project.settings.gatekeeper;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;
import com.pmease.gitop.model.gatekeeper.AndGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.IfThenGateKeeper;
import com.pmease.gitop.model.gatekeeper.NotGateKeeper;
import com.pmease.gitop.model.gatekeeper.OrGateKeeper;

@SuppressWarnings("serial")
public abstract class GateKeeperPanel extends Panel {
	
	private GateKeeper gateKeeper;

	public GateKeeperPanel(String id, GateKeeper gateKeeper) {
		super(id);
		this.gateKeeper = gateKeeper;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Class<? extends GateKeeper> clazz = gateKeeper.getClass();
		add(new Label("title", EditableUtils.getName(clazz)));
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				ModalPanel modalPanel = new ModalPanel("editorModal") {

					@Override
					protected Component newContent(String id) {
						return new GateKeeperEditor(id, gateKeeper) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								close(target);
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								close(target);
								GateKeeperPanel.this.onSave(target, gateKeeper);
							}
							
						};
					}
					
				};
				GateKeeperPanel.this.replace(modalPanel);
				target.add(modalPanel);
			}
			
		});
		add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		add(new WebMarkupContainer("editorModal").setOutputMarkupPlaceholderTag(true).setVisible(false));
		
		if (clazz == AndGateKeeper.class) {
			add(new Fragment("content", "andFrag", GateKeeperPanel.this));
		} else if (clazz == OrGateKeeper.class) {
			add(new Fragment("content", "orFrag", GateKeeperPanel.this));
		} else if (clazz == NotGateKeeper.class) {
			add(new Fragment("content", "notFrag", GateKeeperPanel.this));
		} else if (clazz == IfThenGateKeeper.class) {
			add(new Fragment("content", "ifThenFrag", GateKeeperPanel.this));
		} else {
			Fragment fragment = new Fragment("content", "otherFrag", GateKeeperPanel.this);
			EditContext editContext = EditableUtils.getContext(gateKeeper);
			fragment.add((Component) editContext.renderForView("viewer"));
			add(fragment);
		}
	}

	protected abstract void onDelete(AjaxRequestTarget target);
	
	protected abstract void onSave(AjaxRequestTarget target, GateKeeper gateKeeper);
	
}
