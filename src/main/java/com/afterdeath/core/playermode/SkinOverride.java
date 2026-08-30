package com.afterdeath.core.playermode;

import com.afterdeath.core.AfterdeathCore;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side skin swap for morphed players. The 3p body of SKELETON players
 * is drawn by our own skeleton path (which picks its texture in
 * SkeletonRenderer, unaffected by this), so this override matters where the
 * vanilla PlayerRenderer pipeline still runs — the first-person arm, tab
 * list, chat heads, and any other view that reads {@link AbstractClientPlayer#getSkin()}.
 */
public final class SkinOverride {

    public static final ResourceLocation SKELETON = ResourceLocation.fromNamespaceAndPath(
            AfterdeathCore.MODID, "skins/skeleton.png");
    public static final ResourceLocation GHOST = ResourceLocation.fromNamespaceAndPath(
            AfterdeathCore.MODID, "skins/ghost.png");

    private SkinOverride() {}

    @Nullable
    public static ResourceLocation textureFor(AbstractClientPlayer player) {
        PlayerMode mode = player.getData(PlayerModeAttachments.PLAYER_MODE);
        return switch (mode) {
            case SKELETON -> SKELETON;
            case SOUL -> GHOST;
            case HUMAN -> null;
        };
    }
}
