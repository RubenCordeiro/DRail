package pt.up.fe.cmov.drail;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RemoteViews;

public class TicketImageView extends ImageView {
    private static final int[] STATE_USED = { R.attr.state_used };
    private boolean mIsUsed = false;

    public void setUsed(boolean isUsed) {
        mIsUsed = isUsed;
    }

    public TicketImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (mIsUsed) {
            mergeDrawableStates(drawableState, STATE_USED);
        }

        return drawableState;
    }
}
