/*
 *
 *     Copyright (C) 2023 Calum (mineblock11), enjarai
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */

package com.mineblock11.skinshuffle.client.preset;

import com.mineblock11.skinshuffle.api.SkinAPIs;
import com.mineblock11.skinshuffle.client.cape.provider.CapeProvider;
import com.mineblock11.skinshuffle.client.cape.provider.CapeProviders;
import com.mineblock11.skinshuffle.client.skin.ResourceSkin;
import com.mineblock11.skinshuffle.client.skin.Skin;
import com.mineblock11.skinshuffle.client.skin.UrlSkin;
import com.mineblock11.skinshuffle.util.NetworkingUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SkinPreset {
    public static final Codec<SkinPreset> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Skin.CODEC.fieldOf("skin").forGetter(SkinPreset::getSkin),
                    Codec.STRING.fieldOf("name").forGetter(SkinPreset::getName),
                    CapeProvider.CODEC.optionalFieldOf("cape", CapeProviders.DEFAULT).forGetter(SkinPreset::getCapeProvider)
            ).apply(instance, SkinPreset::new));

    private String name;
    private Skin skin;
    private CapeProvider capeProvider;

    public SkinPreset(Skin skin) {
        this(skin, "Unnamed Preset", CapeProvider.DEFAULT);
    }

    public SkinPreset(Skin skin, String name, @Nullable CapeProvider cape) {
        this.skin = skin;
        this.name = name;
        this.capeProvider = cape;
    }

    public static SkinPreset generateDefaultPreset() {
        MinecraftClient client = MinecraftClient.getInstance();
        Session session = client.getSession();
        String name = session.getUsername();

        if (!NetworkingUtil.isLoggedIn()) {
            Identifier skinTexture = client.getSkinProvider().loadSkin(session.getProfile());
            Skin skin = new ResourceSkin(skinTexture, skinTexture.getPath().contains("/slim/") ? "slim" : "default");

            return new SkinPreset(skin, name, CapeProvider.DEFAULT);
        } else {
            var skinQueryResult = SkinAPIs.getPlayerSkinTexture(session.getUuid());

            if (skinQueryResult.usesDefaultSkin()) {
                Identifier skinTexture = client.getSkinProvider().loadSkin(session.getProfile());
                Skin skin = new ResourceSkin(skinTexture, skinTexture.getPath().contains("/slim/") ? "slim" : "default");

                return new SkinPreset(skin, name, CapeProvider.DEFAULT);
            }

            return new SkinPreset(new UrlSkin(skinQueryResult.skinURL(), skinQueryResult.modelType()), name, CapeProvider.DEFAULT);
        }
    }

    public CapeProvider getCapeProvider() {
        return capeProvider;
    }

    public void setCapeProvider(CapeProvider capeProvider) {
        this.capeProvider = capeProvider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Skin getSkin() {
        return skin;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public void copyFrom(SkinPreset other) {
        this.name = other.name;
        this.skin = other.skin;
        this.capeProvider = other.capeProvider;
    }

    public SkinPreset copy() {
        return new SkinPreset(this.skin, this.name, this.capeProvider);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SkinPreset that = (SkinPreset) o;

        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(skin, that.skin)) return false;
        return Objects.equals(capeProvider, that.capeProvider);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (skin != null ? skin.hashCode() : 0);
        result = 31 * result + (capeProvider != null ? capeProvider.hashCode() : 0);
        return result;
    }
}
