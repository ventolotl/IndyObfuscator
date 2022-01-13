package de.ventolotl.obfuscator;

import de.ventolotl.obfuscator.configuration.Configuration;
import de.ventolotl.obfuscator.configuration.ConfigurationSets;
import de.ventolotl.obfuscator.file.FileProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class Main {
  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Invalid arguments. Expected: java -jar Obfuscator.jar <configuration>");
      return;
    }

    String pathToConfig = args[0];
    File file = new File(pathToConfig);

    Configuration configuration;
    try {
      configuration = Configuration.from(file, ConfigurationSets.defaults());
    } catch (FileNotFoundException e) {
      System.err.println("File does not exist");
      return;
    }

    FileProcessor fileProcessor;
    try {
      fileProcessor = FileProcessor.from(configuration);
    } catch (IOException e) {
      System.err.println("Something went wrong while reading the input file");
      e.printStackTrace();
      return;
    }

    new Obfuscator(fileProcessor);
  }
}