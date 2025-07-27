package ch.llcch.nx.scroll.launcher;

import ch.llcch.nx.scroll.loader.ScrollClassLoader;
import ch.llcch.nx.scroll.main.Bootstrap;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Launches minecraft
 * 
 * @author  Alexander R. O.
 * @version 0.0.2
 * @since   0.0.1
 */
public class ScrollMinecraftLauncher {
    public static final String MINECRAFT_CLASS = "net.minecraft.client.main.Main";
    public static final String MINECRAFT_MAIN  = "main";

    /**
     * As simple as it gets, launches minecraft and replaces the classloader with `ScrollClassLoader`
     * 
     * @param launchArgs Minecraft launch arguments
     * @throws Exception
     * 
     * WEIRD IF I DO URLCLASSLOADER IT WONT LOAD THE JARS IF I DONT I HAVE TO ENUMERATE LIKE A BILLION CLASSES TO NOT LOAD OR A MILLION TO LOAD WTF MOJANG. AND THEN I HAVE TO DEOBFUSCATE THEM WITH ENIGMA WTF MOJANG. AND I HAVE TO REDO THAT EVERY UPDATE BECAUSE YOU FUCKWITS CHANGE EVERYTHING EVERY SECOND WTF MOJANG.
     */
    public static void launchMinecraft(String[] launchArgs) throws Exception {
        File mcJar = new File("1.21.8.jar");
        ScrollClassLoader loader = new ScrollClassLoader(new URL[] { mcJar.toURI().toURL() }, Bootstrap.ORIGINAL_CLASSLOADER);
        
        Thread.currentThread().setContextClassLoader(loader);
        
        JarFile jarFile = new JarFile("1.21.8.jar");
        Enumeration<JarEntry> e = jarFile.entries();

        Class<?> mainClass = null;

        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if(je.isDirectory() || !je.getName().endsWith(".class")){
                continue;
            }

            String className = je.getName().substring(0, je.getName().length() - 6);
            className = className.replace('/', '.');
            
            InputStream is = jarFile.getInputStream(je);
            byte[] isBytes = is.readAllBytes();
            
            if (className.contains("$")) {
                continue;
            }
            
            Class<?> c = loader.loadClass(isBytes, className);
            
            if (je.getName().equals("net/minecraft/client/main/Main.class")) {
            	mainClass = c;
            }
        }
        
        Method main = mainClass.getMethod("main", String[].class);
        
        loader.close();
        
        main.invoke(null, (Object) launchArgs);
    }
}
