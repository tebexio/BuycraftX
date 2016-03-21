package net.buycraft.plugin.data.responses;

import lombok.Value;

import java.util.Date;

@Value
public class Version {
    private final String version;
    private final Date released;
}