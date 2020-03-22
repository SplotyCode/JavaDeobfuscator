package io.github.splotycode.deobfuscator.flow.stack;

import jdk.internal.org.objectweb.asm.Type;

public interface StackType {

    default boolean hasDeclarationType() {
        return false;
    }

    default boolean isReference() {
        return false;
    }

    static StackType fromPoolConstant(Object constant) {
        if (constant instanceof Integer) {
            return ValueStackType.INT;
        } else if (constant instanceof Float) {
            return ValueStackType.FLOAT;
        } else if (constant instanceof Long) {
            return ValueStackType.LONG;
        } else if (constant instanceof Double) {
            return ValueStackType.DOUBLE;
        } else if (constant instanceof String) {
            return ValueStackType.STRING;
        } else if (constant instanceof Type) {
            return TypeStackType.INSTANCE;
        }
        throw new IllegalArgumentException(constant + " has an illegal type");
    }

}
