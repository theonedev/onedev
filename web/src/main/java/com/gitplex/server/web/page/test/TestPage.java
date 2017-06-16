package com.gitplex.server.web.page.test;

import java.io.File;

import org.apache.wicket.markup.html.link.Link;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.migration.VersionedDocument;
import com.gitplex.server.security.authenticator.ldap.LdapAuthenticator;
import com.gitplex.server.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("save") {

			@Override
			public void onClick() {
				LdapAuthenticator authenticator = new LdapAuthenticator();
				authenticator.setManagerPassword("12345");
				authenticator.setManagerDN("robin");
				authenticator.setLdapUrl("ldaps://localhost:389");
				authenticator.setUserSearchFilter("&(ab)");
				VersionedDocument dom = VersionedDocument.fromBean(authenticator);
				dom.writeToFile(new File("w:\\temp\\authenticator.xml"), true);
				GitPlex.getInstance(ConfigManager.class).saveAuthenticator(authenticator);
			}
			
		});
		
		add(new Link<Void>("load") {

			@Override
			public void onClick() {
				VersionedDocument dom = VersionedDocument.fromFile(new File("w:\\temp\\authenticator.xml"));
				System.out.println(dom.asXML());
				LdapAuthenticator authenticator = (LdapAuthenticator) dom.toBean();
				System.out.println(authenticator.getUserSearchFilter());
			}
			
		});
		
	}

}
