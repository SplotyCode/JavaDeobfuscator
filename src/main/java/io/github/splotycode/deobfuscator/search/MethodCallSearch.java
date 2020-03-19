package io.github.splotycode.deobfuscator.search;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public interface MethodCallSearch extends InstructionSearch {

    MethodPattern desiredMethod();

    @Override
    default boolean onInstruction(MethodNode caller, AbstractInsnNode instruction) {
        if (instruction instanceof MethodInsnNode) {
            MethodInsnNode methodCall = (MethodInsnNode) instruction;
            if (desiredMethod().matches(methodCall)) {
                return onMethod(caller, methodCall);
            }
        }
        return false;
    }

    boolean onMethod(MethodNode caller, MethodInsnNode methodCall);

}
