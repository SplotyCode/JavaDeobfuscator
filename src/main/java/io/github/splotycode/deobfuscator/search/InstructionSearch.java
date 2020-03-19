package io.github.splotycode.deobfuscator.search;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public interface InstructionSearch {

    default boolean search(ClassNode classNode) {
        boolean changed = false;
        for (MethodNode methodNode : classNode.methods) {
            InsnList instructions = methodNode.instructions;
            for (int i = 0; i < instructions.size(); i++) {
                if (onInstruction(methodNode, instructions.get(i))) {
                    changed = true;
                }
            }
        }
        return changed;
    }

    boolean onInstruction(MethodNode caller, AbstractInsnNode instruction);

}
