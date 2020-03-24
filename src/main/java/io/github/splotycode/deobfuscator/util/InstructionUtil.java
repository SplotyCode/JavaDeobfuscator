package io.github.splotycode.deobfuscator.util;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;
import jdk.internal.org.objectweb.asm.util.Printer;

import java.util.Collection;

public final class InstructionUtil {

    public static Integer readInt(AbstractInsnNode node) {
        if (node == null) return null;
        switch (node.getOpcode()) {
            case Opcodes.ICONST_0:
                return 0;
            case Opcodes.ICONST_1:
                return 1;
            case Opcodes.ICONST_2:
                return 2;
            case Opcodes.ICONST_3:
                return 3;
            case Opcodes.ICONST_4:
                return 4;
            case Opcodes.ICONST_5:
                return 5;
            case Opcodes.ICONST_M1:
                return -1;
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                return ((IntInsnNode) node).operand;
            case Opcodes.LDC:
                Object pooled = ((LdcInsnNode) node).cst;
                if (pooled instanceof Integer) {
                    return (int) pooled;
                }
                return null;
            default:
                return null;
        }
    }

    public static AbstractInsnNode getStoreIntInstruction(int number) {
        switch (number) {
            case 0:
                return new InsnNode(Opcodes.ICONST_0);
            case 1:
                return new InsnNode(Opcodes.ICONST_1);
            case 2:
                return new InsnNode(Opcodes.ICONST_2);
            case 3:
                return new InsnNode(Opcodes.ICONST_3);
            case 4:
                return new InsnNode(Opcodes.ICONST_4);
            case 5:
                return new InsnNode(Opcodes.ICONST_5);
            case -1:
                return new InsnNode(Opcodes.ICONST_M1);
            default:
                return new LdcInsnNode(number);
        }
    }

    public static boolean isPutField(int opCode) {
        return opCode == Opcodes.PUTFIELD || opCode == Opcodes.PUTSTATIC;
    }

    public static int putToGet(int opCode) {
        switch (opCode) {
            case Opcodes.PUTFIELD:
                return Opcodes.GETFIELD;
            case Opcodes.PUTSTATIC:
                return Opcodes.GETSTATIC;
            default:
                throw new IllegalArgumentException(opCode + " is not a valid put opcode");
        }
    }

    public static boolean sameField(FieldInsnNode one, FieldInsnNode two) {
        return one.name.equals(two.name) && one.owner.equals(two.owner) && one.desc.equals(two.desc);
    }

    public static String opCodeName(AbstractInsnNode instruction) {
        return opCodeName(instruction.getOpcode());
    }

    public static String opCodeNames(Collection<AbstractInsnNode> instructions) {
        StringBuilder builder = new StringBuilder();
        for (AbstractInsnNode instruction : instructions) {
            builder.append(opCodeName(instruction)).append(", ");
        }
        if (builder.length() != 0) {
            builder.setLength(builder.length() - 2);
        }
        return builder.toString();
    }

    public static String opCodeName(int opCode) {
        String[] names = Printer.OPCODES;
        return opCode - 1 > names.length ? "Unknown" : names[opCode];
    }

}
