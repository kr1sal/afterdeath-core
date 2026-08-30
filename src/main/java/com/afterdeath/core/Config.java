package com.afterdeath.core;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue PHYLACTERY_MAX_CHARGE;
    public static final ModConfigSpec.IntValue PHYLACTERY_TICKS_PER_UNIT;
    public static final ModConfigSpec.IntValue PHYLACTERY_CHARGE_PER_USE;
    public static final ModConfigSpec.IntValue PHYLACTERY_PORTAL_CHARGE_COST;
    public static final ModConfigSpec.DoubleValue PHYLACTERY_HEAL_AMOUNT;
    public static final ModConfigSpec.IntValue PHYLACTERY_REGEN_TICKS;
    public static final ModConfigSpec.IntValue PHYLACTERY_ABSORPTION_TICKS;
    public static final ModConfigSpec.IntValue PHYLACTERY_FIRE_RES_TICKS;
    public static final ModConfigSpec.BooleanValue PHYLACTERY_EMPTY_ON_TOSS;
    public static final ModConfigSpec.BooleanValue PHYLACTERY_EMPTY_ON_DEATH;

    public static final ModConfigSpec.IntValue PLAYERMODE_ANGER_TICKS;
    public static final ModConfigSpec.DoubleValue PLAYERMODE_BROADCAST_RADIUS;

    public static final ModConfigSpec.IntValue SPIRIT_FLIGHT_MAX_TICKS;
    public static final ModConfigSpec.IntValue SPIRIT_FLIGHT_REGEN_TICKS_PER_UNIT;
    public static final ModConfigSpec.DoubleValue SPIRIT_FLIGHT_SPEED;

    public static final ModConfigSpec.IntValue STORY_COMPASS_RADIUS_CHUNKS;
    public static final ModConfigSpec.IntValue STORY_COMPASS_INTERVAL_TICKS;
    public static final ModConfigSpec.DoubleValue STORY_COMPASS_REFRESH_DIST;

    static {
        B.comment("Phylactery item settings").translation("configuration.AfterdeathCore.phylactery").push("phylactery");

        PHYLACTERY_MAX_CHARGE = B
                .comment("Maximum charge the phylactery can hold")
                .translation("configuration.AfterdeathCore.phylactery.max_charge")
                .defineInRange("max_charge", 100, 1, 100000);

        PHYLACTERY_TICKS_PER_UNIT = B
                .comment("Ticks between +1 charge while in inventory (lower = faster recharge)")
                .translation("configuration.AfterdeathCore.phylactery.ticks_per_unit")
                .defineInRange("ticks_per_unit", 10, 1, 20000);

        PHYLACTERY_CHARGE_PER_USE = B
                .comment("Charge consumed each time the phylactery saves you from lethal damage")
                .translation("configuration.AfterdeathCore.phylactery.charge_per_use")
                .defineInRange("charge_per_use", 50, 1, 100000);

        PHYLACTERY_PORTAL_CHARGE_COST = B
                .comment("Charge consumed when the phylactery is used to ignite a Nether portal frame")
                .translation("configuration.AfterdeathCore.phylactery.portal_charge_cost")
                .defineInRange("portal_charge_cost", 50, 1, 100000);

        PHYLACTERY_HEAL_AMOUNT = B
                .comment("Health restored when the phylactery triggers")
                .translation("configuration.AfterdeathCore.phylactery.heal_amount")
                .defineInRange("heal_amount", 6.0D, 0.5D, 1024.0D);

        PHYLACTERY_REGEN_TICKS = B
                .comment("Regeneration II duration (in ticks) applied after rescue")
                .translation("configuration.AfterdeathCore.phylactery.regen_ticks")
                .defineInRange("regeneration_ticks", 900, 0, 200000);

        PHYLACTERY_ABSORPTION_TICKS = B
                .comment("Absorption II duration (in ticks) applied after rescue")
                .translation("configuration.AfterdeathCore.phylactery.absorption_ticks")
                .defineInRange("absorption_ticks", 100, 0, 200000);

        PHYLACTERY_FIRE_RES_TICKS = B
                .comment("Fire Resistance duration (in ticks) applied after rescue")
                .translation("configuration.AfterdeathCore.phylactery.fire_res_ticks")
                .defineInRange("fire_resistance_ticks", 800, 0, 200000);

        PHYLACTERY_EMPTY_ON_TOSS = B
                .comment("Reset phylactery charge to 0 when it is dropped from inventory")
                .translation("configuration.AfterdeathCore.phylactery.empty_on_toss")
                .define("empty_on_toss", true);

        PHYLACTERY_EMPTY_ON_DEATH = B
                .comment("Reset phylactery charge to 0 in death drops")
                .translation("configuration.AfterdeathCore.phylactery.empty_on_death")
                .define("empty_on_death", true);

        B.pop();

        B.comment("Player-mode (soul / skeleton / human) settings")
                .translation("configuration.AfterdeathCore.player_mode")
                .push("player_mode");

        PLAYERMODE_ANGER_TICKS = B
                .comment("How long (ticks) a mob stays hostile after a non-human player attacks it")
                .translation("configuration.AfterdeathCore.player_mode.anger_ticks")
                .defineInRange("anger_ticks", 800, 20, 20 * 60 * 60);

        PLAYERMODE_BROADCAST_RADIUS = B
                .comment("Radius (blocks) around the attacked mob whose peers also get angry")
                .translation("configuration.AfterdeathCore.player_mode.broadcast_radius")
                .defineInRange("broadcast_radius", 16.0D, 0.0D, 256.0D);

        B.comment("Spirit-only flight settings (soul stage)")
                .translation("configuration.AfterdeathCore.player_mode.spirit_flight")
                .push("spirit_flight");

        SPIRIT_FLIGHT_MAX_TICKS = B
                .comment("Maximum spirit-flight stamina in ticks (200 = 10s)")
                .translation("configuration.AfterdeathCore.player_mode.spirit_flight.max_ticks")
                .defineInRange("max_ticks", 160, 20, 12000);

        SPIRIT_FLIGHT_REGEN_TICKS_PER_UNIT = B
                .comment("Ticks per +1 stamina while grounded (lower = faster regen)")
                .translation("configuration.AfterdeathCore.player_mode.spirit_flight.regen_ticks_per_unit")
                .defineInRange("regen_ticks_per_unit", 1, 1, 200);

        SPIRIT_FLIGHT_SPEED = B
                .comment("Fly speed for the spirit (vanilla creative is 0.05; lower = slower)")
                .translation("configuration.AfterdeathCore.player_mode.spirit_flight.fly_speed")
                .defineInRange("fly_speed", 0.025D, 0.005D, 0.1D);

        B.pop();
        B.pop();

        B.comment("Story compass settings")
                .translation("configuration.AfterdeathCore.story_compass")
                .push("story_compass");

        STORY_COMPASS_RADIUS_CHUNKS = B
                .comment("Search radius in chunks")
                .translation("configuration.AfterdeathCore.story_compass.radius_chunks")
                .defineInRange("radius_chunks", 1000, 1, 10000);

        STORY_COMPASS_INTERVAL_TICKS = B
                .comment("Ticks between re-scans while the compass sits in inventory")
                .translation("configuration.AfterdeathCore.story_compass.interval_ticks")
                .defineInRange("interval_ticks", 40, 1, 20000);

        STORY_COMPASS_REFRESH_DIST = B
                .comment("Compass rescans only after moving this many blocks away from the last target")
                .translation("configuration.AfterdeathCore.story_compass.refresh_distance")
                .defineInRange("refresh_distance", 32.0D, 1.0D, 10000.0D);

        B.pop();
    }

    public static final ModConfigSpec SPEC = B.build();

    private Config() {}

    private static boolean isValidResourceLocation(Object o) {
        return o instanceof String s && ResourceLocation.tryParse(s) != null;
    }
}
