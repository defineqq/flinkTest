package com.cp.data.exposure.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtil {

  private static final Pattern PATTERN = Pattern.compile("\\$\\{\\w+\\}");

  public static String processTemplate(String template, Map<String, Object> params) {
    StringBuffer sb = new StringBuffer();
    Matcher m = PATTERN.matcher(template);
    while (m.find()) {
      String param = m.group();
      Object value = params.get(param.substring(2, param.length() - 1));
      m.appendReplacement(sb, value == null ? "" : value.toString().replaceAll("\\$", ""));
    }
    m.appendTail(sb);
    return sb.toString();
  }

  public static List<String> getVariableNames(String template) {
    List<String> variables = Lists.newArrayList();
    StringBuffer sb = new StringBuffer();
    Matcher m = PATTERN.matcher(template);
    while (m.find()) {
      String param = m.group();
      String varName = param.substring(2, param.length() - 1).trim();
      variables.add(varName);
      m.appendReplacement(sb, "");
    }
    return variables;
  }
}
