package com.lewa.player.helper;

import android.animation.TypeEvaluator;

/**
 * Created by wuzixiu on 1/2/14.
 */
public class AnimationHelper {
    public static TypeEvaluator getIntEvaluator() {
        return new TypeEvaluator() {

            @Override
            public Object evaluate(float fraction, Object startValue, Object endValue) {
                try {
                    int startSize = (Integer) startValue;
                    int endSize = (Integer) endValue;

                    return startSize + Math.round(fraction * (endSize - startSize));
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }
}
