package com.pmease.gitop.web.common.soy.api;

import java.util.Map;

/**
 * Render google closure soy template on server side
 */
public interface SoyTemplateRenderer {
  
  /**
   * Render a single template with the supplied data and injected data to an appendable 
   * 
   * @param key
   * @param out
   * @param data
   * @param ijData
   */
  void render(TemplateKey key, Appendable out, Map<String, ?> data, Map<String, ?> ijData);

  /**
   * Render a single template with the supplied data to an appendable
   * 
   * @param key
   * @param out
   * @param data
   */
  void render(TemplateKey key, Appendable out, Map<String, ?> data);
  
}
