package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.utils.ByteUtils;

import eu.darkbot.api.API;

import static com.github.manolo8.darkbot.Main.API;

public class TestProxy extends Updatable implements API.Singleton {

    public TestProxy() {
    }

    @Override
    public void update() {
        if (address == 0)
            return;

        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;
        int dayClaimed = API.readMemoryInt(data + 0x40);

        long claimableAddr = API.readMemoryLong(data + 0x50) & ByteUtils.ATOM_MASK;
        boolean claimable = API.readBoolean(claimableAddr + 0x20);
    }

}