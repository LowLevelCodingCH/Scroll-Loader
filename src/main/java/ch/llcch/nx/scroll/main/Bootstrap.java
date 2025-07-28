package ch.llcch.nx.scroll.main;

import ch.llcch.nx.scroll.annotate.Version;
import ch.llcch.nx.scroll.launcher.ScrollMinecraftLauncher;
import ch.llcch.nx.scroll.logger.ScrollLogger;

@Version(ver = "0.0.3")
public class Bootstrap {
    public static final ScrollLogger LOGGER = new ScrollLogger("Scroll Mod Loader");
    public static final ClassLoader ORIGINAL_CLASSLOADER = Thread.currentThread().getContextClassLoader();


    public static void main(String[] args) throws Exception {
        LOGGER.info(">> Trying to launch minecraft");
        
        ScrollMinecraftLauncher.launchMinecraft(args);
    }
}
