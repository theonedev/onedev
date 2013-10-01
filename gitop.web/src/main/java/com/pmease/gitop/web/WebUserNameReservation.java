package com.pmease.gitop.web;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.core.request.mapper.ResourceMapper;
import org.apache.wicket.request.IRequestMapper;
import org.eclipse.jetty.servlet.ServletMapping;

import com.google.common.base.Preconditions;
import com.pmease.commons.jetty.JettyPlugin;
import com.pmease.commons.util.ReflectionUtils;
import com.pmease.gitop.core.validation.UserNameReservation;

public class WebUserNameReservation implements UserNameReservation {

	private final JettyPlugin jettyPlugin;
	
	private final GitopWebApp webApp;
	
	@Inject
	public WebUserNameReservation(JettyPlugin jettyPlugin, GitopWebApp webApp) {
		this.jettyPlugin = jettyPlugin;
		this.webApp = webApp;
	}
	
	@Override
	public Set<String> getReserved() {
		Set<String> reserved = new HashSet<String>();
		for (ServletMapping mapping: jettyPlugin.getContextHandler().getServletHandler().getServletMappings()) {
			for (String pathSpec: mapping.getPathSpecs()) {
				pathSpec = StringUtils.stripStart(pathSpec, "/");
				pathSpec = StringUtils.substringBefore(pathSpec, "/");
				if (pathSpec.trim().length() != 0)
					reserved.add(pathSpec.trim());
			}
		}
		
		reserved.add("wicket");
		
		for (IRequestMapper mapper: webApp.getRequestMappers()) {
			if (mapper instanceof MountedMapper || mapper instanceof ResourceMapper) {
				try {
					Field field = ReflectionUtils.findField(mapper.getClass(), "mountSegments");
					Preconditions.checkNotNull(field);
					field.setAccessible(true);
					String[] mountSegments = (String[]) field.get(mapper);
					if (mountSegments != null && mountSegments.length != 0 && mountSegments[0] != null)
						reserved.add(mountSegments[0]);
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return reserved;
	}

}
