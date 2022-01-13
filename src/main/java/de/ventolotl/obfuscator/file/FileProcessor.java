package de.ventolotl.obfuscator.file;

import de.ventolotl.obfuscator.configuration.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static de.ventolotl.obfuscator.file.CustomClassWriter.DEFAULT_SUPER_CLASS;

public final class FileProcessor {
  private final ZipFile inputFile;
  private final List<ZipFile> librariesFiles;

  private final File output;

  private final Map<String, ClassNode> classes = new HashMap<>();
  private final Map<String, ClassNode> libraryClasses = new HashMap<>();
  private final Map<String, ClassNode> classPath = new HashMap<>();

  private final Map<String, ClassHierarchy> classHierarchy = new HashMap<>();
  private final Map<String, byte[]> resources = new HashMap<>();

  public static FileProcessor from(Configuration configuration) throws IOException {
    return from(configuration.inputFile(), configuration.outputFile(), configuration.libraries());
  }

  public static FileProcessor from(File input, File output, List<File> libraries) {
    return new FileProcessor(input, output, libraries);
  }

  public FileProcessor(File input, File output, List<File> libraries) {
    this.inputFile = zipFileOf(input);
    this.output = output;
    this.librariesFiles = libraries.stream().map(this::zipFileOf).collect(Collectors.toList());

    load();
    buildHierarchy();
  }

  private ZipFile zipFileOf(File file) {
    try {
      return new ZipFile(file);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to resolve ZipFile of " + file.getAbsolutePath());
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////
  // File Loading
  ////////////////////////////////////////////////////////////////////////////////////////

  private void load() {
    loadZipFile(inputFile, ((entry, bytes) -> {
      if (isClass(entry)) {
        classes.put(entry.getName(), initializeClassNode(bytes));
      } else {
        resources.put(entry.getName(), bytes);
      }
    }));
    for (ZipFile librariesFile : librariesFiles) {
      loadZipFile(librariesFile, ((entry, bytes) -> {
        if (isClass(entry)) {
          libraryClasses.put(entry.getName(), initializeClassNode(bytes));
        }
      }));
    }
    classPath.putAll(classes);
    classPath.putAll(libraryClasses);
  }

  private void loadZipFile(ZipFile file, BiConsumer<ZipEntry, byte[]> consumer) {
    file.stream().forEach(entry -> {
      if (!entry.isDirectory()) {
        try {
          processEntry(file, entry, consumer);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void processEntry(
    ZipFile file,
    ZipEntry entry,
    BiConsumer<ZipEntry, byte[]> consumer
  ) throws IOException {
    InputStream inputStream = file.getInputStream(entry);
    consumer.accept(entry, toByteArray(inputStream));
    inputStream.close();
  }

  private byte[] toByteArray(InputStream in) {
    try {
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final byte[] buffer = new byte[1024];
      while (in.available() > 0) {
        final int data = in.read(buffer);
        out.write(buffer, 0, data);
      }
      in.close();
      out.close();
      return out.toByteArray();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      throw new RuntimeException(ioe.getMessage());
    }
  }

  private boolean isClass(ZipEntry entry) {
    return entry.getName().endsWith(".class");
  }

  private ClassNode initializeClassNode(byte[] bytes) {
    ClassReader classReader = new ClassReader(bytes);
    ClassNode classNode = new ClassNode();
    classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
    return classNode;
  }

  public void transformClasses(Consumer<ClassNode> consumer) {
    for (ClassNode classNode : classes.values()) {
      consumer.accept(classNode);
    }
  }

  private void buildHierarchy() {
    for (Map.Entry<String, ClassNode> entry : classPath.entrySet()) {
      ClassNode classNode = entry.getValue();

      classHierarchy.put(entry.getKey(), hierarchyOf(classNode));
    }
  }

  private ClassHierarchy hierarchyOf(ClassNode classNode) {
    String superName = classNode.superName == null ? DEFAULT_SUPER_CLASS : classNode.superName;
    ClassNode superClass = classPath.get(superName + ".class");

    if (superClass == null) {
      throw new IllegalStateException("Class " + superName + " not in ClassPath. Check your libraries.");
    }

    boolean objectClass = classNode.name.equals(DEFAULT_SUPER_CLASS);

    return !objectClass
      ? new ClassHierarchy(classNode, hierarchyOf(superClass), classNode.interfaces)
      : null;
  }

  ////////////////////////////////////////////////////////////////////////////////////////
  // File Saving
  ////////////////////////////////////////////////////////////////////////////////////////

  public void save() {
    ZipOutputStream outputStream;
    try {
      outputStream = prepareOutputFile();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to prepare output file", e);
    }

    writeClasses(outputStream);
    writeResources(outputStream);

    try {
      outputStream.close();
    } catch (IOException e) {
      throw new IllegalStateException("An IOError has occurred", e);
    }
  }

  private ZipOutputStream prepareOutputFile() throws IOException {
    if (output.exists()) {
      output.delete();
    }

    output.createNewFile();

    return new ZipOutputStream(new FileOutputStream(output));
  }

  private void writeClasses(ZipOutputStream outputStream) {
    for (Map.Entry<String, ClassNode> entry : classes.entrySet()) {
      try {
        writeClass(outputStream, entry.getValue());
      } catch (Exception e) {
        System.err.println("Cannot write class " + entry.getKey());
        e.printStackTrace();
      }
    }
  }

  private void writeClass(ZipOutputStream outputStream, ClassNode classNode) throws IOException {
    outputStream.putNextEntry(new ZipEntry(classNode.name + ".class"));
    outputStream.write(ClassBytesResolver.bytesOfClass(classNode, classHierarchy));
    outputStream.closeEntry();
  }

  private void writeResources(ZipOutputStream outputStream) {
    for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
      try {
        writeResource(outputStream, entry.getKey(), entry.getValue());
      } catch (Exception e) {
        System.err.println("Cannot write resource " + entry.getKey());
        e.printStackTrace();
      }
    }
  }

  private void writeResource(ZipOutputStream outputStream, String name, byte[] bytes) throws IOException {
    outputStream.putNextEntry(new ZipEntry(name));
    outputStream.write(bytes);
    outputStream.closeEntry();
  }
}