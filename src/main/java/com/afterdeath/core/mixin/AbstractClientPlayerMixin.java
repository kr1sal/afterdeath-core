package com.afterdeath.core.mixin;

import com.afterdeath.core.playermode.SkinOverride;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Swaps the skin returned by {@link AbstractClientPlayer#getSkin()} for
 * players who are in a morph mode. Every consumer that reads the vanilla
 * player skin (first-person arm, tab list, chat portraits) picks up the
 * morph texture automatically.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Shadow
    protected abstract PlayerInfo getPlayerInfo();

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void afterdeath_core$overrideMorphSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        ResourceLocation texture = SkinOverride.textureFor(self);
        if (texture == null) return;

        // Preserve the player's Slim/Wide arm width so morph geometry lines
        // up; fall back to Wide when playerInfo is momentarily missing.
        PlayerSkin.Model model = PlayerSkin.Model.WIDE;
        PlayerInfo info = this.getPlayerInfo();
        if (info != null) {
            model = info.getSkin().model();
        }
        cir.setReturnValue(new PlayerSkin(texture, null, null, null, model, true));
    }
}
