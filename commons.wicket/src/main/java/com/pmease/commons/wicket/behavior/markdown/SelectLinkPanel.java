package com.pmease.commons.wicket.behavior.markdown;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.upload.FileUploadBase.SizeLimitExceededException;
import org.apache.wicket.util.upload.FileUploadException;

import com.pmease.commons.wicket.behavior.FormComponentInputBehavior;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;

@SuppressWarnings("serial")
class SelectLinkPanel extends Panel {

	private String linkName;
	
	private String linkUrl;
	
	private final MarkdownBehavior markdownBehavior;
	
	public SelectLinkPanel(String id, MarkdownBehavior markdownBehavior) {
		super(id);
		this.markdownBehavior = markdownBehavior;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new FeedbackPanel("feedback", this));
		
		Form<?> form = new Form<Void>("form");
		
		final TextField<String> urlField = new TextField<String>("url", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return linkUrl;
			}

			@Override
			public void setObject(String object) {
				linkUrl = object;
			}
			
		}); 
		urlField.add(new FormComponentInputBehavior() {

			@Override
			protected void onInput(AjaxRequestTarget target) {
				linkName = null;
			}

		});
		urlField.setOutputMarkupId(true);
		form.add(urlField);
		
		final AttachmentSupport attachmentSupport = markdownBehavior.getAttachmentSupport();
		if (attachmentSupport != null) {
			urlField.add(AttributeAppender.append("placeholder", "Input link url here or select link below"));
			final Fragment fragment = new Fragment("attachments", "attachmentsFrag", this);
			fragment.setOutputMarkupId(true);
			
			final IModel<List<String>> attachmentsModel = new LoadableDetachableModel<List<String>>() {

				@Override
				protected List<String> load() {
					return attachmentSupport.getAttachments();
				}
				
			};
			fragment.add(new ListView<String>("attachments", attachmentsModel) {

				@Override
				protected void populateItem(final ListItem<String> item) {
					final String imageUrl = attachmentSupport.getAttachmentUrl(item.getModelObject());
					AjaxLink<Void> link = new AjaxLink<Void>("link") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							linkName = item.getModelObject();
							String script = String.format("$('#%s').val('%s');", 
									urlField.getMarkupId(), 
									StringEscapeUtils.escapeEcmaScript(imageUrl));
							target.appendJavaScript(script);
						}
						
					};
					link.add(new Label("name", item.getModelObject()));
					item.add(link);
				}
				
			});
			fragment.add(new WebMarkupContainer("noAttachments") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					
					setVisible(attachmentsModel.getObject().isEmpty());
				}
				
			});
			
			Form<?> fileForm = new Form<Void>("form") {
				
				@Override
				protected void onFileUploadException(FileUploadException e, Map<String, Object> model) {
					if (e instanceof SizeLimitExceededException) 
					    error("Upload must be less than " + FileUtils.byteCountToDisplaySize(getMaxSize().bytes()));
				}
				
			};
			fileForm.setMaxSize(Bytes.bytes(attachmentSupport.getAttachmentMaxSize()));
			fileForm.setMultiPart(true);
			fragment.add(fileForm);
			final FileUploadField uploadField = new FileUploadField("file");
			uploadField.add(new AjaxFormSubmitBehavior("change") {

				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					super.onSubmit(target);
					FileUpload upload = uploadField.getFileUpload();
					if (upload != null) {
						try (InputStream is = upload.getInputStream()) {
							linkName = attachmentSupport.saveAttachment(upload.getClientFileName(), is);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						target.add(SelectLinkPanel.this);
						String linkUrl = attachmentSupport.getAttachmentUrl(linkName);	
						String script = String.format("$('#%s').val('%s');", 
								urlField.getMarkupId(), 
								StringEscapeUtils.escapeEcmaScript(linkUrl));
						target.appendJavaScript(script);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target) {
					super.onError(target);
					target.add(SelectLinkPanel.this);
				}
				
			});
			fileForm.add(uploadField);
			add(fragment);
		} else {
			urlField.add(AttributeAppender.append("placeholder", "Input link url here"));
			add(new WebMarkupContainer("attachments"));
		}
		
		add(new AjaxButton("insert", form) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (StringUtils.isBlank(linkUrl)
						|| linkUrl.startsWith("http://") && linkUrl.length() == 7
						|| linkUrl.startsWith("https://") && linkUrl.length() == 8) {
					error("Link url should be specified");
					target.add(SelectLinkPanel.this);
				} else if (!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
					error("Link url should start with http:// or https://");
					target.add(SelectLinkPanel.this);
				} else {
					markdownBehavior.insertUrl(target, false, linkUrl, linkName);
					markdownBehavior.closeUrlSelector(target, SelectLinkPanel.this);
				}
			}
			
		});
		add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				markdownBehavior.closeUrlSelector(target, SelectLinkPanel.this);
			}
			
		});
		add(form);
		
		setOutputMarkupId(true);
	}

}
