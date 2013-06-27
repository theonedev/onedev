package com.pmease.commons.persistence;

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
	
	@Inject
	public HibernateFilter(UnitOfWork unitOfWork) {
		this.unitOfWork = unitOfWork;
	}
	
	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		unitOfWork.begin();
		try {
			chain.doFilter(request, response);
		} finally {
			unitOfWork.end();
		}
	}

	public void init(FilterConfig config) throws ServletException {
	}

}
