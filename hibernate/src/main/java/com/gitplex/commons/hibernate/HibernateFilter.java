package com.gitplex.commons.hibernate;

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
	
	private final PersistManager persistManager;
	
	@Inject
	public HibernateFilter(UnitOfWork unitOfWork, PersistManager persistManager) {
		this.unitOfWork = unitOfWork;
		this.persistManager = persistManager;
	}
	
	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (persistManager.getSessionFactory() != null) {
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
