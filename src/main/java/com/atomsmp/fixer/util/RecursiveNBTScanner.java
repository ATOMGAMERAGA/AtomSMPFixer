package com.atomsmp.fixer.util;

import com.github.retrooper.packetevents.protocol.nbt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Recursive NBT Scanner for deep-level validation.
 * Uses PacketEvents NBT API to scan the actual NBT tree.
 * 
 * @author AtomSMP
 * @version 1.0.0
 */
public class RecursiveNBTScanner {

    /**
     * Scans an NBT compound recursively to check for depth and tag count.
     *
     * @param compound The NBT compound to scan
     * @param maxDepth Maximum allowed depth
     * @param maxTags Maximum allowed total tag count
     * @return true if safe, false if limits exceeded
     */
    public static boolean isSafe(@Nullable NBTCompound compound, int maxDepth, int maxTags) {
        if (compound == null) return true;
        
        ScanResult result = new ScanResult();
        scan(compound, 1, maxDepth, maxTags, result);
        
        return !result.limitExceeded;
    }

    private static void scan(@NotNull NBT tag, int currentDepth, int maxDepth, int maxTags, ScanResult result) {
        if (result.limitExceeded) return;

        result.tagCount++;
        if (result.tagCount > maxTags || currentDepth > maxDepth) {
            result.limitExceeded = true;
            return;
        }

        if (tag instanceof NBTCompound compound) {
            for (Map.Entry<String, NBT> entry : compound.getTags().entrySet()) {
                scan(entry.getValue(), currentDepth + 1, maxDepth, maxTags, result);
                if (result.limitExceeded) return;
            }
        } else if (tag instanceof NBTList<?> list) {
            for (Object elementObj : list.getTags()) {
                if (elementObj instanceof NBT element) {
                    scan(element, currentDepth + 1, maxDepth, maxTags, result);
                    if (result.limitExceeded) return;
                }
            }
        }
    }

    private static class ScanResult {
        int tagCount = 0;
        boolean limitExceeded = false;
    }

    /**
     * Estimates the size of the NBT tag in bytes.
     *
     * @param tag The tag to estimate
     * @return Estimated size in bytes
     */
    public static int estimateSize(@Nullable NBT tag) {
        if (tag == null) return 0;

        if (tag instanceof NBTByte) return 1;
        if (tag instanceof NBTShort) return 2;
        if (tag instanceof NBTInt) return 4;
        if (tag instanceof NBTLong) return 8;
        if (tag instanceof NBTFloat) return 4;
        if (tag instanceof NBTDouble) return 8;
        if (tag instanceof NBTByteArray ba) return ba.getValue().length;
        if (tag instanceof NBTIntArray ia) return ia.getValue().length * 4;
        if (tag instanceof NBTLongArray la) return la.getValue().length * 8;
        if (tag instanceof NBTString s) return s.getValue().length() * 2;
        
        int size = 0;
        if (tag instanceof NBTCompound compound) {
            for (Map.Entry<String, NBT> entry : compound.getTags().entrySet()) {
                size += entry.getKey().length() * 2;
                size += estimateSize(entry.getValue());
            }
        } else if (tag instanceof NBTList<?> list) {
            for (Object elementObj : list.getTags()) {
                if (elementObj instanceof NBT element) {
                    size += estimateSize(element);
                }
            }
        }
        
        return size;
    }
}
