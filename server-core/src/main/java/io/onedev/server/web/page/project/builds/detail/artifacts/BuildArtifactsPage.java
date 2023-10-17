package io.onedev.server.web.page.project.builds.detail.artifacts;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.artifact.ArtifactInfo;
import io.onedev.server.util.artifact.DirectoryInfo;
import io.onedev.server.util.artifact.FileInfo;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.resource.ArtifactResource;
import io.onedev.server.web.resource.ArtifactResourceReference;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("serial")
public class BuildArtifactsPage extends BuildDetailPage {

	public BuildArtifactsPage(PageParameters params) {
		super(params);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("upload") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new ArtifactUploadPanel(id) {
					
					@Override
					public void onUploaded(AjaxRequestTarget target) {
						updateArtifacts(target);
						modal.close();
					}
					
					@Override
					public void onCancel(AjaxRequestTarget target) {
						modal.close();
					}
					
					@Override
					protected Build getBuild() {
						return BuildArtifactsPage.this.getBuild();
					}
					
				};
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getBuild()));
			}

		});
		
		List<IColumn<ArtifactInfo, Void>> columns = new ArrayList<>();
		
		columns.add(new TreeColumn<>(Model.of("Name")));
		columns.add(new AbstractColumn<>(Model.of("Size")) {

			@Override
			public void populateItem(Item<ICellPopulator<ArtifactInfo>> cellItem, String componentId, IModel<ArtifactInfo> rowModel) {
				ArtifactInfo artifact = rowModel.getObject();
				if (artifact instanceof DirectoryInfo) {
					cellItem.add(new Label(componentId, ""));
				} else {
					cellItem.add(new Label(
							componentId,
							FileUtils.byteCountToDisplaySize(((FileInfo) artifact).getLength())));
				}
			}

		});
		columns.add(new AbstractColumn<>(Model.of("Last Modified")) {

			@Override
			public void populateItem(Item<ICellPopulator<ArtifactInfo>> cellItem, String componentId, IModel<ArtifactInfo> rowModel) {
				ArtifactInfo artifact = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatAge(new Date(artifact.getLastModified()))));
			}

		});
		if (SecurityUtils.canManage(getBuild())) {
			columns.add(new AbstractColumn<>(Model.of("")) {

				@Override
				public void populateItem(Item<ICellPopulator<ArtifactInfo>> cellItem, String componentId, IModel<ArtifactInfo> rowModel) {
					Fragment fragment = new Fragment(componentId, "deleteFrag", BuildArtifactsPage.this);
					AjaxLink<?> link = new AjaxLink<Void>("link") {
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							String confirmMessage;
							if (rowModel.getObject() instanceof DirectoryInfo)
								confirmMessage = "Do you really want to delete this directory?";
							else
								confirmMessage = "Do you really want to delete this file?";
							attributes.getAjaxCallListeners().add(new ConfirmClickListener(confirmMessage));
						}

						@Override
						public void onClick(AjaxRequestTarget target) {
							getBuildManager().deleteArtifact(getBuild(), rowModel.getObject().getPath());
							updateArtifacts(target);
						}

					};
					fragment.add(link);
					cellItem.add(fragment);
				}

			});
		}
		
		ITreeProvider<ArtifactInfo> dataProvider = new ITreeProvider<ArtifactInfo>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends ArtifactInfo> getChildren(ArtifactInfo node) {
				String artifactPath = node != null? node.getPath(): null;
				DirectoryInfo directory = ((DirectoryInfo)getBuildManager()
						.getArtifactInfo(getBuild(), artifactPath));
				return directory.getChildren().iterator();
			}
			
			@Override
			public Iterator<? extends ArtifactInfo> getRoots() {
				return getBuild().getRootArtifacts().iterator();
			}

			@Override
			public boolean hasChildren(ArtifactInfo node) {
				return node instanceof DirectoryInfo && getChildren(node).hasNext();
			}

			@Override
			public IModel<ArtifactInfo> model(ArtifactInfo object) {
				return Model.of(object);
			}
			
		};
		
		add(new TableTree<ArtifactInfo, Void>("artifacts", columns, dataProvider, Integer.MAX_VALUE) {

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
			protected Item<ArtifactInfo> newRowItem(String id, int index, IModel<ArtifactInfo> model) {
				return new OddEvenItem<ArtifactInfo>(id, index, model);
			}

			@Override
			public void expand(ArtifactInfo artifact) {
				super.expand(artifact);
				
				String artifactPath = artifact != null? artifact.getPath(): null;
				DirectoryInfo directory = ((DirectoryInfo) getBuildManager()
						.getArtifactInfo(getBuild(), artifactPath));
				if (directory != null 
						&& directory.getChildren().size() == 1 
						&& directory.getChildren().get(0) instanceof DirectoryInfo) {
					expand(directory.getChildren().get(0));
				}
			}

			@Override
			protected Component newContentComponent(String id, IModel<ArtifactInfo> model) {
				Fragment fragment = new Fragment(id, "artifactFrag", BuildArtifactsPage.this);
				ArtifactInfo artifact = model.getObject();
				WebMarkupContainer link;
				if (artifact instanceof DirectoryInfo) {
					link = new AjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (getState(artifact) == State.EXPANDED)
								collapse(artifact);
							else
								expand(artifact);
						}
						
					};
					link.add(new SpriteImage("icon", "folder"));
					link.add(AttributeAppender.append("class", "folder"));
				} else {
					PageParameters params = ArtifactResource.paramsOf(
							getBuild().getProject().getId(), getBuild().getNumber(), artifact.getPath()); 
					link = new ResourceLink<Void>("link", new ArtifactResourceReference(), params);
					link.add(new SpriteImage("icon", "file"));
					link.add(AttributeAppender.append("class", "file"));
				}
				String fileName = artifact.getPath();
				if (fileName.contains("/"))
					fileName = StringUtils.substringAfterLast(fileName, "/");
				link.add(new Label("label", fileName));
				fragment.add(link);
				
				return fragment;
			}				
			
		});
		add(new WebMarkupContainer("noArtifacts") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getRootArtifacts().size() == 0);
			}
			
		}.setOutputMarkupPlaceholderTag(true));
	}
	
	private BuildManager getBuildManager() {
		return OneDev.getInstance(BuildManager.class);
	}

	private void updateArtifacts(AjaxRequestTarget target) {
		target.add(get("artifacts"));
		target.add(get("noArtifacts"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildArtifactsCssResourceReference()));
	}

}
