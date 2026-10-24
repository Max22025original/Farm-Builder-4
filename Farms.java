package de.farm;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.util.List;

/** Alle 100 Farmen in 10 Kategorien. */
final class Farms {
    private Farms() {}

    // ---- Optik ----
    static final Theme CROP = new Theme(Material.GRASS_BLOCK, Material.OAK_FENCE, 1, Material.HAY_BLOCK, Material.OAK_PLANKS, true);
    static final Theme TREE = new Theme(Material.GRASS_BLOCK, Material.OAK_FENCE, 1, Material.OAK_LOG, Material.OAK_PLANKS, false);
    static final Theme ANIMAL = new Theme(Material.GRASS_BLOCK, Material.OAK_FENCE, 2, Material.HAY_BLOCK, Material.SPRUCE_PLANKS, false);
    static final Theme MOB = new Theme(Material.DEEPSLATE_BRICKS, Material.DEEPSLATE_BRICKS, 4, Material.BLACKSTONE, Material.POLISHED_DEEPSLATE, false);
    static final Theme ORE = new Theme(Material.STONE_BRICKS, Material.STONE_BRICKS, 3, Material.SEA_LANTERN, Material.SMOOTH_STONE, false);
    static final Theme STONE = new Theme(Material.STONE, Material.COBBLESTONE, 2, Material.ANDESITE, Material.STONE_BRICKS, false);
    static final Theme WATER = new Theme(Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE, 3, Material.SEA_LANTERN, Material.PRISMARINE, false);
    static final Theme NETHER = new Theme(Material.NETHERRACK, Material.NETHER_BRICKS, 4, Material.GLOWSTONE, Material.RED_NETHER_BRICKS, false);
    static final Theme VILLAGE = new Theme(Material.OAK_PLANKS, Material.STONE_BRICKS, 3, Material.SEA_LANTERN, Material.STONE_BRICKS, false);
    static final Theme MISC = new Theme(Material.SMOOTH_STONE, Material.QUARTZ_BLOCK, 3, Material.SEA_LANTERN, Material.QUARTZ_BRICKS, false);

    // ---- Hilfen ----
    private static Farm p(String n, Material icon, Material prod) {
        return new Farm(n, icon, prod, 1, 4, null, null, Farm.NORMAL, null);
    }

    private static Farm rare(String n, Material icon, Material prod) {
        return new Farm(n, icon, prod, 3, 1, null, null, Farm.NORMAL, null);
    }

    private static Farm animal(String n, Material icon, Material prod, EntityType e) {
        return new Farm(n, icon, prod, 1, 3, e, null, Farm.NORMAL, null);
    }

    private static Farm trap(String n, Material icon, Material prod, EntityType mob) {
        return new Farm(n, icon, prod, 0, 0, null, null, Farm.TRAP, mob);
    }

    private static Farm hall(String n, Material station, Villager.Profession job) {
        return new Farm(n, station, station, 0, 0, null, job, Farm.HALL, null);
    }

