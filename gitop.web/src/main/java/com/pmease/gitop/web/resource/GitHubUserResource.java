package com.pmease.gitop.web.resource;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authc.credential.PasswordService;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

@Path("/github")
public class GitHubUserResource {

	@Inject ObjectMapper objectMapper;
	@Inject UserManager userManager;
	@Inject PasswordService passwordService;
	
	@GET
	@Path("{org}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> get(@PathParam("org") String org) {
		try {
			return updateDatabase(org);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
	
	private List<User> updateDatabase(String org) throws JsonParseException,
			JsonMappingException, UniformInterfaceException,
			ClientHandlerException, IOException {
		Client client = Client.create();
		client.addFilter(new HTTPBasicAuthFilter("steveluo", "hongmei9"));
		WebResource r = client.resource("https://api.github.com/orgs/" + org
				+ "/public_members");
		String content = r.get(String.class);
		objectMapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		List<HubUser> users = objectMapper.readValue(content,
				new TypeReference<List<HubUser>>() {
				});
		
		List<User> result = Lists.newArrayList();
		
		for (HubUser each : users) {
			try {
				r = client.resource("https://api.github.com/users/"
						+ each.login);
				HubUser u = objectMapper.readValue(r.get(String.class),
						HubUser.class);
				User user = new User();
				user.setName(u.login);
				user.setFullName(u.name);

				if (Strings.isNullOrEmpty(u.email)) {
					user.setEmailAddress(u.login + "@github.com");
				} else {
					user.setEmailAddress(u.email);
				}

				System.out.println(user);
				result.add(user);
//				user.setPassword("12345");
//				userManager.save(user);
//				System.out.println(user.getId() + " " + user);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}

	public static class HubUsers {
		@JsonProperty
		List<HubUser> users = Lists.newArrayList();

		public List<HubUser> getUsers() {
			return users;
		}

		public void setUsers(List<HubUser> users) {
			this.users = users;
		}
	}

	public static class HubUser {
		@JsonProperty
		private String login;

		@JsonProperty
		private String name;

		@JsonProperty
		private String email;

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}
	}

}
