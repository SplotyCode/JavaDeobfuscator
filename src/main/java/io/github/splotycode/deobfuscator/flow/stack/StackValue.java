package io.github.splotycode.deobfuscator.flow.stack;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class StackValue {

    private AbstractInsnNode creator;
    private StackType type;
    private Object staticValue;

}
