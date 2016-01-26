package net.buycraft.plugin.bukkit.signs;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import net.buycraft.plugin.bukkit.util.SerializedBlockLocation;
import org.bukkit.Location;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SignStorage {
    private final List<PurchaseSignPosition> signs = new ArrayList<>();
    private transient final Gson gson = new Gson();

    public void addSign(PurchaseSignPosition location) {
        signs.add(location);
    }

    public boolean removeSign(PurchaseSignPosition location) {
        return signs.remove(location);
    }

    public boolean removeSign(Location location) {
        SerializedBlockLocation sbl = SerializedBlockLocation.fromBukkitLocation(location);
        for (Iterator<PurchaseSignPosition> it = signs.iterator(); it.hasNext(); ) {
            PurchaseSignPosition psp = it.next();
            if (psp.getLocation().equals(sbl)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public List<PurchaseSignPosition> getSigns() {
        return ImmutableList.copyOf(signs);
    }

    public boolean containsLocation(Location location) {
        SerializedBlockLocation sbl = SerializedBlockLocation.fromBukkitLocation(location);
        for (PurchaseSignPosition sign : signs) {
            if (sign.getLocation().equals(sbl))
                return true;
        }
        return false;
    }

    public void load(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                SignStorage s = gson.fromJson(reader, SignStorage.class);
                signs.addAll(s.signs);
            }
        }
    }

    public void save(Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
            gson.toJson(this, writer);
        }
    }
}
