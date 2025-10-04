package io.onedev.server.web.page.admin.ssosetting;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.SsoProviderService;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.groupmanagement.GroupCssResourceReference;

public class NewSsoProviderPage extends AdministrationPage {
	
	public NewSsoProviderPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var bean = new SsoProviderBean();		
		BeanEditor editor = BeanContext.edit("editor", bean);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				SsoProvider providerWithSameName = getSsoProviderService().find(bean.getName());
				if (providerWithSameName != null) {
					editor.error(new Path(new PathNode.Named("name")),
							_T("This name has already been used by another provider"));
				} 
				if (editor.isValid()) {			
					var provider = new SsoProvider();
					bean.populate(provider);
					getSsoProviderService().createOrUpdate(provider);
					var newAuditContent = VersionedXmlDoc.fromBean(provider).toXML();
					OneDev.getInstance(AuditService.class).audit(null, "created SSO provider \"" + provider.getName() + "\"", null, newAuditContent);
					Session.get().success(_T("SSO provider created"));
					setResponsePage(SsoProviderListPage.class);
				}
			}
			
		};
		form.add(editor);
		add(form);
	}

	private SsoProviderService getSsoProviderService() {
		return OneDev.getInstance(SsoProviderService.class);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroupCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("ssoProviders", SsoProviderListPage.class));
		return fragment;
	}

}
