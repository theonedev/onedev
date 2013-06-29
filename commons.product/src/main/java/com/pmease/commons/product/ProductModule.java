package com.pmease.commons.product;

import java.io.File;
import java.util.Properties;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.loader.AppName;
import com.pmease.commons.persistence.Hibernate;
import com.pmease.commons.security.AbstractRealm;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.web.AbstractWicketConfig;

public class ProductModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();
		
		Properties hibernateProps = FileUtils.loadProperties(
				new File(Bootstrap.installDir, "conf/hibernate.properties")); 
		bind(Properties.class).annotatedWith(Hibernate.class).toInstance(hibernateProps);

		bind(AbstractWicketConfig.class).to(WicketConfig.class);		
		bind(AbstractRealm.class).to(UserRealm.class);
		
		bindConstant().annotatedWith(AppName.class).to(Product.PRODUCT_NAME);
		
		bind(HelloResource.class);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return Product.class;
	}

}
