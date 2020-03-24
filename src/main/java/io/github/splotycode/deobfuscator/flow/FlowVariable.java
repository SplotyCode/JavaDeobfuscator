package io.github.splotycode.deobfuscator.flow;

import io.github.splotycode.deobfuscator.flow.stack.StackType;
import jdk.internal.org.objectweb.asm.Type;

import java.util.Collection;
import java.util.Set;

public interface FlowVariable {

    String getName();

    Type baseType();

    Collection<FlowValue> values();

    FlowValue getConstantValue();

    default boolean hasOnlyType(StackType type) {
        Collection<FlowValue> types = values();
        for (FlowValue value : types) {
            if (!value.sourceKnown() || value.getType().equals(type)) {
                return false;
            }
        }
        return !types.isEmpty();
    }

    void update(FlowControl flowControl);

}
