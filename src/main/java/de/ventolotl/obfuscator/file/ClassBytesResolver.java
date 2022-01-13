package de.ventolotl.obfuscator.file;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;

public final class ClassBytesResolver {
  private static final int FLAGS = ClassWriter.COMPUTE_MAXS;

  public static byte[] bytesOfClass(ClassNode classNode, Map<String, ClassHierarchy> hierarchy) {
    CustomClassWriter classWriter = createClassWriter(hierarchy);
    try {
      classNode.accept(classWriter);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to write class " + classNode.name, e);
    }
    return classWriter.toByteArray();
  }

  private static CustomClassWriter createClassWriter(Map<String, ClassHierarchy> hierarchy) {
    return new CustomClassWriter(FLAGS, hierarchy);
  }
}