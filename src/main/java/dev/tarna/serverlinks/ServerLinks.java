package dev.tarna.serverlinks;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ServerLinks.ServerLink;
import org.bukkit.ServerLinks.Type;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public final class ServerLinks extends JavaPlugin {
    private List<ServerLink> links = new ArrayList<>();

    @Override
    public void onEnable() {
        long now = System.currentTimeMillis();

        saveDefaultConfig();
        new ServerLinksReload(this);
        loadLinks();

        getLogger().info("ServerLinks has been enabled in " + (System.currentTimeMillis() - now) + "ms");
    }

    @Override
    public void onDisable() {
        long now = System.currentTimeMillis();
        var serverLinks = Bukkit.getServerLinks();
        for (var link : links) {
            serverLinks.removeLink(link);
        }
        getLogger().info("ServerLinks has been disabled in " + (System.currentTimeMillis() - now) + "ms");
    }

    Type getKnowType(String key) {
        try {
            return Type.valueOf(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    void loadLinks() {

        var serverLinks = Bukkit.getServerLinks();

        // remove old links first from server state
        for (var link : links) {
            serverLinks.removeLink(link);
        }
        links.clear();

        var keys = getConfig().getKeys(false);
        for (String key : keys) {
            String value = getConfig().getString(key);
            try {
                ServerLink link;
                Type type = getKnowType(key);
                if (type != null) {
                    getLogger().info("Adding server link of type " + type + " with url: " + value);
                    link = serverLinks.addLink(type, new URI(value.replace("&", "§")));
                } else {
                    getLogger().info("Adding server link of '" + key + "' with url: " + value);
                    link = serverLinks.addLink(Component.empty().content(key), new URI(value.replace("&", "§")));
                }
                links.add(link);
            } catch (URISyntaxException e) {
                getLogger().warning("Invalid URI for key " + key + ": " + value);
            }
        }

        getLogger().info("Loaded " + keys.size() + " links");
    }
}
