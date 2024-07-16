package com.ampznetwork.lagmod.fabric.config;

import blue.endless.jankson.Comment;
import com.ampznetwork.lagmod.fabric.LagMod$Fabric;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

import java.util.ArrayList;
import java.util.List;

@Modmenu(modId = LagMod$Fabric.AddonId)
@SuppressWarnings({ "FieldMayBeFinal", "unused" })
@Config(name = "lagmod-config", wrapperName = "Config")
public class ConfigModel {
    @Comment("The interal at which items should be cleaned up")
    public int          intervalMinutes = 15;
    @Comment("whitelist or blacklist")
    public ListMode     itemListMode    = ListMode.blacklist;
    @Comment("The item black- or whitelist")
    public List<String> itemList        = new ArrayList<>();
}
