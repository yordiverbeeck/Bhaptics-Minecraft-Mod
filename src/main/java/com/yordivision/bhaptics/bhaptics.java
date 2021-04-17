package com.yordivision.bhaptics;

import com.bhaptics.haptic.HapticPlayer;
import com.bhaptics.haptic.HapticPlayerImpl;
import com.bhaptics.haptic.models.*;
import com.bhaptics.haptic.utils.StringUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;


@Mod("bhaptics")
public class bhaptics
{
    final String appId  = "com.yordivision.bhaptics";
    final String appName = "Bhaptics Minecraft";
    public HapticPlayer hapticPlayer;
    private static final Logger LOGGER = LogManager.getLogger();

    public bhaptics() throws IOException, InterruptedException, URISyntaxException {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);

        hapticPlayer = new HapticPlayerImpl(appId, appName, true,(connected)->{
            LOGGER.info("Haptic Connected");
            LOGGER.info(connected);
            registerFiles();
        });
        //hapticPlayer.register("arrow", content);
    }

    private void registerFiles(){
        List<String> files = Arrays.asList(
                "explosion",
                "explosion2",
                "explosion3",
                "playerhitbyprojectile1",
                "arrowhit",
                "arrowhit2",
                "arrowhit3",
                "playerhitbyprojectile2",
                "playerhitbyprojectile3",
                "ironGolem");

        for (String fileName : files) {
            URL importedURL = getClass().getClassLoader().getResource("tactFiles/"+fileName+".tact");
            if(importedURL != null) {
                File file = new File(importedURL.getFile());
                String content = StringUtils.readFile(file);
                hapticPlayer.register(fileName, content);
            }
        }
    }

    private int randomInt(int minimum, int maximum){
        Random rand = new Random();
        return minimum + rand.nextInt((maximum - minimum) + 1);
    }

    private void playTact(String tactname, float angle){
        hapticPlayer.submitRegistered(tactname, "MinecraftBhaptics",
                new RotationOption(angle, 0),
                new ScaleOption(1, 1));
    }

    private void playTact (String tactname){
        playTact(tactname,0);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Bonsoir Elliot");
    }

    @SubscribeEvent
    public void catchHurtEvent(LivingHurtEvent e) {
        if (e.getEntity() instanceof PlayerEntity) {
            LOGGER.info("Player hurt!");

            //get location where damage is coming from
            if(e.getSource().getSourcePosition() != null){
                Entity AttackerEntity = e.getSource().getDirectEntity();
                LivingEntity player = (LivingEntity) e.getEntityLiving();

                assert AttackerEntity != null;
                float angle = angleCalc(player.getPosition(0), AttackerEntity.getPosition(0));

                float offsetAngle = player.getRotationVector().y - angle;

                while (offsetAngle>360 || offsetAngle < 0) {
                    if (offsetAngle >= 360) {
                        offsetAngle -= 360;
                    } else if (offsetAngle < 0) {
                        offsetAngle += 360;
                    }
                }

                if(AttackerEntity instanceof SkeletonEntity || AttackerEntity instanceof ArrowEntity){
                    playTact("arrowhit", offsetAngle);
                }else if(AttackerEntity instanceof ZombieEntity){
                    playTact("ironGolem",offsetAngle);
                }else if(AttackerEntity instanceof CreeperEntity){
                    switch (randomInt(0,2)){
                        case 0:
                            playTact("explosion1", offsetAngle);
                            break;
                        case 1:
                            playTact("explosion2", offsetAngle);
                            break;
                        case 2:
                            playTact("explosion3", offsetAngle);
                            break;
                    }
                }else if (AttackerEntity instanceof IronGolemEntity){
                       playTact("ironGolem",offsetAngle);
                }else{
                    switch (randomInt(0,2)){
                        case 0:
                            playTact("playerhitbyprojectile1", offsetAngle);
                            break;
                        case 1:
                            playTact("playerhitbyprojectile2", offsetAngle);
                            break;
                        case 2:
                            playTact("playerhitbyprojectile3", offsetAngle);
                            break;
                    }
                }
            }else{
                //general damage, no origin position found
                playTact("explosion1");
            }
        }
    }

    public float angleCalc(Vector3d player, Vector3d attacker) {
        float n = 0;
        float offset = 0;

        float radians = (float) (n * Math.PI * 0.5 + Math.atan2(attacker.x - player.x,attacker.z - player.z));
        float degrees = (float) (offset + (0.5 * radians/Math.PI) * 360);
        if(degrees > 0){
            degrees-=360;
        }
        degrees = Math.abs(degrees);

        return degrees;
    }
}
