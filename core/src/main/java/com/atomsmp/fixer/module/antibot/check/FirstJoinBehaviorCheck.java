package com.atomsmp.fixer.module.antibot.check;

import com.atomsmp.fixer.module.antibot.AntiBotModule;
import com.atomsmp.fixer.module.antibot.PlayerProfile;

public class FirstJoinBehaviorCheck extends AbstractCheck {
    
    public FirstJoinBehaviorCheck(AntiBotModule module) {
        super(module, "ilk-giris-davranis");
    }

    @Override
    public int calculateThreatScore(PlayerProfile profile) {
        int ticks = profile.getTicksSinceJoin();
        int analysisTime = module.getConfigInt("kontroller.ilk-giris-davranis.analiz-suresi-tick", 100);
        
        if (ticks > analysisTime) {
            return profile.getCachedFirstJoinScore();
        }

        int score = 0;

        // 1. Camera movement
        if (ticks >= 60) {
            if (profile.getUniqueYawValues() <= 1 && profile.getUniquePitchValues() <= 1) {
                score += 8;
            }
        }

        // 2. Position packet presence
        if (ticks >= 40) {
            if (!profile.hasSentPositionPacket()) {
                score += 15;
            }
        }

        // 3. Timing of first movement
        long delay = profile.getFirstMovementDelayMs();
        if (delay > 0 && delay < 50) {
            score += 10;
        }

        profile.setCachedFirstJoinScore(score);
        return Math.min(score, 30);
    }
}
