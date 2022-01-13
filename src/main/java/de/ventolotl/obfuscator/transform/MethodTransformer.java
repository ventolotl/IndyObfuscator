package de.ventolotl.obfuscator.transform;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface MethodTransformer extends Transformer {
  @Override
  default void transform(ClassNode classNode) {
    classNode.methods.forEach(method -> transformMethod(classNode, method));
  }

  void transformMethod(ClassNode classNode, MethodNode methodNode);
}