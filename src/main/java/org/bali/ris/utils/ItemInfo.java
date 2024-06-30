package org.bali.ris.utils;

import org.bukkit.Material;
import org.bukkit.World;

public class ItemInfo {
    private World world;
    private double x, y, z;
    private Material item;
    private int value;

    public ItemInfo(World world, double x, double y, double z, Material item, int value) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.item = item;
        this.value = value;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Material getItem() {
        return item;
    }

    public void setItem(Material item) {
        this.item = item;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}