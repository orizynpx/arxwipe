package io.github.orizynpx.arxwipe.cardstackview.internal;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import io.github.orizynpx.arxwipe.cardstackview.Direction;
import io.github.orizynpx.arxwipe.cardstackview.RewindAnimationSetting;
import io.github.orizynpx.arxwipe.cardstackview.StackFrom;
import io.github.orizynpx.arxwipe.cardstackview.SwipeAnimationSetting;
import io.github.orizynpx.arxwipe.cardstackview.SwipeableMethod;

import java.util.List;

public class CardStackSetting {
    public StackFrom stackFrom = StackFrom.None;
    public int visibleCount = 3;
    public float translationInterval = 8.0f;
    public float scaleInterval = 0.95f; // 0.0f - 1.0f
    public float swipeThreshold = 0.3f; // 0.0f - 1.0f
    public float maxDegree = 20.0f;
    public List<Direction> directions = Direction.HORIZONTAL;
    public boolean canScrollHorizontal = true;
    public boolean canScrollVertical = true;
    public SwipeableMethod swipeableMethod = SwipeableMethod.AutomaticAndManual;
    public SwipeAnimationSetting swipeAnimationSetting = new SwipeAnimationSetting.Builder().build();
    public RewindAnimationSetting rewindAnimationSetting = new RewindAnimationSetting.Builder().build();
    public Interpolator overlayInterpolator = new LinearInterpolator();
}
