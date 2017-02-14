package com.gitplex.commons.wicket.behavior.markdown;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Bytes;

import com.gitplex.commons.util.FileUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
class SelectUrlPanel extends Panel {

	private String url;
	
	private final MarkdownBehavior markdownBehavior;
	
	private final boolean isImage;
	
	public SelectUrlPanel(String id, MarkdownBehavior markdownBehavior, boolean isImage) {
		super(id);
		this.markdownBehavior = markdownBehavior;
		this.isImage = isImage;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (isImage)
			add(new Label("title", "Insert Image"));
		else
			add(new Label("title", "Insert Link"));
			
		Form<?> urlForm = new Form<Void>("form");
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
		
		urlForm.add(new AjaxButton("insert", urlForm) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (StringUtils.isBlank(url)
						|| url.startsWith("http://") && url.length() == 7
						|| url.startsWith("https://") && url.length() == 8) {
					error("Url should be specified");
					target.add(SelectUrlPanel.this);
				} else if (!url.startsWith("http://") && !url.startsWith("https://")) {
					error("Url should start with http:// or https://");
					target.add(SelectUrlPanel.this);
				} else {
					markdownBehavior.insertUrl(target, isImage, url, null, null);
					markdownBehavior.closeUrlSelector(target, SelectUrlPanel.this);
				}
			}
			
		});
		
		AttachmentSupport attachmentSupport = markdownBehavior.getAttachmentSupport();
		if (attachmentSupport != null) {
			if (isImage)
				urlField.add(AttributeAppender.append("placeholder", "Input image url here or select below"));
			else
				urlField.add(AttributeAppender.append("placeholder", "Input link url here or select below"));
				
			Fragment fragment = new Fragment("attachments", "attachmentsFrag", this);
			fragment.setOutputMarkupId(true);
			
			fragment.add(new ListView<String>("attachments", new LoadableDetachableModel<List<String>>() {

				@Override
				protected List<String> load() {
					List<String> attachmentNames = new ArrayList<>();
					for (String attachmentName: attachmentSupport.getAttachments()) {
						if (!isImage || markdownBehavior.isWebSafeImage(attachmentName))
							attachmentNames.add(attachmentName);
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
							markdownBehavior.insertUrl(target, isImage, attachmentUrl, attachmentName, null);
							markdownBehavior.closeUrlSelector(target, SelectUrlPanel.this);
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
							target.add(SelectUrlPanel.this);
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
					
					String script = String.format("gitplex.commons.markdown.initFileUpload('%s', %d, '%s');", 
							getMarkupId(), attachmentSupport.getAttachmentMaxSize(), 
							FileUtils.byteCountToDisplaySize(attachmentSupport.getAttachmentMaxSize()));
					response.render(OnDomReadyHeaderItem.forScript(script));
				}
				
			};
			Form<?> fileForm = new Form<Void>("form") {

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
						markdownBehavior.insertUrl(target, isImage, 
								attachmentSupport.getAttachmentUrl(attachmentName), attachmentName, null);
						markdownBehavior.closeUrlSelector(target, SelectUrlPanel.this);
					}
				}
				
				@Override
				protected void onFileUploadException(FileUploadException e, Map<String, Object> model) {
					throw new RuntimeException(e);
				}
				
			};
			fileForm.setMaxSize(Bytes.bytes(attachmentSupport.getAttachmentMaxSize()));
			fileForm.setMultiPart(true);
			fileForm.add(uploadField);
			fileForm.add(new AjaxButton("submit") {});
			
			fragment.add(fileForm);
			
			if (isImage) {
				fragment.add(new Label("hint", "you may drag and drop to insert image directly without "
						+ "opening this dialog, or paste image from clipboard."));
			} else {
				fragment.add(new Label("hint", "you may drag and drop to attach file directly without "
						+ "opening this dialog."));
			}
			
			add(fragment);
		} else {
			if (isImage)
				urlField.add(AttributeAppender.append("placeholder", "Input image url here"));
			else
				urlField.add(AttributeAppender.append("placeholder", "Input link url here"));
				
			add(new WebMarkupContainer("attachments"));
		}
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				markdownBehavior.closeUrlSelector(target, SelectUrlPanel.this);
			}
			
		});

		setOutputMarkupId(true);
	}

}
