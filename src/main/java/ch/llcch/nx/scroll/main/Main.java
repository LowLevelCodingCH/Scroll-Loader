package ch.llcch.nx.scroll.main;

import ch.llcch.nx.scroll.logger.ScrollLogger;
import ch.llcch.nx.scroll.launcher.ScrollMinecraftLauncher;

public class Main {
    public static final ScrollLogger LOGGER = new ScrollLogger("Scroll Mod Loader");

    public static void main(String[] args) throws Exception {
        LOGGER.info("Trying to launch minecraft");

        ScrollMinecraftLauncher.launchMinecraft(args);
    }
}
