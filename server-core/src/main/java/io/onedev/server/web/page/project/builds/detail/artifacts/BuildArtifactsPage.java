package io.onedev.server.web.page.project.builds.detail.artifacts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
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

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.FileInfo;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.resource.ArtifactResource;
import io.onedev.server.web.resource.ArtifactResourceReference;
import io.onedev.server.web.util.ConfirmClickModifier;

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
		
		List<IColumn<FileInfo, Void>> columns = new ArrayList<>();
		
		columns.add(new TreeColumn<FileInfo, Void>(Model.of("Name")));
		columns.add(new AbstractColumn<FileInfo, Void>(Model.of("Size")) {

			@Override
			public void populateItem(Item<ICellPopulator<FileInfo>> cellItem, String componentId, IModel<FileInfo> rowModel) {
				FileInfo file = rowModel.getObject();
				if (!file.isFile())
					cellItem.add(new Label(componentId, ""));
				else
					cellItem.add(new Label(componentId, FileUtils.byteCountToDisplaySize(file.getLength())));
			}
			
		});
		columns.add(new AbstractColumn<FileInfo, Void>(Model.of("Last Modified")) {

			@Override
			public void populateItem(Item<ICellPopulator<FileInfo>> cellItem, String componentId, IModel<FileInfo> rowModel) {
				FileInfo file = rowModel.getObject();
				cellItem.add(new Label(componentId, DateUtils.formatAge(new Date(file.getLastModified()))));
			}
			
		});
		if (SecurityUtils.canManage(getBuild())) {
			columns.add(new AbstractColumn<FileInfo, Void>(Model.of("")) {

				@Override
				public void populateItem(Item<ICellPopulator<FileInfo>> cellItem, String componentId, IModel<FileInfo> rowModel) {
					Fragment fragment = new Fragment(componentId, "deleteFrag", BuildArtifactsPage.this);
					AjaxLink<?> link = new AjaxLink<Void>("link") { 

						@Override
						public void onClick(AjaxRequestTarget target) {
							getBuildManager().deleteArtifact(getBuild(), rowModel.getObject().getPath());
							updateArtifacts(target);
						}
						
					};
					if (!rowModel.getObject().isFile())
						link.add(new ConfirmClickModifier("Do you really want to delete this directory?"));
					else
						link.add(new ConfirmClickModifier("Do you really want to delete this file?"));
					fragment.add(link);
					cellItem.add(fragment);
				}
				
			});
		}
		
		ITreeProvider<FileInfo> dataProvider = new ITreeProvider<FileInfo>() {

			@Override
			public void detach() {
			}

			@Override
			public Iterator<? extends FileInfo> getChildren(FileInfo node) {
				String artifactPath = node != null? node.getPath(): null;
				List<FileInfo> files = getBuildManager().listArtifacts(getBuild(), artifactPath);
				Collections.sort(files, new Comparator<FileInfo>() {

					@Override
					public int compare(FileInfo o1, FileInfo o2) {
						if (o1.isFile() && o2.isFile() || !o1.isFile() && !o2.isFile()) 
							return o1.getPath().compareTo(o2.getPath());
						else if (o1.isFile()) 
							return 1;
						else 
							return -1;
					}

				});
				return files.iterator();
			}
			
			@Override
			public Iterator<? extends FileInfo> getRoots() {
				return getBuild().getRootArtifacts().iterator();
			}

			@Override
			public boolean hasChildren(FileInfo node) {
				return !node.isFile() && getChildren(node).hasNext();
			}

			@Override
			public IModel<FileInfo> model(FileInfo object) {
				return Model.of(object);
			}
			
		};
		
		add(new TableTree<FileInfo, Void>("artifacts", columns, dataProvider, Integer.MAX_VALUE) {

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
			protected Item<FileInfo> newRowItem(String id, int index, IModel<FileInfo> model) {
				return new OddEvenItem<FileInfo>(id, index, model);
			}

			@Override
			public void expand(FileInfo file) {
				super.expand(file);
				
				String artifactPath = file != null? file.getPath(): null;
				List<FileInfo> files = getBuildManager().listArtifacts(getBuild(), artifactPath);
				if (files.size() == 1 && !files.get(0).isFile())
					expand(files.get(0));
			}

			@Override
			protected Component newContentComponent(String id, IModel<FileInfo> model) {
				Fragment fragment = new Fragment(id, "artifactFrag", BuildArtifactsPage.this);
				FileInfo file = model.getObject();
				WebMarkupContainer link;
				if (!file.isFile()) {
					link = new AjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (getState(file) == State.EXPANDED)
								collapse(file);
							else
								expand(file);
						}
						
					};
					link.add(new SpriteImage("icon", "folder"));
					link.add(AttributeAppender.append("class", "folder"));
				} else {
					PageParameters params = ArtifactResource.paramsOf(
							getBuild().getProject().getId(), getBuild().getNumber(), file.getPath()); 
					link = new ResourceLink<Void>("link", new ArtifactResourceReference(), params);
					link.add(new SpriteImage("icon", "file"));
					link.add(AttributeAppender.append("class", "file"));
				}
				String fileName = file.getPath();
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
