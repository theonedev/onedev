package com.pmease.gitplex.web.component.depotfile.filenavigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmLeaveListener;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.PreventDefaultAjaxLink;
import com.pmease.commons.wicket.component.ViewStateAwareAjaxLink;
import com.pmease.commons.wicket.component.floating.AlignPlacement;
import com.pmease.commons.wicket.component.floating.FloatingPanel;
import com.pmease.commons.wicket.component.menu.MenuItem;
import com.pmease.commons.wicket.component.menu.MenuLink;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.BlobIcon;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobNameChangeCallback;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext.Mode;
import com.pmease.gitplex.web.page.depot.commit.DepotCommitsPage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.resource.BlobResource;
import com.pmease.gitplex.web.resource.BlobResourceReference;

@SuppressWarnings("serial")
public abstract class FileNavigator extends Panel {

	private final static String LAST_SEGMENT_ID = "lastSegment";
	
	private final BlobViewContext context;
	
	private final BlobNameChangeCallback callback;
	
	public FileNavigator(String id, BlobViewContext context, @Nullable BlobNameChangeCallback callback) {
		super(id);

		this.context = context;
		this.callback = callback;
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
				AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

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
						DepotFilePage.State state = new DepotFilePage.State();
						state.blobIdent = blobIdent;
						PageParameters params = DepotFilePage.paramsOf(context.getDepot(), state);
						tag.put("href", urlFor(DepotFilePage.class, params));
					}
					
				};
				link.setEnabled(!context.getBlobIdent().isTree() || item.getIndex() != getViewSize()-1);
				
				if (blobIdent.path != null) {
					if (blobIdent.path.indexOf('/') != -1)
						link.add(new Label("label", StringUtils.substringAfterLast(blobIdent.path, "/")));
					else
						link.add(new Label("label", blobIdent.path));
				} else {
					link.add(new Label("label", context.getDepot().getName()));
				}
				
				item.add(link);
				
				item.add(new DropdownLink("subtreeDropdownTrigger", AlignPlacement.bottom(6)) {

					@Override
					protected void onInitialize(FloatingPanel dropdown) {
						super.onInitialize(dropdown);
						dropdown.add(AttributeAppender.append("class", "subtree-dropdown"));
					}

					@Override
					protected Component newContent(String id) {
						return new NestedTree<BlobIdent>(id, new ITreeProvider<BlobIdent>() {

							@Override
							public void detach() {
							}

							@Override
							public Iterator<? extends BlobIdent> getRoots() {
								Repository repository = context.getDepot().getRepository();
								try (RevWalk revWalk = new RevWalk(repository)) {
									RevTree revTree = revWalk.parseCommit(getCommitId()).getTree();
									TreeWalk treeWalk;
									if (blobIdent.path != null) {
										treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(repository, blobIdent.path, revTree));
										treeWalk.enterSubtree();
									} else {
										treeWalk = new TreeWalk(repository);
										treeWalk.addTree(revTree);
									}
									treeWalk.setRecursive(false);
									
									List<BlobIdent> roots = new ArrayList<>();
									while (treeWalk.next()) 
										roots.add(new BlobIdent(context.getBlobIdent().revision, treeWalk.getPathString(), treeWalk.getRawMode(0)));
									Collections.sort(roots);
									return roots.iterator();
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}

							@Override
							public boolean hasChildren(BlobIdent blobIdent) {
								return blobIdent.isTree();
							}

							@Override
							public Iterator<? extends BlobIdent> getChildren(BlobIdent blobIdent) {
								return FileNavigator.this.getChildren(blobIdent).iterator();
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
								
								List<BlobIdent> children = getChildren(blobIdent);
								if (children.size() == 1 && children.get(0).isTree()) 
									expand(children.get(0));
							}

							@Override
							protected Component newContentComponent(String id, final IModel<BlobIdent> model) {
								BlobIdent blobIdent = model.getObject();
								Fragment fragment = new Fragment(id, "treeNodeFrag", FileNavigator.this);

								fragment.add(new BlobIcon("icon", model));
								
								AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>("link") {

									@Override
									protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
										super.updateAjaxAttributes(attributes);
										attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
									}
									
									@Override
									public void onClick(AjaxRequestTarget target) {
										context.onSelect(target, model.getObject(), null);
										close();
									}

									@Override
									protected void onComponentTag(ComponentTag tag) {
										super.onComponentTag(tag);
										
										DepotFilePage.State state = new DepotFilePage.State();
										state.blobIdent = model.getObject();
										PageParameters params = DepotFilePage.paramsOf(context.getDepot(), state);
										tag.put("href", urlFor(DepotFilePage.class, params));
									}
									
								};
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
		if (callback != null) {
			lastSegment = new Fragment(LAST_SEGMENT_ID, "nameEditFrag", this);
			
			String name;
			if (file.isTree())
				name = "";
			else if (file.path.contains("/"))
				name = StringUtils.substringAfterLast(file.path, "/");
			else
				name = file.path;
			
			final TextField<String> nameInput = new TextField<String>("name", Model.of(name));
			lastSegment.add(nameInput);
			nameInput.add(new OnChangeAjaxBehavior() {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					callback.onChange(target, nameInput.getInput());
				}
				
			});
			if (name.length() == 0)
				nameInput.add(AttributeAppender.append("class", "focusable"));
			lastSegment.add(AttributeAppender.append("class", "input"));
		} else if (file.isFile()) {
			lastSegment = new Fragment(LAST_SEGMENT_ID, "blobNameFrag", this);
			
			String blobName = file.path;
			if (blobName.contains("/"))
				blobName = StringUtils.substringAfterLast(blobName, "/");
			lastSegment.add(new Label("label", blobName));
		} else {
			lastSegment = new WebMarkupContainer(LAST_SEGMENT_ID);
		}
		add(lastSegment);
		
		if (callback == null) {
			add(new MenuLink("fileMenu") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new IAjaxCallListener() {
						
						@Override
						public CharSequence getSuccessHandler(Component component) {
							return null;
						}
						
						@Override
						public CharSequence getPrecondition(Component component) {
							return null;
						}
						
						@Override
						public CharSequence getInitHandler(Component component) {
							return null;
						}
						
						@Override
						public CharSequence getFailureHandler(Component component) {
							return null;
						}
						
						@Override
						public CharSequence getDoneHandler(Component component) {
							return null;
						}
						
						@Override
						public CharSequence getCompleteHandler(Component component) {
							return null;
						}
						
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						
						@Override
						public CharSequence getBeforeHandler(Component component) {
							return "$('.autofit:visible').first().trigger('storeViewState');";
						}
						
						@Override
						public CharSequence getAfterHandler(Component component) {
							return null;
						}
						
					});
				}

				@Override
				protected List<MenuItem> getMenuItems() {
					List<MenuItem> generalItems = new ArrayList<>();
					generalItems.add(new MenuItem() {
						
						@Override
						public String getIconClass() {
							return "fa-history";
						}

						@Override
						public String getLabel() {
							return "History";
						}

						@Override
						public AbstractLink newLink(String id) {
							return new Link<Void>(id) {

								@Override
								public void onClick() {
									DepotCommitsPage.State state = new DepotCommitsPage.State();
									String commitHash = context.getDepot().getObjectId(file.revision).name();
									state.setCompareWith(commitHash);
									if (file.path != null) 
										state.setQuery(String.format("path(%s)", file.path));
									setResponsePage(DepotCommitsPage.class, DepotCommitsPage.paramsOf(context.getDepot(), state));
								}
								
							};
						}
						
					});
					if (file.isFile()) {
						generalItems.add(new MenuItem() {
		
							@Override
							public String getIconClass() {
								return "fa-file-text-o";
							}
		
							@Override
							public String getLabel() {
								return "Raw";
							}
		
							@Override
							public AbstractLink newLink(String id) {
								return new ResourceLink<Void>(id, new BlobResourceReference(), 
										BlobResource.paramsOf(context.getDepot(), file)) {
		
									@Override
									protected CharSequence getOnClickScript(CharSequence url) {
										return closeBeforeClick(super.getOnClickScript(url));
									}
									
								};
							}
							
						});
					}
					
					String path = file.path;
					if (path != null && file.isTree())
						path += "/";
					
					List<MenuItem> changeItems = new ArrayList<>();
					if (file.isTree() && context.isOnBranch() && SecurityUtils.canModify(context.getDepot(), file.revision, path)) {
						changeItems.add(new MenuItem() {

							@Override
							public String getIconClass() {
								return "fa-plus";
							}

							@Override
							public String getLabel() {
								return "Create new file";
							}

							@Override
							public AbstractLink newLink(String id) {
								return new AjaxLink<Void>(id) {

									@Override
									protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
										super.updateAjaxAttributes(attributes);
										attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
									}
									
									@Override
									public void onClick(AjaxRequestTarget target) {
										close();
										onNewFile(target);
									}
									
								};
							}
							
						});
					}

					if (file.isFile() && SecurityUtils.canModify(context.getDepot(), file.revision, path)) {
						CodeComment comment = context.getOpenComment();
						if (context.isOnBranch() 
								|| comment != null && comment.getBranchRef() != null && context.getDepot().getObjectId(comment.getBranchRef(), false) != null) {
							if (context.getDepot().getBlob(file).getText() != null) {
								changeItems.add(new MenuItem() {
		
									@Override
									public String getIconClass() {
										return "fa-pencil";
									}
		
									@Override
									public String getLabel() {
										return "Edit";
									}
		
									@Override
									public WebMarkupContainer newLink(String id) {
										if (context.isOnBranch()) {
											AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>(id) {
		
												@Override
												protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
													super.updateAjaxAttributes(attributes);
													attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
												}
												
												@Override
												public void onClick(AjaxRequestTarget target, String viewState) {
													close();
													RequestCycle.get().setMetaData(DepotFilePage.VIEW_STATE_KEY, viewState);
													context.onEdit(target);
												}
											};
											link.add(AttributeAppender.append("title", "Edit on branch " + context.getBlobIdent().revision));
											return link;
										} else {
											CodeComment comment = Preconditions.checkNotNull(context.getOpenComment());
											PageParameters params;
											DepotFilePage.State state = new DepotFilePage.State();
											state.blobIdent.revision = GitUtils.ref2branch(context.getOpenComment().getBranchRef());
											state.blobIdent.path = context.getBlobIdent().path;
											state.commentId = comment.getId();
											state.requestId = PullRequest.idOf(context.getPullRequest());
											state.mode = Mode.EDIT;
											params = DepotFilePage.paramsOf(context.getDepot(), state);
											Link<Void> link = new BookmarkablePageLink<Void>(id, DepotFilePage.class, params);
											link.add(AttributeAppender.append("title", "Edit on branch " + state.blobIdent.revision));
											return link;
										}
									}
								});
							}
							
							changeItems.add(new MenuItem() {

								@Override
								public String getIconClass() {
									return "fa-trash";
								}

								@Override
								public String getLabel() {
									return "Delete";
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									if (context.isOnBranch()) {
										AjaxLink<Void> link = new PreventDefaultAjaxLink<Void>(id) {

											@Override
											protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
												super.updateAjaxAttributes(attributes);
												attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
											}
											
											@Override
											public void onClick(AjaxRequestTarget target) {
												close();
												context.onDelete(target);
											}

										};
										link.add(AttributeAppender.append("title", "Delete from branch " + context.getBlobIdent().revision));
										return link;
									} else {
										DepotFilePage.State state = new DepotFilePage.State();
										state.blobIdent.revision = GitUtils.ref2branch(context.getOpenComment().getBranchRef());
										state.blobIdent.path = context.getBlobIdent().path;
										state.commentId = comment.getId();
										state.requestId = PullRequest.idOf(context.getPullRequest());
										state.mode = Mode.DELETE;
										PageParameters params = DepotFilePage.paramsOf(context.getDepot(), state);
										Link<Void> link = new BookmarkablePageLink<Void>(id, DepotFilePage.class, params);
										link.add(AttributeAppender.append("title", "Delete from branch " + state.blobIdent.revision));
										return link;
									}
								}
								
							});
						} 
					}

					List<MenuItem> customItems = context.getMenuItems(this);
					
					List<MenuItem> menuItems = new ArrayList<>(generalItems);
					if (!changeItems.isEmpty()) {
						if (!menuItems.isEmpty()) 
							menuItems.add(null); // add separator
						menuItems.addAll(changeItems);
					}
					if (!customItems.isEmpty()) {
						if (!menuItems.isEmpty()) 
							menuItems.add(null); // add separator
						menuItems.addAll(customItems);
					}
					return menuItems;
				}
				
			});			
		} else {
			add(new WebMarkupContainer("fileMenu").setVisible(false));
		}
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new FileNavigatorResourceReference()));
		
		if (context.getBlobIdent().isTree()) {
			String script = String.format("$('#%s input[type=text]').focus();", getMarkupId());
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
		
		String script = String.format("$('#%s form').submit(false);", getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	private ObjectId getCommitId() {
		return context.getDepot().getObjectId(context.getBlobIdent().revision);
	}

	protected abstract void onNewFile(AjaxRequestTarget target);
	
	private List<BlobIdent> getChildren(BlobIdent blobIdent) {
		Repository repository = context.getDepot().getRepository();
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevTree revTree = revWalk.parseCommit(getCommitId()).getTree();
			TreeWalk treeWalk = TreeWalk.forPath(repository, blobIdent.path, revTree);
			treeWalk.setRecursive(false);
			treeWalk.enterSubtree();
			
			List<BlobIdent> children = new ArrayList<>();
			while (treeWalk.next()) 
				children.add(new BlobIdent(context.getBlobIdent().revision, treeWalk.getPathString(), treeWalk.getRawMode(0)));
			Collections.sort(children);
			return children;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
