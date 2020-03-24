package io.github.splotycode.deobfuscator.flow;

import com.google.common.collect.HashMultimap;
import io.github.splotycode.deobfuscator.JavaDeobfuscator;
import io.github.splotycode.deobfuscator.search.MethodPattern;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

@Getter
public class FlowControl {

    private HashMap<String, FlowClass> classes = new HashMap<>();

    private HashMultimap<String, FlowMethod> methods = HashMultimap.create();
    private HashMap<String, FlowMethod> methodsSignature = new HashMap<>();

    public void update() {
        System.out.println("->Rebuilding flow control");

        classes.clear();
        methods.clear();
        methodsSignature.clear();

        for (ClassNode classNode : JavaDeobfuscator.getInstance().getClasses().values()) {
            String name = classNode.name;
            FlowClass clazz = new FlowClass(classNode, true);
            classes.put(name, clazz);
            for (MethodNode methodNode : classNode.methods) {
                FlowMethod method = new FlowMethod(methodNode, clazz);
                methods.put(name, method);
                clazz.getMethods().put(method.getName(), method);
                methodsSignature.put(MethodPattern.generatePattern(classNode, methodNode), method);
            }
            for (FieldNode fieldNode : classNode.fields) {
                FlowField field = new FlowField(fieldNode, clazz);
                clazz.getFields().put(field.getName(), field);
            }
        }
        for (FlowClass clazz : classes.values()) {
            clazz.update(this);
        }
    }

    public Collection<FlowClass> getAllClasses() {
        return classes.values();
    }

    public FlowClass getClass(String name) {
        return classes.get(name);
    }

    public FlowClass getClass(ClassNode classNode) {
        return getClass(classNode.name);
    }

    public FlowMethod getMethod(String signature) {
        return methodsSignature.get(signature);
    }

    public ArrayList<FlowMethod> copyMethods() {
        return new ArrayList<>(methods.values());
    }

    public HashSet<String> copySignatures() {
        return new HashSet<>(methodsSignature.keySet());
    }

}
