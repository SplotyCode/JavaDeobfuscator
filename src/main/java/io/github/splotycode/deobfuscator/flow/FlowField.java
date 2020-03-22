package io.github.splotycode.deobfuscator.flow;

import io.github.splotycode.deobfuscator.flow.stack.StackInstruction;
import io.github.splotycode.deobfuscator.flow.stack.StackValue;
import io.github.splotycode.deobfuscator.flow.stack.ValueStackType;
import io.github.splotycode.deobfuscator.search.InstructionSearch;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import lombok.Getter;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

@Getter
public class FlowField implements FlowVariable, InstructionSearch {

    private FieldNode fieldNode;
    private FlowClass flowClass;
    private HashSet<Type> usedTypes = new HashSet<>();
    private HashSet<Object> staticValues = new HashSet<>();
    private Type baseType;

    public FlowField(FieldNode fieldNode, FlowClass flowClass) {
        this.fieldNode = fieldNode;
        this.flowClass = flowClass;
    }

    @Override
    public String getName() {
        return fieldNode.name;
    }

    @Override
    public Type baseType() {
        return baseType;
    }

    @Override
    public Set<Type> usedTypes() {
        return usedTypes;
    }

    @Override
    public Set<Object> staticValues() {
        return staticValues;
    }

    @Override
    public void update(FlowControl flowControl) {
        usedTypes.clear();
        staticValues.clear();

        baseType = Type.getType(fieldNode.desc);
        if (Modifier.isPrivate(fieldNode.access)) {
            search(flowClass.getClassNode());
        } else {
            for (FlowClass clazz : flowControl.getAllClasses()) {
                search(clazz.getClassNode());
            }
        }
    }

    @Override
    public boolean onInstruction(MethodNode caller, AbstractInsnNode instruction) {
        if (instruction.getOpcode() == Opcodes.PUTSTATIC) {
            FieldInsnNode field = (FieldInsnNode) instruction;
            if (field.owner.equals(flowClass.getName()) && field.name.equals(getName()) && field.desc.equals(baseType().getDescriptor())) {
                StackValue value = StackInstruction.guesStackValue(field, 1);
                System.out.println(getName() + " " + value);
                if (value != null && value.getType().hasDeclarationType()) {
                    if (value.getStaticValue() != null) {
                        staticValues.add(value.getStaticValue());
                    }
                    usedTypes.add(((ValueStackType) value.getType()).getType());
                }
            }
        }
        return false;
    }
}
