package de.ventolotl.obfuscator.configuration.options;

import java.io.File;
import java.util.Map;

public final class FileOption extends Option<File> {
  public static FileOption from(String name) {
    return new FileOption(name);
  }

  private FileOption(String name) {
    super(name);
  }

  @Override
  public File resolve(Map<String, Object> entries) {
    return new File((String) entries.get(name));
  }
}