package dev.quarris.bossraids.util;

import com.google.gson.*;
import dev.quarris.bossraids.ModRef;

import java.io.*;

public class JsonUtils {

    public static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting().create();

    private static final JsonParser PARSER = new JsonParser();

    public static JsonElement stringToJson(String jsonString) {
        return PARSER.parse(jsonString);
    }

    public static String jsonToString(JsonElement json) {
        return GSON.toJson(json);
    }

    public static JsonElement parseJsonFile(File file) {
        try {
            return PARSER.parse(new FileReader(file));
        } catch (FileNotFoundException e) {
            ModRef.LOGGER.error("Could not find json file.", e);
        }

        return JsonNull.INSTANCE;
    }

    public static boolean writeJsonToFile(File file, JsonElement json) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(GSON.toJson(json));
            return true;
        } catch (IOException e) {
            ModRef.LOGGER.error("Couldn't write to the file", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    ModRef.LOGGER.error("Couldn't close writer.");
                }
            }
        }

        return false;
    }


}
