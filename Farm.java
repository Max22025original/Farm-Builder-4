package de.farm;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

/**
 * NORMAL: Kisten werden vom Plugin gefüllt. VILLAGER_CROP: wie NORMAL + Bauern-Villager.
 * HALL: Villager-Handelshalle. TRAP: echte Spawner-Falle (Wasser + Trichter + Kiste).
 * BONEMEAL: Skelett-Spawner-Falle + Crafter + Trichter-Uhr, die Knochen automatisch zu Knochenmehl macht.
 */
record Farm(String name, Material icon, Material product, int every, int amount,
            EntityType animal, Villager.Profession job, int kind, EntityType trap) {
    static final int NORMAL = 0, VILLAGER_CROP = 1, HALL = 2, TRAP = 3, BONEMEAL = 4, HALL_ALL = 5;
}
