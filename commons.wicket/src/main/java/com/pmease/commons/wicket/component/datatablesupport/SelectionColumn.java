package com.pmease.commons.wicket.component.datatablesupport;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.WicketAjaxJQueryResourceReference;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * Add this column to the wicket DataTable component so that user can select row of the 
 * data table using checkboxes. Override method ${@link SelectionColumn#onSelectionChange(AjaxRequestTarget)} 
 * to add your process logic when selection changes, and call ${@link SelectionColumn#getSelections()} to get 
 * current selections.
 *   
 * @author robin
 *
 * @param <T>
 * @param <S>
 */
@SuppressWarnings("serial")
public class SelectionColumn<T, S> implements IStyledColumn<T, S> {

	private Set<IModel<T>> selections = new HashSet<IModel<T>>();

	public Component getHeader(String componentId) {
		return new AllSelector(componentId);
	}

	public S getSortProperty() {
		return null;
	}

	public boolean isSortable() {
		return false;
	}

	public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
		cellItem.add(new RowSelector(componentId, rowModel));
	}

	public void detach() {
	}

	public String getCssClass() {
		return "row-selector";
	}

	public Set<IModel<T>> getSelections() {
		return selections;
	}

	private class AllSelector extends Panel implements IMarkupResourceStreamProvider {

		public AllSelector(String id) {
			super(id);

			add(new WebMarkupContainer("checkbox") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);

					final boolean allChecked[] = new boolean[] { true };
					final boolean empty[] = new boolean[] { true };

					findParent(DataTable.class).visitChildren(
							RowSelector.class,
							new IVisitor<RowSelector, Void>() {

								public void component(RowSelector component, IVisit<Void> visit) {
									empty[0] = false;
									if (!selections.contains(component.getModel())) {
										allChecked[0] = false;
										visit.stop();
									}
								}

							});

					if (allChecked[0] && !empty[0])
						tag.put("checked", "true");
				}

			}.setOutputMarkupId(true));

			add(new AbstractDefaultAjaxBehavior() {

				@Override
				protected void respond(AjaxRequestTarget target) {
					final boolean checked = RequestCycle.get()
							.getRequest()
							.getQueryParameters()
							.getParameterValue("checked")
							.toBoolean();
					findParent(DataTable.class).visitChildren(
							RowSelector.class,
							new IVisitor<RowSelector, Void>() {

								public void component(RowSelector component,
										IVisit<Void> visit) {
									if (checked)
										selections.add(component.getModel());
									else
										selections.remove(component.getModel());
								}
							});

					onSelectionChange(target);
				}

				@Override
				public void renderHead(Component component, IHeaderResponse response) {
					super.renderHead(component, response);
					response.render(JavaScriptHeaderItem.forReference(WicketAjaxJQueryResourceReference.get()));

					String template = 
							"$('#%s').change(function() {\n" + 
							"	var checked = this.checked;\n" +
							"	$(this).closest('table').find('.row-selector input').each(function(){\n" +
							"		this.checked = checked;\n" + 
							"	});\n" + 
							"	%s" +
							"});";
					String script = String.format(
							template, 
							AllSelector.this.get("checkbox").getMarkupId(), 
							getCallbackFunctionBody(CallbackParameter.explicit("checked")));
					response.render(OnDomReadyHeaderItem.forScript(script));
				}

			});

		}

		public IResourceStream getMarkupResourceStream(
				MarkupContainer container, Class<?> containerClass) {
			String html = 
					"<wicket:panel>" +
					"	<input type='checkbox' wicket:id='checkbox'></input>" +
					"</wicket:panel>";
			return new StringResourceStream(html);
		}

	}

	private class RowSelector extends Panel implements IMarkupResourceStreamProvider {

		public RowSelector(String id, IModel<T> keyModel) {
			super(id, keyModel);

			add(new WebMarkupContainer("checkbox") {

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (selections.contains(getModel()))
						tag.put("checked", "true");
				}

			}.setOutputMarkupId(true));

			add(new AbstractDefaultAjaxBehavior() {

				@Override
				protected void respond(final AjaxRequestTarget target) {
					boolean checked = RequestCycle.get()
							.getRequest()
							.getQueryParameters()
							.getParameterValue("checked")
							.toBoolean();
					if (checked)
						selections.add(getModel());
					else
						selections.remove(getModel());

					findParent(DataTable.class).visitChildren(
							AllSelector.class,
							new IVisitor<AllSelector, Void>() {

								public void component(AllSelector object, IVisit<Void> visit) {
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

					CallbackParameter param = CallbackParameter.resolved("checked", "this.checked");
					CharSequence callbackFunc = getCallbackFunction(param);
					String template = "$('#%s').change(%s)";
					String checkboxMarkupId = RowSelector.this.get("checkbox").getMarkupId();
					String script = String.format(template, checkboxMarkupId, callbackFunc);
					response.render(OnDomReadyHeaderItem.forScript(script));
				}

			});
		}

		@SuppressWarnings("unchecked")
		public IModel<T> getModel() {
			return (IModel<T>) getDefaultModel();
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
			String html = 
					"<wicket:panel>" +
					"	<input type='checkbox' wicket:id='checkbox'></input>" +
					"</wicket:panel>";
			return new StringResourceStream(html);
		}

	}

	protected void onSelectionChange(AjaxRequestTarget target) {
		
	}
}
