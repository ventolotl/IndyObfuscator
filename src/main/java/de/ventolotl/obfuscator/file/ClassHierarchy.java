package de.ventolotl.obfuscator.file;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public final class ClassHierarchy {
  private final ClassNode classNode;
  private final ClassHierarchy superClass;
  private final List<String> interfaces;

  public ClassHierarchy(ClassNode classNode, ClassHierarchy superClass, List<String> interfaces) {
    this.classNode = classNode;
    this.superClass = superClass;
    this.interfaces = interfaces;
  }

  public boolean isAssignableFrom(ClassHierarchy other) {
    if (this.classNode.equals(other.classNode)) {
      return true;
    } else {
      return other.superClass != null && isAssignableFrom(other.superClass);
    }
  }

  public boolean isInterface() {
    return (classNode.access & Opcodes.ACC_INTERFACE) != 0;
  }

  public ClassNode classNode() {
    return classNode;
  }

  public ClassHierarchy superClass() {
    return superClass;
  }

  public List<String> interfaces() {
    return interfaces;
  }
}