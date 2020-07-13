package com.yordinateur.bhaptics;

import com.bhaptics.haptic.HapticPlayer;
import com.bhaptics.haptic.HapticPlayerImpl;
import com.bhaptics.haptic.models.*;
import com.bhaptics.haptic.utils.StringUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    final String appId  = "com.yordinateur.bhaptics";
    final String appName = "Bhaptics Minecraft";
    public HapticPlayer hapticPlayer;
    private static final Logger LOGGER = LogManager.getLogger();

    public bhaptics() throws IOException, InterruptedException, URISyntaxException {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);


        boolean connected = false;
        hapticPlayer = new HapticPlayerImpl(appId, appName, connected);
        sleep(2000);
        //hapticPlayer.register("arrow", content);

        Map<String, String> tactFiles = new Hashtable<String, String>();

        // Inserting elements into the table
        tactFiles.put("explosion1", "explosion.tact");
        tactFiles.put("explosion2", "explosion2.tact");
        tactFiles.put("explosion3", "explosion3.tact");
        tactFiles.put("playerhitbyprojectile1", "PlayerHitbyProjectile.tact");
        tactFiles.put("arrowhit", "skelettonArrowLongFront.tact");
        tactFiles.put("playerhitbyprojectile2", "PlayerHitbyProjectile2.tact");
        tactFiles.put("playerhitbyprojectile3", "PlayerHitbyProjectile3.tact");
        tactFiles.put("ironGolem", "IronGolem.tact");


        for (Map.Entry<String,String> file : tactFiles.entrySet()) {
            URL importedURL = getClass().getClassLoader().getResource(file.getValue());
            if(importedURL != null) {
                File imported = new File(importedURL.getFile());
                String content = StringUtils.readFile(imported);
                hapticPlayer.register(file.getKey(), content);
                System.out.println("Imported: "+file.getValue());
            }else{
                System.out.println("Didn't import: "+file.getValue());
            }
        }

    }

    private int randomInt(int minimum, int maximum){
        Random rand = new Random();
        return minimum + rand.nextInt((maximum - minimum) + 1);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void playTact(String tactname, float angle){
        hapticPlayer.submitRegistered(tactname, "test2",
                new RotationOption(angle, 0),
                new ScaleOption(1, 1));
        System.out.println("Sent damage event: "+tactname+", angle: "+angle);
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
            LOGGER.info("OOF! " + e.getSource().getTrueSource()+" -- "+e.getSource().getDamageType() +" -- "+e.getSource().getDamageLocation());
            Entity AttackerEntity = e.getSource().getTrueSource();
            LivingEntity player = (LivingEntity) e.getEntity();
            //LOGGER.info(player.rotationYaw);

            //get location where damage is coming from
            if(e.getSource().getDamageLocation() != null){
                //TODO: nullpointer
                float offset = 0;
                float angle = angleCalc(player.getPositionVec(), AttackerEntity.getPositionVec());

                float finalangle = (player.rotationYaw - angle) - offset;

                while (finalangle>360 || finalangle < 0) {
                    if (finalangle >= 360) {
                        finalangle -= 360;
                    } else if (finalangle < 0) {
                        finalangle += 360;
                    }
                }

                if(AttackerEntity instanceof SkeletonEntity){
                    playTact("arrowhit", finalangle);
                }else if(AttackerEntity instanceof CreeperEntity){
                    switch (randomInt(0,2)){
                        case 0:
                            playTact("explosion1", finalangle);
                            break;
                        case 1:
                            playTact("explosion2", finalangle);
                            break;
                        case 2:
                            playTact("explosion3", finalangle);
                            break;
                    }
                }else if (AttackerEntity instanceof IronGolemEntity){
                       playTact("ironGolem",finalangle);
                }else{
                    switch (randomInt(0,2)){
                        case 0:
                            playTact("playerhitbyprojectile1", finalangle);
                            break;
                        case 1:
                            playTact("playerhitbyprojectile2", finalangle);
                            break;
                        case 2:
                            playTact("playerhitbyprojectile3", finalangle);
                            break;
                    }
                }

                /*TODO
                * - map enity on type damage
                * - map #damage on intensity
                *
                * */

                LOGGER.info("yaw rotation " + (player.rotationYaw));
                LOGGER.info("angelcalc " + angle);
                LOGGER.info("final calc " + finalangle);

            }else{
                //general damage
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
