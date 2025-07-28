package ch.llcch.nx.scroll.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import ch.llcch.nx.scroll.annotate.Note;
import ch.llcch.nx.scroll.annotate.Todo;
import ch.llcch.nx.scroll.api.ScrollModInterface;
import ch.llcch.nx.scroll.api.ScrollRegistryItem;
import ch.llcch.nx.scroll.main.Bootstrap;

public class ScrollClassTransformer {
	public static final Map<String, String> OBFUSCATION_MAP = new HashMap<>();

	static {
		OBFUSCATION_MAP.put("Main", "net.minecraft.client.main.Main");
		OBFUSCATION_MAP.put("Blocks", "net.minecraft.class_2246");
	    OBFUSCATION_MAP.put("Items", "net.minecraft.class_1802");
	    OBFUSCATION_MAP.put("AbstractBlock", "net.minecraft.class_4970");
	    OBFUSCATION_MAP.put("AbstractBlock.Settings", "net.minecraft.class_4970$class_2251");
	    OBFUSCATION_MAP.put("Block", "net.minecraft.class_2248");
	    OBFUSCATION_MAP.put("Item", "net.minecraft.class_1792");
	    OBFUSCATION_MAP.put("Item.Settings", "net.minecraft.class_1792$class_1793");
	    OBFUSCATION_MAP.put("LastBlock", "field_56455");
	    OBFUSCATION_MAP.put("LastItem", "field_8574");
	    OBFUSCATION_MAP.put("AbstractBlock.Settings.create", "method_9637");
	    OBFUSCATION_MAP.put("Blocks.register(String, AbstractBlock.Settings)", "method_9492");
	    OBFUSCATION_MAP.put("Items.register(String, Function<Item.Settings, Item>, Item.Settings)", "method_63750");
	}
	
	public static String getMapped(String key) {
		return OBFUSCATION_MAP.get(key);
	}
	
	public static Object currentObject;
		
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

