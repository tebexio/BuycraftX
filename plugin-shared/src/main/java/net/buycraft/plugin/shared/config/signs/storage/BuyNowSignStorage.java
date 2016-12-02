package net.buycraft.plugin.shared.config.signs.storage;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

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

public class BuyNowSignStorage {
    private final List<SavedBuyNowSign> signs = new ArrayList<>();
    private transient final Gson gson = new Gson();

    public void addSign(SavedBuyNowSign location) {
        signs.add(location);
    }

    public boolean removeSign(SavedBuyNowSign location) {
        return signs.remove(location);
    }

    public boolean removeSign(SerializedBlockLocation location) {
        for (Iterator<SavedBuyNowSign> it = signs.iterator(); it.hasNext(); ) {
            SavedBuyNowSign psp = it.next();
            if (psp.getLocation().equals(location)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public List<SavedBuyNowSign> getSigns() {
        return ImmutableList.copyOf(signs);
    }

    public boolean containsLocation(SerializedBlockLocation location) {
        for (SavedBuyNowSign sign : signs) {
            if (sign.getLocation().equals(location))
                return true;
        }
        return false;
    }

    public void load(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                BuyNowSignStorage s = gson.fromJson(reader, BuyNowSignStorage.class);
                if (s != null) {
                    signs.addAll(s.signs);
                }
            }
        }
    }

    public void save(Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(this, writer);
        }
    }
}
