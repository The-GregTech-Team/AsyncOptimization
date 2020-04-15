package cn.ac.origind.asyncoptimization.mixin;

import cn.ac.origind.asyncoptimization.concurrent.IAsyncThreadListener;
import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.*;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.ac.origind.asyncoptimization.AsyncOptimization.LOGGER;

@Mixin(ServerConfigurationManager.class)
public abstract class MixinServerConfigurationManager {
    @Final
    @Shadow
    public MinecraftServer mcServer;

    @Shadow
    public abstract NBTTagCompound readPlayerDataFromFile(EntityPlayerMP player);

    @Shadow
    public abstract void func_72381_a(EntityPlayerMP p_72381_1_, EntityPlayerMP p_72381_2_, World p_72381_3_);

    @Shadow
    public abstract int getMaxPlayers();

    @Shadow
    public int viewDistance;

//    @Inject(method = "initializeConnectionToPlayer*", at = @At("HEAD"), cancellable = true)
//    public void onInitializeConnectionToPlayer(CallbackInfo ci, NetworkManager netManager, EntityPlayerMP player, NetHandlerPlayServer nethandlerplayserver) {
//        GameProfile profile = player.getGameProfile();
//        PlayerProfileCache cache = mcServer.getPlayerProfileCache();
//        GameProfile profileWithCache = cache.func_152652_a(profile.getId());
//        String name = profileWithCache == null ? profile.getName() : profileWithCache.getName();
//        cache.func_152649_a(profile);
//        NBTTagCompound nbt = readPlayerDataFromFile(player);
//        player.setWorld(mcServer.worldServerForDimension(player.dimension));
//        WorldServer playerWorld = mcServer.worldServerForDimension(player.dimension);
//        if (playerWorld == null) {
//            player.dimension = 0;
//            playerWorld = mcServer.worldServerForDimension(0);
//            ChunkCoordinates spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
//            player.setPosition(spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ);
//        }
//        WorldServer world = playerWorld;
//        ((IAsyncThreadListener) world).syncCall(() -> {
//            player.setWorld(world);
//            player.theItemInWorldManager.setWorld((WorldServer) player.worldObj);
//            String address = "local";
//            if (netManager.getRemoteAddress() != null)
//                address = netManager.getRemoteAddress().toString();
//            LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {}, {})", player.getDisplayName(), address,
//                    player.getEntityId(), player.dimension, player.posX, player.posY, player.posZ);
//            WorldInfo info = world.getWorldInfo();
//            ChunkCoordinates chunkcoordinates = player.worldObj.getSpawnPoint();
//            WorldServer worldserver = (WorldServer) player.worldObj;
//            func_72381_a(player, null, world);
//            player.playerNetServerHandler = nethandlerplayserver;
//            nethandlerplayserver.sendPacket(new S01PacketJoinGame(player.getEntityId(), player.theItemInWorldManager.getGameType(), info.isHardcoreModeEnabled(), worldserver.provider.dimensionId, worldserver.difficultySetting, this.getMaxPlayers(), worldserver.getWorldInfo().getTerrainType()));
//            nethandlerplayserver.sendPacket(new S3FPacketCustomPayload("MC|Brand", mcServer.getServerModName().getBytes(Charsets.UTF_8)));
//            nethandlerplayserver.sendPacket(new S05PacketSpawnPosition(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ));
//            nethandlerplayserver.sendPacket(new S39PacketPlayerAbilities(player.capabilities));
//            nethandlerplayserver.sendPacket(new S09PacketHeldItemChange(player.inventory.currentItem));
//            player.getStatFile().func_150877_d();
//            player.getStatFile().func_150884_b(player);
//            this.func_96456_a((ServerScoreboard)worldserver.getScoreboard(), player);
//            this.mcServer.refreshStatusNextTick();
//            ChatComponentTranslation chatcomponenttranslation;
//
//            String s = profileWithCache == null ? profile.getName() : profileWithCache.getName();
//            if (!player.getCommandSenderName().equalsIgnoreCase(s))
//                chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined.renamed", player.getFormattedCommandSenderName(), s);
//            else
//                chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined", player.getFormattedCommandSenderName());
//
//            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.YELLOW);
//            this.sendChatMsg(chatcomponenttranslation);
//            this.playerLoggedIn(player);
//            nethandlerplayserver.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
//            this.updateTimeAndWeatherForPlayer(player, worldserver);
//
//            if (this.mcServer.getTexturePack().length() > 0)
//            {
//                player.requestTexturePackLoad(this.mcServer.getTexturePack());
//            }
//
//            for (Object o : player.getActivePotionEffects()) {
//                PotionEffect potioneffect = (PotionEffect) o;
//                nethandlerplayserver.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
//            }
//
//            player.addSelfToInternalCraftingInventory();
//
//            FMLCommonHandler.instance().firePlayerLoggedIn(player);
//            NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(player);
//            if (nbttagcompound != null && nbttagcompound.hasKey("Riding", 10))
//            {
//                Entity entity = EntityList.createEntityFromNBT(nbttagcompound.getCompoundTag("Riding"), worldserver);
//
//                if (entity != null)
//                {
//                    entity.forceSpawn = true;
//                    worldserver.spawnEntityInWorld(entity);
//                    player.mountEntity(entity);
//                    entity.forceSpawn = false;
//                }
//            }
//            FMLCommonHandler.instance().firePlayerLoggedIn(player);
//        });
//        ci.cancel();
//    }

