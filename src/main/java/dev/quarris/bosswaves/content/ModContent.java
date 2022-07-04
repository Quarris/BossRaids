package dev.quarris.bosswaves.content;

import dev.quarris.bosswaves.ModRef;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContent {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModRef.ID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, ModRef.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ModRef.ID);

    public static RegistryObject<Block> KEYSTONE_BLOCK = BLOCKS.register("keystone", KeystoneBlock::new);

    public static RegistryObject<TileEntityType<?>> KEYSTONE_TILE = TILE_ENTITIES.register("keystone", () -> TileEntityType.Builder.of(KeystoneTileEntity::new, KEYSTONE_BLOCK.get()).build(null));

    public static RegistryObject<Item> KEYSTONE_ITEM = ITEMS.register(KEYSTONE_BLOCK.getId().getPath(), () -> new BlockItem(KEYSTONE_BLOCK.get(), new Item.Properties().tab(ItemGroup.TAB_MISC)));


    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        TILE_ENTITIES.register(bus);
        ITEMS.register(bus);
    }
}
