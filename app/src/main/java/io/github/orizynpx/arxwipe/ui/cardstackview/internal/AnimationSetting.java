package io.github.orizynpx.arxwipe.ui.cardstackview.internal;

import android.view.animation.Interpolator;

import io.github.orizynpx.arxwipe.ui.cardstackview.Direction;

public interface AnimationSetting {
    Direction getDirection();
    int getDuration();
    Interpolator getInterpolator();
}
