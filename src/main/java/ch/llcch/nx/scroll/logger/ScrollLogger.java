package ch.llcch.nx.scroll.logger;

public class ScrollLogger {
    String name;

    public ScrollLogger(String path) {
        name = path;
    }

    public void info(String log) {
        System.out.println(name + " (INFO): " + log);
    }

    public void err(String log) {
        System.err.println(name + " (ERR ): " + log);
    }

    public void warn(String log) {
        System.out.println(name + " (WARN): " + log);
    }
}
