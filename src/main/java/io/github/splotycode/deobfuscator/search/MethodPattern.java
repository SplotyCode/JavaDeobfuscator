package io.github.splotycode.deobfuscator.search;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class MethodPattern {

    public static String generatePattern(ClassNode classNode, MethodNode methodNode) {
        return generatePattern(classNode.name, methodNode.name, methodNode.desc);
    }

    public static String generatePattern(MethodInsnNode methodInsnNode) {
        return generatePattern(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
    }

    public static String generatePattern(String owner, String name, String desc) {
        return owner + "#" + name + " " + desc;
    }

    private String owner, name, desc;

    public MethodPattern(String pattern) {
        String[] split = pattern.split("#");
        if (split.length == 1) {
            desc = pattern;
        } else if (split.length == 2) {
            owner = notEmpty(split[0]);
            name = split[1];
            if (name.isEmpty()) {
                name = null;
            } else {
                String[] nameSplit = name.split(" ");
                if (nameSplit.length == 2) {
                    name = notEmpty(nameSplit[0]);
                    desc = notEmpty(nameSplit[1]);
                }
            }
        } else {
            throw new IllegalStateException("Failed to parse method pattern: " + pattern);
        }
    }

    public MethodPattern(String owner, String name, String desc) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    public boolean matches(MethodInsnNode method) {
        return matchesOrNull(owner, method.owner) &&
                matchesOrNull(name, method.name) &&
                matchesOrNull(desc, method.desc);
    }

    private static boolean matchesOrNull(String pattern, String check) {
        return pattern == null || pattern.equals(check);
    }

    private static String notEmpty(String str) {
        if (str.isEmpty()) {
            return null;
        }
        return str;
    }

}
