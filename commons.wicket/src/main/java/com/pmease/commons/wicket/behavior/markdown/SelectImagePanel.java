package com.pmease.commons.wicket.behavior.markdown;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

import com.google.common.collect.Iterables;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;

@SuppressWarnings("serial")
class SelectImagePanel extends Panel {

	private static final int IMAGE_COLS = 2;
	
	private String url;
	
	private final MarkdownBehavior markdownBehavior;
	
	public SelectImagePanel(String id, MarkdownBehavior markdownBehavior) {
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
				return url;
			}

			@Override
			public void setObject(String object) {
				url = object;
			}
			
		}); 
		urlField.setOutputMarkupId(true);
		form.add(urlField);
		
		final AttachmentSupport attachmentSupport = markdownBehavior.getAttachmentSupport();
		if (attachmentSupport != null) {
			urlField.add(AttributeAppender.append("placeholder", "Input image url here or select image below"));
			final Fragment fragment = new Fragment("attachments", "attachmentsFrag", this);
			fragment.setOutputMarkupId(true);
			
			final IModel<List<List<String>>> rowsModel = new LoadableDetachableModel<List<List<String>>>() {

				@Override
				protected List<List<String>> load() {
					List<List<String>> rows = new ArrayList<>();
					List<String> images = new ArrayList<>();
					for (String attachment: attachmentSupport.getAttachments()) {
						if (markdownBehavior.isWebSafeImage(attachment))
							images.add(attachment);
					}
					for (List<String> row: Iterables.partition(images, IMAGE_COLS)) {
						rows.add(row);
					}
					return rows;
				}
				
			};
			fragment.add(new ListView<List<String>>("rows", rowsModel) {

				@Override
				protected void populateItem(ListItem<List<String>> rowItem) {
					rowItem.add(new ListView<String>("columns", rowItem.getModel()) {

						@Override
						protected void populateItem(ListItem<String> columnItem) {
							String imageUrl = attachmentSupport.getAttachmentUrl(columnItem.getModelObject());
							WebMarkupContainer link = new WebMarkupContainer("link");
							String script = String.format("$('#%s').val('%s');", 
									urlField.getMarkupId(true), 
									StringEscapeUtils.escapeEcmaScript(imageUrl));
							link.add(AttributeAppender.append("onclick", script));
							link.add(new WebMarkupContainer("image").add(AttributeAppender.append("src", imageUrl)));
							columnItem.add(link);
						}
						
					});
				}
				
			});
			fragment.add(new WebMarkupContainer("noImages") {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					
					setVisible(rowsModel.getObject().isEmpty());
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
						String attachment;						
						try (InputStream is = upload.getInputStream()) {
							attachment = attachmentSupport.saveAttachment(upload.getClientFileName(), is);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						target.add(SelectImagePanel.this);
						String imageUrl = attachmentSupport.getAttachmentUrl(attachment);						
						String script = String.format("$('#%s').val('%s');", 
								urlField.getMarkupId(), 
								StringEscapeUtils.escapeEcmaScript(imageUrl));
						target.appendJavaScript(script);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target) {
					super.onError(target);
					target.add(SelectImagePanel.this);
				}
				
			});
			fileForm.add(uploadField);
			add(fragment);
		} else {
			urlField.add(AttributeAppender.append("placeholder", "Input image url here"));
			add(new WebMarkupContainer("attachments"));
		}
		
		add(new AjaxButton("insert", form) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (StringUtils.isBlank(url)) {
					form.error("Image url should be specified");
					target.add(SelectImagePanel.this);
				} else if (!url.startsWith("http://") && !url.startsWith("https://")) {
					form.error("Image url should start with http:// or https://");
					target.add(SelectImagePanel.this);
				} else {
					markdownBehavior.insertImage(target, url);
				}
			}
			
		});
		add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				markdownBehavior.cancelInsertImage(target);
			}
			
		});
		add(form);
		
		setOutputMarkupId(true);
	}

}
