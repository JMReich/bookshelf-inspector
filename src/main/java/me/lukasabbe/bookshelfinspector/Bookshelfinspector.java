package me.lukasabbe.bookshelfinspector;

import me.lukasabbe.bookshelfinspector.network.BookShelfInventoryPayload;
import me.lukasabbe.bookshelfinspector.network.BookShelfInventoryRequestPayload;
import me.lukasabbe.bookshelfinspector.network.ModCheckPayload;
import me.lukasabbe.bookshelfinspector.util.BookshelfTools;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;

import java.util.logging.Logger;

public class Bookshelfinspector implements ModInitializer {
    public static MinecraftServer serverInstance;
    public final static String MOD_ID = "bookshelfinspector";

    public static Logger LOGGER = Logger.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(BookShelfInventoryRequestPayload.ID,BookShelfInventoryRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BookShelfInventoryPayload.ID,BookShelfInventoryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModCheckPayload.ID, ModCheckPayload.CODEC);


        ServerPlayNetworking.registerGlobalReceiver(BookShelfInventoryRequestPayload.ID,((payload, context) -> context.server().execute(() -> {
            if(Bookshelfinspector.serverInstance == null) return;

            ItemStack stack = BookshelfTools.getItemById(payload.pos(),payload.slotNum(),context.player());
            if(stack == null){
                ServerPlayNetworking.send(context.player(), new BookShelfInventoryPayload(Items.AIR.getDefaultStack(), payload.pos(), payload.slotNum()));
                return;
            }
            ServerPlayNetworking.send(context.player(), new BookShelfInventoryPayload(stack, payload.pos(), payload.slotNum()));
        })));

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            serverInstance = server;
            ServerPlayNetworking.send(handler.player, new ModCheckPayload(true));
        }));
    }
}