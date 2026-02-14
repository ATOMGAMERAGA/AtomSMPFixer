package com.atomsmp.fixer.module.antibot;

import com.atomsmp.fixer.module.antibot.check.*;
import com.atomsmp.fixer.module.antibot.action.ActionType;
import java.util.*;

public class ThreatScoreCalculator {
    private final AntiBotModule module;
    private final List<AbstractCheck> checks = new ArrayList<>();

    public ThreatScoreCalculator(AntiBotModule module) {
        this.module = module;
        registerChecks();
    }

    private void registerChecks() {
        checks.add(new ConnectionRateCheck(module));
        checks.add(new PingHandshakeCheck(module));
        checks.add(new UsernamePatternCheck(module));
        checks.add(new ProtocolCheck(module));
        checks.add(new FirstJoinBehaviorCheck(module));
        checks.add(new GravityCheck(module));
        checks.add(new PacketTimingCheck(module));
        checks.add(new PostJoinBehaviorCheck(module));
    }

    public ThreatResult evaluate(PlayerProfile profile) {
        if (module.getWhitelistManager().isWhitelisted(profile.getUuid())) {
            return new ThreatResult(0, ActionType.ALLOW, Collections.emptyMap());
        }

        int totalScore = 0;
        Map<String, Integer> breakdown = new LinkedHashMap<>();

        for (AbstractCheck check : checks) {
            if (!check.isEnabled()) continue;

            int score = check.calculateThreatScore(profile);
            if (module.getAttackTracker().isUnderAttack()) {
                score = (int) (score * check.getAttackModeMultiplier());
            }

            breakdown.put(check.getName(), score);
            totalScore += score;
        }

        profile.updateMaxThreatScore(totalScore);

        // Action thresholds
        ActionType action;
        double multiplier = module.getAttackTracker().isUnderAttack() ? 
                module.getConfigDouble("saldiri-modu.esik-carpani", 0.7) : 1.0;

        int allowThreshold = (int) (module.getConfigInt("skor-esikleri.izin-ver", 30) * multiplier);
        int delayThreshold = (int) (module.getConfigInt("skor-esikleri.geciktir", 60) * multiplier);
        int kickThreshold = (int) (module.getConfigInt("skor-esikleri.at", 80) * multiplier);

        if (totalScore < allowThreshold) {
            action = ActionType.ALLOW;
        } else if (totalScore < delayThreshold) {
            action = ActionType.DELAY;
        } else if (totalScore < kickThreshold) {
            action = ActionType.KICK;
        } else {
            action = ActionType.BLACKLIST;
        }

        return new ThreatResult(totalScore, action, breakdown);
    }

    public static class ThreatResult {
        private final int score;
        private final ActionType action;
        private final Map<String, Integer> breakdown;

        public ThreatResult(int score, ActionType action, Map<String, Integer> breakdown) {
            this.score = score;
            this.action = action;
            this.breakdown = breakdown;
        }

        public int getScore() { return score; }
        public ActionType getAction() { return action; }
        public Map<String, Integer> getBreakdown() { return breakdown; }
    }
}
