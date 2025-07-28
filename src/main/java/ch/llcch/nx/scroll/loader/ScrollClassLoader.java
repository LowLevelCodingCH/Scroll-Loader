package ch.llcch.nx.scroll.loader;

import ch.llcch.nx.scroll.api.ScrollModInterface;
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

public class ScrollClassLoader extends URLClassLoader {
	public ScrollClassLoader(URL[] urls, ClassLoader cl) {
		super(urls, cl);
	}

    private final Set<String> loadedNames = ConcurrentHashMap.newKeySet();
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
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
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
    
    public Class<?> loadClass(byte[] arr, String name, boolean resolve, ScrollModInterface[] modInterfaces) throws ClassNotFoundException {
        if (shouldDelegate(name))
            return Bootstrap.ORIGINAL_CLASSLOADER.loadClass(name);
        
    	return findClass(arr, name, modInterfaces);
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (shouldDelegate(name))
            return Bootstrap.ORIGINAL_CLASSLOADER.loadClass(name);
        
        return loadClass(name, false);
    }
    
    public Class<?> loadClass(byte[] arr, String name, ScrollModInterface[] modInterfaces) throws ClassNotFoundException {
        if (shouldDelegate(name))
            return Bootstrap.ORIGINAL_CLASSLOADER.loadClass(name);
        
        return loadClass(arr, name, false, modInterfaces);
    }

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
			transformed = ScrollClassTransformer.transformClass(classBytes, name, null);
		} catch (ClassFormatException | IOException e) {
			Bootstrap.LOGGER.err(">> Could not load Class " + name);
			e.printStackTrace();
		}

		Bootstrap.LOGGER.info(">> Defining class: " + name);
        return defineClass(name, transformed, 0, transformed.length);
    }
    
    protected Class<?> findClass(byte[] arr, String name, ScrollModInterface[] modInterfaces) throws ClassNotFoundException {
        if (loadedNames.contains(name)) {
            return findLoadedClass(name);
        }
        
        loadedNames.add(name);
        
        byte[] classBytes = arr;
        byte[] transformed = null;
		try {
			transformed = ScrollClassTransformer.transformClass(classBytes, name, modInterfaces);
		} catch (ClassFormatException | IOException e) {
			e.printStackTrace();
		}
		Bootstrap.LOGGER.info(">> Defining class: " + name);
        return defineClass(name, transformed, 0, transformed.length);
    }
    
    public Class<?> defineClass(String name, byte[] bytes) {
        return super.defineClass(name, bytes, 0, bytes.length);
    }
}
