package io.github.splotycode.deobfuscator.flow.stack;

import jdk.internal.org.objectweb.asm.Type;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public class ValueStackType implements StackType {

    public static final ValueStackType INT = new ValueStackType(Type.INT_TYPE);
    public static final ValueStackType FLOAT = new ValueStackType(Type.INT_TYPE);
    public static final ValueStackType LONG = new ValueStackType(Type.INT_TYPE);
    public static final ValueStackType DOUBLE = new ValueStackType(Type.INT_TYPE);

    public static final ValueStackType STRING = new ValueStackType(Type.getType(String.class));

    private Type type;

    public ValueStackType(Type type) {
        this.type = type;
    }

    @Override
    public boolean hasDeclarationType() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueStackType that = (ValueStackType) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
