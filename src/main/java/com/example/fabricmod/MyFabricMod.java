package com.example.fabricmod;

import com.example.fabricmod.db.DatabaseManager;
import com.example.fabricmod.logic.InspectItem;
import com.example.fabricmod.logic.RarityGenerator;
import com.example.fabricmod.logic.RarityGenerator.RarityResult;
import com.example.fabricmod.logic.InspectItem.InspectResult;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MyFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        DatabaseManager.INSTANCE.init();
        UseItemCallback.EVENT.register(this::onUseItem);
    }

    private TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        if (world.isClient) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        ItemStack off  = player.getOffHandStack();
        ItemStack main = player.getStackInHand(hand);
        if (off.getItem() != Items.EMERALD || main.isEmpty()) {
            return TypedActionResult.pass(main);
        }

        // Copy stack and inspect
        ItemStack toGive = main.copy();
        InspectResult info = InspectItem.inspect(main);
        RarityResult extra = RarityGenerator.generateRarity(
            player.getName().getString(),
            info.itemName()
        );

        // Log transaction
        DatabaseManager.INSTANCE.logTransaction(
            player.getName().getString(),
            info.itemName(),
            extra.getTierLine(),
            info.count(),
            extra.getCertLine()
        );

        // Consume items
        off.decrement(1);
        main.decrement(info.count());

        // Prepare human-readable name and lore
        String rawId = info.itemName().contains(":")
            ? info.itemName().split(":")[1]
            : info.itemName();
        String humanName = Arrays.stream(rawId.split("_"))
            .map(w -> w.substring(0,1).toUpperCase() + w.substring(1))
            .collect(Collectors.joining(" "));

        String signedName = humanName + " (Signed)";
        String customNameJson = "'[\"\",{\"text\":\"" + signedName + "\",\"italic\":true,\"color\":\"light_purple\"}]'";

        String rarity = extra.getTierLine().replace(":", "");
        String cert   = extra.getCertLine().replace(":", "");
        String time   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String signer = player.getName().getString();

        String[] loreParts = new String[]{ rarity, cert, time, signer };
        StringBuilder loreJson = new StringBuilder("[");
        for (int i = 0; i < loreParts.length; i++) {
            loreJson.append("'[\"\",{\"text\":\"" + loreParts[i] + "\",\"italic\":false}]'");
            if (i < loreParts.length - 1) loreJson.append(",");
        }
        loreJson.append("]");

        // Build NBT suffix
        String suffix = "[minecraft:custom_name=" + customNameJson
                      + ",minecraft:lore=" + loreJson
                      + "]";

        // Dispatch via console to avoid wrapping
        String cmd = "give @a " + info.itemName() + suffix + " " + toGive.getCount();
        System.out.println("[DEBUG] " + cmd);

        try {
            MinecraftServer server = ((ServerWorld) world).getServer();
            server.getCommandManager()
                  .getDispatcher()
                  .execute(cmd, server.getCommandSource());
        } catch (Exception e) {
            player.sendMessage(Text.literal("[ERROR] /give failed: " + e.getMessage()), false);
        }

        // Confirmation
        player.sendMessage(Text.literal(
            String.format("[Signed] %s given by %s",
                signedName, player.getName().getString()
            )
        ), false);

        return TypedActionResult.success(toGive);
    }
}
