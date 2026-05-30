package NXxueyue.mouse.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class MouseClient implements ClientModInitializer {

    
    public static boolean attackEnabled = false;
    private static KeyBinding attackToggleKey;
    private static boolean wasAttackKeyPressed = false;
    private static int attackTickCounter = 0;

    // 右键放方块
    public static boolean placeEnabled = false;
    private static KeyBinding placeToggleKey;
    private static boolean wasPlaceKeyPressed = false;
    private static int placeTickCounter = 0;

    @Override
    public void onInitializeClient() {

        attackToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mouse.attack",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.mouse.main"
        ));

 
        placeToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mouse.place",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.mouse.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(MouseClient::onTick);
    }

    private static void onTick(MinecraftClient client) {
        if (client.player == null) return;

        
        boolean attackPressed = attackToggleKey.isPressed();
        if (attackPressed && !wasAttackKeyPressed) {
            attackEnabled = !attackEnabled;
            client.player.sendMessage(
                Text.literal(attackEnabled ? "§aAttack ON" : "§cAttack OFF"),
                true
            );
        }
        wasAttackKeyPressed = attackPressed;


        boolean placePressed = placeToggleKey.isPressed();
        if (placePressed && !wasPlaceKeyPressed) {
            placeEnabled = !placeEnabled;
            client.player.sendMessage(
                Text.literal(placeEnabled ? "§aPlace ON" : "§cPlace OFF"),
                true
            );
        }
        wasPlaceKeyPressed = placePressed;

        if (client.interactionManager == null) return;

        
        
        if (attackEnabled
            && isWeapon(client.player.getMainHandStack().getItem())
            && client.options.attackKey.isPressed()) {

            attackTickCounter++;
            if (attackTickCounter >= 2) {
                attackTickCounter = 0;

                if (client.crosshairTarget instanceof EntityHitResult hit) {
                    Entity target = hit.getEntity();
                    client.interactionManager.attackEntity(client.player, target);
                } else {
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }
        } else {
            attackTickCounter = 0;
        }


        if (placeEnabled
            && client.player.getMainHandStack().getItem() instanceof BlockItem
            && client.options.useKey.isPressed()) {

            placeTickCounter++;
            
            int interval = Math.random() < 0.75 ? 1 : 2;
            if (placeTickCounter >= interval) {
                placeTickCounter = 0;

                if (client.crosshairTarget instanceof BlockHitResult blockHit) {
                    client.interactionManager.interactBlock(
                        client.player,
                        Hand.MAIN_HAND,
                        blockHit
                    );
                }
            }
        } else {
            placeTickCounter = 0;
        }
    }

    private static boolean isWeapon(Item item) {
        return item instanceof SwordItem
            || item instanceof AxeItem
            || item instanceof TridentItem
            || item instanceof MaceItem;
    }
}