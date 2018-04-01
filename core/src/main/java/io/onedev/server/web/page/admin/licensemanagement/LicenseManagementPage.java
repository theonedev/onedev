package io.onedev.server.web.page.admin.licensemanagement;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.joda.time.DateTime;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.ConfigManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.Constants;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.utils.license.LicenseDetail;

@SuppressWarnings("serial")
public class LicenseManagementPage extends AdministrationPage {

	private static final String LICENSE_DETAIL_ID = "licenseDetail";
	
	private String licenseKey;
	
	private final IModel<LicenseDetail> licenseModel = new LoadableDetachableModel<LicenseDetail>() {

		@Override
		protected LicenseDetail load() {
			return OneDev.getInstance(ConfigManager.class).getLicense();
		}
		
	};
	
	private final IModel<Integer> userCountModel = new LoadableDetachableModel<Integer>() {

		@Override
		protected Integer load() {
			return OneDev.getInstance(UserManager.class).count(EntityCriteria.of(User.class));
		}
		
	};
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		LicenseDetail licenseDetail = getLicense();
		if (licenseDetail != null) {
			Fragment fragment = new Fragment(LICENSE_DETAIL_ID, "licenseDetailFrag", this);
			fragment.add(new Label("expiredNotice", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					return String.format("License was expired. The free %d-user license is now taking effect", 
							LicenseDetail.FREE_LICENSE_USERS);
				}
				
			}).setVisible(licenseDetail.getRemainingDays()<0).setEscapeModelStrings(false));
			
			fragment.add(new Label("aboutToExpireNotice", new LoadableDetachableModel<String>() {

				@Override
				protected String load() {
					if (getUserCount() > LicenseDetail.FREE_LICENSE_USERS) {
						return String.format(""
								+ "License will expire in %d days. The free %d-user license will take effect "
								+ "after expiration", licenseDetail.getRemainingDays(), LicenseDetail.FREE_LICENSE_USERS);
					} else {
						return String.format("License will expire in %d days, The free %d-user license will take "
								+ "effect after expiration", licenseDetail.getRemainingDays(), 
								LicenseDetail.FREE_LICENSE_USERS);
					}
				}
				
			}).setVisible(licenseDetail.getRemainingDays()>=0 && licenseDetail.getRemainingDays() < LicenseDetail.ABOUT_TO_EXPIRE_DAYS).setEscapeModelStrings(false));
			
			String usersInfo = String.format("%d free + %d licensed (<a href=\"https://www.onedev.io/purchase\">add more</a>)", 
					LicenseDetail.FREE_LICENSE_USERS, licenseDetail.getLicensedUsers());
			fragment.add(new Label("users", usersInfo).setEscapeModelStrings(false));
			fragment.add(new Label("issueDate", 
					Constants.DATE_FORMATTER.print(new DateTime(licenseDetail.getIssueDate()))));
			fragment.add(new Label("expirationDate", 
					Constants.DATE_FORMATTER.print(new DateTime(licenseDetail.getExpirationDate())) + " (<a href=\"https://www.onedev.io/purchase\">renew</a>)").setEscapeModelStrings(false));
			fragment.add(new Label("issueAccount", licenseDetail.getIssueAccount()));
			add(fragment);
		} else {
			if (getUserCount() > LicenseDetail.FREE_LICENSE_USERS) {
				String message = String.format(""
						+ "Free %d-user license. <a href=\"https://www.onedev.io/purchase\">Add additional users</a>",
						LicenseDetail.FREE_LICENSE_USERS);
				add(new Label(LICENSE_DETAIL_ID, message).setEscapeModelStrings(false));
			} else {
				String message = String.format(""
						+ "Free %d-user license. <a href=\"https://www.onedev.io/purchase\">Add additional users</a>", 
						LicenseDetail.FREE_LICENSE_USERS);
				add(new Label(LICENSE_DETAIL_ID, message).setEscapeModelStrings(false));
			}
		}
		
		add(new ModalLink("inputLicenseKey") {
			
			@Override
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "licenseKeyInputFrag", LicenseManagementPage.this);
				licenseKey = null;
				Form<?> form = new Form<Void>("form");
				form.add(new TextArea<String>("licenseKey", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return licenseKey;
					}

					@Override
					public void setObject(String object) {
						licenseKey = object;
					}
					
				}));
				
				NotificationPanel feedback = new NotificationPanel("feedback", fragment);
				feedback.setOutputMarkupId(true);
				form.add(feedback);
				
				form.add(new AjaxButton("ok") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						if (licenseKey == null) {
							error("Please input license key");
							target.add(feedback);
						} else {
							LicenseDetail license = LicenseDetail.decode(licenseKey);
							if (license == null) {
								error("Invalid license key");
								target.add(feedback);
							} else {
								OneDev.getInstance(ConfigManager.class).saveLicense(license);
								setResponsePage(LicenseManagementPage.class);
								getSession().success("License key applied successfully");
							}
						}
					}
					
				});
				
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				
				form.add(new AjaxLink<Void>("close") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				
				fragment.add(form);
				
				return fragment;
			}
			
		});
		
		String confirmMessage = String.format(""
				+ "The free-%d user license will take effect after removal. Do you really want to continue?", 
				LicenseDetail.FREE_LICENSE_USERS);
		add(new Link<Void>("removeLicense") {

			@Override
			public void onClick() {
				OneDev.getInstance(ConfigManager.class).saveLicense(null);
				setResponsePage(LicenseManagementPage.class);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getLicense() != null);
			}
			
		}.add(new ConfirmOnClick(confirmMessage)));
	}

	@Nullable
	private LicenseDetail getLicense() {
		return licenseModel.getObject();
	}
	
	private int getUserCount() {
		return userCountModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		userCountModel.detach();
		licenseModel.detach();
		super.onDetach();
	}

}
