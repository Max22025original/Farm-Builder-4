package de.farm;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FarmBuilder extends JavaPlugin implements Listener {

    private static final int[] SIZES = {15, 31, 51, 75};
    private static final String[] SIZE_NAMES = {"Klein", "Mittel", "Mega", "Riesig"};
    private static final int HEIGHT = 6;
    private static final Material[] SIZE_ICONS = {
        Material.LIME_CONCRETE, Material.YELLOW_CONCRETE, Material.ORANGE_CONCRETE, Material.RED_CONCRETE
    };
    private static final String PERM = "farmbuilder.use";

    private final Map<UUID, Preview> previews = new HashMap<>();
    private final List<Entry> registry = new ArrayList<>();
    private final Set<String> traps = new HashSet<>();
    private int cycle = 0;

    @Override
    public void onEnable() {
        getConfig().addDefault("cycle-seconds", 10);
        getConfig().addDefault("speed-multiplier", 1);
        getConfig().addDefault("max-villagers", 100);
        getConfig().addDefault("max-animals", 24);
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadEntries();
        traps.addAll(getConfig().getStringList("traps"));
        getServer().getPluginManager().registerEvents(this, this);
        long ticks = Math.max(1, getConfig().getInt("cycle-seconds", 10)) * 20L;
        Bukkit.getScheduler().runTaskTimer(this, this::produce, ticks, ticks);
        getLogger().info("FarmBuilder geladen: " + Farms.totalFarms() + " Farmen in " + Farms.CATEGORIES.size()
            + " Kategorien, " + registry.size() + " Produktionskisten.");
    }

    private void loadEntries() {
        registry.clear();
        for (String s : getConfig().getStringList("entries")) {
            try {
                registry.add(Entry.parse(s));
            } catch (RuntimeException ex) {
                getLogger().warning("Ungültiger Eintrag übersprungen: " + s);
            }
        }
    }

    private void saveEntries() {
        List<String> list = new ArrayList<>();
        for (Entry e : registry) list.add(e.serialize());
        getConfig().set("entries", list);
        getConfig().set("traps", new ArrayList<>(traps));
        saveConfig();
    }

    private static String key(Location l) {
        return l.getWorld().getName() + ";" + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ();
    }

    /** Spawner-Fallen: gespawnte Mobs nach kurzer Zeit töten, damit ihre Drops ins Wasser fallen und nichts liegen bleibt. */
    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        CreatureSpawner s = e.getSpawner();
        if (s == null || !traps.contains(key(s.getLocation()))) return;
        Entity ent = e.getEntity();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (ent.isValid() && ent instanceof LivingEntity le) le.damage(100000.0);
        }, 30L);
    }

    /** Füllt alle Produktionskisten (nur in geladenen Chunks). */
    private void produce() {
        cycle++;
        int mult = Math.max(1, getConfig().getInt("speed-multiplier", 1));
        List<Entry> remove = new ArrayList<>();
        for (Entry e : registry) {
            if (e.every() <= 0 || cycle % e.every() != 0) continue;
            World w = Bukkit.getWorld(e.world());
            if (w == null || !w.isChunkLoaded(e.x() >> 4, e.z() >> 4)) continue;
            Block bl = w.getBlockAt(e.x(), e.y(), e.z());
            if (bl.getType() != Material.CHEST) {
                remove.add(e);
                continue;
            }
            Chest c = (Chest) bl.getState();
            c.getBlockInventory().addItem(new ItemStack(e.product(), Math.max(1, e.amount() * mult)));
        }
        if (!remove.isEmpty()) {
            registry.removeAll(remove);
            saveEntries();
        }
    }

    @Override
    public void onDisable() {
        for (Preview p : previews.values()) p.task.cancel();
        previews.clear();
    }

    // ------------------------------------------------------------------ Befehl

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur im Spiel nutzbar.");
            return true;
        }
        if (!p.isOp() && !p.hasPermission(PERM)) {
            p.sendMessage(Component.text("Nur Admins dürfen /farm benutzen.", NamedTextColor.RED));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
            cancelPreview(p, true);
            return true;
        }
        openCategories(p);
        return true;
    }

    // ------------------------------------------------------------------ Menüs

    private static final class Menu implements InventoryHolder {
        final int level; // 0 = Kategorien, 1 = Farmen, 2 = Größen
        final Category cat;
        final Farm farm;
        Inventory inv;

        Menu(int level, Category cat, Farm farm) {
            this.level = level;
            this.cat = cat;
            this.farm = farm;
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }
    }

    private static ItemStack item(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        if (lore.length > 0) {
            meta.lore(java.util.Arrays.stream(lore)
                .map(s -> Component.text(s, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .toList());
        }
        it.setItemMeta(meta);
        return it;
    }

    private static Inventory create(Menu m, int size, String title) {
        Inventory inv = Bukkit.createInventory(m, size, Component.text(title));
        m.inv = inv;
        return inv;
    }

    private void openCategories(Player p) {
        Menu m = new Menu(0, null, null);
        Inventory inv = create(m, 36, "Farm-Menü: Kategorie");
        List<Category> cats = Farms.CATEGORIES;
        for (int i = 0; i < cats.size() && i < 10; i++) {
            int slot = i < 5 ? 11 + i : 20 + (i - 5);
            Category c = cats.get(i);
            inv.setItem(slot, item(c.icon(), c.name(), c.farms().size() + " Farm(en)"));
        }
        p.openInventory(inv);
    }

    private void openFarms(Player p, Category c) {
        Menu m = new Menu(1, c, null);
        Inventory inv = create(m, 36, "Farm-Menü: " + c.name());
        for (int i = 0; i < c.farms().size() && i < 27; i++) {
            Farm f = c.farms().get(i);
            String info = switch (f.kind()) {
                case Farm.HALL -> "Echt: Villager mit Beruf zum Handeln";
                case Farm.HALL_ALL -> "Große Halle mit allen 13 handelnden Berufen und festgelegten Startangeboten";
                case Farm.TRAP -> "Echt: Spawner-Falle (Wasser, Trichter, Kiste)";
                case Farm.BONEMEAL -> "Echt: Skelett-Falle + Crafter + Trichter-Uhr (Redstone)";
                default -> "Plugin-gesteuert: Kisten füllen sich automatisch";
            };
            inv.setItem(i, item(f.icon(), f.name(), info, "Klick: Größe wählen"));
        }
        inv.setItem(35, item(Material.BARRIER, "Zurück"));
        p.openInventory(inv);
    }

    private void openSizes(Player p, Category c, Farm f) {
        Menu m = new Menu(2, c, f);
        Inventory inv = create(m, 27, f.name() + ": Größe");
        int[] slots = {10, 12, 14, 16};
        for (int i = 0; i < 4; i++) {
            inv.setItem(slots[i], item(SIZE_ICONS[i], SIZE_NAMES[i], SIZES[i] + " x " + SIZES[i] + " Blöcke"));
        }
        inv.setItem(26, item(Material.BARRIER, "Zurück"));
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof Menu m)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!p.isOp() && !p.hasPermission(PERM)) {
            p.closeInventory();
            return;
        }
        if (e.getClickedInventory() != e.getInventory()) return;
        int slot = e.getRawSlot();
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (m.level) {
            case 0 -> {
                int idx = slot >= 20 ? 5 + (slot - 20) : slot - 11;
                if (idx >= 0 && idx < Farms.CATEGORIES.size()) openFarms(p, Farms.CATEGORIES.get(idx));
            }
            case 1 -> {
                if (slot == 35) {
                    openCategories(p);
                } else if (slot < m.cat.farms().size()) {
                    openSizes(p, m.cat, m.cat.farms().get(slot));
                }
            }
            case 2 -> {
                if (slot == 26) {
                    openFarms(p, m.cat);
                    return;
                }
                int idx = switch (slot) {
                    case 10 -> 0;
                    case 12 -> 1;
                    case 14 -> 2;
                    case 16 -> 3;
                    default -> -1;
                };
                if (idx >= 0) startPreview(p, m.farm, m.cat.theme(), SIZES[idx], SIZE_NAMES[idx]);
            }
            default -> { }
        }
    }

    // ------------------------------------------------------------------ Vorschau

    private static final class Preview {
        final Farm farm;
        final Theme theme;
        final int n;
        final String sizeName;
        BukkitTask task;
        Location origin;

        Preview(Farm farm, Theme theme, int n, String sizeName) {
            this.farm = farm;
            this.theme = theme;
            this.n = n;
            this.sizeName = sizeName;
        }
    }

    private void startPreview(Player p, Farm f, Theme theme, int n, String sizeName) {
        cancelPreview(p, false);
        p.closeInventory();
        Preview pv = new Preview(f, theme, n, sizeName);
        pv.task = Bukkit.getScheduler().runTaskTimer(this, () -> tickPreview(p, pv), 0L, 5L);
        previews.put(p.getUniqueId(), pv);
        p.sendMessage(Component.text("Vorschau: " + f.name() + " (" + sizeName + ", " + n + "x" + n + "). "
            + "Bewege dich und schau auf die Stelle. Rechtsklick = platzieren, Linksklick oder /farm cancel = abbrechen.",
            NamedTextColor.GREEN));
    }

    private void cancelPreview(Player p, boolean message) {
        Preview pv = previews.remove(p.getUniqueId());
        if (pv != null) {
            pv.task.cancel();
            if (message) p.sendMessage(Component.text("Vorschau abgebrochen.", NamedTextColor.YELLOW));
        } else if (message) {
            p.sendMessage(Component.text("Keine aktive Vorschau.", NamedTextColor.YELLOW));
        }
    }

    private Location computeOrigin(Player p, Preview pv) {
        Block t = p.getTargetBlockExact(50);
        if (t != null) return t.getLocation();
        Location l = p.getLocation();
        Vector d = l.getDirection().setY(0);
        if (d.lengthSquared() < 1.0E-6) d = new Vector(0, 0, 1);
        d.normalize().multiply(pv.n / 2.0 + 3);
        return l.clone().add(d).add(0, -1, 0).getBlock().getLocation();
    }

    private void tickPreview(Player p, Preview pv) {
        if (!p.isOnline()) {
            cancelPreview(p, false);
            return;
        }
        Location o = computeOrigin(p, pv);
        pv.origin = o;
        int h = pv.n / 2;
        double x0 = o.getBlockX() - h;
        double x1 = o.getBlockX() + h + 1;
        double z0 = o.getBlockZ() - h;
        double z1 = o.getBlockZ() + h + 1;
        double y = o.getBlockY() + 1.05;
        for (int i = 0; i <= pv.n; i++) {
            spawn(p, x0 + i, y, z0);
            spawn(p, x0 + i, y, z1);
            spawn(p, x0, y, z0 + i);
            spawn(p, x1, y, z0 + i);
        }
        for (int dy = 0; dy <= HEIGHT; dy++) {
            spawn(p, x0, y + dy, z0);
            spawn(p, x1, y + dy, z0);
            spawn(p, x0, y + dy, z1);
            spawn(p, x1, y + dy, z1);
        }
        p.sendActionBar(Component.text(pv.farm.name() + " – " + pv.sizeName + " (" + pv.n + "x" + pv.n
            + ") | Rechtsklick: platzieren | Linksklick: abbrechen", NamedTextColor.AQUA));
    }

    private static void spawn(Player p, double x, double y, double z) {
        p.spawnParticle(Particle.END_ROD, x, y, z, 1, 0, 0, 0, 0);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Preview pv = previews.get(p.getUniqueId());
        if (pv == null || e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() == Action.PHYSICAL) return;
        e.setCancelled(true);
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            place(p, pv);
        } else {
            cancelPreview(p, true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cancelPreview(e.getPlayer(), false);
    }

    // ------------------------------------------------------------------ Platzieren

    private void place(Player p, Preview pv) {
        if (!p.isOp() && !p.hasPermission(PERM)) {
            cancelPreview(p, false);
            return;
        }
        Location o = pv.origin != null ? pv.origin : computeOrigin(p, pv);
        Farm f = pv.farm;
        cancelPreview(p, false);

        Builder b = new Builder(o, pv.n);
        int maxV = getConfig().getInt("max-villagers", 100);
        int maxA = getConfig().getInt("max-animals", 24);
        if (f.kind() == Farm.TRAP || f.kind() == Farm.BONEMEAL) {
            traps.addAll(Constructions.trap(b, f, pv.theme));
            saveEntries();
        } else {
            // Platz freiräumen (Boden-Ebene y=0 ist der angeklickte Block)
            b.fill(-b.h, 1, -b.h, b.h, HEIGHT, b.h, Material.AIR);
            if (f.kind() == Farm.HALL || f.kind() == Farm.HALL_ALL) {
                Constructions.hall(b, f, maxV);
            } else {
                registry.addAll(Constructions.production(b, f, pv.theme, maxV, maxA));
                saveEntries();
            }
        }

        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        p.sendMessage(Component.text(f.name() + " (" + pv.n + "x" + pv.n + ") wurde platziert.", NamedTextColor.GREEN));
        if (f.kind() == Farm.TRAP || f.kind() == Farm.BONEMEAL) {
            p.sendMessage(Component.text("Spawner-Falle: Items landen in der Kiste ganz unten am Turm. Sie läuft, solange du in der Nähe (48 Blöcke) bist.",
                NamedTextColor.GRAY));
        } else if (f.kind() != Farm.HALL && f.kind() != Farm.HALL_ALL) {
            p.sendMessage(Component.text("Die Kisten füllen sich automatisch (Plugin-gesteuert, nur in geladenen Chunks).",
                NamedTextColor.GRAY));
        }
    }
}
