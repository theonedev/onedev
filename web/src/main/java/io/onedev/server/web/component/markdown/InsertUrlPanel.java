package io.onedev.server.web.component.markdown;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.image.ExternalImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Bytes;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.blobpicker.BlobPicker;
import io.onedev.server.web.component.dropzonefield.DropzoneField;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.BlobUploadException;
import io.onedev.utils.PathUtils;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
abstract class InsertUrlPanel extends Panel {

	private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

	private static final MetaDataKey<String> ACTIVE_TAB = new MetaDataKey<String>(){};
	
	private static final MetaDataKey<String> UPLOAD_DIRECTORY = new MetaDataKey<String>(){};
	
	private static final MetaDataKey<HashSet<String>> BLOB_PICKER_STATE = new MetaDataKey<HashSet<String>>(){};
	
	static final String TAB_INPUT_URL = "Input URL";
	
	static final String TAB_PICK_EXISTING = "Pick Existing";
	
	static final String TAB_UPLOAD = "Upload";
	
	private static final String CONTENT_ID = "content";
	
	private Collection<FileUpload> uploads;
	
	private String url;
	
	private String text;
	
	private String summaryCommitMessage;
	
	private String detailCommitMessage;
	
	private final MarkdownEditor markdownEditor;
	
	private final boolean isImage;
	
	public InsertUrlPanel(String id, MarkdownEditor markdownEditor, boolean isImage) {
		super(id);
		this.markdownEditor = markdownEditor;
		this.isImage = isImage;
	}

