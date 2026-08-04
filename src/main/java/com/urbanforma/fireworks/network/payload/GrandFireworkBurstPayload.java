package com.urbanforma.fireworks.network.payload;

import com.urbanforma.fireworks.UrbanformaFireworks;
import com.urbanforma.fireworks.content.FireworkStyle;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** A small server-authored description that lets the client reconstruct one style-specific burst locally. */
public record GrandFireworkBurstPayload(double x, double y, double z, long seed, int styleIndex)
        implements CustomPacketPayload {
    public static final Type<GrandFireworkBurstPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(UrbanformaFireworks.MOD_ID, "grand_firework_burst"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GrandFireworkBurstPayload> STREAM_CODEC = StreamCodec.of(
            GrandFireworkBurstPayload::encode,
            GrandFireworkBurstPayload::decode);

    /** Compatibility constructor for packets/tests that predate the series index. */
    public GrandFireworkBurstPayload(double x, double y, double z, long seed) {
        this(x, y, z, seed, FireworkStyle.GRAND_GOLDEN_SPHERE.index());
    }

    public GrandFireworkBurstPayload {
        styleIndex = FireworkStyle.fromIndex(styleIndex).index();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GrandFireworkBurstPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.enqueueWork(() -> ClientPayloadHandler.enqueue(payload));
        }
    }

    private static void encode(RegistryFriendlyByteBuf buffer, GrandFireworkBurstPayload payload) {
        buffer.writeDouble(payload.x());
        buffer.writeDouble(payload.y());
        buffer.writeDouble(payload.z());
        buffer.writeLong(payload.seed());
        buffer.writeVarInt(payload.styleIndex());
    }

    private static GrandFireworkBurstPayload decode(RegistryFriendlyByteBuf buffer) {
        return new GrandFireworkBurstPayload(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readLong(),
                buffer.readVarInt());
    }

    public FireworkStyle style() {
        return FireworkStyle.fromIndex(this.styleIndex);
    }

    private static final class ClientPayloadHandler {
        private ClientPayloadHandler() {
        }

        private static void enqueue(GrandFireworkBurstPayload payload) {
            com.urbanforma.fireworks.client.GrandFireworkClientEffects.enqueue(payload);
        }
    }
}
