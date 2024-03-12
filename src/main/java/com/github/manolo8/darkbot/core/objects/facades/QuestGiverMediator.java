package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;

import lombok.Getter;

import static com.github.manolo8.darkbot.Main.API;

public class QuestGiverMediator extends Updatable {

    @Getter
    private double positionX;

    @Getter
    private double positionY;

    @Getter
    private boolean active;

    @Override
    public void update() {
        if (address == 0) {
            return;
        }

        long data = API.readAtom(address + 0x30);

        this.positionX = API.readDouble(data + 0x2F8);
        this.positionY = API.readDouble(data + 0x300);

        this.active = API.readBoolean(data + 0x49C);
    }

}
