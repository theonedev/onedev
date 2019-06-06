package io.onedev.server.web.component.user.tokengenerate;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.codec.Base64;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.User;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
public class TokenGeneratePanel extends GenericPanel<User> {
	
	private String token;
	
	public TokenGeneratePanel(String id, IModel<User> model) {
		super(id, model);
	}

	private User getUser() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TokenGenerateBean bean = new TokenGenerateBean();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			public void process(IFormSubmitter submittingComponent) {
				SecurityUtils.getSubject().runAs(getUser().getPrincipals());
				try {
					super.process(submittingComponent);
				} finally {
					SecurityUtils.getSubject().releaseRunAs();
				}
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				token = new String(Base64.encode((getUser().getName() + ":" + bean.getPassword()).getBytes()));
				
				bean.setPassword(null);
				replace(BeanContext.edit("editor", bean));
			}

		};
		add(form);
		
		form.add(BeanContext.edit("editor", bean));
		
		add(new Label("token", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return token;
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(token != null);
			}
			
		});
		add(new WebMarkupContainer("copy").add(new CopyClipboardBehavior(new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return token;
			}
			
		})));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new TokenGenerateCssResourceReference()));
	}
	
}
