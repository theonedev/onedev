package io.onedev.server.web.component.datatable.selectioncolumn;

import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

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
		return new HeaderSelector<T>(componentId) {

			@Override
			protected Set<IModel<T>> getSelections() {
				return selections;
			}

			@Override
			protected void onSelectionChange(AjaxRequestTarget target) {
				SelectionColumn.this.onSelectionChange(target);
			}
			
		};
	}

	public S getSortProperty() {
		return null;
	}

	public boolean isSortable() {
		return false;
	}

	public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
		cellItem.add(new RowSelector<T>(componentId, rowModel) {

			@Override
			protected Set<IModel<T>> getSelections() {
				return selections;
			}

			@Override
			protected void onSelectionChange(AjaxRequestTarget target) {
				SelectionColumn.this.onSelectionChange(target);
			}
			
		});
	}

	public void detach() {
	}

	public String getCssClass() {
		return "row-selector";
	}

	public Set<IModel<T>> getSelections() {
		return selections;
	}

	protected void onSelectionChange(AjaxRequestTarget target) {
	}
	
}
