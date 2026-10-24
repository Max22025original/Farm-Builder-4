package de.farm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

/** Baut die Farmen. Produktions-Farmen: Kisten, die vom Plugin automatisch befüllt werden. */
final class Constructions {
    private Constructions() {}

    /** Mittelpunkte der Produktionsmodule (3x3), Abstand 6. */
    static List<int[]> modules(int h) {
        List<int[]> list = new ArrayList<>();
        for (int cx = -h + 3; cx <= h - 2; cx += 6)
            for (int cz = -h + 3; cz <= h - 2; cz += 6)
                list.add(new int[]{cx, cz});
        return list;
    }

    private static boolean inModule(List<int[]> mods, int x, int z) {
        for (int[] m : mods)
            if (Math.abs(x - m[0]) <= 1 && Math.abs(z - m[1]) <= 1) return true;
        return false;
    }

    private static Material cropFor(Material product) {
        return switch (product) {
            case CARROT -> Material.CARROTS;
            case POTATO -> Material.POTATOES;
            case BEETROOT -> Material.BEETROOTS;
            case PUMPKIN -> Material.PUMPKIN_STEM;
            case MELON_SLICE -> Material.MELON_STEM;
            case NETHER_WART -> Material.NETHER_WART;
            case SWEET_BERRIES -> Material.SWEET_BERRY_BUSH;
            default -> Material.WHEAT;
        };
    }

    private static void villager(Builder b, int x, int z, Villager.Profession job) {
        Location loc = new Location(b.w, b.ox + x + 0.5, b.oy + 1, b.oz + z + 0.5);
        b.w.spawn(loc, Villager.class, (Villager v) -> {
            v.setProfession(job);
            v.setPersistent(true);
            v.setAdult();
            v.setRecipes(fixedStarterTrades(job));
        });
    }

    /** Festgelegte, einfache Startangebote. Sie sind bewusst keine Behauptung, alle Level-/Buchtrades abzudecken. */
    private static List<MerchantRecipe> fixedStarterTrades(Villager.Profession job) {
        List<MerchantRecipe> recipes = new ArrayList<>();
        switch (job) {
            case FARMER -> {
                recipes.add(trade(Material.BREAD, 1, Material.EMERALD, 1));
                recipes.add(trade(Material.GOLDEN_CARROT, 3, Material.EMERALD, 1));
            }
            case LIBRARIAN -> {
                recipes.add(trade(Material.BOOKSHELF, 1, Material.EMERALD, 2));
                recipes.add(trade(Material.LANTERN, 1, Material.EMERALD, 1));
            }
            case ARMORER -> {
                recipes.add(trade(Material.IRON_INGOT, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.IRON_CHESTPLATE, 1, Material.EMERALD, 5));
            }
            case WEAPONSMITH -> {
                recipes.add(trade(Material.IRON_INGOT, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.IRON_SWORD, 1, Material.EMERALD, 3));
            }
            case TOOLSMITH -> {
                recipes.add(trade(Material.IRON_INGOT, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.IRON_PICKAXE, 1, Material.EMERALD, 3));
            }
            case CLERIC -> {
                recipes.add(trade(Material.ROTTEN_FLESH, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.REDSTONE, 2, Material.EMERALD, 1));
            }
            case BUTCHER -> {
                recipes.add(trade(Material.CHICKEN, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.COOKED_PORKCHOP, 1, Material.EMERALD, 1));
            }
            case LEATHERWORKER -> {
                recipes.add(trade(Material.LEATHER, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.LEATHER, 2, Material.EMERALD, 1));
            }
            case MASON -> {
                recipes.add(trade(Material.CLAY_BALL, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.BRICK, 4, Material.EMERALD, 1));
            }
            case FISHERMAN -> {
                recipes.add(trade(Material.STRING, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.COD, 4, Material.EMERALD, 1));
            }
            case FLETCHER -> {
                recipes.add(trade(Material.STICK, 8, Material.EMERALD, 1));
                recipes.add(trade(Material.ARROW, 1, Material.EMERALD, 1));
            }
            case SHEPHERD -> {
                recipes.add(trade(Material.WHITE_WOOL, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.WHITE_DYE, 3, Material.EMERALD, 1));
            }
            case CARTOGRAPHER -> {
                recipes.add(trade(Material.PAPER, 4, Material.EMERALD, 1));
                recipes.add(trade(Material.GLASS_PANE, 4, Material.EMERALD, 1));
            }
            default -> { }
        }
        return recipes;
    }

