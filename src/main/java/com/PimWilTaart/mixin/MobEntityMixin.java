package com.PimWilTaart.silentsneak.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mixin(HostileEntity.class)
public abstract class MobEntityMixin {

    // Standaard detectie-ranges per mob
    private static final Map<String, Double> DETECTION_RANGES = new HashMap<>();

    static {
        DETECTION_RANGES.put("minecraft:warden", 10.0);
        DETECTION_RANGES.put("minecraft:enderman", 7.0);
        DETECTION_RANGES.put("minecraft:creeper", 3.0);
        DETECTION_RANGES.put("minecraft:zombie", 5.0);
        DETECTION_RANGES.put("minecraft:skeleton", 6.0);
        DETECTION_RANGES.put("minecraft:spider", 4.0);
        DETECTION_RANGES.put("minecraft:phantom", 8.0);
    }

    @Inject(method = "canSee", at = @At("HEAD"), cancellable = true)
    private void modifyDetectionRange(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof PlayerEntity player) {
            if (player.isSneaking()) {
                String mobId = ((HostileEntity) (Object) this).getType().toString();
                double customRange = DETECTION_RANGES.getOrDefault(mobId, 5.0); // Standaard 5 blokken als fallback
                double distance = player.squaredDistanceTo((LivingEntity) (Object) this);

                if (distance > customRange * customRange) {
                    cir.setReturnValue(false); // De mob kan de speler niet zien
                }
            }
        }
    }
}

private static void loadConfig() {
    File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "silent_sneak_config.json");
    if (configFile.exists()) {
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            for (String key : json.keySet()) {
                DETECTION_RANGES.put("minecraft:" + key, json.get(key).getAsDouble());
            }
        } catch (IOException e) {
            System.err.println("Fout bij laden van configuratie: " + e.getMessage());
        }
    }
}