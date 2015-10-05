package io.ucoin.app.technical.view;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * @author Cyril Mottier
 */
public class NotifyingScrollView extends ScrollView {

    /**
     * @author Cyril Mottier
     */
    public interface OnScrollChangedListener {
        /**
         * This is called in response to an internal scroll in this view (i.e., the
         * view scrolled its own contents). This is typically as a result of
         * {@link #scrollBy(int, int)} or {@link #scrollTo(int, int)} having been
         * called.
         *
         * @param l Current horizontal scroll origin.
         * @param t Current vertical scroll origin.
         * @param oldl Previous horizontal scroll origin.
         * @param oldt Previous vertical scroll origin.
         */
        void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt);
    }

    private OnScrollChangedListener mOnScrollChangedListener;

    public NotifyingScrollView(Context context) {
        super(context);
    }

    public NotifyingScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotifyingScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }

}
