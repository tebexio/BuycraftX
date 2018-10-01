package net.buycraft.plugin.data.responses;

import lombok.Data;
import net.buycraft.plugin.data.GiftCard;

import java.util.List;

@Data
public class GiftCardListing {
    private final List<GiftCard> data;
}
