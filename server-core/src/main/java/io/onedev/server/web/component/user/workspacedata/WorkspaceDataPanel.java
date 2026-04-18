package io.onedev.server.web.component.user.workspacedata;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.web.translation.Translation._T;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.User;
import io.onedev.server.service.UserService;
import io.onedev.server.service.support.PathInfo;
import io.onedev.server.util.PathIndexUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.indexedpath.IndexedPathsPanel;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

public class WorkspaceDataPanel extends Panel {

	@Inject
	private UserService userService;

	@Inject
	private ClusterService clusterService;

	private final Long userId;

	private DataTable<String, Void> dataTable;

	public WorkspaceDataPanel(String id, Long userId) {
		super(id);
		this.userId = userId;
	}

	private File getWorkspaceDataBaseDir() {
		return userService.getWorkspaceDataBaseDir(userId);
	}

	private List<String> getKeys() {
		var baseDir = getWorkspaceDataBaseDir();
		return Arrays.stream(baseDir.listFiles(File::isDirectory))
			.map(File::getName)
			.map(User::decodeWorkspaceDataKey)
			.sorted()
			.collect(Collectors.toList());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		List<IColumn<String, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<>(Model.of(_T("Key"))) {

			@Override
			public void populateItem(Item<ICellPopulator<String>> cellItem, String componentId,
									 IModel<String> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject()));
			}

		});

		columns.add(new AbstractColumn<>(Model.of(_T("#Paths"))) {

			@Override
			public void populateItem(Item<ICellPopulator<String>> cellItem, String componentId,
									 IModel<String> rowModel) {
				var key = rowModel.getObject();
				List<PathInfo> indexedPaths = read(User.getWorkspaceDataLockName(userId, key), () -> {
					var keyDir = userService.getWorkspaceDataDir(userId, key, false);
					if (!keyDir.exists())
						return new ArrayList<PathInfo>();
					return PathIndexUtils.listIndexedPaths(keyDir);
				});
				cellItem.add(new IndexedPathsPanel(componentId, Model.ofList(indexedPaths)));
			}

		});

		columns.add(new AbstractColumn<>(Model.of(_T("Last Updated"))) {

			@Override
			public void populateItem(Item<ICellPopulator<String>> cellItem, String componentId,
									 IModel<String> rowModel) {
				var key = rowModel.getObject();
				long lastModified = read(User.getWorkspaceDataLockName(userId, key), () -> {
					var keyDir = userService.getWorkspaceDataDir(userId, key, false);
					if (!keyDir.exists())
						return 0L;
					long maxLastModified = keyDir.lastModified();
					var children = keyDir.listFiles();
					for (var child : children) 
						maxLastModified = Math.max(maxLastModified, child.lastModified());		
					return maxLastModified;
				});
				if (lastModified > 0)
					cellItem.add(new Label(componentId, DateUtils.formatAge(new Date(lastModified))));
				else
					cellItem.add(new Label(componentId, "N/A"));
			}

		});

		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<String>> cellItem, String componentId,
									 IModel<String> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", WorkspaceDataPanel.this);

				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						var key = rowModel.getObject();
						var lockName = User.getWorkspaceDataLockName(userId, key);
						clusterService.runOnAllServers(() -> {
							write(lockName, () -> {
								var dataDir = userService.getWorkspaceDataDir(userId, key, false);
								FileUtils.deleteDir(dataDir);
							});
							return null;
						});
						Session.get().success(_T("Workspace data deleted"));
						target.add(dataTable);
					}

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(
								_T("Do you really want to delete this workspace data?")));
					}

				});

				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "minimum actions";
			}

		});

		SortableDataProvider<String, Void> dataProvider = new LoadableDetachableDataProvider<>() {

			@Override
			public Iterator<? extends String> iterator(long first, long count) {
				return getKeys().iterator();
			}

			@Override
			public long calcSize() {
				return getKeys().size();
			}

			@Override
			public IModel<String> model(String key) {
				return Model.of(key);
			}

		};

		add(dataTable = new DefaultDataTable<>("keys", columns, dataProvider,
				Integer.MAX_VALUE, null));
	}

}
