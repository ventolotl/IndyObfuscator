package de.ventolotl.obfuscator.configuration;

import de.ventolotl.obfuscator.configuration.options.FileOption;
import de.ventolotl.obfuscator.configuration.options.ListFileOption;
import de.ventolotl.obfuscator.configuration.options.Option;

public final class ConfigurationSets {
  public final static FileOption INPUT_FILE = FileOption.from("input");
  public final static FileOption OUTPUT_FILE = FileOption.from("output");
  public final static ListFileOption LIBRARIES = ListFileOption.from("libraries");

  private final static Option<?>[] options = new Option[]{
    INPUT_FILE,
    OUTPUT_FILE,
    LIBRARIES
  };

  public static Option<?>[] defaults() {
    return options;
  }
}