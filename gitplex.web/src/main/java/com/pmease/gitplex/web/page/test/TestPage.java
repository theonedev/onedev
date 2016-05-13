package com.pmease.gitplex.web.page.test;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.pmease.commons.lang.extractors.java.JavaExtractor;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();

		JavaExtractor extractor = new JavaExtractor();
		try {
			extractor.extract(FileUtils.readFileToString(new File("w:\\linux\\MAINTAINERS")));
		} catch (Exception e) {
		}
	}

}
