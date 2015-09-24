package com.pmease.commons.wicket.behavior.markdown;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Iterables;
import com.pmease.commons.wicket.ImageUtils;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;

@SuppressWarnings("serial")
public class InsertImagePanel extends Panel {

	private static final int IMAGE_COLS = 2;
	
	private String url = "http://";
	
	private final MarkdownBehavior markdownBehavior;
	
	public InsertImagePanel(String id, MarkdownBehavior markdownBehavior) {
		super(id);
		this.markdownBehavior = markdownBehavior;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
			}
			
		};
		form.add(new FeedbackPanel("feedback", form));
		
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
		form.add(urlField);
		
		final AttachmentSupport attachmentSupport = getAttachmentSupport();
		if (attachmentSupport != null) {
			Fragment fragment = new Fragment("attachments", "attachmentsFrag", this);
			fragment.setOutputMarkupId(true);
			
			final IModel<List<List<String>>> rowsModel = new LoadableDetachableModel<List<List<String>>>() {

				@Override
				protected List<List<String>> load() {
					List<List<String>> rows = new ArrayList<>();
					List<String> images = new ArrayList<>();
					if (attachmentSupport.getStoreDir().exists()) {
						for (File file: attachmentSupport.getStoreDir().listFiles()) {
							if (ImageUtils.isWebSafe(file.getName()))
								images.add(file.getName());
						}
					}
					for (List<String> row: Iterables.partition(images, IMAGE_COLS)) {
						rows.add(row);
					}
					return rows;
				}
				
			};
			fragment.add(new ListView<List<String>>("rows") {

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
			final FileUploadField uploadField = new FileUploadField("file");
			uploadField.add(new OnChangeAjaxBehavior() {
				
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					System.out.println(uploadField.getFileUpload().getClientFileName());
				}
				
			});
			fragment.add(uploadField);
			add(fragment);
		} else {
			add(new WebMarkupContainer("attachments"));
		}
		
		form.add(new AjaxButton("insert") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				if (StringUtils.isBlank(url)) {
					form.error("Url should not be empty");
					target.add(form);
				} else if (!url.startsWith("http://") && !url.startsWith("https://")) {
					form.error("Url should start with http:// or https://");
					target.add(form);
				} else {
					markdownBehavior.insertImage(target, url);
				}
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				markdownBehavior.cancelInsertImage(target);
			}
			
		});
		form.setOutputMarkupId(true);
		add(form);
	}

	@Nullable
	protected AttachmentSupport getAttachmentSupport() {
		return null;
	}

}
