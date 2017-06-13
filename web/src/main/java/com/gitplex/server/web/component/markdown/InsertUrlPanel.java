package com.gitplex.server.web.component.markdown;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Bytes;
import org.eclipse.jgit.lib.FileMode;

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.BlobIdentFilter;
import com.gitplex.server.model.Project;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.PathUtils;
import com.gitplex.server.web.component.floating.FloatingPanel;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.projectfilepicker.ProjectFilePicker;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
class InsertUrlPanel extends Panel {

	private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();
	
	private String url;
	
	private final MarkdownEditor markdownEditor;
	
	private final boolean isImage;
	
	public InsertUrlPanel(String id, MarkdownEditor markdownEditor, boolean isImage) {
		super(id);
		this.markdownEditor = markdownEditor;
		this.isImage = isImage;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				markdownEditor.closeUrlInserter(target, this);
			}
			
		});
		
		if (isImage)
			add(new Label("title", "Insert Image"));
		else
			add(new Label("title", "Insert Link"));
			
		Form<?> urlForm = new Form<Void>("urlForm");
		add(urlForm);
		urlForm.add(new NotificationPanel("feedback", urlForm));
		
		TextField<String> urlField = new TextField<String>("url", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return url;
			}

			@Override
			public void setObject(String object) {
				url = object;
			}
			
		}); 
		urlField.setOutputMarkupId(true);
		urlForm.add(urlField);
		
		urlForm.add(new AjaxButton("ok", urlForm) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (StringUtils.isBlank(url)) {
					error("Url should be specified");
					target.add(InsertUrlPanel.this);
				} else {
					markdownEditor.insertUrl(target, isImage, url, null, null);
					markdownEditor.closeUrlInserter(target, InsertUrlPanel.this);
				}
			}
			
		});

		BlobReferenceSupport blobReferenceSupport = markdownEditor.getBlobReferenceSupport();
		if (blobReferenceSupport != null) {
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
			
			BlobIdent rootBlobIdent = new BlobIdent(blobReferenceSupport.getRevision(), null, 
					FileMode.TYPE_TREE);
			if (!blobReferenceSupport.getProject().getChildren(rootBlobIdent, blobIdentFilter).isEmpty()) {
				add(new DropdownLink("blobPicker") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new ProjectFilePicker(id, new AbstractReadOnlyModel<Project>() {

							@Override
							public Project getObject() {
								return blobReferenceSupport.getProject();
							}
							
						}, blobReferenceSupport.getRevision()) {

							@Override
							protected void onSelect(AjaxRequestTarget target, BlobIdent blobIdent) {
								String baseUrl = markdownEditor.getBaseUrl();
								String referenceUrl = urlFor(ProjectBlobPage.class, 
										ProjectBlobPage.paramsOf(blobReferenceSupport.getProject(), blobIdent)).toString();
								String relativized = PathUtils.relativize(baseUrl, referenceUrl);							
								String relativePath = relativized.toString().substring("../".length());
								markdownEditor.insertUrl(target, isImage, relativePath, blobIdent.getName(), null);
								markdownEditor.closeUrlInserter(target, InsertUrlPanel.this);
								dropdown.close();
							}

							@Override
							protected BlobIdentFilter getBlobIdentFilter() {
								return blobIdentFilter;
							}
							
						};
					}
					
				});
			} else {
				add(new WebMarkupContainer("blobPicker").setVisible(false));
			}
		} else {
			add(new WebMarkupContainer("blobPicker").setVisible(false));
		}
		
		AttachmentSupport attachmentSupport = markdownEditor.getAttachmentSupport();
		add(new ListView<String>("attachments", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				List<String> attachmentNames = new ArrayList<>();
				if (attachmentSupport != null) {
					for (String attachmentName: attachmentSupport.getAttachments()) {
						if (!isImage || markdownEditor.isWebSafeImage(attachmentName))
							attachmentNames.add(attachmentName);
					}
				}
				return attachmentNames;
			}
			
		}) {

			@Override
			protected void populateItem(final ListItem<String> item) {
				String attachmentName = item.getModelObject();
				String attachmentUrl = attachmentSupport.getAttachmentUrl(attachmentName);
				item.add(new AjaxLink<Void>("select") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markdownEditor.insertUrl(target, isImage, attachmentUrl, attachmentName, null);
						markdownEditor.closeUrlInserter(target, InsertUrlPanel.this);
					}

					@Override
					public IModel<?> getBody() {
						String body;
						if (isImage) 
							body = "<img src='" + StringEscapeUtils.escapeHtml4(attachmentUrl) + "'></img>";
						else 
							body = "<span>" + StringEscapeUtils.escapeHtml4(item.getModelObject()) + "<span>";
						return Model.of(body);
					}
					
				}.setEscapeModelStrings(false));
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						attachmentSupport.deleteAttachemnt(attachmentName);
						target.add(InsertUrlPanel.this);
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getModelObject().isEmpty());
			}
			
		});
		
		FileUploadField uploadField = new FileUploadField("file") {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				
				String script = String.format("gitplex.server.markdown.onFileUploadDomReady('%s', %d, '%s');", 
						getMarkupId(), attachmentSupport.getAttachmentMaxSize(), 
						FileUtils.byteCountToDisplaySize(attachmentSupport.getAttachmentMaxSize()));
				response.render(OnDomReadyHeaderItem.forScript(script));
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (isImage)
					tag.put("accept", "image/*");
			}
			
		};
		
		Form<?> uploadForm = new Form<Void>("uploadForm") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				FileUpload upload = uploadField.getFileUpload();
				if (upload != null) {
					String attachmentName;
					try (InputStream is = upload.getInputStream()) {
						attachmentName = attachmentSupport.saveAttachment(upload.getClientFileName(), is);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					markdownEditor.insertUrl(target, isImage, 
							attachmentSupport.getAttachmentUrl(attachmentName), attachmentName, null);
					markdownEditor.closeUrlInserter(target, InsertUrlPanel.this);
				} else {
					error("Please select a non-empty file");
					target.add(InsertUrlPanel.this);						
				}
			}
			
			@Override
			protected void onFileUploadException(FileUploadException e, Map<String, Object> model) {
				throw new RuntimeException(e);
			}
			
		};
		if (attachmentSupport != null) 
			uploadForm.setMaxSize(Bytes.bytes(attachmentSupport.getAttachmentMaxSize()));
		uploadForm.setMultiPart(true);
		uploadForm.add(new NotificationPanel("feedback", uploadForm).setOutputMarkupPlaceholderTag(true));
		uploadForm.add(uploadField);
		uploadForm.add(new AjaxButton("submit") {});
		uploadForm.setVisible(attachmentSupport != null);
		add(uploadForm);
		
		setOutputMarkupId(true);
	}

}
