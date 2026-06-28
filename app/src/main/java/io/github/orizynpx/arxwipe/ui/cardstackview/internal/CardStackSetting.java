package io.github.orizynpx.arxwipe.ui.cardstackview.internal;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.List;

import io.github.orizynpx.arxwipe.ui.cardstackview.Direction;
import io.github.orizynpx.arxwipe.ui.cardstackview.RewindAnimationSetting;
import io.github.orizynpx.arxwipe.ui.cardstackview.StackFrom;
import io.github.orizynpx.arxwipe.ui.cardstackview.SwipeAnimationSetting;
import io.github.orizynpx.arxwipe.ui.cardstackview.SwipeableMethod;

public class CardStackSetting {
    public StackFrom stackFrom = StackFrom.None;
    public int visibleCount = 3;
    public float translationInterval = 8.0f;
    public float scaleInterval = 0.95f; 
    public float swipeThreshold = 0.3f; 
    public float maxDegree = 20.0f;
    public List<Direction> directions = Direction.HORIZONTAL;
    public boolean canScrollHorizontal = true;
    public boolean canScrollVertical = true;
    public SwipeableMethod swipeableMethod = SwipeableMethod.AutomaticAndManual;
    public SwipeAnimationSetting swipeAnimationSetting = new SwipeAnimationSetting.Builder().build();
    public RewindAnimationSetting rewindAnimationSetting = new RewindAnimationSetting.Builder().build();
    public Interpolator overlayInterpolator = new LinearInterpolator();
}
