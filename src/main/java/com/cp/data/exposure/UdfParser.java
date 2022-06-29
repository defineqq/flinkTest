package com.cp.data.exposure;

import java.util.List;

import lombok.ToString;
import lombok.Data;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

/**
 *  udf parse --> FuncNode
 */
public class UdfParser {

  private FuncNode root;

  public UdfParser(String exp) {
    root = new FuncNode();
    root.setExp(exp);
    visit();
  }

  public FuncNode parse() {
    return root;
  }

  private void visit() {
    visitFunc(root, root.getExp());
  }

  private String visitFunc(FuncNode funcNode, String parentExp) {
    String exp = parentExp;
    int funNameEndIdx = exp.indexOf("(");
    funcNode.setFuncName(exp.substring(0, funNameEndIdx));
    exp = exp.substring(funNameEndIdx + 1).trim();
    if (exp.startsWith(")")) {
      if (exp.trim().equals(")")) {
        return "";
      }
      return exp.substring(1).trim();
    }
    if (StringUtils.isEmpty(exp)) {
      return exp;
    }
    while (!StringUtils.isEmpty(exp)) {
      char start = exp.charAt(0);
      if (start == '\''
          || start == '"'
          || Character.isDigit(start)
          || start == '-'
          || start == '.') { // 静态参数
        StaticNode staticParam = new StaticNode();
        exp = visitStaticVal(staticParam, exp);
        funcNode.getParams().add(staticParam);
      } else if (start == ',') {
        exp = exp.substring(1).trim();
      } else if (start == ')') {
        return exp.substring(1).trim();
      } else {
        FuncNode functionParam = new FuncNode();
        exp = visitFunc(functionParam, exp);
        funcNode.getParams().add(functionParam);
      }
    }
    return exp;
  }

  private String visitStaticVal(StaticNode staticParam, String exp) {
    StringBuilder param = new StringBuilder();
    char start = exp.charAt(0);
    boolean isDigital = false;
    for (int i = 0; i < exp.length(); i++) {
      char c = exp.charAt(i);
      if (start == '"' || start == '\'') { //string process
        if (i == 0) {
          continue;
        }
        if (c == start && exp.charAt(i - 1) != '\\') {
          exp = exp.substring(i + 1).trim();
          break;
        } else {
          param.append(c);
        }
      } else if (c == ')') {
        exp = exp.substring(i).trim();
        break;
      } else {
        isDigital = true;
        if (c == ',') { // number process
          exp = exp.substring(i).trim();
          break;
        } else {
          param.append(c);
        }
      }
    }
    if (isDigital && param.toString().contains(".")) {
      staticParam.setValue(Double.valueOf(param.toString().trim()));
    } else if (isDigital) {
      staticParam.setValue(Long.valueOf(param.toString().trim()));
    } else {
      staticParam.setValue(param);
    }
    return exp;
  }

  @Data
  public class Node {
    String exp;
  }

  @Data
  public class StaticNode extends Node {
    Object value;
  }

  @Data
  @ToString
  public class FuncNode extends Node {
    String funcName;
    List<Node> params = Lists.newArrayList();
  }

}
