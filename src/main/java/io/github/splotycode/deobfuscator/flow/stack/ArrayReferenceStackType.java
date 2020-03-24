package io.github.splotycode.deobfuscator.flow.stack;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import jdk.internal.org.objectweb.asm.tree.TypeInsnNode;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@ToString
@AllArgsConstructor
public class ArrayReferenceStackType implements StackType {

    public static ArrayReferenceStackType INT_ARRAY = new ArrayReferenceStackType(Type.getType("[I"));
    public static ArrayReferenceStackType BOOLEAN_ARRAY = new ArrayReferenceStackType(Type.getType("[Z"));
    public static ArrayReferenceStackType CHAR_ARRAY = new ArrayReferenceStackType(Type.getType("[C"));
    public static ArrayReferenceStackType FLOAT_ARRAY = new ArrayReferenceStackType(Type.getType("[F"));
    public static ArrayReferenceStackType DOUBLE_ARRAY = new ArrayReferenceStackType(Type.getType("[D"));
    public static ArrayReferenceStackType BYTE_ARRAY = new ArrayReferenceStackType(Type.getType("[B"));
    public static ArrayReferenceStackType SHORT_ARRAY = new ArrayReferenceStackType(Type.getType("[S"));
    public static ArrayReferenceStackType LONG_ARRAY = new ArrayReferenceStackType(Type.getType("[J"));

    public static ArrayReferenceStackType byInstruction(AbstractInsnNode instruction) {
        if (instruction instanceof IntInsnNode) {
            switch (((IntInsnNode) instruction).operand) {
                case Opcodes.T_INT:
                    return INT_ARRAY;
                case Opcodes.T_BOOLEAN:
                    return BOOLEAN_ARRAY;
                case Opcodes.T_CHAR:
                    return CHAR_ARRAY;
                case Opcodes.T_FLOAT:
                    return FLOAT_ARRAY;
                case Opcodes.T_DOUBLE:
                    return DOUBLE_ARRAY;
                case Opcodes.T_BYTE:
                    return BYTE_ARRAY;
                case Opcodes.T_SHORT:
                    return SHORT_ARRAY;
                case Opcodes.T_LONG:
                    return LONG_ARRAY;
                default:
                    throw new IllegalStateException("Invalid array type on operand stack");
            }
        }
        return byInstructionReference(instruction);
    }

    public static ArrayReferenceStackType byInstructionReference(AbstractInsnNode instruction) {
        return new ArrayReferenceStackType(Type.getType(((TypeInsnNode) instruction).desc));
    }

    private Type type;

    public Type arrayType() {
        return type;
    }

    public Type typeInArray() {
        return type.getElementType();
    }

    @Override
    public boolean hasDeclarationType() {
        return true;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayReferenceStackType that = (ArrayReferenceStackType) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
