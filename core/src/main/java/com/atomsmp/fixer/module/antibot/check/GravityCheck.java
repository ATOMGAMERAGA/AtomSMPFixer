package com.atomsmp.fixer.module.antibot.check;

import com.atomsmp.fixer.module.antibot.AntiBotModule;
import com.atomsmp.fixer.module.antibot.PlayerProfile;
import java.util.List;

public class GravityCheck extends AbstractCheck {
    
    private static final double GRAVITY = 0.08;
    private static final double DRAG = 0.98;

    public GravityCheck(AntiBotModule module) {
        super(module, "yercekimi");
    }

    @Override
    public int calculateThreatScore(PlayerProfile profile) {
        List<Double> yPositions = profile.getRecentYPositions();
        int minData = module.getConfigInt("kontroller.yercekimi.min-veri-sayisi", 5);
        
        if (yPositions.size() < minData) return 0;

        int violations = 0;
        int totalChecks = 0;
        double tolerance = module.getConfigDouble("kontroller.yercekimi.tolerans", 0.03);

        for (int i = 2; i < yPositions.size(); i++) {
            double deltaY1 = yPositions.get(i - 1) - yPositions.get(i - 2);
            double deltaY2 = yPositions.get(i) - yPositions.get(i - 1);

            if (Math.abs(deltaY1) < 0.001 && Math.abs(deltaY2) < 0.001) continue;

            totalChecks++;
            double expectedDeltaY = (deltaY1 - GRAVITY) * DRAG;
            double difference = Math.abs(deltaY2 - expectedDeltaY);

            if (difference > tolerance) {
                violations++;
            }
        }

        if (totalChecks < 3) return 0;

        double violationRate = (double) violations / totalChecks;

        if (violationRate > 0.7) return 40;
        if (violationRate > 0.5) return 25;
        if (violationRate > 0.3) return 10;

        return 0;
    }
}
