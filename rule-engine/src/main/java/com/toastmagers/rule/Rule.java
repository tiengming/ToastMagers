package com.toastmagers.rule;

public class Rule {
    public enum Type {
        REGEX,
        KEYWORD
    }

    public enum Action {
        BLOCK,
        ALLOW,
        SILENT
    }

    private String name;
    private Type type;
    private String pattern;
    private Action action;

    public Rule(String name, Type type, String pattern, Action action) {
        this.name = name;
        this.type = type;
        this.pattern = pattern;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getPattern() {
        return pattern;
    }

    public Action getAction() {
        return action;
    }

    public boolean matches(String text) {
        if (text == null || pattern == null) {
            return false;
        }
        if (type == Type.KEYWORD) {
            return text.contains(pattern);
        } else if (type == Type.REGEX) {
            return SafeRegexMatcher.matches(pattern, text);
        }
        return false;
    }
}