    static final List<Category> CATEGORIES = List.of(
        new Category("Getreide & Gemüse", Material.WHEAT, CROP, List.of(
            new Farm("Weizen-Farm mit Villager", Material.WHEAT, Material.WHEAT, 1, 6, null, Villager.Profession.FARMER, Farm.VILLAGER_CROP, null),
            p("Karotten-Farm", Material.CARROT, Material.CARROT),
            p("Kartoffel-Farm", Material.POTATO, Material.POTATO),
            p("Rote-Bete-Farm", Material.BEETROOT, Material.BEETROOT),
            p("Kürbis-Farm", Material.PUMPKIN, Material.PUMPKIN),
            p("Melonen-Farm", Material.MELON_SLICE, Material.MELON_SLICE),
            p("Süßbeeren-Farm", Material.SWEET_BERRIES, Material.SWEET_BERRIES),
            p("Leuchtbeeren-Farm", Material.GLOW_BERRIES, Material.GLOW_BERRIES),
            p("Kakao-Farm", Material.COCOA_BEANS, Material.COCOA_BEANS),
            p("Netherwarzen-Farm", Material.NETHER_WART, Material.NETHER_WART)
        )),
        new Category("Holz & Pflanzen", Material.OAK_SAPLING, TREE, List.of(
            p("Eichenholz-Farm", Material.OAK_LOG, Material.OAK_LOG),
            p("Birkenholz-Farm", Material.BIRCH_LOG, Material.BIRCH_LOG),
            p("Fichtenholz-Farm", Material.SPRUCE_LOG, Material.SPRUCE_LOG),
            p("Dschungelholz-Farm", Material.JUNGLE_LOG, Material.JUNGLE_LOG),
            p("Akazienholz-Farm", Material.ACACIA_LOG, Material.ACACIA_LOG),
            p("Schwarzeichen-Farm", Material.DARK_OAK_LOG, Material.DARK_OAK_LOG),
            p("Mangroven-Farm", Material.MANGROVE_LOG, Material.MANGROVE_LOG),
            p("Kirschholz-Farm", Material.CHERRY_LOG, Material.CHERRY_LOG),
            p("Bambus-Farm", Material.BAMBOO, Material.BAMBOO),
            p("Zuckerrohr-Farm", Material.SUGAR_CANE, Material.SUGAR_CANE)
        )),
        new Category("Tiere", Material.LEAD, ANIMAL, List.of(
            trap("Kuh-Farm (Spawner)", Material.BEEF, Material.BEEF, EntityType.COW),
            trap("Schweine-Farm (Spawner)", Material.PORKCHOP, Material.PORKCHOP, EntityType.PIG),
            trap("Schaf-Farm (Spawner)", Material.MUTTON, Material.MUTTON, EntityType.SHEEP),
            trap("Hühner-Farm (Spawner)", Material.CHICKEN, Material.CHICKEN, EntityType.CHICKEN),
            trap("Kaninchen-Farm (Spawner)", Material.RABBIT, Material.RABBIT, EntityType.RABBIT),
            animal("Eier-Farm", Material.EGG, Material.EGG, EntityType.CHICKEN),
            trap("Leder-Farm (Spawner)", Material.LEATHER, Material.LEATHER, EntityType.COW),
            trap("Woll-Farm (Spawner)", Material.WHITE_WOOL, Material.WHITE_WOOL, EntityType.SHEEP),
            trap("Federn-Farm (Spawner)", Material.FEATHER, Material.FEATHER, EntityType.CHICKEN),
            animal("Honig-Farm", Material.HONEYCOMB, Material.HONEYCOMB, EntityType.BEE)
        )),
        new Category("Monster", Material.CREEPER_HEAD, MOB, List.of(
            trap("Creeper-Farm", Material.GUNPOWDER, Material.GUNPOWDER, EntityType.CREEPER),
            trap("Skelett-Farm", Material.BONE, Material.BONE, EntityType.SKELETON),
            trap("Zombie-Farm", Material.ROTTEN_FLESH, Material.ROTTEN_FLESH, EntityType.ZOMBIE),
            trap("Spinnen-Farm", Material.STRING, Material.STRING, EntityType.SPIDER),
            trap("Enderman-Farm", Material.ENDER_PEARL, Material.ENDER_PEARL, EntityType.ENDERMAN),
            trap("Hexen-Farm", Material.REDSTONE, Material.REDSTONE, EntityType.WITCH),
            trap("Magmawürfel-Farm", Material.MAGMA_CREAM, Material.MAGMA_CREAM, EntityType.MAGMA_CUBE),
            trap("Wither-Skelett-Farm", Material.COAL, Material.COAL, EntityType.WITHER_SKELETON),
            new Farm("Knochenmehl-Farm (automatisch)", Material.BONE_MEAL, Material.BONE_MEAL, 0, 0, null, null, Farm.BONEMEAL, EntityType.SKELETON),
            trap("Blaze-Farm", Material.BLAZE_ROD, Material.BLAZE_ROD, EntityType.BLAZE)
        )),
        new Category("Erze & Metalle", Material.IRON_INGOT, ORE, List.of(
            trap("Große Eisenfarm (Golem-Spawner)", Material.IRON_INGOT, Material.IRON_INGOT, EntityType.IRON_GOLEM),
            trap("Gold-Farm (Piglin-Spawner)", Material.GOLD_INGOT, Material.GOLD_NUGGET, EntityType.ZOMBIFIED_PIGLIN),
            p("Kupfer-Farm", Material.COPPER_INGOT, Material.COPPER_INGOT),
            p("Kohle-Farm", Material.COAL, Material.COAL),
            p("Redstone-Farm", Material.REDSTONE, Material.REDSTONE),
            p("Lapislazuli-Farm", Material.LAPIS_LAZULI, Material.LAPIS_LAZULI),
            rare("Diamant-Farm", Material.DIAMOND, Material.DIAMOND),
            rare("Smaragd-Farm", Material.EMERALD, Material.EMERALD),
            p("Quarz-Farm", Material.QUARTZ, Material.QUARTZ),
            rare("Obsidian-Farm", Material.OBSIDIAN, Material.OBSIDIAN)
        )),
        new Category("Steine & Blöcke", Material.COBBLESTONE, STONE, List.of(
            p("Bruchstein-Farm", Material.COBBLESTONE, Material.COBBLESTONE),
            p("Stein-Farm", Material.STONE, Material.STONE),
            p("Basalt-Farm", Material.BASALT, Material.BASALT),
            p("Tuff-Farm", Material.TUFF, Material.TUFF),
            p("Sand-Farm", Material.SAND, Material.SAND),
            p("Kies-Farm", Material.GRAVEL, Material.GRAVEL),
            p("Lehm-Farm", Material.CLAY_BALL, Material.CLAY_BALL),
            p("Schneeball-Farm", Material.SNOWBALL, Material.SNOWBALL),
            p("Glowstone-Farm", Material.GLOWSTONE_DUST, Material.GLOWSTONE_DUST),
            p("Netherrack-Farm", Material.NETHERRACK, Material.NETHERRACK)
        )),
        new Category("Wasser", Material.COD, WATER, List.of(
            p("Kabeljau-Farm", Material.COD, Material.COD),
            p("Lachs-Farm", Material.SALMON, Material.SALMON),
            p("Tropenfisch-Farm", Material.TROPICAL_FISH, Material.TROPICAL_FISH),
            p("Kugelfisch-Farm", Material.PUFFERFISH, Material.PUFFERFISH),
            p("Seetang-Farm", Material.KELP, Material.KELP),
            p("Tintenbeutel-Farm", Material.INK_SAC, Material.INK_SAC),
            p("Leuchttinten-Farm", Material.GLOW_INK_SAC, Material.GLOW_INK_SAC),
            p("Prismarin-Farm", Material.PRISMARINE_SHARD, Material.PRISMARINE_SHARD),
            rare("Schwamm-Farm", Material.SPONGE, Material.SPONGE),
            rare("Nautilus-Farm", Material.NAUTILUS_SHELL, Material.NAUTILUS_SHELL)
        )),
        new Category("Pilze & Nether", Material.RED_MUSHROOM, NETHER, List.of(
            p("Brauner-Pilz-Farm", Material.BROWN_MUSHROOM, Material.BROWN_MUSHROOM),
            p("Roter-Pilz-Farm", Material.RED_MUSHROOM, Material.RED_MUSHROOM),
            p("Karmesin-Pilz-Farm", Material.CRIMSON_FUNGUS, Material.CRIMSON_FUNGUS),
            p("Wirrpilz-Farm", Material.WARPED_FUNGUS, Material.WARPED_FUNGUS),
            p("Leuchtpilz-Farm", Material.SHROOMLIGHT, Material.SHROOMLIGHT),
            p("Trauerranken-Farm", Material.WEEPING_VINES, Material.WEEPING_VINES),
            p("Zwirbelranken-Farm", Material.TWISTING_VINES, Material.TWISTING_VINES),
            p("Magmablock-Farm", Material.MAGMA_BLOCK, Material.MAGMA_BLOCK),
            p("Seelensand-Farm", Material.SOUL_SAND, Material.SOUL_SAND),
            p("Wartenblock-Farm", Material.NETHER_WART_BLOCK, Material.NETHER_WART_BLOCK)
        )),
        new Category("Villager-Hallen", Material.EMERALD, VILLAGE, List.of(
            hall("Trading Hall: Bibliothekar", Material.LECTERN, Villager.Profession.LIBRARIAN),
            hall("Trading Hall: Waffenschmied", Material.GRINDSTONE, Villager.Profession.WEAPONSMITH),
            hall("Trading Hall: Rüstungsschmied", Material.BLAST_FURNACE, Villager.Profession.ARMORER),
            hall("Trading Hall: Werkzeugschmied", Material.SMITHING_TABLE, Villager.Profession.TOOLSMITH),
            hall("Trading Hall: Geistlicher", Material.BREWING_STAND, Villager.Profession.CLERIC),
            hall("Trading Hall: Bauer", Material.COMPOSTER, Villager.Profession.FARMER),
            hall("Trading Hall: Metzger", Material.SMOKER, Villager.Profession.BUTCHER),
            hall("Trading Hall: Gerber", Material.CAULDRON, Villager.Profession.LEATHERWORKER),
            hall("Trading Hall: Steinmetz", Material.STONECUTTER, Villager.Profession.MASON),
            hall("Trading Hall: Fischer", Material.BARREL, Villager.Profession.FISHERMAN),
            new Farm("Große Trading Hall – alle Berufe", Material.EMERALD_BLOCK, Material.EMERALD, 0, 0,
                null, null, Farm.HALL_ALL, null)
        )),
        new Category("Zusatzfarmen", Material.CHEST, MISC, List.of(
            p("Papier-Farm", Material.PAPER, Material.PAPER),
            p("Buch-Farm", Material.BOOK, Material.BOOK),
            p("Glas-Farm", Material.GLASS, Material.GLASS),
            p("Ziegel-Farm", Material.BRICK, Material.BRICK),
            p("Blumentopf-Farm", Material.FLOWER_POT, Material.FLOWER_POT),
            p("Faden-Farm", Material.STRING, Material.STRING),
            p("Spinnenauge-Farm", Material.SPIDER_EYE, Material.SPIDER_EYE),
            p("Schleimball-Farm", Material.SLIME_BALL, Material.SLIME_BALL),
            p("Prismarin-Kristall-Farm", Material.PRISMARINE_CRYSTALS, Material.PRISMARINE_CRYSTALS),
            p("Enderauge-Farm", Material.ENDER_EYE, Material.ENDER_EYE),
            p("Feuerwerksstern-Farm", Material.FIREWORK_STAR, Material.FIREWORK_STAR),
            p("Papier-und-Raketen-Zutaten-Farm", Material.FIREWORK_ROCKET, Material.PAPER),
            p("Seil-Farm (Leine)", Material.LEAD, Material.LEAD),
            p("Namensschild-Farm", Material.NAME_TAG, Material.NAME_TAG),
            p("Schild-Farm", Material.OAK_SIGN, Material.OAK_SIGN),
            p("Laternen-Farm", Material.LANTERN, Material.LANTERN),
            p("Fackel-Farm", Material.TORCH, Material.TORCH),
            p("Ziegelstein-Farm", Material.STONE_BRICKS, Material.STONE_BRICKS),
            p("Quarzblock-Farm", Material.QUARTZ_BLOCK, Material.QUARTZ_BLOCK),
            p("Knochen-Farm (Materialausgabe)", Material.BONE, Material.BONE)
        )),
        new Category("Spezial", Material.EXPERIENCE_BOTTLE, MISC, List.of(
            p("Erfahrungs-Farm (Fläschchen)", Material.EXPERIENCE_BOTTLE, Material.EXPERIENCE_BOTTLE),
            p("Pfeil-Farm", Material.ARROW, Material.ARROW),
            p("Feuerwerk-Farm", Material.FIREWORK_ROCKET, Material.FIREWORK_ROCKET),
            rare("TNT-Farm", Material.TNT, Material.TNT),
            p("Stock-Farm", Material.STICK, Material.STICK),
            p("Honigflaschen-Farm", Material.HONEY_BOTTLE, Material.HONEY_BOTTLE),
            p("Trockentang-Farm", Material.DRIED_KELP, Material.DRIED_KELP),
            p("Brot-Farm", Material.BREAD, Material.BREAD),
            p("Goldkarotten-Farm", Material.GOLDEN_CARROT, Material.GOLDEN_CARROT),
            p("Steak-Farm (gegart)", Material.COOKED_BEEF, Material.COOKED_BEEF)
        ))
    );

    static int totalFarms() {
        return CATEGORIES.stream().mapToInt(c -> c.farms().size()).sum();
    }
}