    @Overwrite(remap = false)
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP player, NetHandlerPlayServer nethandlerplayserver) {
        GameProfile profile = player.getGameProfile();
        PlayerProfileCache cache = mcServer.getPlayerProfileCache();
        GameProfile profileWithCache = cache.func_152652_a(profile.getId());
        String name = profileWithCache == null ? profile.getName() : profileWithCache.getName();
        cache.func_152649_a(profile);
        NBTTagCompound nbt = readPlayerDataFromFile(player);
        player.setWorld(mcServer.worldServerForDimension(player.dimension));
        WorldServer playerWorld = mcServer.worldServerForDimension(player.dimension);
        if (playerWorld == null) {
            player.dimension = 0;
            playerWorld = mcServer.worldServerForDimension(0);
            ChunkCoordinates spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
            player.setPosition(spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ);
        }
        WorldServer world = playerWorld;
        ((IAsyncThreadListener) world).syncCall(() -> {
            player.setWorld(world);
            player.theItemInWorldManager.setWorld((WorldServer) player.worldObj);
            String address = "local";
            if (netManager.getRemoteAddress() != null)
                address = netManager.getRemoteAddress().toString();
            LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {}, {})", player.getDisplayName(), address,
                    player.getEntityId(), player.dimension, player.posX, player.posY, player.posZ);
            WorldInfo info = world.getWorldInfo();
            ChunkCoordinates chunkcoordinates = player.worldObj.getSpawnPoint();
            WorldServer worldserver = (WorldServer) player.worldObj;
            func_72381_a(player, null, world);
            player.playerNetServerHandler = nethandlerplayserver;
            nethandlerplayserver.sendPacket(new S01PacketJoinGame(player.getEntityId(), player.theItemInWorldManager.getGameType(), info.isHardcoreModeEnabled(), worldserver.provider.dimensionId, worldserver.difficultySetting, this.getMaxPlayers(), worldserver.getWorldInfo().getTerrainType()));
            nethandlerplayserver.sendPacket(new S3FPacketCustomPayload("MC|Brand", mcServer.getServerModName().getBytes(Charsets.UTF_8)));
            nethandlerplayserver.sendPacket(new S05PacketSpawnPosition(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ));
            nethandlerplayserver.sendPacket(new S39PacketPlayerAbilities(player.capabilities));
            nethandlerplayserver.sendPacket(new S09PacketHeldItemChange(player.inventory.currentItem));
            player.getStatFile().func_150877_d();
            player.getStatFile().func_150884_b(player);
            this.func_96456_a((ServerScoreboard)worldserver.getScoreboard(), player);
            this.mcServer.refreshStatusNextTick();
            ChatComponentTranslation chatcomponenttranslation;

            String s = profileWithCache == null ? profile.getName() : profileWithCache.getName();
            if (!player.getCommandSenderName().equalsIgnoreCase(s))
                chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined.renamed", player.getFormattedCommandSenderName(), s);
            else
                chatcomponenttranslation = new ChatComponentTranslation("multiplayer.player.joined", player.getFormattedCommandSenderName());

            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.YELLOW);
            this.sendChatMsg(chatcomponenttranslation);
            this.playerLoggedIn(player);
            nethandlerplayserver.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
            this.updateTimeAndWeatherForPlayer(player, worldserver);

            if (this.mcServer.getTexturePack().length() > 0)
            {
                player.requestTexturePackLoad(this.mcServer.getTexturePack());
            }

            for (Object o : player.getActivePotionEffects()) {
                PotionEffect potioneffect = (PotionEffect) o;
                nethandlerplayserver.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
            }

            player.addSelfToInternalCraftingInventory();

            FMLCommonHandler.instance().firePlayerLoggedIn(player);
            NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(player);
            if (nbttagcompound != null && nbttagcompound.hasKey("Riding", 10))
            {
                Entity entity = EntityList.createEntityFromNBT(nbttagcompound.getCompoundTag("Riding"), worldserver);

                if (entity != null)
                {
                    entity.forceSpawn = true;
                    worldserver.spawnEntityInWorld(entity);
                    player.mountEntity(entity);
                    entity.forceSpawn = false;
                }
            }
            FMLCommonHandler.instance().firePlayerLoggedIn(player);
        });
    }


    @Overwrite
    public void func_72375_a(EntityPlayerMP player, @Nullable WorldServer oldWorld) {
        WorldServer newWorld = player.getServerForPlayer();
        if (oldWorld != null)
            ((IAsyncThreadListener) oldWorld).syncCall(() -> {
                oldWorld.getPlayerManager().removePlayer(player);
            });
        ((IAsyncThreadListener) newWorld).syncCall(() -> {
            newWorld.getPlayerManager().addPlayer(player);
            newWorld.theChunkProviderServer.loadChunk((int)player.posX >> 4, (int)player.posZ >> 4);
        });
    }

    @Overwrite
    public void transferPlayerToDimension(EntityPlayerMP player, int targetDimension) {
        Teleporter teleporter = player.getServerForPlayer().getDefaultTeleporter();
        int dimension = player.dimension;
        WorldServer oldWorld = mcServer.worldServerForDimension(player.dimension);
        player.dimension = targetDimension;
        WorldServer newWorld = mcServer.worldServerForDimension(player.dimension);
        ((IAsyncThreadListener) oldWorld).syncCall(() -> {
            player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, newWorld.difficultySetting,
                    newWorld.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
            oldWorld.removePlayerEntityDangerously(player);
            player.isDead = false;
            System.out.println("pos: " + player.getCommandSenderPosition());
            transferEntityToWorld(player, dimension, oldWorld, newWorld);
            System.out.println("pos: " + player.getCommandSenderPosition());
            func_72375_a(player, oldWorld);
            System.out.println("pos: " + player.getCommandSenderPosition());
        });
        ((IAsyncThreadListener) newWorld).syncCall(() -> {
            System.out.println("pos: " + player.getCommandSenderPosition());
            player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw,
                    player.rotationPitch);
            System.out.println("pos: " + player.getCommandSenderPosition());
            player.theItemInWorldManager.setWorld(newWorld);
            player.playerNetServerHandler.sendPacket(new S39PacketPlayerAbilities(player.capabilities));
            updateTimeAndWeatherForPlayer(player, newWorld);
            syncPlayerInventory(player);
            for (Object effect : player.getActivePotionEffects())
                player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), (PotionEffect) effect));
            // Fix MC-88179: on non-death SPacketRespawn, also resend attributes
            BaseAttributeMap attributemap = player.getAttributeMap();
            Collection<IAttributeInstance> watchedAttribs = attributemap.getAllAttributes();
            if (!watchedAttribs.isEmpty())
                player.playerNetServerHandler.sendPacket(new S20PacketEntityProperties(player.getEntityId(), watchedAttribs));
            FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, dimension, targetDimension);
        });
    }

    @Shadow
    public abstract void syncPlayerInventory(EntityPlayerMP player);

    @Overwrite
    public void transferEntityToWorld(Entity entity, int lastDimension, WorldServer oldWorld, WorldServer newWorld) {
        double moveFactor = oldWorld.provider.getMovementFactor() / newWorld.provider.getMovementFactor();
        double dx = entity.posX * moveFactor;
        double dz = entity.posZ * moveFactor;
        float yaw = entity.rotationYaw;
        if (entity.dimension == 1) {
            ChunkCoordinates blockpos;
            if (lastDimension == 1)
                blockpos = newWorld.getSpawnPoint();
            else
                blockpos = newWorld.getEntrancePortalLocation();
            dx = blockpos.posX;
            entity.posY = blockpos.posY;
            dz = blockpos.posZ;
            double fdx = dx, fdz = dz;
            ((IAsyncThreadListener) oldWorld).syncCall(() -> {
                entity.setLocationAndAngles(fdx, entity.posY, fdz, 90.0F, 0.0F);
                if (entity.isEntityAlive())
                    oldWorld.updateEntityWithOptionalForce(entity, false);
            });
        }
        if (lastDimension != 1) {
            dx = MathHelper.clamp_int((int) dx, -29999872, 29999872);
            dz = MathHelper.clamp_int((int) dz, -29999872, 29999872);
            double fdx = dx, fdz = dz;
            ((IAsyncThreadListener) newWorld).syncCall(() -> {
                if (entity.isEntityAlive()) {
                    entity.setLocationAndAngles(fdx, entity.posY, fdz, entity.rotationYaw, entity.rotationPitch);
                    Teleporter teleporter = newWorld.getDefaultTeleporter();
                    if (entity instanceof EntityPlayerMP)
                        teleporter.placeInPortal(entity, yaw, 0, 0, 0);
                    else
                        teleporter.placeInExistingPortal(entity, yaw, 0, 0, 0);
                    newWorld.spawnEntityInWorld(entity);
                    newWorld.updateEntityWithOptionalForce(entity, false);
                    entity.setWorld(newWorld);
                }
            });
        }
    }

    @Nonnull
    @Overwrite
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP oldPlayer, int targetDimension,
                                               boolean conqueredEnd) {
        EntityPlayerMP playerMP[] = {null};
        WorldServer oldWorld = mcServer.worldServerForDimension(targetDimension);
        if (oldWorld == null)
            targetDimension = oldPlayer.dimension;
        else if (!oldWorld.provider.canRespawnHere())
            targetDimension = oldWorld.provider.getRespawnDimension(oldPlayer);
        if (mcServer.worldServerForDimension(targetDimension) == null)
            targetDimension = 0;
        int dimension = targetDimension;
        ((IAsyncThreadListener) oldPlayer.getServerForPlayer()).syncCall(() -> {
            oldPlayer.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(oldPlayer);
            oldPlayer.getServerForPlayer().getEntityTracker().untrackEntity(oldPlayer);
            oldPlayer.getServerForPlayer().getPlayerManager().removePlayer(oldPlayer);
            playerEntityList.remove(oldPlayer);
            this.mcServer.worldServerForDimension(oldPlayer.dimension).removePlayerEntityDangerously(oldPlayer);
            oldPlayer.dimension = dimension;
        });
        WorldServer newWorld = mcServer.worldServerForDimension(dimension);
        ((IAsyncThreadListener) newWorld).syncCall(() -> {
            ChunkCoordinates bedPos = oldPlayer.getBedLocation(dimension);
            boolean flag = oldPlayer.isSpawnForced(dimension);
            ItemInWorldManager interactionManager;
            if (mcServer.isDemo())
                interactionManager = new DemoWorldManager(newWorld);
            else
                interactionManager = new ItemInWorldManager(newWorld);
            EntityPlayerMP newPlayer = new EntityPlayerMP(mcServer, newWorld, oldPlayer.getGameProfile(),
                    interactionManager);
            newPlayer.playerNetServerHandler = oldPlayer.playerNetServerHandler;
            newPlayer.clonePlayer(oldPlayer, conqueredEnd);
            newPlayer.dimension = dimension;
            newPlayer.setEntityId(oldPlayer.getEntityId());
            playerEntityList.add(newPlayer);
            func_72381_a(newPlayer, oldPlayer, newWorld);
            if (bedPos != null) {
                ChunkCoordinates spawnPos = EntityPlayer.verifyRespawnCoordinates(newWorld, bedPos, flag);
                if (spawnPos != null) {
                    newPlayer.setLocationAndAngles(spawnPos.posX + 0.5F, spawnPos.posY + 0.1F,
                            spawnPos.posZ + 0.5F, 0.0F, 0.0F);
                    newPlayer.setSpawnChunk(bedPos, flag);
                }
                else
                    newPlayer.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0.0F));
            }
            newWorld.getChunkProvider().provideChunk((int) newPlayer.posX >> 4, (int) newPlayer.posZ >> 4);
            while (!newWorld.getCollidingBoundingBoxes(newPlayer, newPlayer.boundingBox).isEmpty()
                    && newPlayer.posY < 256.0D)
                newPlayer.setPosition(newPlayer.posX, newPlayer.posY + 1.0D, newPlayer.posZ);
            newPlayer.playerNetServerHandler.sendPacket(new S07PacketRespawn(newPlayer.dimension, newPlayer.worldObj.difficultySetting,
                    newPlayer.worldObj.getWorldInfo().getTerrainType(), newPlayer.theItemInWorldManager.getGameType()));
            newPlayer.playerNetServerHandler.setPlayerLocation(newPlayer.posX, newPlayer.posY, newPlayer.posZ,
                    newPlayer.rotationYaw, newPlayer.rotationPitch);
            newPlayer.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(newWorld.getSpawnPoint().posX, newWorld.getSpawnPoint().posY, newWorld.getSpawnPoint().posZ));
            newPlayer.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(newPlayer.experience,
                    newPlayer.experienceTotal, newPlayer.experienceLevel));
            updateTimeAndWeatherForPlayer(newPlayer, newWorld);
            newWorld.getPlayerManager().addPlayer(newPlayer);
            newWorld.spawnEntityInWorld(newPlayer);
            newPlayer.addSelfToInternalCraftingInventory();
            newPlayer.setHealth(newPlayer.getHealth());
            FMLCommonHandler.instance().firePlayerRespawnEvent(newPlayer);
            playerMP[0] = newPlayer;
        });
        return playerMP[0];
    }

    @Shadow
    @Final
    public List playerEntityList;

    @Overwrite
    protected void writePlayerData(EntityPlayerMP player) {
        IAsyncThreadListener listener = (IAsyncThreadListener) player.getServerForPlayer();
        if (listener.isCallingFromMinecraftThread())
            writePlayerDataInternal(player);
        else
            listener.addScheduledTask(() -> writePlayerDataInternal(player));
    }

    @Shadow
    private IPlayerFileData playerNBTManagerObj;

    @Shadow
    @Final
    private Map playerStatFiles;

    protected void writePlayerDataInternal(EntityPlayerMP player) {
        if (player.playerNetServerHandler == null) return;

        this.playerNBTManagerObj.writePlayerData(player);
        StatisticsFile statisticsfile = (StatisticsFile)this.playerStatFiles.get(player.getUniqueID());

        if (statisticsfile != null)
        {
            statisticsfile.func_150883_b();
        }
    }

    @Overwrite
    public void setViewDistance(int distance) {
        viewDistance = distance;
        if (mcServer.worldServers != null)
            for (WorldServer world : mcServer.worldServers)
                if (world != null)
                    ((IAsyncThreadListener) world).addScheduledTask(() -> {
                        world.getPlayerManager().func_152622_a(distance);
                    });
    }

    @Shadow
    protected abstract void updateTimeAndWeatherForPlayer(EntityPlayerMP player, WorldServer worldserver);

    @Shadow
    protected abstract void playerLoggedIn(EntityPlayerMP player);

    @Shadow
    protected abstract void sendChatMsg(IChatComponent chatcomponenttranslation);

    @Shadow
    protected abstract void func_96456_a(ServerScoreboard scoreboard, EntityPlayerMP player);
}
