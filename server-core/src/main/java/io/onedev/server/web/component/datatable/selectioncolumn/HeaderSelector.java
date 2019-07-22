package io.onedev.server.web.component.datatable.selectioncolumn;

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
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
abstract class HeaderSelector<T> extends Panel {

	public HeaderSelector(String id) {
		super(id);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("checkbox") {

			private boolean allChecked;
			
			private boolean noneChecked;
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);

				allChecked = true;
				noneChecked = true;

				findParent(DataTable.class).visitChildren(
						RowSelector.class,
						new IVisitor<RowSelector<T>, Void>() {

							public void component(RowSelector<T> component, IVisit<Void> visit) {
								if (!getSelections().contains(component.getModel())) 
									allChecked = false;
								else 
									noneChecked = false;
							}

						});
				String script;
				if (allChecked && !noneChecked) {
					script = String.format(""
							+ "$('#%s').prop('indeterminate', false);" 
							+ "$('#%s').prop('checked', true);",
							getMarkupId(), getMarkupId());
				} else if (!allChecked && noneChecked) { 
					script = String.format(""
							+ "$('#%s').prop('indeterminate', false);" 
							+ "$('#%s').prop('checked', false);",
							getMarkupId(), getMarkupId());
				} else {
					script = String.format(""
							+ "$('#%s').prop('indeterminate', true);"
							+ "$('#%s').prop('checked', true);", 
							getMarkupId(), getMarkupId());
				}
				
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		}.setOutputMarkupId(true));

		add(new AbstractPostAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
				final boolean checked = RequestCycle.get()
						.getRequest()
						.getPostParameters()
						.getParameterValue("checked")
						.toBoolean();
				findParent(DataTable.class).visitChildren(
						RowSelector.class,
						new IVisitor<RowSelector<T>, Void>() {

							public void component(RowSelector<T> component,
									IVisit<Void> visit) {
								if (checked)
									getSelections().add(component.getModel());
								else
									getSelections().remove(component.getModel());
							}
						});

				onSelectionChange(target);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				response.render(JavaScriptHeaderItem.forReference(WicketAjaxJQueryResourceReference.get()));

				String template = 
						"$('#%s').change(function() {" + 
						"	var checked = this.checked;" +
						"	$(this).closest('table').find('.row-selector input').each(function() {" +
						"		if (!$(this).prop('disabled')) " +
						"			this.checked = checked;" + 
						"	});" + 
						"	%s" +
						"});";
				String script = String.format(
						template, 
						HeaderSelector.this.get("checkbox").getMarkupId(), 
						getCallbackFunctionBody(CallbackParameter.explicit("checked")));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

		});

	}

	protected abstract Set<IModel<T>> getSelections();
	
	protected abstract void onSelectionChange(AjaxRequestTarget target);
	
}