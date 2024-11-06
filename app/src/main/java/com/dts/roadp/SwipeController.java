package com.dts.roadp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeController extends ItemTouchHelper.Callback {
    private static final float SWIPE_THRESHOLD = 0.5f;
    private static final float SWIPE_VELOCITY_THRESHOLD = 0.5f;
    private static final int SWIPE_RIGHT_COLOR = Color.parseColor("#E57373");
    private static final int SWIPE_LEFT_COLOR = Color.parseColor("#66BB6A");

    private Paint paint;
    private float currentSwipeX = 0;
    private SwipeControllerActions swipeActions;
    private boolean swipeBack = false;
    private ValueAnimator animator;

    public interface SwipeControllerActions {
        void onSwipeLeft();
        void onSwipeRight();
    }

    public SwipeController(SwipeControllerActions actions) {
        this.swipeActions = actions;
        this.paint = new Paint();
        setupAnimator();
    }

    private void setupAnimator() {
        animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(200);  // 200ms para la animación
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            currentSwipeX *= value;
        });
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.LEFT) {
            swipeActions.onSwipeLeft();
        } else if (direction == ItemTouchHelper.RIGHT) {
            swipeActions.onSwipeRight();
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            currentSwipeX = dX;
            View itemView = viewHolder.itemView;

            if (dX > 0) { // Swipe derecha
                paint.setColor(SWIPE_RIGHT_COLOR);
                c.drawRect(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + dX, itemView.getBottom(), paint);
            } else if (dX < 0) { // Swipe izquierda
                paint.setColor(SWIPE_LEFT_COLOR);
                c.drawRect(itemView.getRight() + dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom(), paint);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return SWIPE_THRESHOLD;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return SWIPE_VELOCITY_THRESHOLD;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 4;  // Hace que se necesite más velocidad para triggear el swipe
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (currentSwipeX != 0) {
            animator.start();
        }
    }
}