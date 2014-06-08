package com.pmease.gitop.web.page.repository.settings.gatekeeper;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.util.ReflectionUtils;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitop.model.gatekeeper.AbstractGateKeeper;
import com.pmease.gitop.model.gatekeeper.AndGateKeeper;
import com.pmease.gitop.model.gatekeeper.AndOrGateKeeper;
import com.pmease.gitop.model.gatekeeper.DefaultGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.IfThenGateKeeper;
import com.pmease.gitop.model.gatekeeper.NotGateKeeper;

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
		
		final WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		if(!gateKeeper.isEnabled())
			container.add(AttributeAppender.append("class", "disabled"));
		
		final Class<? extends GateKeeper> clazz = gateKeeper.getClass();
		container.add(new Label("title", EditableUtils.getName(clazz)));
		container.add(new AjaxLink<Void>("edit") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(EditableUtils.hasEditableProperties(clazz));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				ModalPanel modalPanel = new ModalPanel("editor") {

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
								GateKeeperPanel.this.onChange(target, gateKeeper);
							}
							
						};
					}
					
				};
				container.replace(modalPanel);
				target.add(modalPanel);
			}
			
		}.add(new TooltipBehavior()));
		container.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		}.add(new TooltipBehavior()));
		container.add(new AjaxLink<Void>("enable") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				((AbstractGateKeeper) gateKeeper).setEnabled(!gateKeeper.isEnabled());
				onChange(target, gateKeeper);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (gateKeeper.isEnabled()) {
					tag.append("class", "icon-checkbox-checked", " ");
				} else {
					tag.append("class", "icon-checkbox-unchecked", " ");
				}
			}
			
		}.add(new TooltipBehavior(Model.of(gateKeeper.isEnabled()?"Disable this gate keeper":"Enable this gate keeper"))));
		
		container.add(new WebMarkupContainer("editor").setOutputMarkupPlaceholderTag(true).setVisible(false));
		
		if (AndOrGateKeeper.class.isAssignableFrom(clazz)) {
			final AndOrGateKeeper andOrGateKeeper = (AndOrGateKeeper) gateKeeper;
			final Fragment fragment;
			if (andOrGateKeeper instanceof AndGateKeeper)
				fragment = new Fragment("content", "andFrag", GateKeeperPanel.this);
			else
				fragment = new Fragment("content", "orFrag", GateKeeperPanel.this);
			
			fragment.add(new ListView<GateKeeper>("children", andOrGateKeeper.getGateKeepers()) {

				@Override
				protected void populateItem(final ListItem<GateKeeper> item) {
					item.add(new GateKeeperPanel("child", item.getModelObject()) {

						@Override
						protected void onDelete(AjaxRequestTarget target) {
							andOrGateKeeper.getGateKeepers().remove(item.getIndex());
							GateKeeperPanel.this.onChange(target, andOrGateKeeper);
						}

						@Override
						protected void onChange(AjaxRequestTarget target, GateKeeper gateKeeper) {
							andOrGateKeeper.getGateKeepers().set(item.getIndex(), gateKeeper);
							GateKeeperPanel.this.onChange(target, andOrGateKeeper);
						}
						
					});
				}
				
			});
			GateKeeperDropdown dropdown = new GateKeeperDropdown("childTypeSelector") {

				@Override
				protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
					final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
					if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
						andOrGateKeeper.getGateKeepers().add(gateKeeper);
						GateKeeperPanel.this.onChange(target, andOrGateKeeper);
					} else {
						ModalPanel childEditor = new ModalPanel("childEditor") {

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
										fragment.replace(new WebMarkupContainer("childEditor").setOutputMarkupPlaceholderTag(true).setVisible(true));
										andOrGateKeeper.getGateKeepers().add(gateKeeper);
										GateKeeperPanel.this.onChange(target, andOrGateKeeper);
									}
									
								};
							}
							
						};
						fragment.replace(childEditor);
						target.add(childEditor);
					}
				}
				
			};
			fragment.add(dropdown);
			DropdownBehavior behavior = new DropdownBehavior(dropdown);
			behavior.alignWithCursor(10, 10);	
			WebMarkupContainer childTypeSelectorTrigger = new WebMarkupContainer("childTypeSelectorTrigger");
			
			childTypeSelectorTrigger.add(behavior);
			fragment.add(childTypeSelectorTrigger);
			fragment.add(new WebMarkupContainer("childEditor").setOutputMarkupPlaceholderTag(true).setVisible(false));
			container.add(fragment);
		} else if (clazz == NotGateKeeper.class) {
			final NotGateKeeper notGateKeeper = (NotGateKeeper) gateKeeper;
			final Fragment fragment = new Fragment("content", "notFrag", GateKeeperPanel.this);

			if (notGateKeeper.getGateKeeper() instanceof DefaultGateKeeper) {
				GateKeeperDropdown dropdown = new GateKeeperDropdown("gateKeeperTypeSelector") {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
						final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
						if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
							notGateKeeper.setGateKeeper(gateKeeper);
							GateKeeperPanel.this.onChange(target, notGateKeeper);
						} else {
							ModalPanel gateKeeperEditor = new ModalPanel("gateKeeperEditor") {

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
											fragment.replace(new WebMarkupContainer("gateKeeperEditor").setOutputMarkupPlaceholderTag(true).setVisible(true));
											notGateKeeper.setGateKeeper(gateKeeper);
											GateKeeperPanel.this.onChange(target, notGateKeeper);
										}
										
									};
								}
								
							};
							fragment.replace(gateKeeperEditor);
							target.add(gateKeeperEditor);
						}
					}
					
				};
				fragment.add(dropdown);
				DropdownBehavior behavior = new DropdownBehavior(dropdown);
				behavior.alignWithCursor(10, 10);	
				WebMarkupContainer gateKeeperTypeSelectorTrigger = new WebMarkupContainer("gateKeeperTypeSelectorTrigger");
				
				gateKeeperTypeSelectorTrigger.add(behavior);
				fragment.add(gateKeeperTypeSelectorTrigger);				
				
				fragment.add(new WebMarkupContainer("gateKeeper").setVisible(false));
				fragment.add(new WebMarkupContainer("gateKeeperEditor").setOutputMarkupPlaceholderTag(true).setVisible(false));
			} else {
				fragment.add(new GateKeeperPanel("gateKeeper", notGateKeeper.getGateKeeper()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						notGateKeeper.setGateKeeper(new DefaultGateKeeper());
						GateKeeperPanel.this.onChange(target, notGateKeeper);
					}

					@Override
					protected void onChange(AjaxRequestTarget target, GateKeeper gateKeeper) {
						notGateKeeper.setGateKeeper(gateKeeper);
						GateKeeperPanel.this.onChange(target, notGateKeeper);
					}
					
				});
				fragment.add(new WebMarkupContainer("gateKeeperTypeSelectorTrigger").setVisible(false));
				fragment.add(new WebMarkupContainer("gateKeeperTypeSelector").setVisible(false));
				fragment.add(new WebMarkupContainer("gateKeeperEditor").setVisible(false));
			}
			container.add(fragment);
		} else if (clazz == IfThenGateKeeper.class) {
			final IfThenGateKeeper ifThenGateKeeper = (IfThenGateKeeper) gateKeeper;
			final Fragment fragment = new Fragment("content", "ifThenFrag", GateKeeperPanel.this);

			if (ifThenGateKeeper.getIfGate() instanceof DefaultGateKeeper) {
				GateKeeperDropdown dropdown = new GateKeeperDropdown("ifTypeSelector") {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
						final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
						if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
							ifThenGateKeeper.setIfGate(gateKeeper);
							GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
						} else {
							ModalPanel ifEditor = new ModalPanel("ifEditor") {

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
											fragment.replace(new WebMarkupContainer("ifEditor").setOutputMarkupPlaceholderTag(true).setVisible(true));
											ifThenGateKeeper.setIfGate(gateKeeper);
											GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
										}
										
									};
								}
								
							};
							fragment.replace(ifEditor);
							target.add(ifEditor);
						}
					}
					
				};
				fragment.add(dropdown);
				DropdownBehavior behavior = new DropdownBehavior(dropdown);
				behavior.alignWithCursor(10, 10);	
				WebMarkupContainer ifTypeSelectorTrigger = new WebMarkupContainer("ifTypeSelectorTrigger");
				
				ifTypeSelectorTrigger.add(behavior);
				fragment.add(ifTypeSelectorTrigger);				
				
				fragment.add(new WebMarkupContainer("if").setVisible(false));
				fragment.add(new WebMarkupContainer("ifEditor").setOutputMarkupPlaceholderTag(true).setVisible(false));
			} else {
				fragment.add(new GateKeeperPanel("if", ifThenGateKeeper.getIfGate()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						ifThenGateKeeper.setIfGate(new DefaultGateKeeper());
						GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
					}

					@Override
					protected void onChange(AjaxRequestTarget target, GateKeeper gateKeeper) {
						ifThenGateKeeper.setIfGate(gateKeeper);
						GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
					}
					
				});
				fragment.add(new WebMarkupContainer("ifTypeSelectorTrigger").setVisible(false));
				fragment.add(new WebMarkupContainer("ifTypeSelector").setVisible(false));
				fragment.add(new WebMarkupContainer("ifEditor").setVisible(false));
			}
			
			if (ifThenGateKeeper.getThenGate() instanceof DefaultGateKeeper) {
				GateKeeperDropdown dropdown = new GateKeeperDropdown("thenTypeSelector") {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
						final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
						if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
							ifThenGateKeeper.setThenGate(gateKeeper);
							GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
						} else {
							ModalPanel thenEditor = new ModalPanel("thenEditor") {

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
											fragment.replace(new WebMarkupContainer("thenEditor").setOutputMarkupPlaceholderTag(true).setVisible(true));
											ifThenGateKeeper.setThenGate(gateKeeper);
											GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
										}
										
									};
								}
								
							};
							fragment.replace(thenEditor);
							target.add(thenEditor);
						}
					}
					
				};
				fragment.add(dropdown);
				DropdownBehavior behavior = new DropdownBehavior(dropdown);
				behavior.alignWithCursor(10, 10);	
				WebMarkupContainer thenTypeSelectorTrigger = new WebMarkupContainer("thenTypeSelectorTrigger");
				
				thenTypeSelectorTrigger.add(behavior);
				fragment.add(thenTypeSelectorTrigger);				
				
				fragment.add(new WebMarkupContainer("then").setVisible(false));
				fragment.add(new WebMarkupContainer("thenEditor").setOutputMarkupPlaceholderTag(true).setVisible(false));
			} else {
				fragment.add(new GateKeeperPanel("then", ifThenGateKeeper.getThenGate()) {

					@Override
					protected void onDelete(AjaxRequestTarget target) {
						ifThenGateKeeper.setThenGate(new DefaultGateKeeper());
						GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
					}

					@Override
					protected void onChange(AjaxRequestTarget target, GateKeeper gateKeeper) {
						ifThenGateKeeper.setThenGate(gateKeeper);
						GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
					}
					
				});
				fragment.add(new WebMarkupContainer("thenTypeSelectorTrigger").setVisible(false));
				fragment.add(new WebMarkupContainer("thenTypeSelector").setVisible(false));
				fragment.add(new WebMarkupContainer("thenEditor").setVisible(false));
			}

			container.add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "otherFrag", GateKeeperPanel.this);
			fragment.add(BeanContext.view("viewer", gateKeeper));
			container.add(fragment);
		}
	}

	protected abstract void onDelete(AjaxRequestTarget target);
	
	protected abstract void onChange(AjaxRequestTarget target, GateKeeper gateKeeper);

}
