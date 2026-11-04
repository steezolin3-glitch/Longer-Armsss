package com.example.longarms;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LongArms implements ModInitializer {
    public static final String MOD_ID = "long_arms";

    /** Zusaetzliche Reichweite in Bloecken. Hier kannst du den Wert aendern. */
    private static final double EXTRA_REACH = 1.0;

    private static final Identifier BLOCK_REACH_ID =
            Identifier.fromNamespaceAndPath(MOD_ID, "block_reach");
    private static final Identifier ENTITY_REACH_ID =
            Identifier.fromNamespaceAndPath(MOD_ID, "entity_reach");

    @Override
    public void onInitialize() {
        // Beim Betreten der Welt bzw. des Servers
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                applyReach(handler.getPlayer()));

        // Nach dem Respawn (neue Spielerinstanz)
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                applyReach(newPlayer));
    }

    private static void applyReach(ServerPlayer player) {
        addReach(player, Attributes.BLOCK_INTERACTION_RANGE, BLOCK_REACH_ID);   // Abbauen/Platzieren
        addReach(player, Attributes.ENTITY_INTERACTION_RANGE, ENTITY_REACH_ID); // Angreifen/Interagieren
    }

    private static void addReach(ServerPlayer player, Holder<Attribute> attribute, Identifier id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        // "addOrReplace": verhindert, dass der Bonus mehrfach gestapelt wird
        instance.addOrReplacePermanentModifier(
                new AttributeModifier(id, EXTRA_REACH, AttributeModifier.Operation.ADD_VALUE));
    }
}
