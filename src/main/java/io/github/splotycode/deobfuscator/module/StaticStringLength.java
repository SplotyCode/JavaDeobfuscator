package io.github.splotycode.deobfuscator.module;

import io.github.splotycode.deobfuscator.search.MethodCallSearch;
import io.github.splotycode.deobfuscator.search.MethodPattern;
import io.github.splotycode.deobfuscator.util.InstructionUtil;
import jdk.internal.org.objectweb.asm.tree.*;

public class StaticStringLength extends Module implements MethodCallSearch {

    private static final MethodPattern STRING_LENGTH = new MethodPattern("java/lang/String#length ()I");

    @Override
    public boolean transform(ClassNode classNode) {
        return search(classNode);
    }

    @Override
    public MethodPattern desiredMethod() {
        return STRING_LENGTH;
    }

    @Override
    public boolean onMethod(MethodNode caller, MethodInsnNode methodCall) {
        if (methodCall.getPrevious() instanceof LdcInsnNode) {
            LdcInsnNode previous = (LdcInsnNode) methodCall.getPrevious();
            if (previous.cst instanceof String) {
                int length = ((String) previous.cst).length();
                AbstractInsnNode instruction = InstructionUtil.getStoreIntInstruction(length);
                caller.instructions.set(methodCall, instruction);
                caller.instructions.remove(previous);
                return true;
            }
        }
        return false;
    }
}
