package ru.helena.chatreminder.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.helena.chatreminder.ChatReminderClient;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void chatreminder$onClientTick(CallbackInfo ci) {
        ChatReminderClient.onClientTick((MinecraftClient) (Object) this);
    }
}
