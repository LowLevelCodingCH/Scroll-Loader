package ch.llcch.nx.scroll.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import ch.llcch.nx.scroll.main.Bootstrap;

/**
 * Transforms class bytecode (byte[]) to a transformed bytecode. (A bytecode)
 * 
 * @author  Alexander R. O.
 * @version 0.0.2
 * @since   0.0.2
 */
public class ScrollClassTransformer {
	/**
	 * Finds a method in a JavaClass (Apache BCEL) (Time Complexity: O(n)).
	 * 
	 * @param clazz The JavaClass
	 * @param clGen The ClassGen
	 * @param cpGen The ConstantPoolGen
	 * @param name The method name
	 * @return The MethodGen of the found method
	 */
	private static MethodGen findMethod(JavaClass clazz, ClassGen clGen,
										ConstantPoolGen cpGen, String name) {
		MethodGen mdGen = null;
		
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                mdGen = new MethodGen(method, clGen.getClassName(), cpGen);
                break;
            }
        }
        
        if (mdGen == null)
            throw new RuntimeException("Method not found: " + name);
        
        return mdGen;
	}
	
	/**
	 * Recalculates stuff to fix stuff. I don't really know. I just found out this works.
	 * 
	 * @param in Input bytecode. 
	 * @return The output bytecode, the fixed one.
	 */
	private static byte[] fixBytecode(byte[] in) {
		ClassReader reader = new ClassReader(in);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.SKIP_FRAMES);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
	}
	
	/**
	 * The injection for the class_1802 class (Items registry).
	 * 
	 * @param in Input bytecode.
	 * @return Transformed bytecode.
	 * @throws ClassFormatException
	 * @throws IOException
	 */
	private static byte[] Class1802(byte[] in) throws ClassFormatException, IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayInputStream  inStream  = new ByteArrayInputStream(in);

        ClassParser parser = new ClassParser(inStream, "net.minecraft.class_1802");
        JavaClass   clazz  = parser.parse();
        
        ClassGen        clGen = new ClassGen(clazz);
        ConstantPoolGen cpGen = clGen.getConstantPool();
        MethodGen       mdGen = findMethod(clazz, clGen, cpGen, "<clinit>");
        MethodGen       oldMd = mdGen;
        
        InstructionFactory factory = new InstructionFactory(clGen, cpGen);
        InstructionList    il      = mdGen.getInstructionList();

        InstructionList prepend = new InstructionList();
        
        prepend.append(new PUSH(cpGen, "scroll_test_item"));
        prepend.append(factory.createInvoke("net.minecraft.class_1802", "method_7990",
                new ObjectType("net.minecraft.class_1792"), new Type[]{Type.STRING}, Const.INVOKESTATIC));
        
        il.insert(prepend);

        mdGen.setMaxStack();
        mdGen.setMaxLocals();

        clGen.replaceMethod(oldMd.getMethod(), mdGen.getMethod());

        clazz = clGen.getJavaClass();
        
        clazz.dump(outStream);
                
        return fixBytecode(outStream.toByteArray());
    }
	
	/**
	 * The injection for the main class and main method.
	 * 
	 * @param in Input bytecode.
	 * @return Transformed bytecode.
	 * @throws ClassFormatException
	 * @throws IOException
	 */
	private static byte[] NetMinecraftClientMainMain(byte[] in) throws ClassFormatException, IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayInputStream  inStream  = new ByteArrayInputStream(in);

        ClassParser parser = new ClassParser(inStream, "net.minecraft.client.main.Main");
        JavaClass   clazz  = parser.parse();
        
        ClassGen        clGen = new ClassGen(clazz);
        ConstantPoolGen cpGen = clGen.getConstantPool();
        MethodGen       mdGen = findMethod(clazz, clGen, cpGen, "main");
        MethodGen       oldMd = mdGen;
        
        InstructionFactory factory = new InstructionFactory(clGen, cpGen);
        InstructionList    il      = mdGen.getInstructionList();

        InstructionList prepend = new InstructionList();
        prepend.append(factory.createFieldAccess("java.lang.System", "out",
                new ObjectType("java.io.PrintStream"), Const.GETSTATIC));
        prepend.append(new PUSH(cpGen, ">>> Injected this into that: net.minecraft.client.main.Main.main([Ljava/lang/String;)V"));
        prepend.append(factory.createInvoke("java.io.PrintStream", "println",
                Type.VOID, new Type[]{Type.STRING}, Const.INVOKEVIRTUAL));

        il.insert(prepend);

        mdGen.setMaxStack();
        mdGen.setMaxLocals();

        clGen.replaceMethod(oldMd.getMethod(), mdGen.getMethod());
        
        clazz = clGen.getJavaClass();
        
        clazz.dump(outStream);
        
        return fixBytecode(outStream.toByteArray());
    }
   
	/**
	 * Routes a class transformation via a name.
	 * 
	 * @param in Input bytecode.
	 * @param name Determines which method is going to be called to transform `in`.
	 * @return The transformed bytecode.
	 * @throws ClassFormatException
	 * @throws IOException
	 */
    private static byte[] routeClassTransform(byte[] in, String name) throws ClassFormatException, IOException {
        if (name.equals("net.minecraft.client.main.Main")) {
			try {
				return NetMinecraftClientMainMain(in);
			} catch (ClassFormatException | IOException e) {
				Bootstrap.LOGGER.err("Could not load the class because of a format error or Input/Output exception");
				e.printStackTrace();
			}
        } else if (name.equals("net.minecraft.class_1802")) { // Items
			try {
				return Class1802(in);
			} catch (ClassFormatException | IOException e) {
				Bootstrap.LOGGER.err("Could not load the class because of a format error or Input/Output exception");
				e.printStackTrace();
			}
        }

        return null;
    }

    /**
     * Transforms a class (The public method).
     * 
     * @param in Input bytecode
     * @param name Full class name (format: tld.domain.package.Class).
     * @return Transformed bytecode.
     * @throws ClassFormatException
     * @throws IOException
     */
    public static byte[] transformClass(byte[] in, String name) throws ClassFormatException, IOException {
        byte[] transformed = routeClassTransform(in, name);
        return transformed != null ? transformed : in;
    }
}
