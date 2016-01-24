package net.buycraft.plugin.client;

import com.google.gson.*;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.Type;
import java.util.Calendar;

public class Iso8601CalendarSerializer implements JsonSerializer<Calendar>, JsonDeserializer<Calendar> {
    @Override
    public Calendar deserialize(JsonElement e, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        return DatatypeConverter.parseDate(e.getAsString());
    }

    @Override
    public JsonElement serialize(Calendar calendar, Type type, JsonSerializationContext ctx) {
        return ctx.serialize(DatatypeConverter.printDateTime(calendar));
    }
}
