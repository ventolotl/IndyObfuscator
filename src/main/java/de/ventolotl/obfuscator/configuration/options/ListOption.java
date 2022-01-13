package de.ventolotl.obfuscator.configuration.options;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ListOption<T> extends Option<List<T>> {
  public ListOption(String name) {
    super(name);
  }

  @Override
  public List<T> resolve(Map<String, Object> entries) {
    //noinspection unchecked
    List<String> lines = (List<String>) entries.get(name);
    return lines.stream().map(this::convert).collect(Collectors.toList());
  }

  public abstract T convert(String line);
}