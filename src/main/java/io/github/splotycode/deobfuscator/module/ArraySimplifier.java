package io.github.splotycode.deobfuscator.module;

import io.github.splotycode.deobfuscator.search.InstructionSearch;
import io.github.splotycode.deobfuscator.util.InstructionUtil;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

public class ArraySimplifier extends Module implements InstructionSearch {

    @Override
    public boolean transform(ClassNode classNode) {
        return search(classNode);
    }

    @Override
    public boolean onInstruction(MethodNode caller, AbstractInsnNode instruction) {
        Integer size;
        if (instruction.getOpcode() == Opcodes.NEWARRAY &&
                ((IntInsnNode) instruction).operand == Opcodes.T_INT &&
                (size = InstructionUtil.readInt(instruction.getPrevious())) != null &&
                InstructionUtil.isPutField(instruction.getNext().getOpcode())) {
            FieldInsnNode putField = (FieldInsnNode) instruction.getNext();
            Integer[] values = new Integer[size];

            AbstractInsnNode current = putField.getNext();
            for (int i = 0; i < size; i++) {
                if (current instanceof FieldInsnNode && InstructionUtil.sameField(putField, (FieldInsnNode) current)) {
                    current = current.getNext();
                    Integer index;
                    if ((index = InstructionUtil.readInt(current)) != null && index == i) {
                        current = current.getNext();
                        if ((values[i] = InstructionUtil.readInt(current)) != null) {
                            current = current.getNext();
                            if (current.getOpcode() == Opcodes.IASTORE) {
                                current = current.getNext();
                                continue;
                            }
                        }
                    }
                }
                return false;
            }

            AbstractInsnNode position = putField.getNext();
            for (int i = 0; i < size; i++) {
                AbstractInsnNode next = position.getNext();
                caller.instructions.set(position, new InsnNode(Opcodes.DUP));
                position = next;
                next = position.getNext();
                caller.instructions.set(position, InstructionUtil.getStoreIntInstruction(i));
                position = next;
                next = position.getNext();
                caller.instructions.set(position, InstructionUtil.getStoreIntInstruction(values[i]));
                position = next;
                next = position.getNext();
                caller.instructions.set(position, new InsnNode(Opcodes.IASTORE));
                position = next;
            }
            caller.instructions.remove(putField);
            caller.instructions.insertBefore(position, putField);
            return true;
        }
        return false;
    }
}
