package com.extendedclip.papi.expansion.pinger;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PingerExpansion extends PlaceholderExpansion {
    @Override
    public String getIdentifier() {
        return "pinger";
    }

    @Override
    public String getPlugin() {
        return null;
    }

    @Override
    public String getAuthor() {
        return "clip";
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String s) {



        return null;
    }
}
