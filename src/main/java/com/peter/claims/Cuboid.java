package com.peter.claims;

import org.joml.Vector3i;

import net.minecraft.util.math.BlockPos;

public class Cuboid {

    public Vector3i min;
    public Vector3i max;

    public Cuboid(Vector3i p1, Vector3i p2) {
        int minX = p1.x <= p2.x ? p1.x : p2.x;
        int minY = p1.y <= p2.y ? p1.y : p2.y;
        int minZ = p1.z <= p2.z ? p1.z : p2.z;
        min = new Vector3i(minX, minY, minZ);

        int maxX = p1.x > p2.x ? p1.x : p2.x;
        int maxY = p1.y > p2.y ? p1.y : p2.y;
        int maxZ = p1.z > p2.z ? p1.z : p2.z;
        max = new Vector3i(maxX, maxY, maxZ);
    }

    public Cuboid(BlockPos p1, BlockPos p2) {
        int minX = p1.getX() <= p2.getX() ? p1.getX() : p2.getX();
        int minY = p1.getY() <= p2.getY() ? p1.getY() : p2.getY();
        int minZ = p1.getZ() <= p2.getZ() ? p1.getZ() : p2.getZ();
        min = new Vector3i(minX, minY, minZ);

        int maxX = p1.getX() > p2.getX() ? p1.getX() : p2.getX();
        int maxY = p1.getY() > p2.getY() ? p1.getY() : p2.getY();
        int maxZ = p1.getZ() > p2.getZ() ? p1.getZ() : p2.getZ();
        max = new Vector3i(maxX, maxY, maxZ);
    }

    public boolean inside(Vector3i p) {
        return inside(p.x, p.y, p.z);
    }

    public boolean inside(int x, int y, int z) {
        return (min.x <= x && x <= max.x)
                && (min.y <= y && y <= max.y)
                && (min.z <= z && z <= max.z);
    }

    public boolean overlaps(Vector3i min, Vector3i max) {
        return overlaps(new Cuboid(min, max));
    }

    public boolean overlaps(CuboidLike other) {
        return overlaps(other.getMin(), other.getMax());
    }

    public boolean overlaps(Cuboid other) {

        // Check if we totally can't overlap at all
        if (max.x < other.min.x || min.x > other.max.x)
            return false;
        if (max.z < other.min.z || min.z > other.max.z)
            return false;
        if (max.y < other.min.y || min.y > other.max.y)
            return false;

        // If not, we must be overlapping
        return true;
    }
    
    public interface CuboidLike {

        public boolean inside(Vector3i pos);

        public Vector3i getMin();
        public Vector3i getMax();
    }

}
