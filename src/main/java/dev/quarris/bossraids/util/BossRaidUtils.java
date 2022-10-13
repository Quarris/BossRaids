package dev.quarris.bossraids.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.util.offsets.IOffset;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.potion.Effect;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class BossRaidUtils {

    private static Gson gson;

    public static Gson getBossRaidGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, (JsonDeserializer) (json, typeOfT, context) -> new ResourceLocation(json.getAsString()))
                .registerTypeAdapter(RangedInteger.class, (JsonDeserializer) (json, typeOfT, context) -> {
                    if (json.isJsonObject()) {
                        JsonObject jo = json.getAsJsonObject();
                        return RangedInteger.of(jo.get("min").getAsInt(), jo.get("max").getAsInt());
                    }
                    return RangedInteger.of(json.getAsInt(), json.getAsInt());
                })
                .registerTypeAdapter(CompoundNBT.class, (JsonDeserializer) (json, typeOfT, context) -> {
                    try {
                        return JsonToNBT.parseTag(json.getAsString());
                    } catch (CommandSyntaxException e) {
                        ModRef.LOGGER.warn("Invalid Compound tag found {}", json);
                        return null;
                    }
                })
                .registerTypeAdapter(ItemStack.class, (JsonDeserializer) (json, typeOfT, context) -> {
                    if (json.isJsonPrimitive()) {
                        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.getAsString()));
                        return new ItemStack(item);
                    }

                    JsonObject jo = json.getAsJsonObject();
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(jo.get("item").getAsString()));
                    int count = JSONUtils.getAsInt(jo, "count", 1);
                    CompoundNBT nbt = JSONUtils.getAsObject(jo, "nbt", null, context, CompoundNBT.class);
                    return new ItemStack(item, count, nbt);
                })
                .registerTypeAdapter(EntityType.class, resourceRegistryAdapter(ForgeRegistries.ENTITIES))
                .registerTypeAdapter(Effect.class, resourceRegistryAdapter(ForgeRegistries.POTIONS))
                .registerTypeAdapter(Attribute.class, resourceRegistryAdapter(ForgeRegistries.ATTRIBUTES))
                .registerTypeAdapter(Ingredient.class, (JsonDeserializer) (json, typeOfT, context) -> Ingredient.fromJson(json))
                .registerTypeAdapter(IOffset.class, (JsonDeserializer) (json, typeOfT, context) -> IOffset.fromJson(json.getAsJsonObject()))
                .create();
        }

        return gson;
    }

    private static JsonDeserializer resourceRegistryAdapter(IForgeRegistry registry) {
        return (json, typeOfT, context) -> {
            ResourceLocation name = context.deserialize(json, ResourceLocation.class);
            if (!registry.containsKey(name)) {
                ModRef.LOGGER.error("Value with id '{}' for registry '{}' not found", name, registry.getRegistryName());
                return null;
            }

            return registry.getValue(name);
        };
    }
}
