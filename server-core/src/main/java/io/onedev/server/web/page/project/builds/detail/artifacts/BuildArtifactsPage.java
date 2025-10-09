package io.onedev.server.web.page.project.builds.detail.artifacts;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ChildrenAggregator;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.Pair;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.artifact.DirectoryInfo;
import io.onedev.server.util.artifact.FileInfo;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.resource.ArtifactResource;
import io.onedev.server.web.resource.ArtifactResourceReference;

public class BuildArtifactsPage extends BuildDetailPage {

	public BuildArtifactsPage(PageParameters params) {
		super(params);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		List<IColumn<Pair<String, ArtifactInfo>, Void>> columns = new ArrayList<>();
		
		columns.add(new TreeColumn<>(Model.of(_T("Name"))));
		columns.add(new AbstractColumn<>(Model.of(_T("Size"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Pair<String, ArtifactInfo>>> cellItem, String componentId, IModel<Pair<String, ArtifactInfo>> rowModel) {
				var artifact = rowModel.getObject().getRight();
				if (artifact instanceof DirectoryInfo) {
					cellItem.add(new Label(componentId, ""));
				} else {
					cellItem.add(new Label(
							componentId,
							FileUtils.byteCountToDisplaySize(((FileInfo) artifact).getLength())));
				}
			}

		});
		columns.add(new AbstractColumn<>(Model.of(_T("Last Modified"))) {

			@Override
			public void populateItem(Item<ICellPopulator<Pair<String, ArtifactInfo>>> cellItem, String componentId, IModel<Pair<String, ArtifactInfo>> rowModel) {
				ArtifactInfo artifact = rowModel.getObject().getRight();
				cellItem.add(new Label(componentId, DateUtils.formatAge(new Date(artifact.getLastModified()))));
			}

		});
		if (SecurityUtils.canManageBuild(getBuild())) {
			columns.add(new AbstractColumn<>(Model.of("")) {

				@Override
				public void populateItem(Item<ICellPopulator<Pair<String, ArtifactInfo>>> cellItem, String componentId, IModel<Pair<String, ArtifactInfo>> rowModel) {
					Fragment fragment = new Fragment(componentId, "deleteFrag", BuildArtifactsPage.this);
					AjaxLink<?> link = new AjaxLink<Void>("link") {
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							String confirmMessage;
							if (rowModel.getObject().getRight() instanceof DirectoryInfo)
								confirmMessage = _T("Do you really want to delete this directory?");
							else
								confirmMessage = _T("Do you really want to delete this file?");
							attributes.getAjaxCallListeners().add(new ConfirmClickListener(confirmMessage));
						}

						@Override
						public void onClick(AjaxRequestTarget target) {
							buildService.deleteArtifact(getBuild(), rowModel.getObject().getRight().getPath());
							if (getBuild().getRootArtifacts().size() != 0)
								updateArtifacts(target);
							else
								setResponsePage(BuildDashboardPage.class, BuildDashboardPage.paramsOf(getBuild()));
						}

					};
					fragment.add(link);
					cellItem.add(fragment);
				}

			});
		}
		
		var childrenAggregator = new ChildrenAggregator<ArtifactInfo>() {

			@Override
			protected List<ArtifactInfo> getChildren(ArtifactInfo node) {
				if (node != null) {
					if (node instanceof DirectoryInfo) {
						var directoryNode = (DirectoryInfo) node;
						if (directoryNode.getChildren() != null) {
							return directoryNode.getChildren();
						} else {
							directoryNode = ((DirectoryInfo) buildService
									.getArtifactInfo(getBuild(), directoryNode.getPath()));
							return directoryNode.getChildren();
						}
					} else {
						return new ArrayList<>();
					}
				} else {
					return getBuild().getRootArtifacts();
				}
			}
		};
		ITreeProvider<Pair<String, ArtifactInfo>> dataProvider = new ITreeProvider<>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends Pair<String, ArtifactInfo>> getChildren(Pair<String, ArtifactInfo> node) {
				return childrenAggregator.getAggregatedChildren(node.getRight()).stream().map(it->new Pair<>(node.getRight().getPath(), it)).iterator();
			}

			@Override
			public Iterator<? extends Pair<String, ArtifactInfo>> getRoots() {
				return childrenAggregator.getAggregatedChildren(null).stream().map(it->new Pair<String, ArtifactInfo>(null, it)).iterator();
			}

			@Override
			public boolean hasChildren(Pair<String, ArtifactInfo> node) {
				return node.getRight() instanceof DirectoryInfo;
			}

			@Override
			public IModel<Pair<String, ArtifactInfo>> model(Pair<String, ArtifactInfo> object) {
				return Model.of(object);
			}

		};
		
		add(new TableTree<Pair<String, ArtifactInfo>, Void>("artifacts", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getTable().addTopToolbar(new HeadersToolbar<Void>(getTable(), null));
				getTable().addBottomToolbar(new NoRecordsToolbar(getTable()));
				getTable().add(new NoRecordsBehavior());
				getTable().add(AttributeAppender.append("class", "table"));
				add(new HumanTheme());
				expand(null);
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getRootArtifacts().size() != 0);
			}

			@Override
			protected Item<Pair<String, ArtifactInfo>> newRowItem(String id, int index, IModel<Pair<String, ArtifactInfo>> model) {
				return new OddEvenItem<>(id, index, model);
			}

			@Override
			protected Component newContentComponent(String id, IModel<Pair<String, ArtifactInfo>> model) {
				Fragment fragment = new Fragment(id, "artifactFrag", BuildArtifactsPage.this);
				Pair<String, ArtifactInfo> pair = model.getObject();
				WebMarkupContainer link;
				if (pair.getRight() instanceof DirectoryInfo) {
					link = new AjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (getState(pair) == State.EXPANDED)
								collapse(pair);
							else
								expand(pair);
						}

					};
					link.add(new SpriteImage("icon", "folder"));
					link.add(AttributeAppender.append("class", "folder"));
				} else {
					PageParameters params = ArtifactResource.paramsOf(
							getBuild().getProject().getId(), getBuild().getNumber(), pair.getRight().getPath());
					link = new ResourceLink<Void>("link", new ArtifactResourceReference(), params);
					link.add(new SpriteImage("icon", "file"));
					link.add(AttributeAppender.append("class", "file"));
				}
				var label = pair.getRight().getPath();
				if (pair.getLeft() != null)
					label = label.substring(pair.getLeft().length()+1);
				link.add(new Label("label", label));
				fragment.add(link);

				return fragment;
			}

		});
	}
	
	private void updateArtifacts(AjaxRequestTarget target) {
		target.add(get("artifacts"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildArtifactsCssResourceReference()));
	}

}
