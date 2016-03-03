package net.buycraft.plugin.platform.standalone.runner;

/**
 * {@code CommandDispatcher}s are called when Buycraft processes a command. The dispatcher will not get any other
 * information from the command.
 */
public interface CommandDispatcher {
    void dispatchCommand(String command);
}
