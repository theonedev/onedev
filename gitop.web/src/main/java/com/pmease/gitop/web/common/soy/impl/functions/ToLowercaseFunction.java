package com.pmease.gitop.web.common.soy.impl.functions;

import static com.google.template.soy.shared.restricted.SoyJavaRuntimeFunctionUtils.toSoyData;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.javasrc.restricted.JavaExpr;
import com.google.template.soy.javasrc.restricted.SoyJavaSrcFunction;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcFunction;
import com.google.template.soy.shared.restricted.SoyPureFunction;
import com.google.template.soy.tofu.restricted.SoyAbstractTofuFunction;

@Singleton
@SoyPureFunction
class ToLowercaseFunction extends SoyAbstractTofuFunction
        implements SoyJsSrcFunction, SoyJavaSrcFunction {

  @Override
  public String getName() {
    return "toLowercase";
  }

  @Override
  public Set<Integer> getValidArgsSizes() {
    return ImmutableSet.of(1);
  }

  @Override
  public SoyData compute(List<SoyData> args) {
    SoyData arg = args.get(0);

    String s = arg.stringValue();
    return toSoyData(s.toLowerCase());
  }

  @Override
  public JavaExpr computeForJavaSrc(List<JavaExpr> args) {
    throw new UnsupportedOperationException();
  }

  @Override
  public JsExpr computeForJsSrc(List<JsExpr> args) {
    throw new UnsupportedOperationException();
  }
}