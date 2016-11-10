package ${package};

import org.apache.wicket.Page;

import com.gitplex.commons.wicket.AbstractWicketConfig;

import ${package}.web.ErrorPage404;
import ${package}.web.HomePage;

public class WicketConfig extends AbstractWicketConfig {

	@Override
	protected void init() {
		super.init();
		
		mountPage("/404", ErrorPage404.class);
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

}
