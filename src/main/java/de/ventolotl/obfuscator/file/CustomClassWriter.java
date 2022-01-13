package de.ventolotl.obfuscator.file;

import org.objectweb.asm.ClassWriter;

import java.util.Map;

final class CustomClassWriter extends ClassWriter {
  public final static String DEFAULT_SUPER_CLASS = "java/lang/Object";

  private final Map<String, ClassHierarchy> classes;

  public CustomClassWriter(
    int flags,
    Map<String, ClassHierarchy> hierarchy
  ) {
    super(flags);

    this.classes = hierarchy;
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    if (!classes.containsKey(type1)) {
      System.out.println("Class " + type1 + " not in ClassPath, this may causes runtime errors");
    }
    if (!classes.containsKey(type2)) {
      System.out.println("Class " + type2 + " not in ClassPath, this may causes runtime errors");
    }

    ClassHierarchy class1 = classes.get(type1);
    ClassHierarchy class2 = classes.get(type2);
    if (class1 == null || class2 == null) {
      return DEFAULT_SUPER_CLASS;
    }

    if (class1.isAssignableFrom(class2)) {
      return type1;
    }
    if (class2.isAssignableFrom(class1)) {
      return type2;
    }
    if (class1.isInterface() || class2.isInterface()) {
      return DEFAULT_SUPER_CLASS;
    } else {
      do {
        class1 = class1.superClass();
      } while (!class1.isAssignableFrom(class2));
      return class1.classNode().name.replace('.', '/');
    }
  }
}