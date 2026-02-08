package com.atomsmp.fixer.listener;

import com.atomsmp.fixer.AtomSMPFixer;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Netty pipeline'ına enjekte edilen crash koruması handler'ı.
 * "decoder" handler'ından SONRA yerleştirilir.
 *
 * Bu handler, dekoder tarafından parse edilen paketlerdeki hataları yakalar
 * ve sunucu crash'lerini önler. NaN, Infinity gibi değerler ve
 * malformed paketler bu katmanda engellenir.
 *
 * @author AtomSMP
 * @version 2.0.0
 */
public class NettyCrashHandler extends ChannelDuplexHandler {

    /** Handler pipeline adı — tekil olmalı */
    public static final String HANDLER_NAME = "atomsmpfixer-crash-handler";

    private final AtomSMPFixer plugin;
    private final UUID playerUuid;
    private final String playerName;

    /**
     * NettyCrashHandler constructor
     *
     * @param plugin     Ana plugin instance
     * @param playerUuid Oyuncu UUID'si
     * @param playerName Oyuncu adı (loglama için)
     */
    public NettyCrashHandler(@NotNull AtomSMPFixer plugin,
                             @NotNull UUID playerUuid,
                             @NotNull String playerName) {
        this.plugin = plugin;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    /**
     * Gelen mesajları (dekode edilmiş paketleri) dinler.
     * Hata oluşursa bağlantıyı güvenli şekilde kapatır.
     */
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        // Dekode edilmiş paket nesneleri buradan geçer.
        // NMS paket nesnelerini doğrudan incelemek yerine,
        // exception handler olarak çalışır — hatalı paketlerin crash yapmasını önler.
        super.channelRead(ctx, msg);
    }

    /**
     * Pipeline'da oluşan hataları yakalar.
     * DecoderException, overflow vb. hatalar sunucuyu çökertebilir.
     */
    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) {
        // Crash girişimini tespit et
        String errorMsg = cause.getMessage();
        boolean isCrashAttempt = false;

        if (errorMsg != null) {
            String lowerMsg = errorMsg.toLowerCase();
            // Bilinen crash vektörleri
            isCrashAttempt = lowerMsg.contains("overflow")
                    || lowerMsg.contains("nan")
                    || lowerMsg.contains("infinity")
                    || lowerMsg.contains("out of bounds")
                    || lowerMsg.contains("negative length")
                    || lowerMsg.contains("varint too big")
                    || lowerMsg.contains("string too long")
                    || lowerMsg.contains("packet too large");
        }

        // DecoderException her zaman şüpheli
        if (cause.getClass().getSimpleName().contains("DecoderException")) {
            isCrashAttempt = true;
        }

        if (isCrashAttempt) {
            // Crash girişimini logla
            plugin.getLogManager().logExploit(playerName, "netty-crash",
                    String.format("Netty crash girişimi tespit edildi! Hata: %s, UUID: %s",
                            cause.getClass().getSimpleName() + ": " + errorMsg,
                            playerUuid));

            // Bağlantıyı güvenli şekilde kapat
            if (ctx.channel().isActive()) {
                ctx.close();
            }
            return;
        }

        // Bilinmeyen hatalar için varsayılan davranış
        ctx.close();
    }

    /**
     * Channel inaktif olduğunda temizlik yapar
     */
    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * Oyuncu UUID'sini döndürür
     */
    @NotNull
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Oyuncu adını döndürür
     */
    @NotNull
    public String getPlayerName() {
        return playerName;
    }
}
