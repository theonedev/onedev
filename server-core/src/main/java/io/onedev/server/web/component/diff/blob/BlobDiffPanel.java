package io.onedev.server.web.component.diff.blob;

import static io.onedev.server.util.diff.DiffUtils.MAX_LINE_LEN;
import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.jspecify.annotations.Nullable;

import org.apache.tika.mime.MediaType;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.FileMode;

import io.onedev.server.OneDev;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobChange;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.LfsObject;
import io.onedev.server.git.LfsPointer;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.diff.DiffRenderer;
import io.onedev.server.web.component.diff.blob.text.BlobTextDiffPanel;
import io.onedev.server.web.component.diff.diffstat.DiffStatBar;
import io.onedev.server.web.component.diff.difftitle.BlobDiffTitle;
import io.onedev.server.web.component.diff.revision.DiffViewMode;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.util.DiffPlanarRange;
import io.onedev.server.web.util.EditParamsAware;

public class BlobDiffPanel extends Panel {

	private static final String BODY_ID = "body";

	private final BlobChange change;

	private final IModel<Boolean> blameModel;

	private final DiffViewMode diffMode;

	private boolean reviewed;

	private boolean collapsed;

	public BlobDiffPanel(String id, BlobChange change, DiffViewMode diffMode, @Nullable IModel<Boolean> blameModel) {
		super(id);

		this.change = change;
		this.blameModel = blameModel;
		this.diffMode = diffMode;

		var reviewSupport = getReviewSupport();
		if (reviewSupport != null)
			reviewed = reviewSupport.isReviewed();
		var annotationSupport = getAnnotationSupport();
		if (annotationSupport == null || annotationSupport.getMarkRange() == null)
			collapsed = reviewed;
	}

	private Fragment newMessageFragment(String message, boolean warning) {
		Fragment fragment = new Fragment(BODY_ID, "messageFrag", this);
		if (warning)
			fragment.add(new SpriteImage("icon", "warning"));
		else
			fragment.add(new SpriteImage("icon", "info-circle"));
		fragment.add(new Label("message", message));
		fragment.add(AttributeAppender.append("class",
				"message p-3 d-flex align-items-center border border-top-0 rounded-bottom"));
		return fragment;
	}

	@Nullable
	protected PullRequest getPullRequest() {
		return null;
	}

