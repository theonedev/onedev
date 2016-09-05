package com.pmease.commons.hibernate;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HibernateFilter implements Filter {
	
	private final UnitOfWork unitOfWork;
	
	private final PersistService persistService;
	
	@Inject
	public HibernateFilter(UnitOfWork unitOfWork, PersistService persistService) {
		this.unitOfWork = unitOfWork;
		this.persistService = persistService;
	}
	
	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (persistService.isReady()) {
			unitOfWork.begin();
			try {
				chain.doFilter(request, response);
			} finally {
				unitOfWork.end();
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	public void init(FilterConfig config) throws ServletException {
	}

}
