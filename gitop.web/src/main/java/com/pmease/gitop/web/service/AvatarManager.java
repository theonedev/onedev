package com.pmease.gitop.web.service;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.service.impl.DefaultAvatarManager;

@ImplementedBy(DefaultAvatarManager.class)
public interface AvatarManager {
	
	String getAvatarUrl(@Nullable User user);
	
	String getAvatarUrl(String email);
	
	String getDefaultAvatarUrl();
}

