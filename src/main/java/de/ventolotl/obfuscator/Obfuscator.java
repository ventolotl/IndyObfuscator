package de.ventolotl.obfuscator;

import de.ventolotl.obfuscator.file.FileProcessor;
import de.ventolotl.obfuscator.transform.InvokeDynamicTransformer;

public final class Obfuscator {
  private final FileProcessor fileProcessor;

  private final InvokeDynamicTransformer transformer = new InvokeDynamicTransformer();

  public Obfuscator(FileProcessor fileProcessor) {
    this.fileProcessor = fileProcessor;

    try {
      transform();
    } catch (Throwable t) {
      System.err.println("An error has occurred in the obfuscation process");
      t.printStackTrace();
    }

    this.fileProcessor.save();
  }

  private void transform() {
    fileProcessor.transformClasses(transformer::transform);
  }
}