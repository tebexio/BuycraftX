package io.tebex.plugin.util;

import com.cryptomorin.xseries.XMaterial;

import java.util.Optional;

public class MaterialUtil {
    public static Optional<XMaterial> fromString(String material) {
        try {
            int id = Integer.parseInt(material);
            return XMaterial.matchXMaterial(id, (byte) 0);
        } catch(NumberFormatException e) {
            if(material.contains(":")) {
                String[] split = material.split(":");
                try {
                    int id = Integer.parseInt(split[0]);
                    byte data = Byte.parseByte(split[1]);
                    return XMaterial.matchXMaterial(id, data);
                } catch(NumberFormatException ex) {
                    if(split[0].equalsIgnoreCase("minecraft")) {
                        return XMaterial.matchXMaterial(split[1].toUpperCase());
                    }

                    return XMaterial.matchXMaterial(material);
                }
            } else {
                return XMaterial.matchXMaterial(material);
            }
        }
    }
}
