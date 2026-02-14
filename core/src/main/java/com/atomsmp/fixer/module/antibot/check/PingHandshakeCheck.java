package com.atomsmp.fixer.module.antibot.check;

import com.atomsmp.fixer.module.antibot.AntiBotModule;
import com.atomsmp.fixer.module.antibot.PlayerProfile;

public class PingHandshakeCheck extends AbstractCheck {
    
    public PingHandshakeCheck(AntiBotModule module) {
        super(module, "ping-handshake");
    }

    @Override
    public int calculateThreatScore(PlayerProfile profile) {
        long now = System.currentTimeMillis();
        long lastPing = profile.getLastPingTime();
        
        if (lastPing == 0) {
            // No ping recorded for this IP
            return module.getAttackTracker().isUnderAttack() ? 
                    module.getConfigInt("kontroller.ping-handshake.ping-yok-skor-saldiri", 10) : 
                    module.getConfigInt("kontroller.ping-handshake.ping-yok-skor-normal", 5);
        }

        long timeSincePing = now - profile.getHandshakeTime();
        if (timeSincePing < 500) {
            return 10;
        }

        // Protocol version check could be added if we store it from the ping packet
        // For now, simple timing is enough as specified in the prompt
        
        return 0;
    }
}
