package com.pmease.gitop.web.common.soy.impl;

import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

@Singleton
class ClasspathFileSetSupplier implements SoyFileSetSupplier {

  public static final String CFG_SOY_PACKAGES = "CFG_SOY_PACKAGES";
  private static final String SOY = ".*\\.soy";
  private static final Pattern PATTERN_SOY = Pattern.compile(SOY);
  
//  private final Set<String> packageNames;

  private final Set<String> soyPackages;
  
  @Inject
  ClasspathFileSetSupplier(@Named(CFG_SOY_PACKAGES) Set<String> soyPackages) {
    this.soyPackages = soyPackages;
  }
  
  @Override
  public Set<URL> get() {
    ConfigurationBuilder builder = new ConfigurationBuilder();
    for (String each : soyPackages) {
      builder.addUrls(ClasspathHelper.forPackage(each));
    }
    
    builder.setScanners(new ResourcesScanner());
    Reflections reflections = new Reflections(builder);
    Set<URL> files = Sets.newHashSet();
    Set<String> set = reflections.getResources(PATTERN_SOY);
    
    for (String each : set) {
      files.add(Resources.getResource(each));
    }
    
    return files;
  }
}
