package ru.macehelper.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class MaceHelperConfig {
    public static final double DEFAULT_DURATION_SECONDS = 3.0;
    public static final int DEFAULT_FIRST_COLOR = 0x00FF00;
    public static final int DEFAULT_SECOND_COLOR = 0xFF0000;

    private static final double MIN_DURATION_SECONDS = 0.01;
    private static final double MAX_DURATION_SECONDS = 3600.0;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("mace_helper.json");

    private boolean enabled;
    private int firstColor;
    private int secondColor;
    private double durationSeconds;

    private MaceHelperConfig(boolean enabled, int firstColor, int secondColor, double durationSeconds) {
        this.enabled = enabled;
        this.firstColor = firstColor;
        this.secondColor = secondColor;
        this.durationSeconds = durationSeconds;
    }

    public static MaceHelperConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            MaceHelperConfig defaults = defaults();
            defaults.save();
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new IllegalArgumentException("The config root must be a JSON object");
            }

            JsonObject object = root.getAsJsonObject();
            boolean enabled = getBoolean(object, "enabled", true);
            int firstColor = parseColor(getString(object, "first_color", toHex(DEFAULT_FIRST_COLOR)));
            int secondColor = parseColor(getString(object, "second_color", toHex(DEFAULT_SECOND_COLOR)));
            double durationSeconds = parseDuration(getString(object, "duration_seconds", "3.0"));
            return new MaceHelperConfig(enabled, firstColor, secondColor, durationSeconds);
        } catch (IOException | RuntimeException exception) {
            MaceHelperClient.LOGGER.error("Не удалось прочитать конфигурацию {}. Используются значения по умолчанию.", CONFIG_PATH, exception);
            return defaults();
        }
    }

    public static MaceHelperConfig defaults() {
        return new MaceHelperConfig(true, DEFAULT_FIRST_COLOR, DEFAULT_SECOND_COLOR, DEFAULT_DURATION_SECONDS);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getFirstColor() {
        return firstColor;
    }

    public int getSecondColor() {
        return secondColor;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public String getFirstColorHex() {
        return toHex(firstColor);
    }

    public String getSecondColorHex() {
        return toHex(secondColor);
    }

    public ApplyResult apply(String firstColorText, String secondColorText, String durationText, boolean enabled) {
        try {
            int parsedFirstColor = parseColor(firstColorText);
            int parsedSecondColor = parseColor(secondColorText);
            double parsedDuration = parseDuration(durationText);

            this.enabled = enabled;
            this.firstColor = parsedFirstColor;
            this.secondColor = parsedSecondColor;
            this.durationSeconds = parsedDuration;
            return ApplyResult.ok();
        } catch (IllegalArgumentException exception) {
            return ApplyResult.failure(exception.getMessage());
        }
    }

    public boolean save() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("first_color", getFirstColorHex());
        object.addProperty("second_color", getSecondColorHex());
        object.addProperty("duration_seconds", durationSeconds);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(object, writer);
            }
            return true;
        } catch (IOException exception) {
            MaceHelperClient.LOGGER.error("Не удалось сохранить конфигурацию {}.", CONFIG_PATH, exception);
            return false;
        }
    }

    public static String toHex(int color) {
        return String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }

    private static boolean getBoolean(JsonObject object, String name, boolean fallback) {
        JsonElement value = object.get(name);
        return value == null ? fallback : value.getAsBoolean();
    }

    private static String getString(JsonObject object, String name, String fallback) {
        JsonElement value = object.get(name);
        return value == null ? fallback : value.getAsString();
    }

    private static int parseColor(String value) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}")) {
            throw new IllegalArgumentException("Цвет должен быть в формате #RRGGBB");
        }

        try {
            return Integer.parseInt(value.substring(1), 16);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Цвет должен быть в формате #RRGGBB", exception);
        }
    }

    private static double parseDuration(String value) {
        final double duration;
        try {
            duration = Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Длительность должна быть числом в секундах", exception);
        }

        if (!Double.isFinite(duration) || duration < MIN_DURATION_SECONDS || duration > MAX_DURATION_SECONDS) {
            throw new IllegalArgumentException("Длительность должна быть от 0.01 до 3600 секунд");
        }
        return duration;
    }

    public record ApplyResult(boolean success, String error) {
        public static ApplyResult ok() {
            return new ApplyResult(true, "");
        }

        public static ApplyResult failure(String error) {
            return new ApplyResult(false, error == null ? "Некорректные настройки" : error);
        }
    }
}
