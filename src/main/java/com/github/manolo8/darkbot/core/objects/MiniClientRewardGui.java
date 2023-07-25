package com.github.manolo8.darkbot.core.objects;

import com.github.manolo8.darkbot.Main;

public class MiniClientRewardGui extends Gui {
    private final Main main;
    private long nextAction;

    public MiniClientRewardGui(Main main) {
        this.main = main;
    }

    @Override
    public void update() {
        super.update();
        if (nextAction > System.currentTimeMillis()) return;

        nextAction = System.currentTimeMillis() + 100;
        if (this.main.facadeManager.rewardProxy.isClaimable()) {
            claimReward();
        }
    }

    public boolean claimReward() {
        if (trySetShowing(true)) {
            click((int) (getWidth() / 2), 110);
        }
        return isAnimationDone();
    }

}
