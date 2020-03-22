package io.github.splotycode.deobfuscator.flow;

import jdk.internal.org.objectweb.asm.Type;

import java.util.Set;

public interface FlowVariable {

    String getName();

    Type baseType();

    Set<Type> usedTypes();
    Set<Object> staticValues();

    default boolean hasOnlyType(Type type) {
        Set<Type> types = usedTypes();
        return types != null && types.size() == 1 && types.contains(type);
    }

    void update(FlowControl flowControl);

}
