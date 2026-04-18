package io.onedev.server.web.component.indexedpath;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.service.support.PathInfo;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

/**
 * Reusable panel showing the number of paths backed by an indexed-path
 * directory (see {@link io.onedev.server.util.PathIndexUtils}). Clicking the
 * count opens a dropdown listing each path with its on-disk size.
 */
public class IndexedPathsPanel extends GenericPanel<List<PathInfo>> {

	private static final long serialVersionUID = 1L;

	public IndexedPathsPanel(String id, IModel<List<PathInfo>> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var indexedPaths = getModelObject();

		var link = new DropdownLink("link") {

			private static final long serialVersionUID = 1L;

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment fragment = new Fragment(id, "dropdownFrag", IndexedPathsPanel.this);
				fragment.add(new ListView<>("paths", indexedPaths) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(ListItem<PathInfo> item) {
						item.add(new Label("path", item.getModelObject().getPath()));
						item.add(new Label("size",
								FileUtils.byteCountToDisplaySize(item.getModelObject().getSize())));
					}

				});
				return fragment;
			}

		};
		link.add(new Label("label", Model.of(String.valueOf(indexedPaths.size()))));
		link.setEnabled(!indexedPaths.isEmpty());
		add(link);
	}

}
