package de.ventolotl.obfuscator.configuration.options;

import java.util.Map;

public abstract class Option<T> {
  protected final String name;

  public Option(String name) {
    this.name = name;
  }

  public abstract T resolve(Map<String, Object> entries);

  public record Value<T>(Option<T> option, T value) {
  }
}