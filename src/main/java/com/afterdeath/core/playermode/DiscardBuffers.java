package com.afterdeath.core.playermode;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

final class DiscardBuffers implements MultiBufferSource {

    static final DiscardBuffers INSTANCE = new DiscardBuffers();

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return NoOp.INSTANCE;
    }

    private DiscardBuffers() {}

    private static final class NoOp implements VertexConsumer {
        static final NoOp INSTANCE = new NoOp();

        @Override public VertexConsumer addVertex(float x, float y, float z)   { return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a)   { return this; }
        @Override public VertexConsumer setUv(float u, float v)                { return this; }
        @Override public VertexConsumer setUv1(int u, int v)                   { return this; }
        @Override public VertexConsumer setUv2(int u, int v)                   { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z)   { return this; }
    }
}
