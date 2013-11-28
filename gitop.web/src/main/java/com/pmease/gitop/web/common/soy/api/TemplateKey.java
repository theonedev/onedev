package com.pmease.gitop.web.common.soy.api;

import java.io.Serializable;

import com.google.common.base.Objects;

public class TemplateKey implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private final String namespace;
  private final String templateName;
  
  TemplateKey(final String namespace, final String templateName) {
    this.namespace = namespace;
    this.templateName = templateName;
  }
  
  public static TemplateKey of(String namespace, String templateName) {
    return new TemplateKey(namespace, templateName);
  }

  public String getNamespace() {
    return namespace;
  }

  public String getTemplateName() {
    return templateName;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof TemplateKey)) return false;
    if (this == other) return true;
    
    TemplateKey rhs = (TemplateKey) other;
    return Objects.equal(this.namespace, rhs.namespace)
        && Objects.equal(this.templateName, rhs.templateName);
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(namespace, templateName);
  }
  
  @Override
  public String toString() {
    return namespace + "." + templateName;
  }
}