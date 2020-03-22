package io.github.splotycode.deobfuscator.flow.stack;

import jdk.internal.org.objectweb.asm.Type;
import lombok.Getter;
import lombok.ToString;

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
}
