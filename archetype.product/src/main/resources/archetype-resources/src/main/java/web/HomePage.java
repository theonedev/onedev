package ${package}.web;

import java.util.Collection;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.gitplex.commons.hibernate.dao.GeneralDao;
import com.gitplex.commons.loader.AppLoader;
import ${package}.model.User;

@SuppressWarnings("serial")
public class HomePage extends WebPage  {
	public HomePage() {
		add(new Link<Void>("addUser") {

			@Override
			public void onClick() {
				GeneralDao generalDao = AppLoader.getInstance(GeneralDao.class);
				Collection<User> result = generalDao.search(User.class, 
						new Criterion[]{Restrictions.eq("name", "robin")}, null, 0, 0); 
				if (result.isEmpty()) {
					User user = new User();
					user.setName("robin");
					user.setEmail("robin@example.com");
					generalDao.save(user);
				}
			}
			
		});
	}
}