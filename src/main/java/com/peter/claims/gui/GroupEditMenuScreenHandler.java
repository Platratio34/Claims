package com.peter.claims.gui;

import java.util.List;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import com.peter.claims.Claims;
import com.peter.claims.PlayerCache;
import com.peter.claims.PlayerCache.PlayerData;
import com.peter.claims.claim.Claim;

import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GroupEditMenuScreenHandler extends ServerOnlyScreenHandler {

    protected final Claim claim;
    protected final String group;

    public GroupEditMenuScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim, String group) {
        super(syncId, playerInventory, 4);

        this.claim = claim;
        this.group = group;
        PlayerManager playerManager = player.getServer().getPlayerManager();

        inventory.items[0] = new ItemButton(new ItemStack(Items.ANVIL), "Add Player", (b, a) -> {
            StringInputScreenHandler.getString(player, (pName, sh) -> {
                if (pName == null) {
                    openMenu(player, claim, group);
                }
                
                UUID playerUUID = null;
                ServerPlayerEntity p2 = playerManager.getPlayer(pName);
                if (p2 == null) {
                    PlayerData playerData = PlayerCache.getPlayer(pName);
                    if(playerData != null) {
                        playerUUID = playerData.uuid();
                    } else {
                        Claims.LOGGER.info("Getting offline player information");
                        SkullBlockEntity.fetchProfileByName(pName).thenAcceptAsync(optional ->{
                            if (optional.isPresent()) {
                                GameProfile profile = optional.get();
                                PlayerCache.addPlayer(profile);

                                claim.setGroup(profile.getId(), group);
                            } else {
                                player.sendMessage(Text.of("Unknown player: \""+pName+"\"").copy().formatted(Formatting.RED));
                            }
                            openMenu(player, claim, group);
                        });
                        return;
                    }
                } else {
                    playerUUID = p2.getUuid();
                }

                if (playerUUID == null) {
                    player.sendMessage(Text.of("Unknown player: \""+pName+"\"").copy().formatted(Formatting.RED));
                    return;
                } else {
                    claim.setGroup(playerUUID, group);
                }
                openMenu(player, claim, group);
            }, "", Text.of("Player Name"));
        });
        
        inventory.items[8] = new ItemButton(new ItemStack(Items.BARRIER), "Back", (b, a) -> {
            GroupMenuScreenHandler.openMenu(player, claim);
        });

        List<UUID> players = claim.getGroupPlayers(group);
        int i = 9;
        for (UUID uuid : players) {
            ServerPlayerEntity p2 = playerManager.getPlayer(uuid);
            String pName = "";
            if (p2 == null) {
                pName = "Unknown :(";
                PlayerData playerData = PlayerCache.getPlayer(uuid);
                if (playerData != null) {
                    pName = playerData.username();
                }
            } else {
                pName = p2.getNameForScoreboard();
            }
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            SkullBlockEntity.fetchProfileByUuid(uuid).thenAcceptAsync(profile -> {
                if (profile.isEmpty())
                    return;
                stack.set(DataComponentTypes.PROFILE, new ProfileComponent(profile.get()));
            });
            String playerName = pName;
            inventory.items[i] = new ItemButton(stack, playerName, (b, a) -> {
                ConfirmScreenHandler.confirm(player, "Confirm remove " + playerName, () -> {
                    claim.setGroup(uuid, null);
                    openMenu(player, claim, group);
                }, () -> {
                    openMenu(player, claim, group);
                });
            });
            i++;
        }
    }

    public static void openMenu(ServerPlayerEntity player, Claim claim, String group) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new GroupEditMenuScreenHandler(syncId, playerInventory, claim, group);
            }
            @Override
            public Text getDisplayName() {
                return Text.of(claim.getName() + " - Group Edit");
            }
        };
        player.openHandledScreen(fac);
    }
}
