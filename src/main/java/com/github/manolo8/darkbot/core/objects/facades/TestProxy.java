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
        API.readMemory(address, 330);
        byte[] data = API.readMemory(address, 330);

        long otherData = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        System.out.println(otherData);
    }

}