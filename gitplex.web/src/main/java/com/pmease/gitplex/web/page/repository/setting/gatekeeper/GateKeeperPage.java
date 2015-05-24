package com.pmease.gitplex.web.page.repository.setting.gatekeeper;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.util.ReflectionUtils;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.web.page.repository.setting.RepoSettingPage;

@SuppressWarnings("serial")
public class GateKeeperPage extends RepoSettingPage {

	private static final String CONTAINER_ID = "gateKeeperSetting";
	
	public GateKeeperPage(PageParameters params) {
		super(params);
	}

	private WebMarkupContainer newContent() {
		WebMarkupContainer content = new WebMarkupContainer(CONTAINER_ID);
		content.setOutputMarkupId(true);
		
		content.add(new ListView<GateKeeper>("gateKeepers", getRepository().getGateKeepers()) {

			@Override
			protected void populateItem(final ListItem<GateKeeper> item) {
				item.add(new GateKeeperPanel("gateKeeper", item.getModelObject()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						getRepository().getGateKeepers().remove(item.getIndex());
						onGateKeeperChanged(target);
					}

					@Override
					protected void onChange(AjaxRequestTarget target, GateKeeper gateKeeper) {
						getRepository().getGateKeepers().set(item.getIndex(), gateKeeper);
						onGateKeeperChanged(target);
					}
					
				});
			}
			
		});
		GateKeeperDropdown gateKeeperDropdown = new GateKeeperDropdown("gateKeeperDropdown") {

			@Override
			protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
				final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
				if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
					getRepository().getGateKeepers().add(gateKeeper);
					onGateKeeperChanged(target);
				} else {
					ModalPanel modalPanel = new ModalPanel("gateKeeperModal", true) {

						@Override
						protected Component newContent(String id, ModalBehavior behavior) {
							return new GateKeeperEditor(id, gateKeeper) {

								@Override
								protected void onCancel(AjaxRequestTarget target) {
									close(target);
								}

								@Override
								protected void onSave(AjaxRequestTarget target, GateKeeper gateKeeper) {
									close(target);
									getRepository().getGateKeepers().add(gateKeeper);
									onGateKeeperChanged(target);
								}
								
							};
						}
						
					};
					((WebMarkupContainer)GateKeeperPage.this.get(CONTAINER_ID)).replace(modalPanel);
					target.add(modalPanel);
				}
			}
			
		};
		content.add(gateKeeperDropdown);
		DropdownBehavior behavior = new DropdownBehavior(gateKeeperDropdown);
		behavior.alignWithCursor(10, 10);
		content.add(new WebMarkupContainer("gateKeeperDropdownTrigger").add(behavior));
		content.add(new WebMarkupContainer("gateKeeperPanel").setVisible(false));
		content.add(new WebMarkupContainer("gateKeeperModal").setOutputMarkupPlaceholderTag(true).setVisible(false));
		return content;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(newContent());
	}
	
	private void onGateKeeperChanged(AjaxRequestTarget target) {
		GitPlex.getInstance(RepositoryManager.class).save(getRepository());
		replace(newContent());
		target.add(get(CONTAINER_ID));
	}

	@Override
	protected String getPageTitle() {
		return "Gate Keepers - " + getRepository();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(GateKeeperPage.class, "gate-keeper.css")));
	}

}
