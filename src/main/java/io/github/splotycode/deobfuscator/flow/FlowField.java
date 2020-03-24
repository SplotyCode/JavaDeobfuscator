package io.github.splotycode.deobfuscator.flow;

import io.github.splotycode.deobfuscator.flow.stack.StackInstruction;
import io.github.splotycode.deobfuscator.flow.stack.StackValue;
import io.github.splotycode.deobfuscator.search.InstructionSearch;
import io.github.splotycode.deobfuscator.util.InstructionUtil;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.*;
import lombok.Getter;

import java.lang.reflect.Modifier;
import java.util.*;

@Getter
public class FlowField implements FlowVariable, InstructionSearch {

    private FieldNode fieldNode;
    private FlowClass flowClass;
    private List<FlowValue> values = new ArrayList<>();
    private Type baseType;

    private ArrayList<AbstractInsnNode> writes = new ArrayList<>();
    private HashMap<AbstractInsnNode, InsnList> reads = new HashMap<>();

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
    public void update(FlowControl flowControl) {
        values.clear();
        writes.clear();
        reads.clear();

        baseType = Type.getType(fieldNode.desc);
        if (Modifier.isPrivate(fieldNode.access)) {
            search(flowClass.getClassNode());
        } else {
            for (FlowClass clazz : flowControl.getAllClasses()) {
                search(clazz.getClassNode());
            }
        }

        for (Map.Entry<AbstractInsnNode, InsnList> instruction : reads.entrySet()) {
            for (FlowValue value : values) {
                value.trackUsage(instruction.getKey(), instruction.getValue());
            }
        }
    }

    private boolean matches(FieldInsnNode fieldInstruction) {
        return fieldInstruction.owner.equals(flowClass.getName()) &&
                fieldInstruction.name.equals(getName()) &&
                fieldInstruction.desc.equals(baseType().getDescriptor());
    }

    @Override
    public boolean onInstruction(MethodNode caller, AbstractInsnNode instruction) {
        if (instruction.getOpcode() == Opcodes.PUTSTATIC) {
            FieldInsnNode field = (FieldInsnNode) instruction;
            if (matches(field)) {
                StackValue source = StackInstruction.guesStackValue(field, 1);
                values.add(new FlowValue(source, caller.instructions));
                writes.add(field);
            }
        } else if (instruction.getOpcode() == Opcodes.GETSTATIC) {
            FieldInsnNode field = (FieldInsnNode) instruction;
            if (matches(field)) {
                reads.put(field, caller.instructions);
            }
        }
        return false;
    }

    @Override
    public Collection<FlowValue> values() {
        return values;
    }

    @Override
    public FlowValue getConstantValue() {
        return values.size() == 1 ? values.get(0) : null;
    }
}
