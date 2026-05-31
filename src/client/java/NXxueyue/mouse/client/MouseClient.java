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

    // 右键放方块 15 CPS
    public static boolean placeEnabled = false;
    private static KeyBinding placeToggleKey;
    private static boolean wasPlaceKeyPressed = false;
    private static int placeTickCounter = 0;

    // 左键连点 15 CPS（直接发包）
    public static boolean attackEnabled = false;
    private static KeyBinding attackToggleKey;
    private static boolean wasAttackKeyPressed = false;
    private static int attackTickCounter = 0;

    @Override
    public void onInitializeClient() {
        placeToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mouse.place", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.mouse.main"));
        attackToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mouse.attack", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.mouse.main"));

        ClientTickEvents.END_CLIENT_TICK.register(MouseClient::onTick);
    }

    private static void onTick(MinecraftClient client) {
        if (client.player == null) return;
        if (client.interactionManager == null) return;

        // ===== B 键切换 =====
        boolean placePressed = placeToggleKey.isPressed();
        if (placePressed && !wasPlaceKeyPressed) {
            placeEnabled = !placeEnabled;
            client.player.sendMessage(Text.literal(placeEnabled ? "§aPlace ON" : "§cPlace OFF"), true);
        }
        wasPlaceKeyPressed = placePressed;

        // ===== V 键切换 =====
        boolean attackPressed = attackToggleKey.isPressed();
        if (attackPressed && !wasAttackKeyPressed) {
            attackEnabled = !attackEnabled;
            client.player.sendMessage(Text.literal(attackEnabled ? "§aAttack ON" : "§cAttack OFF"), true);
        }
        wasAttackKeyPressed = attackPressed;

        // ===== 右键放方块 15 CPS =====
        if (placeEnabled
            && client.player.getMainHandStack().getItem() instanceof BlockItem
            && client.options.useKey.isPressed()) {

            placeTickCounter++;
            if (placeTickCounter == 1 || placeTickCounter == 2 || placeTickCounter == 4) {
                InputUtil.Key useKey = client.options.useKey.getDefaultKey();
                KeyBinding.setKeyPressed(useKey, true);
                KeyBinding.onKeyPressed(useKey);
            }
            if (placeTickCounter >= 4) {
                placeTickCounter = 0;
            }
        } else {
            placeTickCounter = 0;
        }

        // ===== 左键连点 15 CPS（直接发包） =====
        if (attackEnabled
            && isWeapon(client.player.getMainHandStack().getItem())
            && client.options.attackKey.isPressed()) {

            attackTickCounter++;
            if (attackTickCounter == 1 || attackTickCounter == 2 || attackTickCounter == 4) {
                if (client.crosshairTarget instanceof EntityHitResult hit) {
                    client.interactionManager.attackEntity(client.player, hit.getEntity());
                } else {
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }
            if (attackTickCounter >= 4) {
                attackTickCounter = 0;
            }
        } else {
            attackTickCounter = 0;
        }
    }

    private static boolean isWeapon(Item item) {
        return item instanceof SwordItem
            || item instanceof AxeItem
            || item instanceof TridentItem
            || item instanceof MaceItem;
    }
}