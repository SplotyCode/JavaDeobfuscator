package io.github.splotycode.deobfuscator.flow.stack;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;

import java.util.HashMap;

public enum StackInstruction {

    NOP(Opcodes.NOP, 0, 0, false),

    AALOAD(Opcodes.AALOAD, 2, 1),
    AASTORE(Opcodes.AASTORE, 3, 0),
    ACONST_NULL(Opcodes.ACONST_NULL, 0, 1),//TODO what is a null reference?


    DUP(Opcodes.DUP, 1, 2) {
        @Override
        int redirect(int index) {
            return 1;
        }

        @Override
        boolean hasRedirect(int index) {
            return true;
        }
    },
    IASTORE(Opcodes.IASTORE, 3, 0),
    NEWARRAY(Opcodes.NEWARRAY, 1, 1) {
        @Override
        StackType getType(AbstractInsnNode node, int index) {
            return ArrayReferenceStackType.byInstruction(node);
        }
    },

    ICONST_0(Opcodes.ICONST_0, 0, 1, false, 0, ValueStackType.INT),
    ICONST_1(Opcodes.ICONST_1, 0, 1, false, 1, ValueStackType.INT),
    ICONST_2(Opcodes.ICONST_2, 0, 1, false, 2, ValueStackType.INT),
    ICONST_3(Opcodes.ICONST_3, 0, 1, false, 3, ValueStackType.INT),
    ICONST_4(Opcodes.ICONST_4, 0, 1, false, 4, ValueStackType.INT),
    ICONST_5(Opcodes.ICONST_5, 0, 1, false, 5, ValueStackType.INT),
    ICONST_M1(Opcodes.ICONST_M1, 0, 1, false, -1, ValueStackType.INT),

    BIPUSH(Opcodes.BIPUSH, 0, 1, false, Type.INT) {
        @Override
        Object getValue(AbstractInsnNode node, int index) {
            return ((IntInsnNode) node).operand;
        }
    },
    SIPUSH(Opcodes.SIPUSH, 0, 1, false, Type.INT) {
        @Override
        Object getValue(AbstractInsnNode node, int index) {
            return ((IntInsnNode) node).operand;
        }
    },

    LDC(Opcodes.LDC, 0, 1, false) {
        @Override
        StackType getType(AbstractInsnNode node, int index) {
            return StackType.fromPoolConstant(((LdcInsnNode) node).cst);
        }

        @Override
        Object getValue(AbstractInsnNode node, int index) {
            return ((LdcInsnNode) node).cst;
        }
    };

    private static HashMap<Integer, StackInstruction> instructions = new HashMap<>();

    public static StackInstruction getInstruction(int opCode) {
        return instructions.get(opCode);
    }

    public static StackInstruction getInstruction(AbstractInsnNode instructionNode) {
        if (instructionNode == null) {
            return null;
        }
        return getInstruction(instructionNode.getOpcode());
    }

    static {
        for (StackInstruction instruction : values()) {
            instructions.put(instruction.opCode, instruction);
        }
    }

    public static StackValue guesStackValue(AbstractInsnNode position, int level) {
        int valuesBefore = level;
        AbstractInsnNode currentPosition = position;
        StackInstruction instruction;

        do {
            currentPosition = currentPosition.getPrevious();
            instruction = getInstruction(currentPosition);
            while (instruction != null) {
                if (instruction.manipulatePosition) {
                    return null;
                }
                if (valuesBefore <= instruction.put) {
                    break;
                }
                valuesBefore += instruction.take;
                valuesBefore -= instruction.put;

                currentPosition = currentPosition.getPrevious();
                instruction = getInstruction(currentPosition);
            }
        } while (instruction != null && instruction.hasRedirect(0) && (valuesBefore = instruction.redirect(0)) != 0);//TODO index?

        if (currentPosition == null || instruction == null) {
            return null;
        }

        StackType type = instruction.getType(currentPosition, 0);//TODO index?
        Object value = instruction.getValue(currentPosition, 0);//TODO index?
        return new StackValue(currentPosition, type, value);
    }

    private int opCode;
    private int take;
    private int put;
    private int stackDifference;
    private boolean manipulatePosition;
    private StackType[] staticOutputTypes;
    private Object staticValue;

    StackInstruction(int opCode, int take, int put, StackType... staticOutputTypes) {
        this(opCode, take, put, false, null, staticOutputTypes);
    }

    StackInstruction(int opCode, int take, int put, boolean manipulatePosition, StackType... staticOutputTypes) {
        this(opCode, take, put, manipulatePosition, null, staticOutputTypes);
    }

    StackInstruction(int opCode, int take, int put, boolean manipulatePosition, Object staticValue, StackType... staticOutputTypes) {
        this.opCode = opCode;
        this.take = take;
        this.put = put;
        this.manipulatePosition = manipulatePosition;
        this.staticOutputTypes = staticOutputTypes;
        this.staticValue = staticValue;

        stackDifference = put - take;
    }

    boolean hasRedirect(int index) {
        return false;
    }

    int redirect(int index) {
        throw new IllegalArgumentException();
    }

    Object getValue(AbstractInsnNode node, int index) {
        return staticValue;
    }

    StackType getType(AbstractInsnNode node, int index) {
        if (index > staticOutputTypes.length - 1) {
            return null;
        }
        return staticOutputTypes[index];
    }
}
