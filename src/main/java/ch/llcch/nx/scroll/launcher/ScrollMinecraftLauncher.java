package ch.llcch.nx.scroll.launcher;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;

import java.lang.reflect.Method;

public class ScrollMinecraftLauncher {
    public static final String MINECRAFT_CLASS = "net.minecraft.client.main.Main";
    public static final String MINECRAFT_MAIN  = "main";

    public static void launchMinecraft(String[] launchArgs) throws Exception {
        modifyClasses();

        Class<?> mainClass = Class.forName(MINECRAFT_CLASS);

        Method mainMethod = mainClass.getMethod(MINECRAFT_MAIN, String[].class);

        Object inst = mainClass.getDeclaredConstructor().newInstance();
        mainMethod.invoke(inst, (Object) launchArgs);
    }

    public static void modifyClasses() throws ClassNotFoundException {
        // TODO: Use BCEL to modify classes (Minecraft) right here
        String mcMainClassName = "net.minecraft.client.main.Main";
        String mcMainClassMain = "main";
        JavaClass mcMainClass = Repository.lookupClass(mcMainClassName);
        ClassGen mcMainClassGen = new ClassGen(mcMainClass);
        ConstantPoolGen mcMainConstantPoolGen = mcMainClassGen.getConstantPool();
        org.apache.bcel.classfile.Method[] mcMainMethods = mcMainClassGen.getMethods();
        // TODO: Add more

        // TODO: Then save modified classes to files, then load those classes
    }
}