    private static MerchantRecipe trade(Material result, int resultCount, Material input, int inputCount) {
        MerchantRecipe recipe = new MerchantRecipe(new ItemStack(result, resultCount), 999999);
        recipe.addIngredient(new ItemStack(input, inputCount));
        return recipe;
    }

    private static final Villager.Profession[] TRADING_JOBS = {
        Villager.Profession.LIBRARIAN, Villager.Profession.ARMORER, Villager.Profession.WEAPONSMITH,
        Villager.Profession.TOOLSMITH, Villager.Profession.CLERIC, Villager.Profession.FARMER,
        Villager.Profession.BUTCHER, Villager.Profession.LEATHERWORKER, Villager.Profession.MASON,
        Villager.Profession.FISHERMAN, Villager.Profession.FLETCHER, Villager.Profession.SHEPHERD,
        Villager.Profession.CARTOGRAPHER
    };

    // ------------------------------------------------------------ Produktions-Farmen

    static List<Entry> production(Builder b, Farm f, Theme t, int maxVillagers, int maxAnimals) {
        int h = b.h;
        List<Entry> out = new ArrayList<>();
        List<int[]> mods = modules(h);

        b.floor(t.floor());

        if (t.fields()) {
            Material crop = cropFor(f.product());
            Material soil = crop == Material.NETHER_WART ? Material.SOUL_SAND : Material.FARMLAND;
            for (int x = -h + 1; x <= h - 1; x++) {
                for (int z = -h + 1; z <= h - 1; z++) {
                    if (inModule(mods, x, z)) continue;
                    if (soil == Material.FARMLAND && Math.floorMod(x, 5) == 0) {
                        b.set(x, 0, z, Material.WATER);
                    } else {
                        b.set(x, 0, z, soil);
                        b.crop(x, 1, z, crop);
                    }
                }
            }
        }

        b.ring(1, t.wallH(), t.wall());
        if (f.kind() != Farm.VILLAGER_CROP) {
            for (int y = 1; y <= t.wallH(); y++) b.set(0, y, -h, Material.AIR);
        }

        for (int[] m : mods) {
            int cx = m[0], cz = m[1];
            b.fill(cx - 1, 0, cz - 1, cx + 1, 0, cz + 1, t.base());
            b.fill(cx - 1, 1, cz - 1, cx + 1, 1, cz + 1, Material.AIR);
            b.set(cx, 1, cz, Material.CHEST);
            BlockData bd = b.at(cx, 1, cz).getBlockData();
            if (bd instanceof Directional d) {
                d.setFacing(BlockFace.SOUTH);
                b.at(cx, 1, cz).setBlockData(d, false);
            }
            b.set(cx - 1, 1, cz - 1, t.deco());
            b.set(cx + 1, 1, cz - 1, t.deco());
            b.set(cx - 1, 1, cz + 1, t.deco());
            b.set(cx + 1, 1, cz + 1, t.deco());
            out.add(new Entry(b.w.getName(), b.ox + cx, b.oy + 1, b.oz + cz, f.product(), f.every(), f.amount()));
        }

        if (f.animal() != null) {
            int count = Math.min(maxAnimals, 6 + mods.size() / 2);
            scatter(b, mods, count, (x, z) -> b.spawn(f.animal(), x, z));
        }
        if (f.kind() == Farm.VILLAGER_CROP) {
            int count = Math.min(maxVillagers, 2 + mods.size());
            scatter(b, mods, count, (x, z) -> villager(b, x, z, f.job()));
        }
        return out;
    }

    private interface Spawn {
        void at(int x, int z);
    }

    private static void scatter(Builder b, List<int[]> mods, int count, Spawn spawn) {
        int h = b.h;
        int range = b.n - 4;
        int placed = 0;
        for (int i = 0; placed < count && i < count * 25; i++) {
            int x = -h + 2 + (i * 7) % range;
            int z = -h + 2 + (i * 11) % range;
            if (inModule(mods, x, z)) continue;
            if (b.at(x, 1, z).getType().isSolid()) continue;
            if (b.at(x, 0, z).getType() == Material.WATER) continue;
            spawn.at(x, z);
            placed++;
        }
    }

    // ------------------------------------------------------------ Spawner-Fallen

    private static final int SH = 24; // Höhe des Schachts

    private static boolean isAnimal(EntityType t) {
        return t == EntityType.COW || t == EntityType.PIG || t == EntityType.SHEEP
            || t == EntityType.CHICKEN || t == EntityType.RABBIT;
    }

