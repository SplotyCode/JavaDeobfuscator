package io.github.splotycode.deobfuscator.flow;

import io.github.splotycode.deobfuscator.flow.stack.StackInstruction;
import io.github.splotycode.deobfuscator.flow.stack.StackType;
import io.github.splotycode.deobfuscator.flow.stack.StackValue;
import io.github.splotycode.deobfuscator.util.InstructionUtil;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class FlowValue {

    private StackType type;
    private Object staticValue;
    private Map<String, FlowValue> components = new HashMap<>();
    private boolean missingComponents;
    private boolean hasUnknownUsages;

    private Map<AbstractInsnNode, InsnList> accessors = new HashMap<>();
    private Map<AbstractInsnNode, InsnList> usages = new HashMap<>();

    public FlowValue() {}

    public FlowValue(StackValue stackValue, InsnList instructions) {
        this(stackValue, stackValue == null ? null : stackValue.getCreator(), instructions);
    }

    public FlowValue(StackValue stackValue, AbstractInsnNode creator, InsnList instructions) {
       setCreation(stackValue, creator, instructions);
    }

    private void setCreation(StackValue stackValue, AbstractInsnNode creator, InsnList instructions) {
        if (stackValue == null) return;
        type = stackValue.getType();
        staticValue = stackValue.getStaticValue();

        trackUsage(creator, instructions, true);
    }

    public void trackUsage(AbstractInsnNode instruction, InsnList instructions) {
        trackUsage(instruction, instructions ,false);
    }

    void trackUsage(AbstractInsnNode instruction, InsnList instructions, boolean creation) {
        if (!creation) {
            accessors.put(instruction, instructions);
        }
        if (sourceKnown() && type.isReference()) {
            for (StackInstruction.StackNode usageNode : StackInstruction.getUsages(instruction, 1)) {
                AbstractInsnNode usage = usageNode.getInstruction();
                usages.put(usage, instructions);
                switch (usage.getOpcode()) {
                    case Opcodes.AASTORE: /* reference */
                    case Opcodes.BASTORE: /* byte or boolean */
                    case Opcodes.CASTORE: /* char */
                    case Opcodes.DASTORE: /* double */
                    case Opcodes.FASTORE: /* float */
                    case Opcodes.LASTORE: /* long */
                    case Opcodes.IASTORE: {/* int */
                        StackValue index = StackInstruction.guesStackValue(usage, 2);
                        if (index != null && index.getStaticValue() instanceof Integer) {
                            StackValue rawValue = StackInstruction.guesStackValue(usage, 1);
                            FlowValue value = components.get(index.getStaticValue().toString());
                            if (value == null) {
                                value = new FlowValue(rawValue, usage, instructions);
                                components.put(index.getStaticValue().toString(), value);
                            } else {
                                value.setCreation(rawValue, usage, instructions);
                            }
                            //value.accessors.put(usage, instructions);
                        } else {
                            missingComponents = true;
                        }
                        break;
                    } case Opcodes.AALOAD: /* reference */
                    case Opcodes.BALOAD: /* byte or boolean */
                    case Opcodes.CALOAD: /* char */
                    case Opcodes.DALOAD: /* double */
                    case Opcodes.FALOAD: /* float */
                    case Opcodes.LALOAD: /* long */
                    case Opcodes.IALOAD: /* int */
                        StackValue indexValue = StackInstruction.guesStackValue(usage, 1);
                        if (indexValue != null && indexValue.getStaticValue() instanceof Integer) {
                            FlowValue value = components.computeIfAbsent(indexValue.getStaticValue().toString(), s -> new FlowValue());
                            value.trackUsage(usage, instructions);
                            value.accessors.put(usage, instructions);
                        } else {
                            hasUnknownUsages = true;
                        }
                        break;
                    default:
                        hasUnknownUsages = true;
                }

            }
        }
    }

    public AbstractInsnNode createStaticInstruction() {
        if (staticValue instanceof Integer) {
            return InstructionUtil.getStoreIntInstruction((Integer) staticValue);
        }
        throw new IllegalStateException("TODO");
    }

    public boolean foundFlow() {
        return !missingComponents && !hasUnknownUsages;
    }

    public boolean hasDeclarationType() {
        return type != null && type.hasDeclarationType();
    }

    public boolean sourceKnown() {
        return type != null;
    }

    public boolean isConstant() {
        return staticValue != null;
    }

    @Override
    public String toString() {
        return "FlowValue{" +
                "type=" + type +
                ", staticValue=" + staticValue +
                ", components=" + components +
                ", missingComponents=" + missingComponents +
                ", hasUnknownUsages=" + hasUnknownUsages +
                ", accessors=" + InstructionUtil.opCodeNames(accessors.keySet()) +
                ", usages=" + InstructionUtil.opCodeNames(usages.keySet()) +
                '}';
    }
}
