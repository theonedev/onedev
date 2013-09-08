package com.pmease.gitop.product;

import java.io.File;
import java.util.Properties;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.Hibernate;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.loader.AbstractPluginModule;
import com.pmease.commons.loader.AppName;
import com.pmease.commons.util.FileUtils;

public class ProductModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();
		
		bindConstant().annotatedWith(AppName.class).to(Product.NAME);
		
		Properties hibernateProps = FileUtils.loadProperties(
				new File(Bootstrap.installDir, "conf/hibernate.properties")); 
		bind(Properties.class).annotatedWith(Hibernate.class).toInstance(hibernateProps);
	}

	@Override
	protected Class<? extends AbstractPlugin> getPluginClass() {
		return Product.class;
	}

}
