package io.github.splotycode.deobfuscator.module;

import io.github.splotycode.deobfuscator.util.TreePrint;
import jdk.internal.org.objectweb.asm.tree.*;

public class PrettyPrintModule extends Module {

    @Override
    public boolean transform(ClassNode classNode) {
        System.out.println(classNode.name);
        for (MethodNode methodNode : classNode.methods) {
            System.out.println(methodNode.name + " " + methodNode.desc);
            TreePrint.prettyPrint(methodNode);
            System.out.println();
        }
        System.out.println();
        System.out.println();
        System.out.println();
        return false;
    }

}
