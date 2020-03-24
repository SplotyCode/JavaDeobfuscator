package io.github.splotycode.deobfuscator.flow.stack;

import com.sun.jmx.remote.internal.ArrayQueue;
import io.github.splotycode.deobfuscator.util.InstructionUtil;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import lombok.Data;

import java.util.*;

public enum StackInstruction {

    NOP(Opcodes.NOP, 0, 0, false),

    AALOAD(Opcodes.AALOAD, 2, 1),
    AASTORE(Opcodes.AASTORE, 3, 0),
    ACONST_NULL(Opcodes.ACONST_NULL, 0, 1),//TODO what is a null reference?

    GETSTATIC(Opcodes.GETSTATIC, 0, 1),

    DUP(Opcodes.DUP, 1, 2) {
        @Override
        int redirect(int index) {
            return 1;
        }

        @Override
        boolean hasRedirect(int index) {
            return true;
        }

        @Override
        boolean hasInputRedirect(int index) {
            return true;
        }

        private int[] inputRedirects = new int[] {1, 2};

        @Override
        int[] inputRedirect(int index) {
            return inputRedirects;
        }
    },
    IASTORE(Opcodes.IASTORE, 3, 0),
    NEWARRAY(Opcodes.NEWARRAY, 1, 1) {
        @Override
        StackType getType(AbstractInsnNode node, int index) {
            return ArrayReferenceStackType.byInstruction(node);
        }
    },
    ANEWARRAY(Opcodes.ANEWARRAY, 1, 1) {
        @Override
        StackType getType(AbstractInsnNode node, int index) {
            return ArrayReferenceStackType.byInstructionReference(node);
        }
    },

    IALOAD(Opcodes.IALOAD, 2, 1, false, ValueStackType.INT),

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
        StackInstruction instruction = instructions.get(opCode);
        if (instruction == null) {
            //System.out.println("unknown instruction: " + InstructionUtil.opCodeName(opCode));
        }
        return instruction;
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

    public static ArrayList<StackNode> getUsages(AbstractInsnNode instructionNode, int index) {
        ArrayList<StackNode> usages = new ArrayList<>();

        ArrayList<Integer> search = new ArrayList<>();
        search.add(index);
        AbstractInsnNode position = instructionNode.getNext();
        StackInstruction instruction = getInstruction(position);
        while (instruction != null && !search.isEmpty()) {
            if (instruction.manipulatePosition) {
                return usages;
            }
            ListIterator<Integer> iterator = search.listIterator();
            while (iterator.hasNext()) {
                int currentSearch = iterator.next();
                int indexAfter = currentSearch + instruction.stackDifference;
                if (currentSearch > instruction.take) {
                    iterator.set(indexAfter);
                } else {
                    iterator.remove();
                    if (instruction.hasInputRedirect(currentSearch)) {
                        for (int redirect : instruction.inputRedirect(currentSearch)) {
                            iterator.add(redirect);
                        }
                    } else {
                        usages.add(new StackNode(position, indexAfter));
                    }
                }
            }

            position = position.getNext();
            instruction = getInstruction(position);
        }
        return usages;
    }

    @Data
    public static class StackNode {

        private final AbstractInsnNode instruction;
        private final int argument;

    }

    public static boolean removeAllArguments(AbstractInsnNode instruction, InsnList instructionList) {
        StackInstruction insn = getInstruction(instruction);
        ArrayList<AbstractInsnNode> arguments = new ArrayList<>();
        for (int i = 1; i < insn.take + 1; i++) {
            StackValue argument = guesStackValue(instruction, i, false);
            if (argument == null) {
                return false;
            }
            arguments.add(argument.getCreator());
        }
        for (AbstractInsnNode argument : arguments) {
            instructionList.remove(argument);
        }
        return true;
    }

    public static StackValue guesStackValue(AbstractInsnNode position, int index) {
        return guesStackValue(position, index, true);
    }

    public static StackValue guesStackValue(AbstractInsnNode position, int index, boolean followRedirects) {
        int valuesBefore = index;
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
                valuesBefore -= instruction.stackDifference;

                currentPosition = currentPosition.getPrevious();
                instruction = getInstruction(currentPosition);
            }
        } while (followRedirects && instruction != null &&
                instruction.hasRedirect(stackIndexToArgumentIndex(valuesBefore, instruction.put)) &&
                (valuesBefore = instruction.put - instruction.redirect(stackIndexToArgumentIndex(valuesBefore, instruction.put))) >= 0
        );

        if (currentPosition == null || instruction == null) {
            return null;
        }

        int argument = stackIndexToArgumentIndex(valuesBefore, instruction.put);
        StackType type = instruction.getType(currentPosition, argument);
        Object value = instruction.getValue(currentPosition, argument);
        return new StackValue(currentPosition, type, value);
    }

    private static int stackIndexToArgumentIndex(int index, int arguments) {
        if (index == 0) {
            index = 1;
        }
        return arguments - index;
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
        throw new IllegalStateException(name() + " does not redirect");
    }

    boolean hasInputRedirect(int index) {
        return false;
    }

    int[] inputRedirect(int index) {
        throw new IllegalStateException(name() + " does not redirect inputs");
    }

    Object getValue(AbstractInsnNode node, int index) {
        return staticValue;
    }

    StackType getType(AbstractInsnNode node, int index) {
        if (staticOutputTypes.length == 0) {
            return null;
        }
        return staticOutputTypes[index];
    }
}
