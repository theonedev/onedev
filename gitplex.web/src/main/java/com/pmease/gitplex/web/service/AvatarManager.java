package com.pmease.gitplex.web.service;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.service.impl.DefaultAvatarManager;

@ImplementedBy(DefaultAvatarManager.class)
public interface AvatarManager {
	
	String getAvatarUrl(@Nullable User user);
	
	String getAvatarUrl(String email);
	
	String getDefaultAvatarUrl();
}

