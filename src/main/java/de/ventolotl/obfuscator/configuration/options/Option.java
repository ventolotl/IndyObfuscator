package de.ventolotl.obfuscator.configuration.options;

import java.util.Map;

public abstract class Option<T> {
  protected final String name;

  public Option(String name) {
    this.name = name;
  }

  public abstract T resolve(Map<String, Object> entries);

  public static class Value<T> {
    private final Option<T> option;
    private final T value;

    public Value(Option<T> option, T value) {
      this.option = option;
      this.value = value;
    }

    public Option<T> option() {
      return option;
    }

    public T value() {
      return value;
    }
  }
}