/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(RangedMobsUseModdedWeaponsTest.MOD_ID)
public class RangedMobsUseModdedWeaponsTest {

    // Testing if the new alternative for ProjectileHelper.getWeaponHoldingHand works for vanilla mobs
    // as well as replacing their usages of LivingEntity#isHolding(Item) with LivingEntity#isHolding(Predicate<ItemStack>)
    // Skeletons and Illusioners should be able to use the modded bow.
    // Piglins and Pillagers should be able to use the modded crossbow.
    public static final boolean ENABLE = true;

    public static final String MOD_ID = "ranged_mobs_use_modded_weapons_test";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    private static final DeferredItem<Item> MODDED_BOW = ITEMS.register("modded_bow", () -> new BowItem(new Item.Properties().durability(384)));
    private static final DeferredItem<Item> MODDED_CROSSBOW = ITEMS.register("modded_crossbow", () -> new CrossbowItem(new Item.Properties().durability(326)));

    public RangedMobsUseModdedWeaponsTest(IEventBus modEventBus) {
        if (ENABLE) {
            ITEMS.register(modEventBus);
            modEventBus.addListener(this::onClientSetup);
            modEventBus.addListener(this::addCreative);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(MODDED_BOW);
            event.accept(MODDED_CROSSBOW);
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        RangedWeaponModeLProperties.initBowModelProperties();
        RangedWeaponModeLProperties.initCrossbowModelProperties();
    }

    private static class RangedWeaponModeLProperties {
        static void initBowModelProperties() {
            ItemProperties.register(MODDED_BOW.get(), new ResourceLocation("pull"), (itemStack, clientWorld, livingEntity, seed) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return livingEntity.getUseItem() != itemStack ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / 20.0F;
                }
            });
            ItemProperties.register(MODDED_BOW.get(), new ResourceLocation("pulling"), (itemStack, clientWorld, livingEntity, seed) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F);
        }

        static void initCrossbowModelProperties() {
            ItemProperties.register(MODDED_CROSSBOW.get(), new ResourceLocation("pull"), (itemStack, clientWorld, livingEntity, seed) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return CrossbowItem.isCharged(itemStack) ? 0.0F : (float) (itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(itemStack);
                }
            });
            ItemProperties.register(MODDED_CROSSBOW.get(), new ResourceLocation("pulling"), (itemStack, clientWorld, livingEntity, seed) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F);
            ItemProperties.register(MODDED_CROSSBOW.get(), new ResourceLocation("charged"), (itemStack, clientWorld, livingEntity, seed) -> livingEntity != null && CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F);
            ItemProperties.register(MODDED_CROSSBOW.get(), new ResourceLocation("firework"), (itemStack, clientWorld, livingEntity, seed) -> {
                ChargedProjectiles chargedprojectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
                return chargedprojectiles != null && chargedprojectiles.contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
            });
        }
    }
}
