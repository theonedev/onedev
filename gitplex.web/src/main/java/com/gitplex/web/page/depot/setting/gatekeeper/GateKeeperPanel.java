package com.gitplex.web.page.depot.setting.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.gitplex.core.gatekeeper.AbstractGateKeeper;
import com.gitplex.core.gatekeeper.AndGateKeeper;
import com.gitplex.core.gatekeeper.AndOrGateKeeper;
import com.gitplex.core.gatekeeper.ConditionalGateKeeper;
import com.gitplex.core.gatekeeper.DefaultGateKeeper;
import com.gitplex.core.gatekeeper.GateKeeper;
import com.gitplex.core.gatekeeper.NotGateKeeper;
import com.google.common.base.Joiner;
import com.gitplex.commons.util.ReflectionUtils;
import com.gitplex.commons.wicket.ajaxlistener.ConfirmListener;
import com.gitplex.commons.wicket.behavior.dragdrop.DragBehavior;
import com.gitplex.commons.wicket.component.modal.ModalLink;
import com.gitplex.commons.wicket.component.modal.ModalPanel;
import com.gitplex.commons.wicket.editable.BeanContext;
import com.gitplex.commons.wicket.editable.EditableUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;

@SuppressWarnings("serial")
abstract class GateKeeperPanel extends Panel {
	
	private final GateKeeper gateKeeper;
	
	private final List<Integer> position;
	
