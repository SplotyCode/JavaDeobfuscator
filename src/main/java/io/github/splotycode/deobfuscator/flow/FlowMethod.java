package io.github.splotycode.deobfuscator.flow;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class FlowMethod {

    private MethodNode method;
    private FlowClass clazz;

    private FlowMethod overrides;
    private ArrayList<FlowMethod> overriders = new ArrayList<>();

    public FlowMethod(MethodNode method, FlowClass clazz) {
        this.method = method;
        this.clazz = clazz;
    }

    public void update(FlowControl flowControl) {
        overrides = null;
        if (!hasModifier(Opcodes.ACC_STATIC)) {
            FlowClass currentSuperClass = clazz.getSuperClass();
            while (currentSuperClass != null) {
                FlowMethod overridden = currentSuperClass.getMethod(getName());
                if (overridden != null) {
                    overrides = overridden;
                    overridden.getOverriders().add(this);
                    break;
                }
                currentSuperClass = currentSuperClass.getSuperClass();
            }
        }
    }

    public String getName() {
        return method.name;
    }

    public void remove() {
        clazz.getClassNode().methods.remove(method);
    }

    public boolean hasModifier(int modifier) {
        return (method.access & modifier) != 0;
    }

    /* TODO check if it is the actual entry point via manifest */
    public boolean isEntryPoint() {
        return method.name.equals("main") &&
                method.desc.equals("([Ljava/lang/String;)V") &&
                hasModifier(Opcodes.ACC_PUBLIC) &&
                hasModifier(Opcodes.ACC_STATIC);
    }

    public boolean isClassInit() {
        return hasModifier(Opcodes.ACC_STATIC) &&
                method.name.equals("<clinit>") &&
                method.desc.equals("()V");
    }
}
