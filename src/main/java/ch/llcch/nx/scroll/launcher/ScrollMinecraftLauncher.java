package ch.llcch.nx.scroll.launcher;

import ch.llcch.nx.scroll.annotate.*;
import ch.llcch.nx.scroll.api.ScrollModInterface;
import ch.llcch.nx.scroll.loader.ScrollClassLoader;
import ch.llcch.nx.scroll.main.Bootstrap;
import ch.llcch.nx.scroll.transform.ScrollLambdaGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Todo(what = "Make the API (Scroll API) talk to this reflectively to allow communication between the mod and loader, like so: " +
"Item newItemLol = ScrollAPI.ITEMS.register(....); " +
"Without the current ScrollModInterface", due = "3.0.0")
@Note(what = "Could be complicated since we inject the code when the class runs, which makes it hard to communicate. " +
"So, we'll just use the static fields currentObject (and more to come) and then assign them to the mod stuffz")
@Note(what = "Do that via ScrollModContexts maybe. that's just an idea")
public class ScrollMinecraftLauncher {
    public static final String MINECRAFT_CLASS = "net.minecraft.client.main.Main";
    public static final String MINECRAFT_MAIN  = "main";

    public static void launchMinecraft(String[] launchArgs) throws Exception {
    	ScrollModInterface[] modInterfaces = loadMods();
        File mcJar = new File("1.21.8.jar");
        ScrollClassLoader loader = new ScrollClassLoader(new URL[] { mcJar.toURI().toURL() }, Bootstrap.ORIGINAL_CLASSLOADER);

    	byte[] helperClassBytes = ScrollLambdaGenerator.generateHelperClass();

    	loader.defineClass("helper.ItemFactoryHelper", helperClassBytes);
    	        
        Thread.currentThread().setContextClassLoader(loader);
        
        JarFile jarFile = new JarFile(mcJar);
        Enumeration<JarEntry> e = jarFile.entries();

        Class<?> mainClass = null;

        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if(je.isDirectory() || !je.getName().endsWith(".class"))
                continue;

            String className = je.getName().substring(0, je.getName().length() - 6);
            className = className.replace('/', '.');
            
            InputStream is = jarFile.getInputStream(je);
            byte[] isBytes = is.readAllBytes();
            
            if (className.contains("$"))
                continue;
            
            Class<?> c = loader.loadClass(isBytes, className, modInterfaces);
            
            if (je.getName().equals("net/minecraft/client/main/Main.class"))
            	mainClass = c;
        }
        
        Method main = mainClass.getMethod("main", String[].class);
                
        main.invoke(null, (Object) launchArgs);
        
        jarFile.close();
    }
    
    private static List<URL> findMods() {
    	List<URL> urls = new ArrayList<URL>();
    	
        File modsDir = new File("mods");

        if (modsDir.exists() && modsDir.isDirectory()) {
            File[] files = modsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        urls.add(file.toURI().toURL());
                    } catch (Exception e) {}
                }
            }
        }
    	
    	return urls;
    }
    
	private static ScrollModInterface[] loadMods() throws IOException, ClassNotFoundException,
    				NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {    	
    	List<ScrollModInterface> modInterfaces = new ArrayList<ScrollModInterface>(64);
    	List<URL> urls = findMods();
    	
    	// Dont send mods through the pipeline
    	URL[] urlArray = urls.toArray(new URL[0]);
    	URLClassLoader cl = new URLClassLoader(urlArray, Bootstrap.ORIGINAL_CLASSLOADER);
    	
    	for (URL url : urls) {
    		String jarPath = url.getPath();
    		Bootstrap.LOGGER.info(">> Loading mod " + jarPath);
    		JarFile jarFile = new JarFile(jarPath);
    		Enumeration<JarEntry> e = jarFile.entries();
    		
    		Class<?> modMainClass = null;
    		
    		while(e.hasMoreElements()) {
    			JarEntry je = e.nextElement();
    			
                if(je.isDirectory() || !je.getName().endsWith("ScrollModEntry.class"))
                    continue;
    			
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');

                if (className.contains("$"))
                    continue;
                
                modMainClass = cl.loadClass(className);
                break;
    		}
    		
    		Method onInitialize = modMainClass.getMethod("onInitialize");
    		ScrollModInterface iface = (ScrollModInterface) onInitialize.invoke(null);
    		modInterfaces.add(iface);
    		
    		jarFile.close();
    	}
    	
    	ScrollModInterface[] modArrayInterfaces = modInterfaces.toArray(new ScrollModInterface[0]);
		cl.close();

    	return modArrayInterfaces;
    }
}
