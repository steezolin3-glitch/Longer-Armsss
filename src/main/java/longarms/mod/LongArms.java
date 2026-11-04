package longarms.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LongArms implements ModInitializer {
    public static final String MOD_ID = "long_arms";

    /** Zusätzliche Reichweite in Blöcken. */
    private static final double EXTRA_REACH = 1.0;

    private static final Identifier BLOCK_REACH_ID =
            Identifier.fromNamespaceAndPath(MOD_ID, "block_reach");
    private static final Identifier ENTITY_REACH_ID =
            Identifier.fromNamespaceAndPath(MOD_ID, "entity_reach");

    /** Spieler, die die Mod ausgeschaltet haben (gilt bis zum Neustart von Spiel/Server). */
    private static final Set<UUID> DISABLED = ConcurrentHashMap.newKeySet();

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                updateReach(handler.getPlayer()));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                updateReach(newPlayer));

        // /longarms        -> umschalten
        // /longarms on     -> einschalten
        // /longarms off    -> ausschalten
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("longarms")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayer();
                            boolean enable = p != null && DISABLED.contains(p.getUUID());
                            return setEnabled(ctx.getSource(), enable);
                        })
                        .then(Commands.literal("on")
                                .executes(ctx -> setEnabled(ctx.getSource(), true)))
                        .then(Commands.literal("off")
                                .executes(ctx -> setEnabled(ctx.getSource(), false)))));
    }

    private static int setEnabled(CommandSourceStack source, boolean enable) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Dieser Befehl geht nur für Spieler."));
            return 0;
        }

        if (enable) {
            DISABLED.remove(player.getUUID());
        } else {
            DISABLED.add(player.getUUID());
        }
        updateReach(player);

        source.sendSuccess(() -> Component.literal(
                enable ? "Long Arms: AN (+" + EXTRA_REACH + " Block Reichweite)"
                       : "Long Arms: AUS"), false);
        return 1;
    }

    /** Setzt oder entfernt den Reichweiten-Bonus je nach Zustand des Spielers. */
    private static void updateReach(ServerPlayer player) {
        boolean enabled = !DISABLED.contains(player.getUUID());
        applyModifier(player, Attributes.BLOCK_INTERACTION_RANGE, BLOCK_REACH_ID, enabled);
        applyModifier(player, Attributes.ENTITY_INTERACTION_RANGE, ENTITY_REACH_ID, enabled);
    }

    private static void applyModifier(ServerPlayer player, Holder<Attribute> attribute,
                                      Identifier id, boolean enabled) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (enabled) {
            instance.addOrReplacePermanentModifier(
                    new AttributeModifier(id, EXTRA_REACH, AttributeModifier.Operation.ADD_VALUE));
        } else {
            instance.removeModifier(id);
        }
    }
}