	private Component newInputUrlPanel() {
		Fragment fragment = new Fragment(CONTENT_ID, "inputUrlFrag", this) {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				String script = String.format("onedev.server.markdown.onInputUrlDomReady('%s');", getMarkupId());
				response.render(OnDomReadyHeaderItem.forScript(script));
			}
			
		};
		
		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));
		
		form.add(new Label("urlLabel", isImage?"Image URL":"Link URL"));
		form.add(new Label("urlHelp", isImage?"Absolute or relative url of the image":"Absolute or relative url of the link"));
		form.add(new TextField<String>("url", new PropertyModel<String>(this, "url")));
		
		form.add(new Label("textLabel", isImage?"Image Text": "Link Text"));
		form.add(new TextField<String>("text", new PropertyModel<String>(this, "text")));
		
		form.add(new AjaxButton("insert", form) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (StringUtils.isBlank(url)) {
					if (isImage)
						error("Image URL should be specified");
					else
						error("Link URL should be specified");
					target.add(fragment);
				} else {
					if (text == null)
						text = StringUtils.describeUrl(url);
					markdownEditor.insertUrl(target, isImage, url, text, null);
					onClose(target);
				}
			}
			
		});
		
		fragment.add(form);
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private Component newPickExistingPanel() {
		Fragment fragment;
		BlobRenderContext context = markdownEditor.getBlobRenderContext();
		if (context != null) {
			fragment = new Fragment(CONTENT_ID, "pickBlobFrag", this);
			BlobIdentFilter blobIdentFilter = new BlobIdentFilter() {

				@Override
				public boolean filter(BlobIdent blobIdent) {
					if (isImage) {
						if (blobIdent.isTree()) {
							return true;
						} else {
					        String mimetype= MIME_TYPES.getContentType(new File(blobIdent.path));
					        return mimetype.split("/")[0].equals("image");									
						}
					} else {
						return true;
					}
				}
				
			};
			
			/*
			 * We resolve revision to get latest commit id so that we can select to insert newly 
			 * added/uploaded files while editing a markdown file
			 */
			ObjectId commitId;
			try {
				commitId = context.getProject().getRepository()
						.resolve(context.getBlobIdent().revision);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			Set<BlobIdent> blobPickerState = new HashSet<>();
			Set<String> expandedPaths = WebSession.get().getMetaData(BLOB_PICKER_STATE);
			if (expandedPaths != null) {
				for (String path: expandedPaths)
					blobPickerState.add(new BlobIdent(commitId.name(), path, FileMode.TREE.getBits()));
			} 
			
			BlobIdent blobIdent = context.getBlobIdent();
			String parentPath;
			if (blobIdent.isTree())
				parentPath = blobIdent.path;
			else if (blobIdent.path.contains("/"))
				parentPath = StringUtils.substringBeforeLast(blobIdent.path, "/");
			else
				parentPath = null;
			
			while (parentPath != null) {
				blobPickerState.add(new BlobIdent(commitId.name(), parentPath, FileMode.TYPE_TREE));
				if (parentPath.contains("/"))
					parentPath = StringUtils.substringBeforeLast(parentPath, "/");
				else
					parentPath = null;
			}
			
			fragment.add(new BlobPicker("files", commitId) {

				@Override
				protected void onSelect(AjaxRequestTarget target, BlobIdent blobIdent) {
					blobIdent = new BlobIdent(context.getBlobIdent().revision, blobIdent.path, blobIdent.mode);
					String baseUrl = context.getBaseUrl();
					String referenceUrl = urlFor(ProjectBlobPage.class, 
							ProjectBlobPage.paramsOf(context.getProject(), blobIdent)).toString();
					String relativized = PathUtils.relativize(baseUrl, referenceUrl);		
					markdownEditor.insertUrl(target, isImage, relativized, StringUtils.describeUrl(blobIdent.getName()), null);
					onClose(target);
				}

				@Override
				protected BlobIdentFilter getBlobIdentFilter() {
					return blobIdentFilter;
				}

				@Override
				protected Project getProject() {
					return markdownEditor.getBlobRenderContext().getProject();
				}

				@Override
				protected void onStateChange() {
					HashSet<String> expandedPaths = new HashSet<>();
					for (BlobIdent blobIdent: blobPickerState)
						expandedPaths.add(blobIdent.path);
					WebSession.get().setMetaData(BLOB_PICKER_STATE, expandedPaths);
				}

				@Override
				protected Set<BlobIdent> getState() {
					return blobPickerState;
				}
				
			});
		} else {
			AttachmentSupport attachmentSupport = Preconditions.checkNotNull(markdownEditor.getAttachmentSupport());
			if (isImage) {
				fragment = new Fragment(CONTENT_ID, "pickAttachedImageFrag", this);
				fragment.add(new ListView<String>("attachments", new LoadableDetachableModel<List<String>>() {

					@Override
					protected List<String> load() {
						List<String> attachmentNames = new ArrayList<>();
						for (String attachmentName: attachmentSupport.getAttachments()) {
							if (markdownEditor.isWebSafeImage(attachmentName))
								attachmentNames.add(attachmentName);
						}
						return attachmentNames;
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<String> item) {
						String attachmentName = item.getModelObject();
						String attachmentUrl = attachmentSupport.getAttachmentUrl(attachmentName);
						
						AjaxLink<Void> selectLink = new AjaxLink<Void>("select") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								String displayName = StringUtils.describeUrl(attachmentName);
								markdownEditor.insertUrl(target, true, attachmentUrl, displayName, null);
								onClose(target);
							}

						};
						selectLink.add(new ExternalImage("image", StringEscapeUtils.escapeHtml4(attachmentUrl)));
						item.add(selectLink);
						
						item.add(new AjaxLink<Void>("delete") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								attachmentSupport.deleteAttachemnt(attachmentName);
								target.add(fragment);
							}
							
						});
					}

				});			
				
			} else {
				fragment = new Fragment(CONTENT_ID, "pickAttachedFileFrag", this);
				fragment.add(new ListView<String>("attachments", new LoadableDetachableModel<List<String>>() {

					@Override
					protected List<String> load() {
						return attachmentSupport.getAttachments();
					}
					
				}) {

					@Override
					protected void populateItem(final ListItem<String> item) {
						String attachmentName = item.getModelObject();
						String attachmentUrl = attachmentSupport.getAttachmentUrl(attachmentName);
						
						AjaxLink<Void> selectLink = new AjaxLink<Void>("select") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								String displayName = StringUtils.describeUrl(attachmentName);
								markdownEditor.insertUrl(target, false, attachmentUrl, displayName, null);
								onClose(target);
							}

						};
						selectLink.add(new Label("file", StringEscapeUtils.escapeHtml4(attachmentName)));
						item.add(selectLink);
						
						item.add(new AjaxLink<Void>("delete") {

							@Override
							public void onClick(AjaxRequestTarget target) {
								attachmentSupport.deleteAttachemnt(attachmentName);
								target.add(fragment);
							}
							
						});
					}

				});			
			}
		}
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	private Component newUploadPanel() {
		Fragment fragment;

		IModel<Collection<FileUpload>> model = new PropertyModel<Collection<FileUpload>>(this, "uploads");
		String acceptedFiles;
		if (isImage)
			acceptedFiles = "image/*";
		else
			acceptedFiles = null;
		
		AttachmentSupport attachmentSupport = markdownEditor.getAttachmentSupport();
		if (attachmentSupport != null) {
			fragment = new Fragment(CONTENT_ID, "uploadAttachmentFrag", this);
			
			Form<?> form = new Form<Void>("form") {

				@Override
				protected void onSubmit() {
					super.onSubmit();
					
					AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
					String attachmentName;
					FileUpload upload = uploads.iterator().next();
					try (InputStream is = upload.getInputStream()) {
						attachmentName = attachmentSupport.saveAttachment(upload.getClientFileName(), is);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					markdownEditor.insertUrl(target, isImage, 
							attachmentSupport.getAttachmentUrl(attachmentName), StringUtils.describeUrl(attachmentName), null);
					onClose(target);
				}

				@Override
				protected void onFileUploadException(FileUploadException e, Map<String, Object> model) {
					throw new RuntimeException(e);
				}
				
			};
			form.setMaxSize(Bytes.bytes(attachmentSupport.getAttachmentMaxSize()));
			form.setMultiPart(true);
			form.add(new NotificationPanel("feedback", form));
			
			int maxFilesize = (int) (attachmentSupport.getAttachmentMaxSize()/1024/1024);
			if (maxFilesize <= 0)
				maxFilesize = 1;
			form.add(new DropzoneField("file", model, acceptedFiles, 1, maxFilesize).setRequired(true));
			
			form.add(new AjaxButton("insert"){});
			
			fragment.add(form);
		} else {
			fragment = new Fragment(CONTENT_ID, "uploadBlobFrag", this);
			Form<?> form = new Form<Void>("form");
			form.setMultiPart(true);
			form.setFileMaxSize(Bytes.megabytes(Project.MAX_UPLOAD_SIZE));
			add(form);
			
			NotificationPanel feedback = new NotificationPanel("feedback", form);
			feedback.setOutputMarkupPlaceholderTag(true);
			form.add(feedback);
			
			form.add(new DropzoneField("file", model, acceptedFiles, 1, Project.MAX_UPLOAD_SIZE).setRequired(true));

			form.add(new TextField<String>("directory", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return WebSession.get().getMetaData(UPLOAD_DIRECTORY);
				}

				@Override
				public void setObject(String object) {
					WebSession.get().setMetaData(UPLOAD_DIRECTORY, object);
				}
				
			})); 
			form.add(new TextField<String>("summaryCommitMessage", 
					new PropertyModel<String>(this, "summaryCommitMessage")));
			form.add(new TextArea<String>("detailCommitMessage", 
					new PropertyModel<String>(this, "detailCommitMessage")));
			
			form.add(new AjaxButton("insert") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);

					BlobRenderContext context = Preconditions.checkNotNull(markdownEditor.getBlobRenderContext());
					String commitMessage = summaryCommitMessage;
					if (StringUtils.isBlank(commitMessage))
						commitMessage = "Add files via upload";
					
					if (StringUtils.isNotBlank(detailCommitMessage))
						commitMessage += "\n\n" + detailCommitMessage;

					try {
						String directory = WebSession.get().getMetaData(UPLOAD_DIRECTORY);
						context.uploadFiles(uploads, directory, commitMessage);
						String fileName = uploads.iterator().next().getClientFileName();
						String url;
						if (directory != null) {
							url = directory + "/" + fileName;
						} else {
							url = fileName;
						}
						markdownEditor.insertUrl(target, isImage, url, StringUtils.describeUrl(fileName), null);
						onClose(target);
					} catch (BlobUploadException e) {
						form.error(e.getMessage());
						target.add(feedback);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					super.onError(target, form);
					target.add(feedback);
				}
				
			});
			
			fragment.add(form);
		}
		
		fragment.setOutputMarkupId(true);
		return fragment;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", isImage?"Insert Image":"Insert Link"));
		
		if (markdownEditor.getBlobRenderContext() == null && markdownEditor.getAttachmentSupport() == null) {
			add(newInputUrlPanel());
		} else {
			Fragment fragment = new Fragment(CONTENT_ID, "tabbedFrag", this);
			List<Tab> tabs = new ArrayList<>();
			AjaxActionTab inputUrlTab = new AjaxActionTab(Model.of(TAB_INPUT_URL)) {

				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component content = newInputUrlPanel();
					target.add(content);
					fragment.replace(content);
					WebSession.get().setMetaData(ACTIVE_TAB, TAB_INPUT_URL);
				}
				
			};
			tabs.add(inputUrlTab);
			
			AjaxActionTab pickExistingTab = new AjaxActionTab(Model.of(TAB_PICK_EXISTING)) {

				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component content = newPickExistingPanel();
					target.add(content);
					fragment.replace(content);
					WebSession.get().setMetaData(ACTIVE_TAB, TAB_PICK_EXISTING);
				}
				
			};
			tabs.add(pickExistingTab);
			
			AjaxActionTab uploadTab = new AjaxActionTab(Model.of(TAB_UPLOAD)) {

				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					Component content = newUploadPanel();
					target.add(content);
					fragment.replace(content);
					WebSession.get().setMetaData(ACTIVE_TAB, TAB_UPLOAD);
				}
				
			};
			tabs.add(uploadTab);
			
			fragment.add(new Tabbable("tabs", tabs));
			
			inputUrlTab.setSelected(false);
			String activeTab = WebSession.get().getMetaData(ACTIVE_TAB);
			if (TAB_PICK_EXISTING.equals(activeTab)) {
				pickExistingTab.setSelected(true);
				fragment.add(newPickExistingPanel());
			} else if (TAB_UPLOAD.equals(activeTab)) {
				uploadTab.setSelected(true);
				fragment.add(newUploadPanel());
			} else {
				inputUrlTab.setSelected(true);
				fragment.add(newInputUrlPanel());
			}
			add(fragment);
		}
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
	}

	protected abstract void onClose(AjaxRequestTarget target);
}
