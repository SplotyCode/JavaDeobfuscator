package io.github.splotycode.deobfuscator.flow;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;

@Getter
public class FlowClass {

    private ClassNode classNode;
    private FlowClass superClass;

    private HashMap<String, FlowMethod> methods = new HashMap<>();
    private ArrayList<FlowClass> extenders = new ArrayList<>();

    public FlowClass(ClassNode classNode) {
        this.classNode = classNode;
    }

    public FlowMethod getMethod(String name) {
        return methods.get(name);
    }

    private void registerAsExtender(FlowClass superClass) {
        superClass.extenders.add(this);
    }

    public void update(FlowControl flowControl) {
        superClass = flowControl.getClass(classNode.superName);

        if (superClass != null) {
            registerAsExtender(superClass);
        }
        for (String interfaceName : classNode.interfaces) {
            registerAsExtender(flowControl.getClass(interfaceName));
        }

        for (FlowMethod method : methods.values()) {
            method.update(flowControl);
        }
    }
}
