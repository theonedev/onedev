package com.pmease.gitop.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.pmease.commons.jetty.ClasspathAssetServlet;
import com.pmease.commons.jetty.extensionpoints.ServletContextConfigurator;
import com.pmease.commons.loader.AbstractPlugin;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.commons.persistence.extensionpoints.ModelContribution;
import com.pmease.commons.util.ClassUtils;
import com.pmease.gitop.core.model.ModelLocator;
import com.pmease.gitop.core.web.asset.AssetLocator;

public class CorePlugin extends AbstractPlugin {

	@Override
	public Collection<?> getExtensions() {
		return Arrays.asList(
				new ServletContextConfigurator() {

					@Override
					public void configure(ServletContextHandler context) {
						ServletHolder servletHolder = new ServletHolder(new ClasspathAssetServlet(AssetLocator.class));
						context.addServlet(servletHolder, "/asset/*");
						context.addServlet(servletHolder, "/favicon.ico");
						
						ErrorPageErrorHandler errorHandler = (ErrorPageErrorHandler) context.getErrorHandler();
						errorHandler.addErrorPage(HttpServletResponse.SC_NOT_FOUND, "/asset/404.html");
					}
					
				},				
				new ModelContribution() {
			
					@Override
					public Collection<Class<? extends AbstractEntity>> getModelClasses() {
						Collection<Class<? extends AbstractEntity>> modelClasses = 
								new HashSet<Class<? extends AbstractEntity>>();
						modelClasses.addAll(ClassUtils.findSubClasses(AbstractEntity.class, ModelLocator.class));
						return modelClasses;
					}
		});
	}

	@Override
	public void postStart() {
	}

}
