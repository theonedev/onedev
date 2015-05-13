package com.pmease.gitplex.web.page.account.list;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.avatar.AvatarManager;
import com.pmease.gitplex.web.component.avatar.AvatarPicker;
import com.pmease.gitplex.web.page.layout.MaintabPage;

@SuppressWarnings("serial")
public abstract class AvatarEditPage extends MaintabPage {

	private final User account;
	
	private FileUpload upload;
	
	public AvatarEditPage(User account) {
		this.account = account;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		add(new Label("title", "Change Avatar of " + account.getDisplayName()));
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				GitPlex.getInstance(AvatarManager.class).useAvatar(account, upload);
				onComplete();
			}
			
		};
		add(form);
		
		form.add(new AvatarPicker("avatarPicker", account, new IModel<FileUpload>() {

			@Override
			public void detach() {
			}

			@Override
			public FileUpload getObject() {
				return upload;
			}

			@Override
			public void setObject(FileUpload object) {
				upload = object;
			}
			
		}));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				onComplete();
			}
			
		});
	}

	protected abstract void onComplete();
}
