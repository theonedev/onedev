package io.onedev.server.web.page.admin.ssosetting;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
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
import io.onedev.server.web.util.ConfirmClickModifier;

public class SsoProviderDetailPage extends AdministrationPage {
	
	private static final String PARAM_PROVIDER = "provider";
	
	private final IModel<SsoProvider> providerModel;
	
	public SsoProviderDetailPage(PageParameters params) {
		super(params);

		Long providerId = params.get(PARAM_PROVIDER).toOptionalLong();

		if (providerId == null)
			throw new RestartResponseException(SsoProviderListPage.class);
		
		providerModel = new LoadableDetachableModel<SsoProvider>() {
			@Override
			protected SsoProvider load() {
				return OneDev.getInstance(SsoProviderService.class).load(providerId);
			}
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var bean = SsoProviderBean.of(providerModel.getObject());
		BeanEditor editor = BeanContext.edit("editor", bean);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				SsoProvider providerWithSameName = getSsoProviderService().find(bean.getName());
				if (providerWithSameName != null && !providerWithSameName.equals(providerModel.getObject())) {
					editor.error(new Path(new PathNode.Named("name")),
							_T("This name has already been used by another provider"));
				} 
				if (editor.isValid()) {
					var provider = providerModel.getObject();
					var oldAuditContent = VersionedXmlDoc.fromBean(provider).toXML();
					bean.populate(provider);
					getSsoProviderService().createOrUpdate(provider);
					var newAuditContent = VersionedXmlDoc.fromBean(provider).toXML();
					OneDev.getInstance(AuditService.class).audit(null, "changed sso provider \"" + provider.getName() + "\"", oldAuditContent, newAuditContent);
					Session.get().success(_T("SSO provider updated"));
					setResponsePage(SsoProviderListPage.class);
				}
			}
			
		};
		form.add(editor);

		form.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				var oldAuditContent = VersionedXmlDoc.fromBean(providerModel.getObject()).toXML();
				getSsoProviderService().delete(providerModel.getObject());
				OneDev.getInstance(AuditService.class).audit(null, "deleted SSO provider \"" + providerModel.getObject().getName() + "\"", oldAuditContent, null);
				Session.get().success(MessageFormat.format(_T("SSO provider \"{0}\" deleted"), providerModel.getObject().getName()));
				setResponsePage(SsoProviderListPage.class);
			}
			
		}.add(new ConfirmClickModifier(MessageFormat.format(_T("Do you really want to delete SSO provider \"{0}\"?"), providerModel.getObject().getName()))));
		
		add(form);
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

	private SsoProviderService getSsoProviderService() {
		return OneDev.getInstance(SsoProviderService.class);
	}

	@Override
	protected void onDetach() {
		providerModel.detach();
		super.onDetach();
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "topbarTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("ssoProviders", SsoProviderListPage.class));
		fragment.add(new Label("providerName", providerModel.getObject().getName()));
		return fragment;
	}

	public static PageParameters paramsOf(SsoProvider ssoProvider) {
		var params = new PageParameters();
		params.add(PARAM_PROVIDER, ssoProvider.getId());
		return params;
	}

}
