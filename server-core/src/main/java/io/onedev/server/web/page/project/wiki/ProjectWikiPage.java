package io.onedev.server.web.page.project.wiki;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.jspecify.annotations.Nullable;
import org.eclipse.jgit.lib.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.OneDev;
import io.onedev.server.validation.validator.PathValidator;
import io.onedev.server.exception.NotFoundException;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.exception.BlobEditException;
import io.onedev.server.git.exception.NotTreeException;
import io.onedev.server.git.exception.ObjectAlreadyExistsException;
import io.onedev.server.git.exception.ObsoleteCommitException;
import io.onedev.server.git.service.GitService;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.code.ConventionalCommitChecker;
import io.onedev.server.search.commit.PathCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.FileExtension;
import io.onedev.server.util.FilenameUtils;
import io.onedev.server.util.RevisionAndPath;
import io.onedev.server.util.UrlUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.BlobMarkdownEditor;
import io.onedev.server.web.component.markdown.BlobSelectionSupport;
import io.onedev.server.web.component.markdown.BlobUploadSupport;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.revision.RevisionPicker;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.BlobUploadPanel;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.overview.ProjectOverviewPage;
import io.onedev.server.web.upload.FileUpload;
import io.onedev.server.web.util.DefaultCommitMessage;
import io.onedev.server.web.util.DefaultCommitMessage.Operation;
import io.onedev.server.web.util.WikiLinkResolver;
import io.onedev.server.web.util.WikiUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

public class ProjectWikiPage extends ProjectPage {
	private String revision;
	private String page;
	private final @Nullable String folder;
	private final boolean editing;
	private final boolean creating;
	private String returnPage;
	private ObjectId commitId;
	private WebMarkupContainer actions;

	private WebMarkupContainer navigation;

	private BeanEditor pageEditor;

	private AbstractPostAjaxBehavior navigateBehavior;

