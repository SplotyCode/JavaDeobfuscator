package io.github.splotycode.deobfuscator.module;

import io.github.splotycode.deobfuscator.JavaDeobfuscator;
import io.github.splotycode.deobfuscator.flow.FlowMethod;
import io.github.splotycode.deobfuscator.search.InstructionSearch;
import io.github.splotycode.deobfuscator.search.MethodPattern;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;

public class RemoveUnusedMethods extends Module implements InstructionSearch {

    private HashSet<String> methods;
    private ClassNode classNode;

    @Override
    public void init() {
        methods = JavaDeobfuscator.getInstance().getFlowControl().copySignatures();
    }

    @Override
    public boolean transform(ClassNode classNode) {
        this.classNode = classNode;
        search(classNode);
        return false;
    }


    @Override
    public boolean onInstruction(MethodNode caller, AbstractInsnNode instruction) {
        if (instruction instanceof MethodInsnNode) {
            MethodInsnNode methodCall = (MethodInsnNode) instruction;
            if (!methodCall.owner.equals(classNode.name) ||
                    !methodCall.name.equals(caller.name) ||
                    !methodCall.desc.equals(caller.desc))
            methods.remove(MethodPattern.generatePattern(methodCall));
        }
        return false;
    }

    @Override
    public boolean postTransform() {
        boolean removed = false;
        for (String methodName : methods) {
            FlowMethod method = JavaDeobfuscator.getInstance().getFlowControl().getMethod(methodName);
            if (method.getOverrides() == null && !method.isEntryPoint() && !method.isClassInit()) {
                method.remove();
                removed = true;
            }
        }
        return removed;
    }
}
