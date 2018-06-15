package com.openxc.messages.formatters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.openxc.messages.CustomCommand;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CustomCommandSerializer implements JsonSerializer<CustomCommand> {
    /***
     * A custom serializer for Custom Command. The custom commands are received in
     * HashMap and then each key value is mapped as separate properties rather than a single
     * JsonArray. This allows us to add any number of custom commands and then send it in openxc
     * standard message format.
     * @param customCommand
     * @param typeOfSrc
     * @param context
     * @return
     */
    @Override
    public JsonElement serialize
            (CustomCommand customCommand, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jObject = new JsonObject();
        HashMap<String,String> commands = customCommand.getCommands();
        for (Map.Entry<String, String> entry : commands.entrySet()) {
           jObject.addProperty(entry.getKey(),entry.getValue());
        }
        return jObject;
    }
}