package io.onedev.server.web.component.datatable.selectioncolumn;

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;

@SuppressWarnings("serial")
abstract class RowSelector<T> extends Panel {

	public RowSelector(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("checkbox") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (getSelections().contains(getModel()))
					tag.put("checked", "true");
			}

		}.setOutputMarkupId(true));

		add(new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(final AjaxRequestTarget target) {
				boolean checked = RequestCycle.get()
						.getRequest()
						.getPostParameters()
						.getParameterValue("checked")
						.toBoolean();
				if (checked)
					getSelections().add(getModel());
				else
					getSelections().remove(getModel());

				findParent(DataTable.class).visitChildren(
						HeaderSelector.class,
						new IVisitor<HeaderSelector<?>, Void>() {

							public void component(HeaderSelector<?> object, IVisit<Void> visit) {
								target.add(object);
								visit.stop();
							}

						});

				onSelectionChange(target);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				response.render(JavaScriptHeaderItem.forReference(WicketAjaxJQueryResourceReference.get()));

				CallbackParameter param = CallbackParameter.explicit("checked");
				String script = String.format(""
						+ "$('#%s').change(function() {\n"
						+ "  var checked = this.checked;\n"
						+ "  %s;\n"
						+ "});\n", 
						RowSelector.this.get("checkbox").getMarkupId(), getCallbackFunctionBody(param));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});
	}

	@SuppressWarnings("unchecked")
	public IModel<T> getModel() {
		return (IModel<T>) getDefaultModel();
	}
	
	protected abstract Set<IModel<T>> getSelections();

	protected abstract void onSelectionChange(AjaxRequestTarget target);
	
}