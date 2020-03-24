package io.github.splotycode.deobfuscator.module;

import io.github.splotycode.deobfuscator.JavaDeobfuscator;
import io.github.splotycode.deobfuscator.flow.FlowClass;
import io.github.splotycode.deobfuscator.flow.FlowField;
import io.github.splotycode.deobfuscator.flow.FlowValue;
import io.github.splotycode.deobfuscator.flow.stack.ArrayReferenceStackType;
import io.github.splotycode.deobfuscator.flow.stack.StackInstruction;
import io.github.splotycode.deobfuscator.util.InstructionUtil;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;

import java.util.Map;
import java.util.OptionalInt;

public class StaticArrayModule extends Module {

    private static OptionalInt parseInt(String str) {
        try {
            return OptionalInt.of(Integer.valueOf(str));
        } catch (NumberFormatException ex) {
            return OptionalInt.empty();
        }
    }

    @Override
    public boolean transform(ClassNode classNode) {
        boolean changed = false;
        FlowClass clazz = JavaDeobfuscator.getInstance().getFlowControl().getClass(classNode);
        for (FlowField field : clazz.getFields().values()) {
            FlowValue value = field.getConstantValue();
            if (value != null &&
                    value.getType() instanceof ArrayReferenceStackType &&
                    value.foundFlow()) {
                for (Map.Entry<String, FlowValue> component : value.getComponents().entrySet()) {
                    OptionalInt index = parseInt(component.getKey());
                    FlowValue arrayValue = component.getValue();
                    if (index.isPresent() &&
                            arrayValue.hasDeclarationType() &&
                            arrayValue.isConstant() && arrayValue.foundFlow()) {
                        for (Map.Entry<AbstractInsnNode, InsnList> usage : arrayValue.getAccessors().entrySet()) {
                            AbstractInsnNode instruction = usage.getKey();
                            AbstractInsnNode newInstruction = arrayValue.createStaticInstruction();
                            if (StackInstruction.removeAllArguments(instruction, usage.getValue())) {
                                usage.getValue().set(instruction, newInstruction);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

}
