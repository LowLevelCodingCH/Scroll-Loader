package ch.llcch.nx.scroll.loader;

import ch.llcch.nx.scroll.main.Bootstrap;
import ch.llcch.nx.scroll.transform.ScrollClassTransformer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassFormatException;

/**
 * Replaced classloader
 * 
 * @author  Alexander R. O.
 * @version 0.0.2
 * @since   0.0.2
 */
public class ScrollClassLoader extends URLClassLoader {
	public ScrollClassLoader(URL[] urls, ClassLoader cl) {
		super(urls, cl);
	}

	/**
	 * The already loaded class names
	 */
    private final Set<String> loadedNames = ConcurrentHashMap.newKeySet();
    
    /**
     * The classes which should go through the transformation pipeline
     */
    private static final Set<String> SYSTEM_LOAD = Set.of(
            //
    );
    
    private static boolean shouldDelegate(String name) {
        return !(name.startsWith("net.minecraft.") 
                || name.startsWith("com.mojang.blaze3d.") 
                || name.startsWith("util.jfr.profiling.event."));
    }
    
    @SuppressWarnings("deprecation")
	@Override
    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (url != null) return url;

        for (URL jarUrl : getURLs()) {
            try {
                if (!"file".equals(jarUrl.getProtocol())) continue;

                File jarFile = new File(jarUrl.toURI());
                try (JarFile jf = new JarFile(jarFile)) {
                    JarEntry entry = jf.getJarEntry(name);
                    if (entry != null) {
                        return new URL("jar", "", "file:" + jarFile.getAbsolutePath() + "!/" + name);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Log errors in resource discovery
            }
        }

        return null;
    }

    /**
     * The first (and for some last) stop in the pipeline - Classes that have been chosen to be transformed will get to this.findClass, others get super.loadClass'ed
     * 
     * @return Class object
     * @param resolve If the class should be resolved
     * @param name Class name (format: tld.domain.package.Class).
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // First, check if class already loaded
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            if (shouldDelegate(name)) {
                c = getParent().loadClass(name);
            } else {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    c = getParent().loadClass(name);
                }
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    
    public Class<?> loadClass(byte[] arr, String name, boolean resolve) throws ClassNotFoundException {
        if (shouldDelegate(name))
            return Bootstrap.ORIGINAL_CLASSLOADER.loadClass(name);
        
    	return findClass(arr, name);
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (shouldDelegate(name))
            return Bootstrap.ORIGINAL_CLASSLOADER.loadClass(name);
        
        return loadClass(name, false);
    }
    
    public Class<?> loadClass(byte[] arr, String name) throws ClassNotFoundException {
        if (shouldDelegate(name))
            return Bootstrap.ORIGINAL_CLASSLOADER.loadClass(name);
        
        return loadClass(arr, name, false);
    }

    /**
     * Finds a class, transforms it and defines it
     * 
     * @return Class of the transformed class
     * @throws ClassNotFoundException
     * @param name Class name (format: tld.domain.package.Class).
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (loadedNames.contains(name)) {
            return findLoadedClass(name);
        }
        
        loadedNames.add(name);
        
        String path = name.replace('.', '/') + ".class";
        URL resourceURL = getResource(path);
        Bootstrap.LOGGER.info(">> Resource URL for " + path + ": " + resourceURL);
        if (resourceURL == null) throw new ClassNotFoundException(name);

        InputStream in = null;
		try {
			in = resourceURL.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int bytesRead;

        try {
            while ((bytesRead = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to read class bytes for " + name, e);
        }

        byte[] classBytes = buffer.toByteArray();
        byte[] transformed = null;
		try {
			transformed = ScrollClassTransformer.transformClass(classBytes, name);
		} catch (ClassFormatException | IOException e) {
			Bootstrap.LOGGER.err(">> Could not load Class " + name);
			e.printStackTrace();
		}

		Bootstrap.LOGGER.info(">> Defining class: " + name);
        return defineClass(name, transformed, 0, transformed.length);
    }
    
    protected Class<?> findClass(byte[] arr, String name) throws ClassNotFoundException {
        if (loadedNames.contains(name)) {
            return findLoadedClass(name);
        }
        
        loadedNames.add(name);
        
        byte[] classBytes = arr;
        byte[] transformed = null;
		try {
			transformed = ScrollClassTransformer.transformClass(classBytes, name);
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		}
		Bootstrap.LOGGER.info(">> Defining class: " + name);
        return defineClass(name, transformed, 0, transformed.length);
    }
}
