package com.pmease.commons.product.web;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.hibernate.criterion.DetachedCriteria;

import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.product.model.User;

@SuppressWarnings("serial")
public class TestPage extends WebPage {
	
	public TestPage() {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		AppLoader.getInstance(GeneralDao.class).search(criteria, 0, 200).size();
		
		Form<?> form = new Form<Void>("form") {
			
		};
		add(form);
		form.add(new ListView<User>("persons", new LoadableDetachableModel<List<User>>() {

			@SuppressWarnings("unchecked")
			@Override
			protected List<User> load() {
				DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
				return (List<User>) AppLoader.getInstance(GeneralDao.class).search(criteria, 0, 1);
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<User> item) {
				User u = item.getModelObject();
				item.add(new Label("name", Model.of(u.getName())));
				item.add(new Label("age", Model.of(u.getEmail())));
				item.add(new Label("sex", Model.of(u.getSex())));
				item.add(new Label("nation", Model.of(u.getPassword())));
				
				item.add(new Link<Void>("edit") {

					@Override
					public void onClick() {
						Panel panel = (Panel) TestPage.this.get("panel");
						panel.replaceWith(new Panel2(panel.getId()).setOutputMarkupId(true));
//						target.add(TestPage.this.get("panel"));
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});
			}
			
		});
		
		add(new Panel1("panel").setOutputMarkupId(true));
	}		
	
}