package com.pmease.gitplex.web.service;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.User;

public interface AvatarManager {
	
	String getAvatarUrl(@Nullable User user);
	
	String getAvatarUrl(String email);
	
	String getDefaultAvatarUrl();
}

