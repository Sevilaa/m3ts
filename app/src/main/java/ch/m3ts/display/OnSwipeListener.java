package ch.m3ts.display;

import android.content.Context;
import android.content.res.Resources;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import ch.m3ts.tabletennis.helper.Side;

public class OnSwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeLeft() {
        // do nothing
    }

    public void onSwipeRight() {
        // do nothing
    }

    public void onSwipeDown(Side swipeSide) {
        // do nothing
    }

    public void onSwipeUp(Side swipeSide) {
        // do nothing
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 150;
        private static final int SWIPE_VELOCITY_THRESHOLD = 150;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0) {
                    onSwipeRight();
                } else {
                    onSwipeLeft();
                }
                return true;
            }
            if (Math.abs(distanceY) > Math.abs(distanceX) && Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
                Side swipeSide = Side.RIGHT;
                if(e1.getX() < displayWidth/2) {
                    swipeSide = Side.LEFT;
                }
                if (distanceY > 0) {
                    onSwipeDown(swipeSide);
                } else {
                    onSwipeUp(swipeSide);
                }
                return true;
            }
            return false;
        }
    }
}