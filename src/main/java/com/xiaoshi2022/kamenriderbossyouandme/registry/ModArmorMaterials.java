package com.xiaoshi2022.kamenriderbossyouandme.registry;

import com.xiaoshi2022.kamenriderbossyouandme.KamenRiderBossYOUandME;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ModArmorMaterials {
    // Brain Armor Material
    public static final Holder<ArmorMaterial> BRAIN = register("brain",
            Util.make(new EnumMap<>(ArmorItem.Type.class), attribute -> {
                attribute.put(ArmorItem.Type.HELMET, 6);
                attribute.put(ArmorItem.Type.CHESTPLATE, 10);
                attribute.put(ArmorItem.Type.LEGGINGS, 9);
                attribute.put(ArmorItem.Type.BOOTS, 8);
                attribute.put(ArmorItem.Type.BODY, 9);
            }), 25, 4.0f, 0.3f, () -> Items.EMERALD);

    // Add other rider armor materials here in the future

    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> typeProtection,
                                                 int enchantability, float toughness, float knockbackResistance,
                                                 Supplier<Item> ingredientItem) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(KamenRiderBossYOUandME.MODID, name);
        Holder<SoundEvent> equipSound = null;
        Supplier<Ingredient> ingredient = () -> Ingredient.of(ingredientItem.get(), Items.GOLD_INGOT);
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(location));

        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, location,
                new ArmorMaterial(typeProtection, enchantability, equipSound, ingredient, layers, toughness, knockbackResistance));
    }
}