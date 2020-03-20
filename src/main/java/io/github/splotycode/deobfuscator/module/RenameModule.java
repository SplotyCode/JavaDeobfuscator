package io.github.splotycode.deobfuscator.module;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class RenameModule extends Module {

    private static boolean hardToRead(String input) {
        for (int i = 0; i < input.length(); i++) {
            char ch = Character.toLowerCase(input.charAt(i));
            if (ch != 'l' && ch != 'i') {
                return false;
            }
        }
        return true;
    }

    private static String generateName(List<LocalVariableNode> localVariables) {
        int i = 0;
        String current;
        block:
        while (true) {
            current = "local" + i;
            for (LocalVariableNode localVariable : localVariables) {
                if (localVariable.name.equals(current)) {
                    i++;
                    continue block;
                }
            }
            break;
        }
        return current;
    }

    @Override
    public boolean transform(ClassNode classNode) {
        boolean changed = false;
        for (MethodNode methodNode : classNode.methods) {
            for (LocalVariableNode localVariable : methodNode.localVariables) {
                if (hardToRead(localVariable.name)) {
                    localVariable.name = generateName(methodNode.localVariables);
                    changed = true;
                }
            }
        }
        return changed;
    }

}
