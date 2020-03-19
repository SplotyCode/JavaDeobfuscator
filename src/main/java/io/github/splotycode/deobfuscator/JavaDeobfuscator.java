package io.github.splotycode.deobfuscator;

import io.github.splotycode.deobfuscator.module.Module;
import io.github.splotycode.deobfuscator.module.PrettyPrintModule;
import io.github.splotycode.deobfuscator.module.StaticStringLength;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class JavaDeobfuscator {

    private static JavaDeobfuscator instance = new JavaDeobfuscator();

    public static void main(String[] args) {}

    private File inputFile = new File("java_kZKdm5.zip");
    private File outputFile = new File("output.zip");

    private Map<String, ClassNode> classes = new HashMap<>();
    private ArrayList<Module> modules = new ArrayList<>();
    private ArrayList<ZipEntry> resources = new ArrayList<>();

    private byte[] buffer = new byte[1024 * 4];

    {
        modules.add(new PrettyPrintModule());
        modules.add(new StaticStringLength());
    }

    @SneakyThrows
    public JavaDeobfuscator() {
        loadClasses();
        transformClasses();
        writeClasses();
    }

    @SneakyThrows
    private void writeClasses() {
        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(outputFile))) {
            for (Map.Entry<String, ClassNode> entry : classes.entrySet()) {
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                entry.getValue().accept(writer);
                zipStream.putNextEntry(new ZipEntry(entry.getKey()));
                zipStream.write(writer.toByteArray());
                zipStream.closeEntry();
            }
            try (ZipFile zipIn = new ZipFile(inputFile)) {
                for (ZipEntry resource : resources) {
                    zipStream.putNextEntry(resource);
                    InputStream in = zipIn.getInputStream(resource);
                    while (0 < in.available()){
                        int read = in.read(buffer);
                        if (read > 0) {
                            zipStream.write(buffer, 0, read);
                        }
                    }
                    zipStream.closeEntry();
                }
            }
        }
    }

    private void transformClasses() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ClassNode classNode : classes.values()) {
                for (Module module : modules) {
                    if (module.transform(classNode)) {
                        changed = true;
                    }
                }
            }
        }
    }

    private void loadClasses() throws IOException {
        try (ZipFile zipIn = new ZipFile(inputFile)) {
            Enumeration<? extends ZipEntry> e = zipIn.entries();
            while (e.hasMoreElements()) {
                ZipEntry next = e.nextElement();
                String name = next.getName();
                if (!name.endsWith(".class") && !name.endsWith(".class/")) {
                    resources.add(next);
                    continue;
                }

                ClassReader reader = new ClassReader(zipIn.getInputStream(next));
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.SKIP_FRAMES);
                classes.put(name, node);
            }
        }
    }

}
