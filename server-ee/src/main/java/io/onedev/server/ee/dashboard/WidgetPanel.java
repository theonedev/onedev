package io.onedev.server.ee.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.model.support.Widget;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;

@SuppressWarnings("serial")
class WidgetPanel extends Panel {

	private final Widget widget;
	
	private final boolean failsafe;
	
	private final WidgetEditCallback editCallback;
	
	private AbstractDefaultAjaxBehavior callbackBehavior;
	
	public WidgetPanel(String id, Widget widget, boolean failsafe, @Nullable WidgetEditCallback editCallback) {
		super(id);
		this.widget = widget;
		this.failsafe = failsafe;
		this.editCallback = editCallback;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return widget.getTitle();
			}
			
		}).setOutputMarkupId(true));
		
		add(new MenuLink("actions") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Refresh";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								Component body = widget.render("body", failsafe);
								WidgetPanel.this.replace(body);
								target.add(body);
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Edit";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								new BeanEditModalPanel<Widget>(target, widget) {

									@Override
									protected void onSave(AjaxRequestTarget target, Widget bean) {
										target.add(WidgetPanel.this.get("title"));
										Component body = widget.render("body", failsafe);
										WidgetPanel.this.replace(body);
										target.add(body);
										
										editCallback.onSave(target, WidgetPanel.this);
										close();
									}
									
								};
							}
							
						};
					}
					
				});
				
				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Copy";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								
								new BeanEditModalPanel<Widget>(target, SerializationUtils.clone(widget)) {

									@Override
									protected void onSave(AjaxRequestTarget target, Widget bean) {
										target.add(WidgetPanel.this);
										editCallback.onCopy(target, bean);
										close();
									}
									
								};
							}
							
						};
					}
					
				});

				menuItems.add(new MenuItem() {

					@Override
					public String getLabel() {
						return "Delete";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								editCallback.onDelete(target, WidgetPanel.this);
							}
							
						};
					}
					
				});
				
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editCallback != null);
			}
			
		});
		
		add(new AjaxLink<Void>("refresh") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Component body = widget.render("body", failsafe);
				WidgetPanel.this.replace(body);
				target.add(body);
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editCallback == null);
			}
			
		});
		
		add(widget.render("body", failsafe).setOutputMarkupId(true));
		
		if (editCallback != null) {
			add(callbackBehavior = new AbstractPostAjaxBehavior() {
				
				@Override
				protected void respond(AjaxRequestTarget target) {
					IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
					widget.setLeft(params.getParameterValue("left").toInt());
					widget.setTop(params.getParameterValue("top").toInt());
					widget.setRight(params.getParameterValue("right").toInt());
					widget.setBottom(params.getParameterValue("bottom").toInt());
				}
				
			});
		}
		
		setOutputMarkupId(true);
	}

	public Widget getWidget() {
		return widget;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		CharSequence callback;
		if (callbackBehavior != null) {
			callback = callbackBehavior.getCallbackFunction(
					CallbackParameter.explicit("left"), 
					CallbackParameter.explicit("top"), 
					CallbackParameter.explicit("right"),
					CallbackParameter.explicit("bottom"));
		} else {
			callback = "undefined";
		}
		String script = String.format("onedev.server.dashboard.onWidgetDomReady('%s', %d, %d, %d, %d, %s);", 
				getMarkupId(), widget.getLeft(), widget.getTop(), widget.getRight(), widget.getBottom(), callback); 
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}
