package de.ventolotl.obfuscator.file;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public record ClassHierarchy(ClassNode classNode, ClassHierarchy superClass, List<String> interfaces) {
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
}