	public ProjectWikiPage(PageParameters params) {
		super(params);
		List<String> segments = new ArrayList<>();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String segment = params.get(i).toString();
			if (!segment.isEmpty())
				segments.add(segment);
		}
		RevisionAndPath revisionAndPath;
		if (getProject().getDefaultBranch() == null) {
			// Empty repositories have no revision to prefix page paths with yet.
			revisionAndPath = new RevisionAndPath(null, segments.isEmpty() ? null : String.join("/", segments));
		} else {
			revisionAndPath = RevisionAndPath.parse(getProject(), segments);
		}
		revision = revisionAndPath.getRevision();
		page = revisionAndPath.getPath() != null ? revisionAndPath.getPath() : "Home";
		folder = getProject().getWikiFolder().getPath();
		WikiUtils.pagePath(folder, page);
		editing = params.get("edit").toBoolean(false);
		creating = params.get("new").toBoolean(false);
		returnPage = creating || editing && page.equals("_Sidebar")
				? params.get("return-page").toString("Home") : page;
		WikiUtils.pagePath(folder, returnPage);
	}

	private GitService git() {
		return OneDev.getInstance(GitService.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(navigateBehavior = new AbstractPostAjaxBehavior() {
			@Override
			protected void respond(AjaxRequestTarget target) {
				var params = RequestCycle.get().getRequest().getRequestParameters();
				String destination = params.getParameterValue("page").toString();
				String hash = params.getParameterValue("hash").toString("");
				if (!hash.isEmpty() && !hash.startsWith("#"))
					throw new IllegalArgumentException("Invalid heading anchor");
				selectPage(target, destination);
				String url = urlFor(ProjectWikiPage.class, paramsOf(getProject(), revision, page)).toString() + hash;
				pushState(target, JavaScriptEscape.escapeJavaScript(url), page);
				target.appendJavaScript("onedev.server.wiki.scrollToHeading('"
						+ JavaScriptEscape.escapeJavaScript(hash) + "', true);");
			}
		});
		if (revision != null)
			commitId = getProject().getObjectId(revision, true).copy();
		String path = WikiUtils.pagePath(folder, page);
		String content = read(page);
		if ((editing || creating) && !canEdit(path))
			throw new UnauthorizedException();
		if (commitId != null && content == null && !page.equals("Home") && !editing && !creating)
			throw new NotFoundException("Wiki page not found: " + page);

		boolean missingHome = content == null && !editing && !creating;
		WebMarkupContainer empty = new WebMarkupContainer("empty");
		empty.setVisible(getProject().getDefaultBranch() == null && !editing && !creating);
		empty.add(link("addHome", "Home", true, false)
				.setVisible(canEdit(WikiUtils.pagePath(folder, "Home"))));
		add(empty);

		WebMarkupContainer wiki = new WebMarkupContainer("wiki");
		wiki.setVisible(!empty.isVisible());
		wiki.setOutputMarkupId(true);
		add(wiki);
		wiki.add(new RevisionPicker("revision", new LoadableDetachableModel<Project>() {
			@Override
			protected Project load() {
				return getProject();
			}
		}, revision, revision != null) {
			@Override
			protected void onSelect(AjaxRequestTarget target, String selected) {
				setResponsePage(ProjectWikiPage.class, paramsOf(getProject(), selected, page));
			}
		}.setVisible(commitId != null));
		wiki.add(new Label("title", creating ? _T("Add new page") : getWikiPageTitle()).setOutputMarkupId(true));
		var subtitle = new WebMarkupContainer("subtitle") {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!creating && folderOf(page) != null);
			}
		};
		subtitle.setOutputMarkupPlaceholderTag(true);
		subtitle.add(new Label("folder", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return folderOf(page);
			}
		}));
		wiki.add(subtitle);
		wiki.add(newActions(path, content));
		WebMarkupContainer homeNotFound = new WebMarkupContainer("homeNotFound");
		homeNotFound.setVisible(missingHome);
		homeNotFound.setOutputMarkupPlaceholderTag(true);
		homeNotFound.add(link("addHome", "Home", true, false)
				.setVisible(canEdit(WikiUtils.pagePath(folder, "Home"))));
		wiki.add(homeNotFound);

		WebMarkupContainer view = new WebMarkupContainer("view") {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				if (!editing && !creating && commitId != null)
					response.render(OnDomReadyHeaderItem.forScript(
							"onedev.server.wiki.init('" + navigation.getMarkupId() + "', "
							+ navigateBehavior.getCallbackFunction(
									CallbackParameter.explicit("page"),
									CallbackParameter.explicit("hash")) + ", '"
							+ Base64.encodeBase64String(CryptoUtils.encrypt(
									SerializationUtils.serialize(page))) + "');"));
			}
		};
		view.setVisible(!editing && !creating && !missingHome);
		view.setOutputMarkupPlaceholderTag(true);
		view.add(viewer("body", content != null ? content : _T("This wiki page does not exist at this revision.")));
		view.add(link("addHome", "Home", true, false).setVisible(read("Home") == null
				&& canEdit(WikiUtils.pagePath(folder, "Home"))));
		navigation = new WebMarkupContainer("navigation");
		navigation.setOutputMarkupId(true);
		view.add(navigation);
		String sidebar = read("_Sidebar");
		navigation.add(viewer("sidebar", sidebar).setVisible(sidebar != null).setOutputMarkupPlaceholderTag(true));
		navigation.add(new Label("currentOutline", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return outline(read(page), "");
			}
		}).setEscapeModelStrings(false).setVisible(sidebar == null).setOutputMarkupPlaceholderTag(true));
		navigation.add(newSidebarEdit());
		navigation.add(newUseDefault(sidebar));
		List<String> pages = new ArrayList<>();
		if (commitId != null)
			collectPages(folder, pages);
		Collections.sort(pages);
		navigation.add(new ListView<String>("pages", pages) {
			@Override
			protected void populateItem(ListItem<String> item) {
				String name = item.getModelObject();
				var link = link("page", name, false, false);
				link.add(AttributeModifier.replace("data-wiki-page", name));
				link.add(new Label("name", name.replace('-', ' ')));
				if (name.equals(page))
					link.add(AttributeModifier.replace("aria-current", "page"));
				item.add(link);
				var outline = new Label("outline", new LoadableDetachableModel<String>() {
					@Override
					protected String load() {
						String url = urlFor(ProjectWikiPage.class,
								paramsOf(getProject(), revision, name)).toString();
						return outline(read(name), url);
					}
				});
				outline.setEscapeModelStrings(false).setVisible(false).setOutputMarkupPlaceholderTag(true);
				item.add(outline);
				item.add(new AjaxLink<Void>("expand") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						outline.setVisible(!outline.isVisible());
						target.add(outline, this);
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.put("aria-expanded", String.valueOf(outline.isVisible()));
					}
				});
			}
		});
		wiki.add(view);

		var bean = new WikiEditBean();
		bean.setName(creating ? getPageParameters().get("initial-name").toString("") : page);
		bean.setContent(creating || content == null ? "" : content);
		pageEditor = BeanContext.edit("editor", bean);
		Form<Void> form = new Form<>("form");
		form.add(pageEditor);
		form.add(new Button("save") {
			@Override
			public void onSubmit() {
				save(form, bean.getName(), bean.getContent(),
						bean.getCommitMessage(), false, content != null);
			}
		});
		form.setVisible(editing || creating);
		form.setOutputMarkupId(true);
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(link("cancel", returnPage, false, false));
		wiki.add(form);
	}

	private String getEditedPageName() {
		var bean = (WikiEditBean) pageEditor.getConvertedInput();
		if (bean == null)
			bean = (WikiEditBean) pageEditor.getModelObject();
		return bean.getName();
	}

	public BlobMarkdownEditor newContentEditor(String id, IModel<byte[]> text) {
		String path = WikiUtils.pagePath(folder, page);
		return new BlobMarkdownEditor(id, text, null) {
			private String getEditorPath() {
				String pageName = getEditedPageName();
				if (pageName != null && !pageName.isBlank()) {
					String normalized = pageName.trim().replace(' ', '-');
					if (PathValidator.checkPath(io.onedev.server.annotation.Path.Type.RELATIVE, normalized) == null)
						return WikiUtils.pagePath(folder, normalized);
				}
				return path;
			}

			private String getRelativeUrl(String selectedPath) {
				String relativePath = PathUtils.relativize(folderOf(getEditorPath()), selectedPath);
				return java.util.Arrays.stream(relativePath.split("/"))
						.map(UrlUtils::encodePath).collect(java.util.stream.Collectors.joining("/"));
			}

			@Override
			protected Project getProject() {
				return ProjectWikiPage.this.getProject();
			}

			@Override
			protected String getAutosaveKey() {
				String pageName = creating ? getEditedPageName() : page;
				if (pageName == null || pageName.isBlank())
					return null;
				try {
					return io.onedev.server.web.page.project.blob.render.BlobRenderContext.getAutosaveKey(
							getProject(), WikiUtils.pagePath(folder, creating ? pageName.trim().replace(' ', '-') : pageName));
				} catch (IllegalArgumentException | io.onedev.server.exception.NotAcceptableException e) {
					return null;
				}
			}

			@Override
			public BlobUploadSupport getRepositoryUploadSupport() {
				return new BlobUploadSupport() {
					@Override
					public Project getProject() {
						return ProjectWikiPage.this.getProject();
					}

					@Override
					public BlobIdent getBlobIdent() {
						return new BlobIdent(commitId != null ? commitId.name() : null, folder, FileMode.TREE.getBits());
					}

					@Override
					public String getDefaultDirectory() {
						return folderOf(getEditorPath());
					}


					@Override
					public String upload(FileUpload upload, String directory, String message) {
						String uploadedPath;
						try {
							uploadedPath = uploadPath(directory, FileUpload.getFileName(upload.getItems().iterator().next()));
						} catch (IllegalArgumentException e) {
							throw new BlobEditException(e.getMessage());
						}
						commitId = ProjectWikiPage.this.uploadFiles(upload, directory, message);
						if (revision == null) {
							revision = "main";
							getProject().setDefaultBranch(revision);
						}
						getProject().cacheObjectId(revision, commitId);
						return getRelativeUrl(uploadedPath);
					}

					@Override
					public String getPageReference(String url, String text) {
						String uploadedPath = GitUtils.normalizePath(PathUtils.resolve(folderOf(getEditorPath()), UrlUtils.decodePath(url)));
						if (WikiUtils.isUnderFolder(folder, uploadedPath) && uploadedPath.endsWith(".md"))
							return WikiUtils.pageReference(folder, getEditorPath(), uploadedPath, text);
						return null;
					}
				};
			}

			@Override
			public BlobSelectionSupport getRepositoryFileSelectionSupport() {
				return new BlobSelectionSupport() {
					@Override
					public Project getProject() {
						return ProjectWikiPage.this.getProject();
					}

					@Override
					public BlobIdent getBlobIdent() {
						return new BlobIdent(commitId != null ? commitId.name() : null, path, FileMode.REGULAR_FILE.getBits());
					}

					@Override
					public void onSelect(AjaxRequestTarget target, String selectedPath, boolean image, String label) {
						if (!image && WikiUtils.isUnderFolder(folder, selectedPath) && selectedPath.endsWith(".md")) {
							insertText(target, WikiUtils.pageReference(folder, getEditorPath(), selectedPath, label));
						} else {
							var ident = new BlobIdent(revision, selectedPath, FileMode.REGULAR_FILE.getBits());
							String url = getRelativeUrl(selectedPath);
							insertUrl(target, image, url, label != null ? label : ident.getName(), null);
						}
					}
				};
			}

			@Override
			protected String renderMarkdown(String markdown) {
				return ProjectWikiPage.this.render(markdown, getEditorPath());
			}
		};
	}

	public String getDefaultPageCommitMessage(String name) {
		return defaultCommitMessage(name, false, !creating && read(page) != null);
	}

	String getRevision() {
		return revision;
	}

	String getPageName() {
		return page;
	}

	boolean isCreating() {
		return creating;
	}

	ObjectId getCommitId() {
		return commitId;
	}

	private WebMarkupContainer newActions(String path, String content) {
		actions = new WebMarkupContainer("actions");
		actions.setOutputMarkupPlaceholderTag(true);
		actions.setVisible(!editing && !creating && content != null);

		actions.add(new MenuLink("add") {
			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				return List.of(new MenuItem() {
					@Override
					public String getLabel() {
						return _T("Add page");
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return link(id, "New-page", false, true);
					}
				}, new MenuItem() {
					@Override
					public String getLabel() {
						return _T("Upload files");
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new ModalLink(id) {
							@Override
							public void onClick(AjaxRequestTarget target) {
								super.onClick(target);
								dropdown.close();
							}

							@Override
							protected Component newContent(String id, ModalPanel modal) {
								return new BlobUploadPanel(id, folderOf(path)) {
									@Override
									protected Project getProject() {
										return ProjectWikiPage.this.getProject();
									}

									@Override
									protected ObjectId uploadFiles(FileUpload upload, String directory, String message) {
										try {
											return ProjectWikiPage.this.uploadFiles(upload, directory, message);
										} catch (IllegalArgumentException | NotTreeException | ObjectAlreadyExistsException e) {
											throw new BlobEditException(e.getMessage());
										}
									}

									@Override
									public void onCommitted(AjaxRequestTarget target, ObjectId commitId) {
										modal.close();
										setResponsePage(ProjectWikiPage.class, paramsOf(getProject(), revision != null ? revision : "main", returnPage));
									}

									@Override
									public void onCancel(AjaxRequestTarget target) {
										modal.close();
									}
								};
							}
						};
					}
				});
			}
		}.setVisible(canEdit(WikiUtils.pagePath(folder, "New-page"))));
		actions.add(new ViewStateAwarePageLink<Void>("history", ProjectCommitsPage.class,
				ProjectCommitsPage.paramsOf(getProject(), new PathCriteria(List.of(path)).toString(),
						commitId != null ? commitId.name() : null))
				.setVisible(SecurityUtils.canReadCode(getProject()) && content != null));
		actions.add(link("edit", page, true, false).setVisible(canEdit(path)));
		actions.add(new AjaxLink<Void>("delete") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				showDeleteConfirm(target, page, null);
			}
		}.setVisible(canEdit(path) && content != null));

		return actions;
	}

	private Component newSidebarEdit() {
		var sidebarEdit = link("sidebarEdit", "_Sidebar", true, false);
		sidebarEdit.add(new Label("label", _T("Customize")));
		return sidebarEdit.setVisible(canEdit(WikiUtils.pagePath(folder, "_Sidebar")))
				.setOutputMarkupPlaceholderTag(true);
	}

	private Component newUseDefault(String sidebar) {
		return new AjaxLink<Void>("useDefault") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				showDeleteConfirm(target, "_Sidebar", page);
			}
		}.setVisible(sidebar != null && canEdit(WikiUtils.pagePath(folder, "_Sidebar")))
				.setOutputMarkupPlaceholderTag(true);
	}

	private void selectPage(AjaxRequestTarget target, String destination) {
		if (editing || creating || !isPermitted())
			throw new UnauthorizedException();
		WikiUtils.pagePath(folder, destination);
		String content = read(destination);
		if (content == null && !destination.equals("Home"))
			throw new NotFoundException("Wiki page not found: " + destination);
		page = destination;
		returnPage = destination;
		var wiki = (WebMarkupContainer) get("wiki");
		wiki.get("title").setDefaultModelObject(getWikiPageTitle());
		wiki.addOrReplace(newActions(WikiUtils.pagePath(folder, page), content));
		wiki.get("homeNotFound").setVisible(content == null);
		var view = (WebMarkupContainer) wiki.get("view");
		if (view.isVisible() != (content != null))
			target.add(view, wiki.get("homeNotFound"));
		view.setVisible(content != null);
		view.addOrReplace(viewer("body", content));
		view.get("addHome").setVisible(read("Home") == null && canEdit(WikiUtils.pagePath(folder, "Home")));
		String sidebar = read("_Sidebar");
		navigation.get("currentOutline").setVisible(sidebar == null);
		navigation.addOrReplace(newSidebarEdit());
		navigation.addOrReplace(newUseDefault(sidebar));
		navigation.addOrReplace(viewer("sidebar", sidebar)
				.setVisible(sidebar != null).setOutputMarkupPlaceholderTag(true));
		target.add(wiki.get("title"), wiki.get("subtitle"), actions, view.get("body"),
				navigation.get("sidebar"), navigation.get("currentOutline"), navigation.get("sidebarEdit"), navigation.get("useDefault"));
		target.appendJavaScript("onedev.server.wiki.onPageChanged('"
				+ JavaScriptEscape.escapeJavaScript(page) + "');");
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		selectPage(target, (String) data);
	}

	private String outline(String markdown, String pageUrl) {
		String html = markdown != null ? OneDev.getInstance(MarkdownService.class).render(markdown) : "";
		var result = new Element("div");
		if (!html.isEmpty()) {
			for (var heading : Jsoup.parseBodyFragment(html).select("h1, h2, h3, h4, h5, h6")) {
				var anchor = heading.selectFirst("a[href^=#]:has(span.header-anchor)");
				if (anchor != null) {
					String href = anchor.attr("href");
					anchor.remove();
					int level = Integer.parseInt(heading.tagName().substring(1));
					result.appendElement("a").attr("href", pageUrl + href)
							.attr("class", "d-block text-break py-1")
							.attr("style", "padding-left:" + ((level - 1) * 12) + "px")
							.text(heading.text());
				}
			}
		}
		if (result.children().isEmpty())
			result.appendElement("span").addClass("text-muted").text(_T("No headings"));
		return result.html();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new WikiResourceReference()));
	}

	private String uploadPath(String directory, String filename) {
		String path = FilenameUtils.sanitizeFileName(filename);
		if (directory != null && !directory.isBlank()) {
			WikiUtils.validatePath(directory);
			path = directory + "/" + path;
		}
		return WikiUtils.normalizePath(path);
	}

	private ObjectId uploadFiles(FileUpload upload, String directory, String message) {
		if (!canEdit(WikiUtils.pagePath(folder, "New-page")))
			throw new UnauthorizedException();
		String branchName = revision != null ? GitUtils.ref2branch(getProject().getRef(revision).getName()) : "main";
		var user = SecurityUtils.getAuthUser();
		var protection = getProject().getBranchProtection(branchName, user);
		String error = protection.checkCommitMessage(message, false);
		if (error != null)
			throw new BlobEditException(error);
		Map<String, BlobContent> blobs = new HashMap<>();
		for (var item : upload.getItems()) {
			String path = uploadPath(directory, FileUpload.getFileName(item));
			if (!SecurityUtils.canModifyFile(getProject(), branchName, path))
				throw new BlobEditException(_T("Not allowed to modify this file"));
			String type = FileExtension.getExtension(path);
			if (protection.getDisallowedFileTypes().stream().anyMatch(it -> it.equalsIgnoreCase(type)))
				throw new BlobEditException(MessageFormat.format(_T("Not allowed file type: {0}"), type));
			blobs.put(path, new BlobContent(item.get(), FileMode.REGULAR_FILE.getBits()));
		}
		ObjectId previousCommitId = commitId != null ? commitId : ObjectId.zeroId();
		try {
			return git().commit(getProject(), new BlobEdits(Set.of(), blobs), GitUtils.branch2ref(branchName),
					previousCommitId, previousCommitId, user.asPerson(), message, protection.isCommitSignatureRequired());
		} catch (ObsoleteCommitException e) {
			throw new BlobEditException(_T("The branch changed. Reload before uploading files."));
		}
	}

	private void showDeleteConfirm(AjaxRequestTarget target, String name, String destination) {
		Model<String> message = Model.of("");
		actions.setVisible(false);
		target.add(actions);
		new ModalPanel(target) {
			@Override
			protected Component newContent(String id) {
				Fragment fragment = new Fragment(id, "deleteConfirm", ProjectWikiPage.this);
				fragment.add(new Label("title", destination != null ? _T("Use default sidebar?") : _T("Delete this wiki page?")));
				Form<Void> deleteForm = new Form<>("form");
				deleteForm.setOutputMarkupId(true);
				deleteForm.add(new FencedFeedbackPanel("feedback", deleteForm));
				deleteForm.add(newCommitMessageInput(message, Model.of(name), true, true));
				deleteForm.add(new AjaxButton("confirm") {
					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> submittedForm) {
						save(deleteForm::error, name, null, message.getObject(), true, true, destination);
						if (deleteForm.hasError())
							target.add(deleteForm);
						else
							close();
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> submittedForm) {
						target.add(deleteForm);
					}
				}.add(new Label("label", destination != null ? _T("Use default") : _T("Delete page"))));
				deleteForm.add(new AjaxLink<Void>("cancel") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						close();
					}
				});
				fragment.add(deleteForm);
				return fragment;
			}

			@Override
			protected void onClosed() {
				actions.setVisible(true);
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (target != null)
					target.add(actions);
			}
		};
	}

	private TextArea<String> newCommitMessageInput(Model<String> message, Model<String> name, boolean delete, boolean existed) {
		TextArea<String> input = new TextArea<>("message", message);
		input.setOutputMarkupId(true);
		input.add(AttributeModifier.replace("placeholder", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return defaultCommitMessage(name.getObject(), delete, existed);
			}
		}));
		return input;
	}

	private String defaultCommitMessage(String name, boolean delete, boolean existed) {
		String branchName = revision != null ? revision : "main";
		boolean conventional = getProject().getBranchProtection(branchName, SecurityUtils.getUser())
				.getCommitMessageChecker() instanceof ConventionalCommitChecker;
		if (creating && name != null)
			name = name.trim().replace(' ', '-');
		return defaultCommitMessage(name, delete, existed, conventional);
	}

	private static String defaultCommitMessage(String name, boolean delete, boolean existed, boolean conventional) {
		String pageName = name != null && !name.isBlank() ? name : null;
		Operation operation = delete ? Operation.DELETE : existed ? Operation.EDIT : Operation.ADD;
		return DefaultCommitMessage.generate(operation, pageName, _T("Add new page"), conventional);
	}

	private BookmarkablePageLink<Void> link(String id, String destination, boolean edit, boolean create) {
		var params = paramsOf(getProject(), revision, destination);
		if (edit) {
			params.add("edit", true);
			if (destination.equals("_Sidebar"))
				params.add("return-page", returnPage);
		}
		if (create) {
			params.add("new", true);
			params.add("return-page", returnPage);
		}
		return new BookmarkablePageLink<>(id, ProjectWikiPage.class, params);
	}

	private String read(String name) {
		if (commitId == null)
			return null;
		var blob = getProject().getBlob(new BlobIdent(commitId.name(), WikiUtils.pagePath(folder, name), FileMode.REGULAR_FILE.getBits()), false);
		if (blob == null || blob.getText() == null || blob.getLfsPointer() != null || !blob.getIdent().isFile())
			return null;
		return blob.getText().getContent();
	}

	private void collectPages(@Nullable String directory, List<String> pages) {
		var ident = directory != null ? git().getBlobIdent(getProject(), commitId, directory)
				: new BlobIdent(commitId.name(), null, FileMode.TREE.getBits());
		if (ident != null && ident.isTree()) {
			for (var child : git().getChildren(getProject(), commitId, directory, BlobIdentFilter.ALL, false)) {
				if (child.isTree()) {
					collectPages(child.path, pages);
				} else if (child.isFile() && child.path.endsWith(".md")) {
					String name = child.path.substring(folder != null ? folder.length() + 1 : 0, child.path.length() - 3);
					if (!name.equals("_Sidebar"))
						pages.add(name);
				}
			}
		}
	}

	private String getWikiPageTitle() {
		return page.substring(page.lastIndexOf('/') + 1).replace('-', ' ');
	}

	private String render(String markdown) {
		return render(markdown, WikiUtils.pagePath(folder, page));
	}

	private String render(String markdown, String currentPath) {
		if (markdown == null)
			return "";
		var service = OneDev.getInstance(MarkdownService.class);
		String html = service.process(service.render(markdown), getProject(), null, null, false);
		return new WikiLinkResolver(getProject(), revision, folder, currentPath, returnPage).resolve(html);
	}

	private static String folderOf(String path) {
		int slash = path.lastIndexOf('/');
		return slash >= 0 ? path.substring(0, slash) : null;
	}

	private boolean canEdit(String path) {
		return Objects.equals(folder, getProject().getWikiFolder().getPath()) && SecurityUtils.canEditWikiPage(getProject(), revision, path);
	}

	private void save(Form<?> form, String name, String text, String message, boolean delete, boolean existed) {
		save(form::error, name, text, message, delete, existed, null);
	}

	@SuppressWarnings("unchecked")
	private void save(java.util.function.Consumer<String> reportError, String name, String text, String message,
			boolean delete, boolean existed, String destination) {
		try {
			if (name == null)
				throw new IllegalArgumentException("Specify a page name");
			name = delete ? name : name.trim().replace(' ', '-');
			String path = WikiUtils.pagePath(folder, name);
			String oldPath = WikiUtils.pagePath(folder, delete ? name : page);
			var branch = revision != null ? getProject().getRef(revision) : getProject().getBranchRef("main");
			if (commitId == null ? branch != null || getProject().getDefaultBranch() != null
					: branch == null || !branch.getObjectId().equals(commitId)) {
				reportError.accept(_T("The branch changed. Copy your edits and reload before saving."));
				return;
			}
			if (!canEdit(path) || !creating && !canEdit(oldPath))
				throw new UnauthorizedException();
			String branchName = branch != null ? GitUtils.ref2branch(branch.getName()) : "main";
			var protection = getProject().getBranchProtection(branchName, SecurityUtils.getAuthUser());
			if (message == null || message.isBlank())
				message = defaultCommitMessage(name, delete, !creating && existed,
						protection.getCommitMessageChecker() instanceof ConventionalCommitChecker);
			String error = protection.checkCommitMessage(message, false);
			if (error != null) {
				reportError.accept(error);
				return;
			}
			if (!delete && (creating || !name.equals(page)) && commitId != null
					&& getProject().getBlob(new BlobIdent(commitId.name(), path, FileMode.REGULAR_FILE.getBits()), false) != null) {
				reportError.accept(_T("A page with this name already exists."));
				return;
			}
			Set<String> oldPaths = !creating && existed ? Set.of(oldPath) : Set.of();
			Map<String, BlobContent> blobs = delete ? Map.of() : Map.of(path,
					new BlobContent(text != null ? text.getBytes(StandardCharsets.UTF_8) : new byte[0], FileMode.REGULAR_FILE.getBits()));
			ObjectId previousCommitId = commitId != null ? commitId : ObjectId.zeroId();
			ObjectId newCommitId = git().commit(getProject(), new BlobEdits(oldPaths, blobs), GitUtils.branch2ref(branchName), previousCommitId, previousCommitId,
					SecurityUtils.getAuthUser().asPerson(), message, protection.isCommitSignatureRequired());
			String selectedPage = destination != null ? destination : delete || name.equals("_Sidebar") ? "Home" : name;
			if (delete) {
				commitId = newCommitId;
				revision = branchName;
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				selectPage(target, selectedPage);
				List<String> pages = new ArrayList<>();
				collectPages(folder, pages);
				Collections.sort(pages);
				((ListView<String>) navigation.get("pages")).setList(pages);
				target.add(get("wiki"));
				String url = urlFor(ProjectWikiPage.class, paramsOf(getProject(), revision, page)).toString();
				pushState(target, JavaScriptEscape.escapeJavaScript(url), page);
				target.appendJavaScript("onedev.server.wiki.scrollToHeading('', true);");
			} else {
				setResponsePage(ProjectWikiPage.class, paramsOf(getProject(), branchName, selectedPage));
			}
		} catch (ObsoleteCommitException e) {
			reportError.accept(_T("The branch changed. Copy your edits and reload before saving."));
		} catch (IllegalArgumentException | BlobEditException e) {
			reportError.accept(e.getMessage());
		}
	}

	public static PageParameters paramsOf(Project project, String revision, String page) {
		var params = ProjectPage.paramsOf(project);
		if (revision == null)
			revision = RevisionAndPath.parse(project, Collections.emptyList()).getRevision();
		int index = 0;
		if (revision != null) {
			for (String segment : revision.split("/"))
				params.set(index++, segment);
		}
		if (page != null) {
			for (String segment : page.split("/"))
				params.set(index++, segment);
		}
		return params;
	}

	private Component viewer(String id, String markdown) {
		return new MarkdownViewer(id, Model.of(markdown), null) {
			@Override
			protected String renderMarkdown(String markdown) {
				return ProjectWikiPage.this.render(markdown);
			}
		};
	}

	@Override
	protected boolean isPermitted() {
		return getProject().isCodeManagement() && getProject().isWikiManagement()
				&& SecurityUtils.canAccessProject(getProject());
	}

	@Override
	protected Component newProjectTitle(String id) {
		return new Label(id, _T("Wiki"));
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String id, Project project) {
		if (SecurityUtils.canAccessProject(project) && project.isCodeManagement() && project.isWikiManagement())
			return new BookmarkablePageLink<Void>(id, ProjectWikiPage.class, ProjectPage.paramsOf(project));
		return new BookmarkablePageLink<Void>(id, ProjectOverviewPage.class, ProjectPage.paramsOf(project));
	}
}
