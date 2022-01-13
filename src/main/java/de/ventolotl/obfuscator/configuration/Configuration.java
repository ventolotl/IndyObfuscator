package de.ventolotl.obfuscator.configuration;

import de.ventolotl.obfuscator.configuration.options.Option;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Configuration {
  private final Map<String, Object> entries;
  private final Option<?>[] options;

  private List<Option.Value<?>> values;

  public static Configuration from(File file, Option<?>[] options) throws FileNotFoundException {
    return new Configuration(new FileInputStream(file), options);
  }

  public static Configuration from(InputStream inputStream, Option<?>[] options) {
    return new Configuration(inputStream, options);
  }

  private Configuration(InputStream inputStream, Option<?>[] options) {
    Yaml yaml = new Yaml();

    this.entries = yaml.load(inputStream);
    this.options = options;

    load();
  }

  private void load() {
    List<Option.Value<?>> resolved = new ArrayList<>();
    for (Option<?> option : options) {
      resolved.add(new Option.Value(option, option.resolve(entries)));
    }
    this.values = resolved;
  }

  public File inputFile() {
    return valueOf(ConfigurationSets.INPUT_FILE);
  }

  public File outputFile() {
    return valueOf(ConfigurationSets.OUTPUT_FILE);
  }

  public List<File> libraries() {
    return valueOf(ConfigurationSets.LIBRARIES);
  }

  public <T> T valueOf(Option<T> option) {
    //noinspection unchecked
    return (T) values.stream()
      .filter(o -> o.option() == option)
      .findFirst()
      .orElseThrow(IllegalStateException::new).value();
  }
}