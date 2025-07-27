package ch.llcch.nx.scroll.main;

import ch.llcch.nx.scroll.launcher.ScrollMinecraftLauncher;
import ch.llcch.nx.scroll.logger.ScrollLogger;

/**
 * Sets everything up, no documentation necessary
 * 
 * @author  Alexander R. O.
 * @version 0.0.2
 * @since   0.0.1
 */
public class Bootstrap {
    public static final ScrollLogger LOGGER = new ScrollLogger("Scroll Mod Loader");
    public static final ClassLoader ORIGINAL_CLASSLOADER = Thread.currentThread().getContextClassLoader();


    public static void main(String[] args) throws Exception {
        LOGGER.info(">> Trying to launch minecraft");
        
        ScrollMinecraftLauncher.launchMinecraft(args);
    }
}
