package io.github.splotycode.deobfuscator.module;

import io.github.splotycode.deobfuscator.search.InstructionSearch;
import io.github.splotycode.deobfuscator.util.InstructionUtil;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class StaticCalculation extends Module implements InstructionSearch {

    @Override
    public boolean transform(ClassNode classNode) {
        return search(classNode);
    }

    @Override
    public boolean onInstruction(MethodNode caller, AbstractInsnNode instruction) {
        if (instruction instanceof InsnNode) {
            if (instruction.getOpcode() == Opcodes.INEG) {
                Integer negate = InstructionUtil.readInt(instruction.getPrevious());
                if (negate != null) {
                    caller.instructions.remove(instruction.getPrevious());
                    caller.instructions.set(instruction, InstructionUtil.getStoreIntInstruction(-negate));
                    return true;
                }
                return false;
            }
            AbstractInsnNode rightNode = instruction.getPrevious();
            Integer right = InstructionUtil.readInt(rightNode);
            if (right != null) {
                AbstractInsnNode leftNode = rightNode.getPrevious();
                Integer left = InstructionUtil.readInt(leftNode);
                if (left != null) {
                    int result;
                    switch (instruction.getOpcode()) {
                        case Opcodes.IXOR:
                            result = left ^ right;
                            break;
                        case Opcodes.IAND:
                            result = left & right;
                            break;
                        default:
                            return false;
                    }
                    caller.instructions.set(instruction, InstructionUtil.getStoreIntInstruction(result));
                    caller.instructions.remove(leftNode);
                    caller.instructions.remove(rightNode);
                    return true;
                }
            }
        }
        return false;
    }
}
