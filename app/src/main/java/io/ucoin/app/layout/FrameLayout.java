package io.ucoin.app.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Property;

import io.ucoin.app.util.FloatProperty;

public class FrameLayout extends android.widget.FrameLayout {

    public static final Property<FrameLayout, Float> XRATIO = new FloatProperty<FrameLayout>("xratio") {
        @Override
        public void setValue(FrameLayout object, float value) {
            object.setXRatio(value);
        }

        @Override
        public Float get(FrameLayout object) {
            return object.getXRatio();
        }
    };

    public static final Property<FrameLayout, Float> YRATIO = new FloatProperty<FrameLayout>("yratio") {
        @Override
        public void setValue(FrameLayout object, float value) {
            object.setYRatio(value);
        }

        @Override
        public Float get(FrameLayout object) {
            return object.getYRatio();
        }
    };

    public FrameLayout(Context context) {
        super(context);
    }

    public FrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public FrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Float getXRatio() {
        return getX() / getWidth();
    }

    public void setXRatio(Float ratio) {
        final int width = getWidth();
        setX(ratio * width);
    }

    public Float getYRatio() {
        return getY() / getWidth();
    }

    public void setYRatio(Float ratio) {
        final int width = getWidth();
        setY(ratio * width);
    }
}
