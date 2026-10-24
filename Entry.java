package de.farm;

import org.bukkit.Material;

/** Eine Produktions-Kiste einer Farm (wird in config.yml gespeichert). */
record Entry(String world, int x, int y, int z, Material product, int every, int amount) {
    String serialize() {
        return world + ";" + x + ";" + y + ";" + z + ";" + product.name() + ";" + every + ";" + amount;
    }

    static Entry parse(String s) {
        String[] p = s.split(";");
        return new Entry(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]),
            Material.valueOf(p[4]), Integer.parseInt(p[5]), Integer.parseInt(p[6]));
    }
}
