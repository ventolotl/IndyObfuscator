package de.ventolotl.obfuscator.transform;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.ventolotl.obfuscator.transform.BoostrapMethodGen.BSM_DESC;

public final class InvokeDynamicTransformer implements Transformer {
  @Override
  public void transform(ClassNode classNode) {
    if (!suitableClass(classNode)) {
      return;
    }
    List<MethodNode> methods = classNode.methods;
    String bsmName = computeMethodNew(methods);
    for (MethodNode method : methods) {
      // TODO: Fix VerifyError in constructors
      if (method.name.equals("<init>")) {
        continue;
      }

      transformMethod(classNode, bsmName, method);
    }

    // Add BoostrapMethod
    methods.add(BoostrapMethodGen.boostrapMethodOf(bsmName));
  }

  private String computeMethodNew(List<MethodNode> methods) {
    List<String> occupied = methods.stream().map(Object::toString).collect(Collectors.toList());
    String uuid;
    do {
      uuid = UUID.randomUUID().toString().replace("-", "");
    } while (occupied.contains(uuid));
    return uuid;
  }

  private boolean suitableClass(ClassNode classNode) {
    return (classNode.access & ACC_INTERFACE) == 0;
  }

  private void transformMethod(ClassNode classNode, String bsmName, MethodNode method) {
    InsnList instructions = method.instructions;
    for (AbstractInsnNode instruction : instructions) {
      if (instruction instanceof MethodInsnNode) {
        InvokeDynamicInsnNode indy = convertMethodInstruction(classNode, bsmName, (MethodInsnNode) instruction);
        instructions.set(instruction, indy);
      }
    }
  }

  private InvokeDynamicInsnNode convertMethodInstruction(ClassNode classNode, String bsmName, MethodInsnNode methodInsn) {
    Handle handle = new Handle(Opcodes.H_INVOKESTATIC, classNode.name, bsmName, BSM_DESC, false);
    return new InvokeDynamicInsnNode(
      "_",
      resolveDescriptor(methodInsn.getOpcode(), methodInsn.desc),
      handle,
      createKeyFrom(methodInsn)
    );
  }

  static String resolveDescriptor(int opcode, String desc) {
    return switch (opcode) {
      case INVOKEVIRTUAL,
        INVOKESPECIAL,
        INVOKEINTERFACE -> desc.replace("(", "(Ljava/lang/Object;");
      default -> desc;
    };
  }

  private String createKeyFrom(MethodInsnNode methodInsn) {
    String[] strings = new String[]{
      translateToBsmName(methodInsn.getOpcode()),
      methodInsn.owner.replace("/", "."),
      methodInsn.name,
      methodInsn.desc
    };
    return String.join(";;", strings);
  }

  private String translateToBsmName(int opcode) {
    switch (opcode) {
      case INVOKESTATIC:
        return "0";
      case INVOKEINTERFACE:
      case INVOKEVIRTUAL:
        return "1";
      case INVOKESPECIAL:
        return "2";
    }
    throw new IllegalStateException("Cannot translate opcode: " + opcode);
  }
}