package net.buycraft.plugin.shared.commands;

public enum ChatColor {
    GRAY('7'),
    RED('c'),
    GREEN('a'),
    YELLOW('e');

    private char color;
    private String str;

    ChatColor(char color) {
        this.color = color;
        this.str = new String(new char[]{'\u00a7', color});
    }

    @Override
    public String toString() {
        return str;
    }
}
