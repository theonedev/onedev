package com.pmease.gitop.web.page.project.source.commits;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;

import com.pmease.commons.git.Commit;
import com.pmease.gitop.web.common.datatype.DataTypes;
import com.pmease.gitop.web.common.wicket.component.datagrid.DataGrid;

@SuppressWarnings("serial")
public class CommitTable extends DataGrid<Commit> {

	private transient Date previous;
	
	public CommitTable(String id,
			List<? extends IColumn<Commit, String>> columns,
			IDataProvider<Commit> dataProvider, long rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);
	}

	@Override
	protected Item<Commit> newRowItem(String id, int index, final IModel<Commit> rowModel) {
		Item<Commit> item = new Item<Commit>(id, index, rowModel);
		item.add(new GroupBehavior());
		return item;
	}
	
	class GroupBehavior extends Behavior {
		
		@Override
		public void beforeRender(Component component) {
			Commit current = (Commit) component.getDefaultModelObject();
			
			Date currentDate = current.getCommitter().getDate();
			
			if (previous == null || 
					!DateUtils.isSameDay(previous, currentDate)) {
				component.getResponse().write(
						"<tr><td class='commit-group-head' colspan='" + CommitTable.this.getColumns().size() + "'><h3>"
								+ DataTypes.DATE.asString(currentDate, "MMMMM dd, yyyy")
								+ "</h3></td></tr>");
				previous = currentDate;
			}
		}
	}
}