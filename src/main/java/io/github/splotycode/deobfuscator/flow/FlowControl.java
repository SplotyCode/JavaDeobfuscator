package io.github.splotycode.deobfuscator.flow;

import com.google.common.collect.HashMultimap;
import io.github.splotycode.deobfuscator.JavaDeobfuscator;
import io.github.splotycode.deobfuscator.search.MethodPattern;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class FlowControl {

    private HashMap<String, FlowClass> classes = new HashMap<>();

    private HashMultimap<String, FlowMethod> methods = HashMultimap.create();
    private HashMap<String, FlowMethod> methodsSignature = new HashMap<>();

    public void update() {
        classes.clear();
        methods.clear();
        methodsSignature.clear();
        for (ClassNode classNode : JavaDeobfuscator.getInstance().getClasses().values()) {
            String name = classNode.name;
            FlowClass clazz = new FlowClass(classNode);
            classes.put(name, clazz);
            for (MethodNode methodNode : classNode.methods) {
                FlowMethod method = new FlowMethod(methodNode, clazz, true);
                methods.put(name, method);
                methodsSignature.put(MethodPattern.generatePattern(classNode, methodNode), method);
            }
        }
        for (FlowClass clazz : classes.values()) {
            clazz.update(this);
        }
    }

    public FlowClass getClass(String name) {
        return classes.get(name);
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
