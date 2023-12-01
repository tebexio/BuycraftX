package io.tebex.sdk.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.stream.Collectors;

public class GsonUtil {
    public static List<Integer> arrayToList(JsonArray array) {
        return array.asList().stream().map(JsonElement::getAsInt).collect(Collectors.toList());
    }
}