	public GateKeeperPanel(String id, GateKeeper gateKeeper, List<Integer> position) {
		super(id);
		this.gateKeeper = gateKeeper;
		this.position = position;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container");
		add(container);
		
		if(!gateKeeper.isEnabled())
			container.add(AttributeAppender.append("class", "disabled"));
		
		if (position.size()%2 == 0) {
			container.add(AttributeAppender.append("class", "even panel-danger"));
		} else {
			container.add(AttributeAppender.append("class", "odd panel-success"));
		}
		
		Class<? extends GateKeeper> clazz = gateKeeper.getClass();
		String title = EditableUtils.getName(clazz);
		container.add(new Label("title", title));
		container.add(new WebMarkupContainer("dragHandle").add(new DragBehavior(Joiner.on(":").join(position), title)));
		container.add(new ModalLink("edit") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(EditableUtils.hasEditableProperties(clazz));
			}

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
			
		});
			
		container.add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this gatekeeper?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		
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
					tag.append("class", "fa-check-square-o", " ");
				} else {
					tag.append("class", "fa-square-o", " ");
				}
			}
			
		}.add(AttributeAppender.append("title", Model.of(gateKeeper.isEnabled()?"Disable this gatekeeper":"Enable this gatekeeper"))));
		
		container.add(new WebMarkupContainer("help").add(new TooltipBehavior(Model.of(EditableUtils.getDescription(gateKeeper.getClass())))));
		
		container.add(new WebMarkupContainer("editor").setOutputMarkupPlaceholderTag(true).setVisible(false));
		
		if (AndOrGateKeeper.class.isAssignableFrom(clazz)) {
			AndOrGateKeeper andOrGateKeeper = (AndOrGateKeeper) gateKeeper;
			Fragment fragment;
			if (andOrGateKeeper instanceof AndGateKeeper)
				fragment = new Fragment("content", "andFrag", GateKeeperPanel.this);
			else
				fragment = new Fragment("content", "orFrag", GateKeeperPanel.this);
			
			fragment.add(new ListView<GateKeeper>("children", andOrGateKeeper.getGateKeepers()) {

				@Override
				protected void populateItem(ListItem<GateKeeper> item) {
					List<Integer> newPosition = new ArrayList<>(position);
					newPosition.add(item.getIndex());
					item.add(new GateKeeperPanel("child", item.getModelObject(), newPosition) {

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
			fragment.add(new GateKeeperLink("childTypeSelectorTrigger") {

				@Override
				protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
					final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
					if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
						andOrGateKeeper.getGateKeepers().add(gateKeeper);
						GateKeeperPanel.this.onChange(target, andOrGateKeeper);
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
										fragment.replace(new WebMarkupContainer("childEditor").setOutputMarkupPlaceholderTag(true).setVisible(true));
										andOrGateKeeper.getGateKeepers().add(gateKeeper);
										GateKeeperPanel.this.onChange(target, andOrGateKeeper);
									}
									
								};
							}
							
						};
					}
				}

				@Override
				protected List<Integer> getPosition() {
					List<Integer> newPosition = new ArrayList<>(position);
					newPosition.add(andOrGateKeeper.getGateKeepers().size());
					return newPosition;
				}

			});
			
			fragment.add(new WebMarkupContainer("childEditor").setOutputMarkupPlaceholderTag(true).setVisible(false));
			container.add(fragment);
		} else if (clazz == NotGateKeeper.class) {
			final NotGateKeeper notGateKeeper = (NotGateKeeper) gateKeeper;
			final Fragment fragment = new Fragment("content", "notFrag", GateKeeperPanel.this);

			if (notGateKeeper.getGateKeeper() instanceof DefaultGateKeeper) {
				fragment.add(new GateKeeperLink("gateKeeperTypeSelectorTrigger") {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
						final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
						if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
							notGateKeeper.setGateKeeper(gateKeeper);
							GateKeeperPanel.this.onChange(target, notGateKeeper);
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
											fragment.replace(new WebMarkupContainer("gateKeeperEditor").setOutputMarkupPlaceholderTag(true).setVisible(true));
											notGateKeeper.setGateKeeper(gateKeeper);
											GateKeeperPanel.this.onChange(target, notGateKeeper);
										}
										
									};
								}
								
							};
						}
					}

					@Override
					protected List<Integer> getPosition() {
						List<Integer> newPosition = new ArrayList<>(position);
						newPosition.add(0);
						return newPosition;
					}
					
				});
				
				fragment.add(new WebMarkupContainer("gateKeeper").setVisible(false));
				fragment.add(new WebMarkupContainer("gateKeeperEditor").setOutputMarkupPlaceholderTag(true).setVisible(false));
			} else {
				List<Integer> newPosition = new ArrayList<>(position);
				newPosition.add(0);
				fragment.add(new GateKeeperPanel("gateKeeper", notGateKeeper.getGateKeeper(), newPosition) {

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
		} else if (clazz == ConditionalGateKeeper.class) {
			ConditionalGateKeeper ifThenGateKeeper = (ConditionalGateKeeper) gateKeeper;
			Fragment fragment = new Fragment("content", "ifThenFrag", GateKeeperPanel.this);

			if (ifThenGateKeeper.getIfGate() instanceof DefaultGateKeeper) {
				fragment.add(new GateKeeperLink("ifGate") {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
						GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
						if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
							ifThenGateKeeper.setIfGate(gateKeeper);
							GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
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
											fragment.replace(new WebMarkupContainer("ifEditor").setOutputMarkupPlaceholderTag(true).setVisible(true));
											ifThenGateKeeper.setIfGate(gateKeeper);
											GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
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

					@Override
					protected List<Integer> getPosition() {
						List<Integer> newPosition = new ArrayList<>(position);
						newPosition.add(0);
						return newPosition;
					}
					
				}.setEscapeModelStrings(false).add(AttributeAppender.append("class", "well gate-keeper-add")));
			} else {
				List<Integer> newPosition = new ArrayList<>(position);
				newPosition.add(0);
				fragment.add(new GateKeeperPanel("ifGate", ifThenGateKeeper.getIfGate(), newPosition) {

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
			}
			fragment.add(new WebMarkupContainer("ifEditor").setOutputMarkupPlaceholderTag(true).setVisible(false));
			
			if (ifThenGateKeeper.getThenGate() instanceof DefaultGateKeeper) {
				fragment.add(new GateKeeperLink("thenGate") {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends GateKeeper> gateKeeperClass) {
						final GateKeeper gateKeeper = ReflectionUtils.instantiateClass(gateKeeperClass);
						if (EditableUtils.isDefaultInstanceValid(gateKeeperClass)) {
							ifThenGateKeeper.setThenGate(gateKeeper);
							GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
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
											fragment.replace(new WebMarkupContainer("thenEditor").setOutputMarkupPlaceholderTag(true).setVisible(true));
											ifThenGateKeeper.setThenGate(gateKeeper);
											GateKeeperPanel.this.onChange(target, ifThenGateKeeper);
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

					@Override
					protected List<Integer> getPosition() {
						List<Integer> newPosition = new ArrayList<>(position);
						newPosition.add(1);
						return newPosition;
					}
					
				}.setEscapeModelStrings(false).add(AttributeAppender.append("class", "well gate-keeper-add")));
			} else {
				List<Integer> newPosition = new ArrayList<>(position);
				newPosition.add(1);
				fragment.add(new GateKeeperPanel("thenGate", ifThenGateKeeper.getThenGate(), newPosition) {

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
			}
			fragment.add(new WebMarkupContainer("thenEditor").setOutputMarkupPlaceholderTag(true).setVisible(false));

			container.add(fragment);
		} else {
			Fragment fragment = new Fragment("content", "otherFrag", GateKeeperPanel.this);
			fragment.add(BeanContext.viewBean("viewer", gateKeeper));
			container.add(fragment);
		}
		
		add(new GateKeeperDropBehavior() {

			@Override
			protected List<Integer> getPosition() {
				return position;
			}

		});
	}

	protected abstract void onDelete(AjaxRequestTarget target);
	
	protected abstract void onChange(AjaxRequestTarget target, GateKeeper gateKeeper);

}
