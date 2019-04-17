package net.buycraft.plugin.testutil;

public class TestPlayer {
    private final int freeSlots;

    public TestPlayer(int freeSlots) {
        this.freeSlots = freeSlots;
    }

    public int getFreeSlots() {
        return freeSlots;
    }
}