    private static void data(Builder b, int x, int y, int z, String blockData, boolean physics) {
        b.at(x, y, z).setBlockData(Bukkit.createBlockData(blockData), physics);
    }

    /**
     * Echte Spawner-Falle: Spawner im dunklen Raum, Wasser auf dem Boden spült Mobs und Items zum Loch,
     * ein Wasserschacht führt nach unten zu einem Trichter, der in eine Kiste leitet.
     * (Das Plugin tötet die gespawnten Mobs nach kurzer Zeit, damit nichts liegen bleibt.)
     * Gibt die Positionen der Spawner zurück ("welt;x;y;z").
     */
    static List<String> trap(Builder b, Farm f, Theme t) {
        int k = Math.max(1, b.n / 13);
        int start = -((k - 1) * 13) / 2;
        boolean animal = isAnimal(f.trap());
        List<String> spawners = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                int cx = start + i * 13;
                int cz = start + j * 13;
                trapModule(b, f, t, cx, cz, animal, f.kind() == Farm.BONEMEAL);
                spawners.add(b.w.getName() + ";" + (b.ox + cx) + ";" + (b.oy + SH + 5) + ";" + (b.oz + cz));
            }
        }
        return spawners;
    }

    private static void trapModule(Builder b, Farm f, Theme t, int cx, int cz, boolean animal, boolean boneMeal) {
        int floorY = SH + 3;       // Boden des Spawnraums
        int roofY = SH + 7;        // Decke
        int spawnerY = SH + 5;

        // freiräumen + Bodenplatte
        b.fill(cx - 5, 1, cz - 5, cx + 5, roofY + 1, cz + 5, Material.AIR);
        b.fill(cx - 5, 0, cz - 5, cx + 5, 0, cz + 5, t.base());

        // Schachtwände (3x3 Ring) von y=2 bis zum Boden des Raums
        for (int y = 2; y <= floorY; y++) {
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    if (dx != 0 || dz != 0) b.set(cx + dx, y, cz + dz, Material.STONE_BRICKS);
        }

        // Sammelstelle unten
        if (boneMeal) {
            boneMealBottom(b, cx, cz);
        } else {
            b.set(cx, 1, cz, Material.CHEST);
            Block chest = b.at(cx, 1, cz);
            if (chest.getBlockData() instanceof Directional d) {
                d.setFacing(BlockFace.SOUTH);
                chest.setBlockData(d, false);
            }
        }
        data(b, cx, 2, cz, "minecraft:hopper[facing=down]", false);

        // Wasserschacht
        b.fill(cx, 3, cz, cx, floorY, cz, Material.WATER);

        // Boden des Spawnraums (Radius 4) mit Loch in der Mitte
        Material floor = animal ? Material.GRASS_BLOCK : Material.STONE_BRICKS;
        b.fill(cx - 4, floorY, cz - 4, cx + 4, floorY, cz + 4, floor);
        b.set(cx, floorY, cz, Material.WATER);

        // Wände (Radius 5) und Decke
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    for (int y = floorY; y <= roofY; y++) b.set(cx + x, y, cz + z, Material.STONE_BRICKS);
                }
            }
        }
        b.fill(cx - 5, roofY, cz - 5, cx + 5, roofY, cz + 5, Material.STONE_BRICKS);
        if (animal) { // Tiere brauchen Licht
            for (int sx = -3; sx <= 3; sx += 6)
                for (int sz = -3; sz <= 3; sz += 6)
                    b.set(cx + sx, roofY, cz + sz, Material.SEA_LANTERN);
        }

        // Wasser auf dem Boden (8 Quellen am Rand) - es fließt zum Loch
        int wy = floorY + 1;
        int[][] src = {{4, 0}, {-4, 0}, {0, 4}, {0, -4}, {4, 4}, {-4, 4}, {4, -4}, {-4, -4}};
        for (int[] s : src) b.set(cx + s[0], wy, cz + s[1], Material.WATER);

        // Spawner
        b.set(cx, spawnerY, cz, Material.SPAWNER);
        BlockState st = b.at(cx, spawnerY, cz).getState();
        if (st instanceof CreatureSpawner cs) {
            cs.setSpawnedType(f.trap());
            cs.setMinSpawnDelay(20);
            cs.setMaxSpawnDelay(100);
            cs.setSpawnCount(4);
            cs.setMaxNearbyEntities(16);
            cs.setRequiredPlayerRange(48);
            cs.setSpawnRange(4);
            cs.update(true, false);
        }
    }

    /**
     * Knochenmehl-Automatik unten am Schacht (alles auf y=1):
     * Trichter (oben) -> Crafter (0,0) -> Kiste (0,+1).
     * Trichter-Uhr (2 Trichter) -> Komparator -> Redstone-Staub -> Crafter löst jede Sekunde ein Craften aus.
     */
    private static void boneMealBottom(Builder b, int cx, int cz) {
        // Crafter mit Ausgabe nach Süden in die Kiste
        data(b, cx, 1, cz, "minecraft:crafter[orientation=south_up]", false);
        b.set(cx, 1, cz + 1, Material.CHEST);

        // nur Slot 0 aktiv lassen, damit der Trichter nur dort Knochen einlegt (per Reflection, API-unabhängig)
        try {
            BlockState st = b.at(cx, 1, cz).getState();
            Class<?> iface = Class.forName("org.bukkit.block.Crafter");
            if (iface.isInstance(st)) {
                java.lang.reflect.Method m = iface.getMethod("setSlotDisabled", int.class, boolean.class);
                for (int i = 1; i < 9; i++) m.invoke(st, i, true);
                st.update(true, false);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // wenn nicht verfügbar: Farm läuft trotzdem, nur evtl. mit Stau
        }

        // Trichter-Uhr links vom Crafter
        data(b, cx - 3, 1, cz, "minecraft:hopper[facing=south]", false);
        data(b, cx - 3, 1, cz + 1, "minecraft:hopper[facing=north]", false);
        try {
            Block h1 = b.at(cx - 3, 1, cz);
            if (h1.getState() instanceof org.bukkit.block.Hopper hop) {
                hop.getInventory().addItem(new ItemStack(Material.STICK));
            }
        } catch (RuntimeException ignored) {
            // Uhr-Item konnte nicht gesetzt werden
        }
        data(b, cx - 2, 1, cz, "minecraft:comparator[facing=east]", true);
        data(b, cx - 1, 1, cz, "minecraft:redstone_wire", true);
    }

    // ------------------------------------------------------------ Villager-Handelshalle

    /**
     * Gänge mit Zellen auf beiden Seiten. Pro Zelle: ein Villager mit Beruf, direkt dahinter sein Arbeitsblock.
     * Der Spieler handelt vom Gang aus durch ein Eisengitter. Einlass an der Westseite (x = -h).
     */
    static void hall(Builder b, Farm f, int maxVillagers) {
        int h = b.h, n = b.n;
        b.floor(Material.OAK_PLANKS);
        b.fill(-h, 3, -h, h, 3, h, Material.OAK_PLANKS);
        int spawned = 0;

        for (int z = -h; z <= h; z++) {
            for (int x = -h; x <= h; x++) {
                int zz = z + h, xx = x + h;
                boolean edgeZ = zz == 0 || zz == n - 1;
                int q = zz - 1;
                int unit = q / 8;
                int r = q % 8;
                boolean complete = !edgeZ && (unit * 8 + 6 <= n - 3);
                boolean divider = xx % 2 == 0;

                if (edgeZ || !complete || r == 7) {
                    b.fill(x, 1, z, x, 2, z, Material.STONE_BRICKS);
                    continue;
                }
                switch (r) {
                    case 3 -> { // Gang
                        if (x == h) b.fill(x, 1, z, x, 2, z, Material.STONE_BRICKS);
                        if (xx % 4 == 1) b.set(x, 3, z, Material.SEA_LANTERN);
                    }
                    case 0, 6 -> { // Arbeitsblock-Reihe
                        if (divider) b.fill(x, 1, z, x, 2, z, Material.STONE_BRICKS);
                        else b.set(x, 1, z, f.product());
                    }
                    case 1, 5 -> { // Villager-Reihe
                        if (divider) {
                            b.fill(x, 1, z, x, 2, z, Material.STONE_BRICKS);
                        } else {
                            b.set(x, 3, z, Material.SEA_LANTERN);
                            if (spawned < maxVillagers) {
                                Villager.Profession job = f.kind() == Farm.HALL_ALL
                                    ? TRADING_JOBS[spawned % TRADING_JOBS.length] : f.job();
                                villager(b, x, z, job);
                                spawned++;
                            }
                        }
                    }
                    case 2, 4 -> { // Trennwand zum Gang mit Eisengitter
                        if (divider) b.fill(x, 1, z, x, 2, z, Material.STONE_BRICKS);
                        else b.set(x, 1, z, Material.IRON_BARS);
                    }
                    default -> { }
                }
            }
        }
    }
}
