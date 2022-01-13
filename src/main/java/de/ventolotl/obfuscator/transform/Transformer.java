package de.ventolotl.obfuscator.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public interface Transformer extends Opcodes {
  void transform(ClassNode classNode);
}