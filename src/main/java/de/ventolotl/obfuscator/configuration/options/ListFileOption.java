package de.ventolotl.obfuscator.configuration.options;

import java.io.File;

public final class ListFileOption extends ListOption<File> {
  public static ListFileOption from(String name) {
    return new ListFileOption(name);
  }

  public ListFileOption(String name) {
    super(name);
  }

  @Override
  public File convert(String line) {
    return new File(line);
  }
}