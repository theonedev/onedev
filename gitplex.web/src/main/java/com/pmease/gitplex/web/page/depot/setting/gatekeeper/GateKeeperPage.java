package com.pmease.gitplex.web.page.depot.setting.gatekeeper;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.util.ReflectionUtils;
import com.pmease.commons.wicket.component.modal.ModalPanel;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.gatekeeper.DefaultGateKeeper;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.page.depot.setting.DepotSettingPage;

@SuppressWarnings("serial")
public class GateKeeperPage extends DepotSettingPage {

	public GateKeeperPage(PageParameters params) {
		super(params);
	}

	private Component newContent() {
		if (getDepot().getGateKeeper() instanceof DefaultGateKeeper) {
			return new GateKeeperLink("gateKeeper") {

				@Override
				protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
					final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
					if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
						getDepot().setGateKeeper(gateKeeper);
						onGateKeeperChanged(target);
					} else {
						new ModalPanel(target) {

							@Override
							protected Component newContent(String id) {
								return new GateKeeperEditor(id, gateKeeper) {

									@Override
									protected void onCancel(AjaxRequestTarget target) {
										close(target);
									}

									@Override
									protected void onSave(AjaxRequestTarget target, GateKeeper gateKeeper) {
										close(target);
										getDepot().setGateKeeper(gateKeeper);
										onGateKeeperChanged(target);
									}
									
								};
							}
							
						};
					}
				}
				
				@Override
				public IModel<?> getBody() {
					return Model.of("Define gatekeeper <i class='fa fa-plus-circle'></i>");
				}
				
			}.setOutputMarkupId(true).setEscapeModelStrings(false).add(AttributeAppender.append("class", "well gate-keeper-add"));
		} else {
			return new GateKeeperPanel("gateKeeper", getDepot().getGateKeeper()) {

				@Override
				protected void onDelete(AjaxRequestTarget target) {
					getDepot().setGateKeeper(new DefaultGateKeeper());
					onGateKeeperChanged(target);
				}

				@Override
				protected void onChange(AjaxRequestTarget target, GateKeeper gateKeeper) {
					getDepot().setGateKeeper(gateKeeper);
					onGateKeeperChanged(target);
				}
				
			}.setOutputMarkupId(true);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent());
	}
	
	private void onGateKeeperChanged(AjaxRequestTarget target) {
		GitPlex.getInstance(DepotManager.class).save(getDepot(), null, null);
		Component content = newContent();
		replace(content);
		target.add(content);
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(GateKeeperPage.class, GateKeeperPage.paramsOf(depot));
	}

}
