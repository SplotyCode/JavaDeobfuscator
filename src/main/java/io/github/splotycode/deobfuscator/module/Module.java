package io.github.splotycode.deobfuscator.module;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

public abstract class Module {

    public abstract boolean transform(ClassNode classNode);

}