	private static byte[] fixBytecode(byte[] in) {
		ClassReader reader = new ClassReader(in);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.SKIP_FRAMES);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
	}

	@Todo(what = "Return 'Block' or so. Also use register in items for String Block because we need block items", due = "3.0.0")
	private static byte[] Class2246(byte[] in, ScrollModInterface[] modInterfaces) throws ClassFormatException, IOException {
		@Note(what = "No need to close these Streams")
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayInputStream  inStream  = new ByteArrayInputStream(in);

        ClassParser parser = new ClassParser(inStream, getMapped("Blocks"));
        JavaClass   clazz  = parser.parse();
        
        ClassGen        clGen = new ClassGen(clazz);
        ConstantPoolGen cpGen = clGen.getConstantPool();
        MethodGen       mdGen = findMethod(clazz, clGen, cpGen, "<clinit>");
        MethodGen       oldMd = mdGen;
        
        ConstantPoolGen cp = mdGen.getConstantPool();

        InstructionFactory factory = new InstructionFactory(clGen, cpGen);
        InstructionList    il      = mdGen.getInstructionList();

        InstructionList prepend = new InstructionList();
        
		String nameAbstractBlockSettings = getMapped("AbstractBlock.Settings");
		ObjectType typeAbstractBlockSettings = new ObjectType(nameAbstractBlockSettings);
        
        for (ScrollModInterface modInterface : modInterfaces) {
        	for (ScrollRegistryItem block : modInterface.BLOCKS.getItems()) {
                currentObject = block.obj;
                
                String s = modInterface.modId + "." + block.id;
                
                Bootstrap.LOGGER.info(">> Creating block " + s);
                
                prepend.append(new PUSH(cpGen, s));
                
        		prepend.append(factory.createInvoke(typeAbstractBlockSettings.getClassName(),
        				getMapped("AbstractBlock.Settings.create"), typeAbstractBlockSettings,
        			    Type.NO_ARGS, Const.INVOKESTATIC));

        		prepend.append(factory.createInvoke(getMapped("Blocks"),
        				getMapped("Blocks.register(String, AbstractBlock.Settings)"),
        				new ObjectType(getMapped("Block")), new Type[]{Type.STRING,
        				new ObjectType(nameAbstractBlockSettings)}, Const.INVOKESTATIC));
        	}
        }
        
        InstructionHandle targetHandle = null;
        
        for (InstructionHandle ih : il.getInstructionHandles()) {
            Instruction instr = ih.getInstruction();
            if (instr instanceof PUTSTATIC) {
                PUTSTATIC putstatic = (PUTSTATIC) instr;
                if (putstatic.getName(cp).equals(getMapped("LastBlock"))) {
                    targetHandle = ih;
                    break;
                }
            }
        }
        
        il.append(targetHandle, prepend);

        mdGen.setMaxStack();
        mdGen.setMaxLocals();

        clGen.replaceMethod(oldMd.getMethod(), mdGen.getMethod());

        clazz = clGen.getJavaClass();
        
        clazz.dump(outStream);
                
        return fixBytecode(outStream.toByteArray());
    }
	
	@Todo(what = "Return 'Item' or so with this. and Use RegistryKey.of with Identifier.of and other methods", due = "3.0.0")
	private static byte[] Class1802(byte[] in, ScrollModInterface[] modInterfaces) throws ClassFormatException, IOException {
		@Note(what = "No need to close these Streams")
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayInputStream  inStream  = new ByteArrayInputStream(in);

        ClassParser parser = new ClassParser(inStream, getMapped("Items"));
        JavaClass   clazz  = parser.parse();
        
        ClassGen        clGen = new ClassGen(clazz);
        ConstantPoolGen cpGen = clGen.getConstantPool();
        MethodGen       mdGen = findMethod(clazz, clGen, cpGen, "<clinit>");
        MethodGen       oldMd = mdGen;
        
        ConstantPoolGen cp = mdGen.getConstantPool();
        
        InstructionFactory factory = new InstructionFactory(clGen, cpGen);
        InstructionList    il      = mdGen.getInstructionList();

        InstructionList prepend = new InstructionList();
        
		String nameItemSettings = getMapped("Item.Settings");
		ObjectType typeItemSettings = new ObjectType(nameItemSettings);
        
        for (ScrollModInterface modInterface : modInterfaces) {
        	for (ScrollRegistryItem item : modInterface.ITEMS.getItems()) { // TODO: Use RegistryKey's
        		currentObject = item.obj; // will be used for manual item settings and maybe factories (likely not the latter)
        		// But ill do settings later firstly i gotta get factories down
        		// (btw via static field access of this class)
        		
        		String s = modInterface.modId + "." + item.id;

        		Bootstrap.LOGGER.info(">> Creating item " + s); //TODO: if we get registry keys replace minecraft: with nothing and . with :
        		
        		prepend.append(new PUSH(cpGen, s)); // String for id
        		
        		// Factory<Item.Settings, Item> here (Item::new)   -- ScrollLambdaGenerator's generated class
        		//    Initialized in ScrollMinecraftLauncher
        		prepend.append(factory.createInvoke("helper.ItemFactoryHelper", "createItemFactory",
        			    new ObjectType("java.util.function.Function"), Type.NO_ARGS, Const.INVOKESTATIC));
        		
        		
        		// Item.Settings here
        		
        		// TODO: instead of creating a new instance of Item.Settings
        		// we should push a getstatic of this' currentObject as the argument
        		// but i dont wanna create a whole mapping for a single mod. so this'll do 4 now.
        		//
        		prepend.append(new NEW(cpGen.addClass(typeItemSettings)));        
        		prepend.append(InstructionFactory.createDup(1));
        		prepend.append(factory.createInvoke(
        			    typeItemSettings.getClassName(), "<init>", Type.VOID,
        			    Type.NO_ARGS, Const.INVOKESPECIAL));

        		prepend.append(factory.createInvoke("net.minecraft.class_1802",
        				getMapped("Items.register(String, Function<Item.Settings, Item>, Item.Settings)"),
        				new ObjectType(getMapped("Item")), new Type[]{Type.STRING,
        				new ObjectType("java.util.function.Function"),
        				new ObjectType(getMapped("Item.Settings"))}, Const.INVOKESTATIC));
        	}
        }
        
        InstructionHandle targetHandle = null;
        
        for (InstructionHandle ih : il.getInstructionHandles()) {
            Instruction instr = ih.getInstruction();
            if (instr instanceof PUTSTATIC) {
                PUTSTATIC putstatic = (PUTSTATIC) instr;
                if (putstatic.getName(cp).equals(getMapped("LastItem"))) { // match constant pool index
                    targetHandle = ih;
                    break;
                }
            }
        }
        
        il.append(targetHandle, prepend); // insert after OMINOUS_BOTTLE declaration (after all items, before return)

        mdGen.setMaxStack();
        mdGen.setMaxLocals();

        clGen.replaceMethod(oldMd.getMethod(), mdGen.getMethod());

        clazz = clGen.getJavaClass();
        
        clazz.dump(outStream);
        il.dispose();
                
        return fixBytecode(outStream.toByteArray());
    }
	
	private static byte[] NetMinecraftClientMainMain(byte[] in) throws ClassFormatException, IOException {
		@Note(what = "No need to close these Streams")
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ByteArrayInputStream  inStream  = new ByteArrayInputStream(in);

        ClassParser parser = new ClassParser(inStream, getMapped("Main"));
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

    private static byte[] routeClassTransform(byte[] in, String name, ScrollModInterface[] modInterfaces) throws ClassFormatException, IOException {
        if (name.equals(getMapped("Main"))) {
			try {
				return NetMinecraftClientMainMain(in);
			} catch (ClassFormatException | IOException e) {
				Bootstrap.LOGGER.err(">> Could not load the class because of a format error or Input/Output exception");
				e.printStackTrace();
			}
        } else if (name.equals(getMapped("Items"))) {
			try {
				return Class1802(in, modInterfaces);
			} catch (ClassFormatException | IOException e) {
				Bootstrap.LOGGER.err(">> Could not load the class because of a format error or Input/Output exception");
				e.printStackTrace();
			}
        } else if (name.equals(getMapped("Blocks"))) {
			try {
				return Class2246(in, modInterfaces);
			} catch (ClassFormatException | IOException e) {
				Bootstrap.LOGGER.err(">> Could not load the class because of a format error or Input/Output exception");
				e.printStackTrace();
			}
        }

        return null;
    }

    public static byte[] transformClass(byte[] in, String name, ScrollModInterface[] modInterfaces) throws ClassFormatException, IOException {
        byte[] transformed = routeClassTransform(in, name, modInterfaces);
        return transformed != null ? transformed : in;
    }
    
    public static void youJustLostTheGame() {}
}