	private void showBlob(Blob blob) {
		Component diffPanel = null;
		for (DiffRenderer renderer : OneDev.getExtensions(DiffRenderer.class)) {
			if (blob.getLfsPointer() != null
					&& !new LfsObject(change.getProject().getId(), blob.getLfsPointer().getObjectId()).exists()) {
				diffPanel = newMessageFragment(_T("Storage file missing"), true);
				break;
			}
			MediaType mediaType = change.getProject().detectMediaType(blob.getIdent());
			diffPanel = renderer.render(BODY_ID, mediaType, change, diffMode);
			if (diffPanel != null)
				break;
		}

		if (diffPanel != null) {
			add(diffPanel);
		} else if (blob.getText() != null) {
			if (blob.getText().getLines().size() > DiffUtils.MAX_DIFF_SIZE) {
				add(newMessageFragment(_T("Unable to diff as the file is too large."), true));
			} else if (change.getAdditions() + change.getDeletions() > WebConstants.MAX_SINGLE_DIFF_LINES) {
				add(newMessageFragment(_T("Diff is too large to be displayed."), true));
			} else if (change.getDiffBlocks().isEmpty()) {
				if (change.getNewBlobIdent().path != null)
					add(newMessageFragment(_T("Empty file added."), false));
				else
					add(newMessageFragment(_T("Empty file removed."), false));
			} else {
				add(new BlobTextDiffPanel(BODY_ID, change, diffMode, blameModel) {

					@Override
					protected PullRequest getPullRequest() {
						return BlobDiffPanel.this.getPullRequest();
					}

					@Override
					public BlobAnnotationSupport getAnnotationSupport() {
						return BlobDiffPanel.this.getAnnotationSupport();
					}

					@Override
					protected void onActive(AjaxRequestTarget target) {
						BlobDiffPanel.this.onActive(target);
					}
				});
			}
		} else {
			add(newMessageFragment(_T("Binary file."), false));
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("toggleBody") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				toggleBody(target);
			}

		});

		add(new DiffStatBar("diffStat", change.getAdditions(), change.getDeletions()) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getDiffBody() instanceof BlobTextDiffPanel);
			}
		});
		add(new BlobDiffTitle("title", change));

		var reviewSupport = getReviewSupport();
		if (reviewSupport != null) {
			var toggleReviewedLink = new AjaxLink<Void>("toggleReviewed") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					reviewed = !reviewed;
					reviewSupport.setReviewed(target, reviewed);
					if (reviewed && !collapsed)
						toggleBody(target);
					target.add(this);
				}

			};
			toggleReviewedLink.add(new SpriteImage("icon", new AbstractReadOnlyModel<>() {
				@Override
				public String getObject() {
					return reviewed ? "tick-box" : "square";
				}
			}));
			toggleReviewedLink.add(AttributeAppender.append("data-tippy-content", new AbstractReadOnlyModel<String>() {
				@Override
				public String getObject() {
					return reviewed ? _T("Set unreviewed") : _T("Set reviewed");
				}
			}));
			toggleReviewedLink.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {
				@Override
				public String getObject() {
					return reviewed ? "link-primary" : "";
				}
			}));
			add(toggleReviewedLink);
		} else {
			var toggleReviewedLink = new WebMarkupContainer("toggleReviewed");
			toggleReviewedLink.add(new WebMarkupContainer("icon"));
			toggleReviewedLink.setVisible(false);
			add(toggleReviewedLink);
		}
		add(new AjaxLink<Void>("blameFile") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(blameModel != null
						&& !change.getOldBlobIdent().isGitLink()
						&& !change.getNewBlobIdent().isGitLink()
						&& getDiffBody() instanceof BlobTextDiffPanel);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				((BlobTextDiffPanel) getDiffBody()).toggleBlame(target);
				target.add(this);
			}

		}.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return ((BlobTextDiffPanel) getDiffBody()).isBlame() ? "link-primary" : "";
			}

		})).setOutputMarkupId(true));

		ProjectBlobPage.State viewState = new ProjectBlobPage.State(change.getBlobIdent());

		viewState.requestId = PullRequest.idOf(getPullRequest());
		PageParameters params = ProjectBlobPage.paramsOf(change.getProject(), viewState);
		add(new ViewStateAwarePageLink<Void>("viewFile", ProjectBlobPage.class, params));

		if (change.getType() != ChangeType.DELETE && change.getNewBlob().getLfsPointer() == null) {
			if (getPullRequest() != null
					&& getPullRequest().getSource() != null
					&& getPullRequest().getSource().getObjectName(false) != null
					&& SecurityUtils.canModifyFile(getPullRequest().getSourceProject(),
							getPullRequest().getSourceBranch(), change.getPath())) {
				// we are in context of a pull request and pull request source branch exists, so
				// we edit in source branch instead
				Link<Void> editLink = new Link<Void>("editFile") {

					@Override
					public void onClick() {
						BlobIdent blobIdent = new BlobIdent(getPullRequest().getSourceBranch(), change.getPath(),
								FileMode.REGULAR_FILE.getBits());
						ProjectBlobPage.State editState = new ProjectBlobPage.State(blobIdent);
						editState.requestId = getPullRequest().getId();
						editState.mode = BlobRenderContext.Mode.EDIT;
						editState.urlBeforeEdit = EditParamsAware.getUrlBeforeEdit(getPage());
						editState.urlAfterEdit = EditParamsAware.getUrlAfterEdit(getPage());
						PageParameters params = ProjectBlobPage.paramsOf(getPullRequest().getSourceProject(),
								editState);
						setResponsePage(ProjectBlobPage.class, params);
					}

				};
				add(editLink);
			} else if (SecurityUtils.canModifyFile(change.getProject(), change.getBlobIdent().revision,
					change.getPath())
					&& change.getProject().getBranchRef(change.getBlobIdent().revision) != null) {
				// we are on a branch
				Link<Void> editLink = new Link<Void>("editFile") {

					@Override
					public void onClick() {
						ProjectBlobPage.State editState = new ProjectBlobPage.State(change.getBlobIdent());
						editState.mode = BlobRenderContext.Mode.EDIT;
						editState.urlBeforeEdit = EditParamsAware.getUrlBeforeEdit(getPage());
						editState.urlAfterEdit = EditParamsAware.getUrlAfterEdit(getPage());
						PageParameters params = ProjectBlobPage.paramsOf(change.getProject(), editState);
						setResponsePage(ProjectBlobPage.class, params);
					}

				};
				editLink.add(AttributeAppender.replace("data-tippy-content",
						MessageFormat.format(_T("Edit on branch {0}"), change.getBlobIdent().revision)));
				add(editLink);
			} else {
				add(new WebMarkupContainer("editFile").setVisible(false));
			}
		} else {
			add(new WebMarkupContainer("editFile").setVisible(false));
		}

		if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.COPY) {
			showBlob(change.getNewBlob());
		} else if (change.getType() == ChangeType.DELETE) {
			showBlob(change.getOldBlob());
		} else {
			LfsPointer oldLfsPointer = change.getOldBlob().getLfsPointer();
			LfsPointer newLfsPointer = change.getNewBlob().getLfsPointer();
			if (oldLfsPointer != null
					&& !new LfsObject(change.getProject().getId(), oldLfsPointer.getObjectId()).exists()
					|| newLfsPointer != null
							&& !new LfsObject(change.getProject().getId(), newLfsPointer.getObjectId()).exists()) {
				add(newMessageFragment(_T("Storage file missing"), true));
			} else {
				MediaType oldMediaType = change.getProject().detectMediaType(change.getOldBlobIdent());
				MediaType newMediaType = change.getProject().detectMediaType(change.getNewBlobIdent());

				Component diffPanel = null;

				if (oldMediaType.equals(newMediaType)) {
					for (DiffRenderer renderer : OneDev.getExtensions(DiffRenderer.class)) {
						diffPanel = renderer.render(BODY_ID, newMediaType, change, diffMode);
						if (diffPanel != null)
							break;
					}
				}

				if (diffPanel != null) {
					add(diffPanel);
				} else if (change.getOldText() != null && change.getNewText() != null) {
					if (change.getOldText().getLines().size()
							+ change.getNewText().getLines().size() > DiffUtils.MAX_DIFF_SIZE) {
						add(newMessageFragment(_T("Unable to diff as the file is too large."), true));
					} else if (change.getOldText().getLines().stream().anyMatch(it -> it.length() > MAX_LINE_LEN)
							|| change.getOldText().getLines().stream().anyMatch(it -> it.length() > MAX_LINE_LEN)) {
						add(newMessageFragment(_T("Unable to diff as some line is too long."), true));
					} else if (change.getAdditions() + change.getDeletions() > WebConstants.MAX_SINGLE_DIFF_LINES) {
						add(newMessageFragment(_T("Diff is too large to be displayed."), true));
					} else if (change.getAdditions() + change.getDeletions() == 0) {
						add(newMessageFragment(_T("Content is identical"), false));
					} else {
						add(new BlobTextDiffPanel(BODY_ID, change, diffMode, blameModel) {

							@Override
							protected PullRequest getPullRequest() {
								return BlobDiffPanel.this.getPullRequest();
							}

							@Override
							public BlobAnnotationSupport getAnnotationSupport() {
								return BlobDiffPanel.this.getAnnotationSupport();
							}

							@Override
							protected void onActive(AjaxRequestTarget target) {
								BlobDiffPanel.this.onActive(target);
							}

						});
					}
				} else {
					add(newMessageFragment(_T("Binary file."), false));
				}
			}
		}
		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				return collapsed ? "blob-diff collapsed" : "blob-diff";
			}
		}));

		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BlobDiffCssResourceReference()));
	}

	@Override
	protected void onDetach() {
		if (blameModel != null)
			blameModel.detach();

		super.onDetach();
	}

	public void onCommentDeleted(AjaxRequestTarget target) {
		Component body = getDiffBody();
		if (body instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) body).onCommentDeleted(target);
	}

	public void onCommentClosed(AjaxRequestTarget target) {
		Component body = getDiffBody();
		if (body instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) body).onCommentClosed(target);
	}

	public void onCommentAdded(AjaxRequestTarget target, CodeComment comment, DiffPlanarRange range) {
		Component body = getDiffBody();
		if (body instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) body).onCommentAdded(target, comment, range);
	}

	public void mark(AjaxRequestTarget target, DiffPlanarRange markRange) {
		if (collapsed)
			toggleBody(target);
		Component body = getDiffBody();
		if (body instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) body).mark(target, markRange);
	}

	public void unmark(AjaxRequestTarget target) {
		Component body = getDiffBody();
		if (body instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) body).unmark(target);
	}

	public void onUnblame(AjaxRequestTarget target) {
		Component body = getDiffBody();
		if (body instanceof BlobTextDiffPanel)
			((BlobTextDiffPanel) body).onUnblame(target);
	}

	private Component getDiffBody() {
		return get(BODY_ID);
	}

	private void toggleBody(AjaxRequestTarget target) {
		collapsed = !collapsed;
		String script;
		if (collapsed)
			script = "$('#%s').addClass('collapsed');";
		else
			script = "$('#%s').removeClass('collapsed');";
		target.appendJavaScript(String.format(script, getMarkupId()));
	}

	@Nullable
	protected BlobDiffReviewSupport getReviewSupport() {
		return null;
	}

	@Nullable
	protected BlobAnnotationSupport getAnnotationSupport() {
		return null;
	}

	protected void onActive(AjaxRequestTarget target) {
	}

}
