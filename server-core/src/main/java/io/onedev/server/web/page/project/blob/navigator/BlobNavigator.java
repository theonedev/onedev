package io.onedev.server.web.page.project.blob.navigator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.ajax.AjaxChannel.Type;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Splitter;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.GitUtils;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.blob.BlobIcon;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext.Mode;

@SuppressWarnings("serial")
public class BlobNavigator extends Panel {

	private final static String LAST_SEGMENT_ID = "lastSegment";
	
	private final BlobRenderContext context;
	
	private TextField<String> nameInput;
	
	public BlobNavigator(String id, BlobRenderContext context) {
		super(id);

		this.context = context;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ListView<BlobIdent>("treeSegments", new LoadableDetachableModel<List<BlobIdent>>() {

			@Override
			protected List<BlobIdent> load() {
				BlobIdent file = context.getBlobIdent();
				List<BlobIdent> treeSegments = new ArrayList<>();
				treeSegments.add(new BlobIdent(file.revision, null, FileMode.TREE.getBits()));
				
				if (file.path != null) {
					List<String> segments = Splitter.on('/').omitEmptyStrings().splitToList(file.path);
					
					for (int i=0; i<segments.size(); i++) { 
						BlobIdent parent = treeSegments.get(treeSegments.size()-1);
						int treeMode = FileMode.TREE.getBits();
						if (i<segments.size()-1 || file.mode == treeMode) {
							String treePath = segments.get(i);
							if (parent.path != null)
								treePath = parent.path + "/" + treePath;
							treeSegments.add(new BlobIdent(file.revision, treePath, treeMode));
						}
					}
				}
				
				return treeSegments;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<BlobIdent> item) {
				final BlobIdent blobIdent = item.getModelObject();
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}
					
					@Override
					public void onClick(AjaxRequestTarget target) {
						context.onSelect(target, blobIdent, null);
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
						PageParameters params = ProjectBlobPage.paramsOf(context.getProject(), state);
						tag.put("href", urlFor(ProjectBlobPage.class, params));
					}
					
				};
				link.setEnabled(!context.getBlobIdent().isTree() || item.getIndex() != getViewSize()-1);
				
				if (blobIdent.path != null) {
					if (blobIdent.path.indexOf('/') != -1)
						link.add(new Label("label", StringUtils.substringAfterLast(blobIdent.path, "/")));
					else
						link.add(new Label("label", blobIdent.path));
				} else {
					link.add(new Label("label", "ROOT"));
				}
				
				item.add(link);
				
				item.add(new DropdownLink("subtreeDropdownTrigger", AlignPlacement.bottom(6)) {

					@Override
					protected void onInitialize(FloatingPanel dropdown) {
						super.onInitialize(dropdown);
						dropdown.add(AttributeAppender.append("class", "subtree-dropdown"));
					}

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new NestedTree<BlobIdent>(id, new ITreeProvider<BlobIdent>() {

							@Override
							public void detach() {
							}

							@Override
							public Iterator<? extends BlobIdent> getRoots() {
								if (blobIdent.revision != null)
									return context.getProject().getChildren(blobIdent, BlobIdentFilter.ALL).iterator();
								else
									return new ArrayList<BlobIdent>().iterator();
							}

							@Override
							public boolean hasChildren(BlobIdent blobIdent) {
								return blobIdent.isTree();
							}

							@Override
							public Iterator<? extends BlobIdent> getChildren(BlobIdent blobIdent) {
								return context.getProject().getChildren(blobIdent, BlobIdentFilter.ALL).iterator();
							}

							@Override
							public IModel<BlobIdent> model(BlobIdent blobIdent) {
								return Model.of(blobIdent);
							}
							
						}) {

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(new HumanTheme());
							}

							@Override
							public void expand(BlobIdent blobIdent) {
								super.expand(blobIdent);
								
								List<BlobIdent> children = context.getProject().getChildren(blobIdent, BlobIdentFilter.ALL);
								if (children.size() == 1 && children.get(0).isTree()) 
									expand(children.get(0));
							}

							@Override
							protected Component newContentComponent(String id, final IModel<BlobIdent> model) {
								BlobIdent blobIdent = model.getObject();
								Fragment fragment = new Fragment(id, "treeNodeFrag", BlobNavigator.this);

								AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("link") {

									@Override
									protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
										super.updateAjaxAttributes(attributes);
										attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
									}
									
									@Override
									public void onClick(AjaxRequestTarget target) {
										context.onSelect(target, model.getObject(), null);
										dropdown.close();
									}

									@Override
									protected void onComponentTag(ComponentTag tag) {
										super.onComponentTag(tag);
										
										ProjectBlobPage.State state = new ProjectBlobPage.State(model.getObject());
										PageParameters params = ProjectBlobPage.paramsOf(context.getProject(), state);
										tag.put("href", urlFor(ProjectBlobPage.class, params));
									}
									
								};
								
								link.add(new BlobIcon("icon", model));
								
								if (blobIdent.path.indexOf('/') != -1)
									link.add(new Label("label", StringUtils.substringAfterLast(blobIdent.path, "/")));
								else
									link.add(new Label("label", blobIdent.path));
								fragment.add(link);
								
								return fragment;
							}
							
						};		
					}
					
				});
			}
			
		});
		
		WebMarkupContainer lastSegment;
		BlobIdent file = context.getBlobIdent();
		if (context.getMode() == Mode.EDIT || context.getMode() == Mode.ADD) {
			lastSegment = new Fragment(LAST_SEGMENT_ID, "nameEditFrag", this);
			
			Form<?> form = new Form<Void>("form");
			lastSegment.add(form);
			String name;
			if (context.getMode() == Mode.ADD) 
				name = context.getInitialNewPath();
			else if (file.path.contains("/"))
				name = StringUtils.substringAfterLast(file.path, "/");
			else if (file.path.equals(".onedev-buildspec"))
				name = BuildSpec.BLOB_PATH;
			else
				name = file.path;
			
			nameInput = new TextField<String>("input", Model.of(name));
			form.add(nameInput);
			nameInput.add(new AjaxFormSubmitBehavior(form, "input") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.setChannel(new AjaxChannel("blob-name-edit", Type.DROP));
				}

				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					super.onSubmit(target);
					send(getPage(), Broadcast.BREADTH, new BlobNameChanging(target));
				}
				
			});
			
			if (context.getMode() != Mode.ADD)
				nameInput.add(AttributeAppender.append("class", "no-autofocus"));
			lastSegment.add(AttributeAppender.append("class", "input"));
		} else if (file.isTree()) {
			lastSegment = new WebMarkupContainer(LAST_SEGMENT_ID);
			lastSegment.setVisible(false);
		} else {
			lastSegment = new Fragment(LAST_SEGMENT_ID, "blobNameFrag", this);
			
			String blobName = file.path;
			if (blobName.contains("/"))
				blobName = StringUtils.substringAfterLast(blobName, "/");
			lastSegment.add(new Label("label", blobName));
		} 
		add(lastSegment);
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BlobNavigatorResourceReference()));
		
		if (context.getBlobIdent().isTree()) {
			String script = String.format("$('#%s input[type=text]').focus();", getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
		
		String script = String.format("$('#%s form').submit(false);", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Nullable
	public String getNewPath() {
		if (nameInput != null) {
			String newPath;
			String name = nameInput.getModelObject();
			if (StringUtils.isNotBlank(name)) {
				if (context.getBlobIdent().isTree()) {
					if (context.getBlobIdent().path != null)
						newPath = context.getBlobIdent().path + "/" + name;
					else
						newPath = name;
				} else {
					if (context.getBlobIdent().path.contains("/"))
						newPath = StringUtils.substringBeforeLast(context.getBlobIdent().path, "/") + "/" + name;
					else
						newPath = name;
				}
				return GitUtils.normalizePath(newPath);
			} else {
				return null;
			}
		} else {
			throw new IllegalStateException();
		}
	}
	
}
