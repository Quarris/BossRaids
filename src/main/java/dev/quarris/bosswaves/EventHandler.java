package dev.quarris.bosswaves;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class EventHandler {

    @SubscribeEvent
    public static void registerBossWaveReloadListener(AddReloadListenerEvent event) {
        Gson gson = new GsonBuilder()
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
                }).create();

        event.addListener(new BossWaveManager(gson));
    }
}
