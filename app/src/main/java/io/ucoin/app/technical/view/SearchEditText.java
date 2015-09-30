package io.ucoin.app.technical.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

public class SearchEditText extends EditText {

    private boolean mMagnifyingGlassShown = true;
    private Drawable mMagnifyingGlass;

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMagnifyingGlass = getCompoundDrawables()[0];
    }

    /**
     * Conditionally shows a magnifying glass icon on the left side of the text field
     * when the text it empty.
     */
    @Override
    public boolean onPreDraw() {
        boolean emptyText = TextUtils.isEmpty(getText());
        if (mMagnifyingGlassShown != emptyText) {
            mMagnifyingGlassShown = emptyText;
            if (mMagnifyingGlassShown) {
                setCompoundDrawables(mMagnifyingGlass, null, null, null);
            } else {
                setCompoundDrawables(null, null, null, null);
            }
            return false;
        }
        return super.onPreDraw();
    }
}