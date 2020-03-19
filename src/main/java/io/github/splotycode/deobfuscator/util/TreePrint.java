package io.github.splotycode.deobfuscator.util;

import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.util.Printer;
import jdk.internal.org.objectweb.asm.util.Textifier;
import jdk.internal.org.objectweb.asm.util.TraceMethodVisitor;

public class TreePrint {

    private static final Printer printer = new Textifier();
    private static final TraceMethodVisitor methodPrinter = new TraceMethodVisitor(printer);

    public static void prettyPrint(MethodNode method) {
        method.accept(methodPrinter);
        System.out.println(printer.getText());
        printer.getText().clear();
    }
}
