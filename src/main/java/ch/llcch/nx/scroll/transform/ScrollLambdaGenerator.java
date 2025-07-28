package ch.llcch.nx.scroll.transform;

import org.objectweb.asm.*;

import ch.llcch.nx.scroll.annotate.Note;

@Note(what = "We don't need the ScrollClassTransformer.OBFUSCATION_MAP here, because here we have paths, not packages (/, not .) and more")
public class ScrollLambdaGenerator implements Opcodes {
	public static byte[] generateHelperClass() {
	    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

	    cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "helper/ItemFactoryHelper", null,
	            "java/lang/Object", null);

	    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
	    mv.visitCode();
	    mv.visitVarInsn(ALOAD, 0);
	    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
	    mv.visitInsn(RETURN);
	    mv.visitMaxs(1, 1);
	    mv.visitEnd();

	    generateFactoryMethod(cw);

	    mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "createItemFactory",
	            "()Ljava/util/function/Function;", null, null);
	    mv.visitCode();

	    Handle bsm = new Handle(
	        Opcodes.H_INVOKESTATIC,
	        "java/lang/invoke/LambdaMetafactory",
	        "metafactory",
	        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;" +
	        "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)" +
	        "Ljava/lang/invoke/CallSite;",
	        false);

	    Type samMethodType = Type.getMethodType("(Ljava/lang/Object;)Ljava/lang/Object;");

	    Handle factoryHandle = new Handle(
	        Opcodes.H_INVOKESTATIC,
	        "helper/ItemFactoryHelper",
	        "factory",
	        "(Ljava/lang/Object;)Lnet/minecraft/class_1792;",
	        false);

	    mv.visitInvokeDynamicInsn(
	        "apply",
	        "()Ljava/util/function/Function;",
	        bsm,
	        samMethodType,
	        factoryHandle,
	        samMethodType);

	    mv.visitInsn(ARETURN);
	    mv.visitMaxs(3, 0);
	    mv.visitEnd();

	    cw.visitEnd();
	    return cw.toByteArray();
	}
	
	private static void generateFactoryMethod(ClassWriter cw) {
	    MethodVisitor mv = cw.visitMethod(
	        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
	        "factory",
	        "(Ljava/lang/Object;)Lnet/minecraft/class_1792;",
	        null,
	        null);

	    mv.visitCode();
	    mv.visitVarInsn(Opcodes.ALOAD, 0);
	    mv.visitTypeInsn(Opcodes.CHECKCAST, "net/minecraft/class_1792$class_1793");
	    mv.visitTypeInsn(Opcodes.NEW, "net/minecraft/class_1792");
	    mv.visitInsn(Opcodes.DUP);
	    mv.visitVarInsn(Opcodes.ALOAD, 0);
	    mv.visitTypeInsn(Opcodes.CHECKCAST, "net/minecraft/class_1792$class_1793");
	    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "net/minecraft/class_1792", "<init>", "(Lnet/minecraft/class_1792$class_1793;)V", false);
	    mv.visitInsn(Opcodes.ARETURN);
	    mv.visitMaxs(3, 1);
	    mv.visitEnd();
	}
}