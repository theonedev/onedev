package com.gitplex.http;

import java.util.EnumSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.gitplex.commons.jetty.ServletConfigurator;

@Singleton
public class HttpServletConfigurator implements ServletConfigurator {

    private final GitFilter gitFilter;
    
    @Inject
    public HttpServletConfigurator(GitFilter gitFilter) {
        this.gitFilter = gitFilter;
    }
    
    @Override
    public void configure(ServletContextHandler context) {
        FilterHolder filterHolder = new FilterHolder(gitFilter);
        context.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));

        /*
        ServletHolder servletHolder = new ServletHolder(new GitServlet());
        servletHolder.setInitParameter("export-all", "1");
        servletHolder.setInitParameter("base-path", "w:\\temp\\storage\\1");
        context.addServlet(servletHolder, "/git/*");
        */
    }

}
