package com.atomsmp.fixer.reputation;

import com.atomsmp.fixer.AtomSMPFixer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Advanced IP reputation and Proxy/VPN detection with persistent caching and Geo-IP.
 */
public class IPReputationManager {

    private final AtomSMPFixer plugin;
    private Map<String, ReputationResult> cache;
    private final File cacheFile;
    private final Gson gson;
    
    private final long cacheTtl;
    private final String apiKey;
    private final boolean enabled;
    
    private final int riskThreshold;
    private final List<String> blockedCountries;

    public IPReputationManager(AtomSMPFixer plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cacheFile = new File(plugin.getDataFolder(), "ip_cache.json");
        this.cache = new ConcurrentHashMap<>();
        
        this.cacheTtl = TimeUnit.HOURS.toMillis(plugin.getConfig().getLong("anti-vpn.cache-hours", 24));
        this.apiKey = plugin.getConfig().getString("anti-vpn.api-key", "");
        this.enabled = plugin.getConfig().getBoolean("anti-vpn.enabled", false);
        this.riskThreshold = plugin.getConfig().getInt("anti-vpn.risk-threshold", 60);
        this.blockedCountries = plugin.getConfig().getStringList("anti-vpn.blocked-countries");
        
        loadCache();
    }

    private void loadCache() {
        if (!cacheFile.exists()) return;
        try (Reader reader = new FileReader(cacheFile)) {
            Map<String, ReputationResult> loaded = gson.fromJson(reader, new TypeToken<ConcurrentHashMap<String, ReputationResult>>(){}.getType());
            if (loaded != null) {
                // Remove expired entries on load
                long now = System.currentTimeMillis();
                loaded.entrySet().removeIf(entry -> (now - entry.getValue().timestamp) > cacheTtl);
                this.cache = new ConcurrentHashMap<>(loaded);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load IP cache: " + e.getMessage());
        }
    }

    public void saveCache() {
        try (Writer writer = new FileWriter(cacheFile)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save IP cache: " + e.getMessage());
        }
    }

    /**
     * Checks if an IP is a proxy or VPN asynchronously.
     */
    public CompletableFuture<ReputationResult> checkIp(@NotNull String ip) {
        if (!enabled) {
            return CompletableFuture.completedFuture(new ReputationResult(false, 0, "System Disabled", "Unknown", "Unknown"));
        }

        // Check Cache
        ReputationResult cached = cache.get(ip);
        if (cached != null && (System.currentTimeMillis() - cached.timestamp < cacheTtl)) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // ProxyCheck.io advanced query
                String apiUrl = "https://proxycheck.io/v2/" + ip + "?key=" + apiKey + "&vpn=1&asn=1&cur=1";
                
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "AtomSMPFixer-Protection");
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);

                if (conn.getResponseCode() != 200) {
                    return new ReputationResult(false, 0, "API Error", "Unknown", "Unknown");
                }

                JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
                
                if (json.has(ip)) {
                    JsonObject ipData = json.getAsJsonObject(ip);
                    String proxy = ipData.get("proxy").getAsString();
                    int risk = ipData.has("risk") ? ipData.get("risk").getAsInt() : 0;
                    String country = ipData.has("isocode") ? ipData.get("isocode").getAsString() : "Unknown";
                    String asn = ipData.has("asn") ? ipData.get("asn").getAsString() : "Unknown";
                    
                    boolean isBlocked = proxy.equalsIgnoreCase("yes") || risk >= riskThreshold || blockedCountries.contains(country);
                    
                    ReputationResult result = new ReputationResult(isBlocked, risk, ipData.get("type").getAsString(), country, asn);
                    cache.put(ip, result);
                    return result;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("IP Check failed for " + ip + ": " + e.getMessage());
            }
            return new ReputationResult(false, 0, "Check Failed", "Unknown", "Unknown");
        });
    }

    public static class ReputationResult {
        public final boolean isBlocked;
        public final int riskScore;
        public final String type;
        public final String country;
        public final String asn;
        public final long timestamp;

        public ReputationResult(boolean isBlocked, int riskScore, String type, String country, String asn) {
            this.isBlocked = isBlocked;
            this.riskScore = riskScore;
            this.type = type;
            this.country = country;
            this.asn = asn;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
