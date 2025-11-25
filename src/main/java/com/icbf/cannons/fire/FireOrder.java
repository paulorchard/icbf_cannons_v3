package com.icbf.cannons.fire;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a propagated firing order that moves between cannons.
 * This is server-side only and intentionally lightweight.
 */
public class FireOrder {
    public final UUID origin;
    public final BlockPos target;
    // remaining budget (how many more cannons may receive this order)
    public int remainingBudget;
    // current hop depth
    public int hop;
    // how many neighbors this node should forward to
    public int branchLimit;
    // delay before forwarding (in ticks)
    public int delayTicks;
    // visited cannon positions to prevent cycles
    public final Set<BlockPos> visited;

    public FireOrder(UUID origin, BlockPos target, int remainingBudget, int hop, int branchLimit, int delayTicks) {
        this.origin = origin;
        this.target = target;
        this.remainingBudget = remainingBudget;
        this.hop = hop;
        this.branchLimit = branchLimit;
        this.delayTicks = delayTicks;
        this.visited = new HashSet<>();
    }

    // Optional: UUID of the player who initiated the firing (used to send guaranteed impact feedback)
    public UUID initiatorPlayer = null;

    public FireOrder copyForForward(int newBranchLimit, int newDelayTicks) {
        FireOrder f = new FireOrder(this.origin, this.target, this.remainingBudget, this.hop + 1, newBranchLimit, newDelayTicks);
        f.visited.addAll(this.visited);
        return f;
    }
}
