package net.buycraft.plugin.execution.strategy;

import com.google.common.collect.ImmutableMap;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.testutil.SimulatedPlayerBuycraftPlatform;
import net.buycraft.plugin.testutil.TestPlayer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ToRunQueuedCommandTest {
    private static final QueuedCommand TEST_INSTANT_COMMAND = new QueuedCommand(0, 0, 0, ImmutableMap.of(), "",
            null);
    private static final QueuedCommand TEST_REQUIRE_ONLINE_COMMAND = new QueuedCommand(0, 0, 0, ImmutableMap.of(), "",
            new QueuedPlayer(0, "TestOnline", null));

    private SimulatedPlayerBuycraftPlatform platform;

    private static ToRunQueuedCommand forOffline(QueuedCommand command) {
        return new ToRunQueuedCommand(command.getPlayer(), command, false);
    }

    private static ToRunQueuedCommand forOnline(QueuedCommand command) {
        return new ToRunQueuedCommand(command.getPlayer(), command, true);
    }

    @Before
    public void setup() {
        platform = new SimulatedPlayerBuycraftPlatform();
    }

    @Test
    public void canExecuteInstant() throws Exception {
        assertTrue("Command that can run instantly is not being run", forOffline(TEST_INSTANT_COMMAND).canExecute(platform));
    }

    @Test
    public void canExecuteDenyOffline() throws Exception {
        assertFalse("Command that requires online player is being run", forOnline(TEST_REQUIRE_ONLINE_COMMAND).canExecute(platform));
    }

    @Test
    public void canExecuteAllowOnline() throws Exception {
        platform.getTestPlayerMap().put("TestOnline", new TestPlayer(0));
        assertTrue("Command with online player is not being run", forOnline(TEST_REQUIRE_ONLINE_COMMAND).canExecute(platform));
    }

    @Test
    public void duplicateTest() throws Exception {
        ToRunQueuedCommand one = forOffline(TEST_INSTANT_COMMAND);
        Thread.sleep(10);
        ToRunQueuedCommand two = forOffline(TEST_INSTANT_COMMAND);
        assertEquals("two identical commands are different", one, two);
    }
}