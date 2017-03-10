/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spin.wheelspinlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class RotaryWheelView extends View {

    private final Paint paintCircle;
    private final Paint paintDivider;
    private final Paint paintText;
    private final int RADIUS = 350;
    private Handler handler;
    private Map<Integer, Container> mapper = new HashMap<>();
    private String[] values;

    public void addData(String[] values) {
        this.values = values;
        MAX_ROTATION_ANGLE = (values.length - 3) * 60;
        int index = 6;
        for(int angle = 60; angle <= MAX_ROTATION_ANGLE; angle += 60) {
            ++index;
            for (int i = 0; i < 6; i++) {
                initData(index, i);
            }
            Log.d("map", "fill index "+index);
        }
        Log.d("map", "mapper size "+mapper.size());
    }


    private String text(double rotationAngle, int cell) {
        final int ceil = (int) Math.abs(Math.ceil(rotationAngle / 60));
        int index = ceil + 6;
        if (index > 6) {
            return mapper.get(index).arr[cell];
        } else {
            return values[cell];
        }
    }

    public void initData(int index, int cell) {
        Container container = mapper.get(index);
        if (index > 6) {
            Log.d("RA", " index " + index);
            if (container == null) {
                container = new Container();
                mapper.put(index, container);
            }
            switch (cell) {
                case 0:
                    if ((index - 4) % 6 == 0) {
                        container.arr[cell] = values[index - 6 + 2];
                    } else {
                        setText(index, cell, container);
                    }
                    break;
                case 1:
                    if ((index + 1) % 6 == 0) {
                        container.arr[cell] = values[index - 6 + 2];
                    } else {
                        setText(index, cell, container);
                    }
                    break;
                case 2:
                    if (index % 6 == 0) {
                        container.arr[cell] = values[index - 6 + 2];
                    } else {
                        setText(index, cell, container);
                    }
                    break;
                case 3:
                    if ((index - 1) % 6 == 0) {
                        container.arr[cell] = values[index - 6 + 2];
                    } else {
                        setText(index, cell, container);
                    }
                    break;
                case 4:
                    if (index - 8 == 0 || (index - 8) % 6 == 0) {
                        container.arr[cell] = values[index - 6 + 2];
                    } else {
                        setText(index, cell, container);
                    }
                    break;
                case 5:
                    if (index - 9 == 0 || (index - 9) % 6 == 0) {
                        container.arr[cell] = values[index - 6 + 2];
                    } else {
                        setText(index, cell, container);
                    }
                    break;
                default:
                    //Log.d("RA", " default : " + values[0]);
                    break;
            }

        }


    }

    private String setText(int index, int cell, Container container) {
        if (index > 7) {
            String[] arr = mapper.get(index - 1).arr;
            container.arr[cell] = arr[cell];
        } else {
            container.arr[cell] = values[cell];
        }
        return container.arr[cell];
    }

    private GestureDetector mDetector;
    private boolean mAllowRotating = true;

    private double mStartAngle;
    private double mRotationAngle;
    public static double MAX_ROTATION_ANGLE = 360;

    private int xPosition;        //Center X location of Radial Menu
    private int yPosition;          //Center Y location of Radial Menu

    public RotaryWheelView(Context context) {
        this(context, null);
    }

    /**
     * Constructor used when this widget is created from a layout file.
     */
    public RotaryWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        handler = new Handler(Looper.getMainLooper());
        mDetector = new GestureDetector(context, new MyGestureDetector());


        this.xPosition = (getResources().getDisplayMetrics().widthPixels) / 2;

        this.yPosition = 0;

        setBackgroundColor(Color.parseColor("#31bbc7"));

        paintDivider = new Paint();
        paintDivider.setColor(Color.WHITE);
        paintDivider.setAntiAlias(true);
        paintDivider.setStrokeWidth(1.5f);
        paintDivider.setStyle(Paint.Style.STROKE);

        paintCircle = new Paint();
        paintCircle.setColor(Color.WHITE);
        paintCircle.setAntiAlias(true);
        paintCircle.setStrokeWidth(0.5f);
        paintCircle.setStyle(Paint.Style.STROKE);

        paintText = new Paint();
        paintText.setColor(Color.WHITE);
        paintText.setAntiAlias(true);
        paintText.setTextSize(getResources().getDimensionPixelSize(R.dimen.ten));

        for (int i = 0; i < 6; i++) {
            quadrantInfos[i] = new QuadrantInfo(i);
        }

    }

    @Override
    public void onDraw(Canvas canvas) {

        canvas.rotate((float) mRotationAngle, xPosition, yPosition);

        canvas.drawCircle(xPosition, yPosition, RADIUS, paintCircle);
        for (int i = 0; i < 6; i++) {

            canvas.drawPath(quadrantInfos[i].getDividerPath(), paintDivider);
            String text = text(mRotationAngle, i);
            canvas.drawTextOnPath("c." + (i + 1) + " > " + text, quadrantInfos[i].getTextPath(), center + (paintText.measureText(text) / 2), 0, paintText);

        }

    }

    private QuadrantInfo[] quadrantInfos = new QuadrantInfo[6];
    private float center;

    class QuadrantInfo {
        public float x, y, x1, y1, x2, y2 = 0;
        public LinearGradient linearGradient;
        Path path1 = new Path();

        public QuadrantInfo(int i) {
            path1 = new Path();
            x = xPosition + RADIUS * (float) Math.cos(Math.toRadians(60 * (3 - i)));
            y = yPosition + RADIUS * (float) Math.sin(Math.toRadians(60 * (3 - i)));
            x1 = xPosition + RADIUS * (float) Math.cos(Math.toRadians(60 * (3 - (i + 1))));
            y1 = yPosition + RADIUS * (float) Math.sin(Math.toRadians(60 * (3 - (i + 1))));
            int theta = i == 1 ? 300 : i == 2 ? 240 : i == 3 ? 180 : i == 4 ? 120 : i == 5 ? 60 : 0;
            x2 = x + (RADIUS / 4) * (float) Math.cos(Math.toRadians(theta));
            y2 = y + (RADIUS / 4) * (float) Math.sin(Math.toRadians(theta));
            if (center == 0) {
                center = (x1 - x) / 2;
            }
            linearGradient = new LinearGradient(x, y, x2, y2, Color.WHITE, Color.LTGRAY, Shader.TileMode.CLAMP);
        }

        public Path getTextPath() {
            path1.reset();
            path1.moveTo(x, y);
            path1.lineTo(x1, y1);
            return path1;
        }

        public Path getDividerPath() {
            path1.reset();
            paintDivider.setShader(linearGradient);
            path1.moveTo(x, y);
            path1.lineTo(x2, y2);
            return path1;
        }
    }

    public int[] colors = new int[]{Color.RED, Color.GRAY, Color.GREEN, Color.CYAN, Color.YELLOW, Color.BLUE};

    FlingRunnable flingRunnable;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int state = e.getAction();
        int eventX = (int) e.getX();
        int eventY = (int) e.getY();
        switch (state) {
            case MotionEvent.ACTION_DOWN:
                mStartAngle = getAngle(eventX, eventY);
                mAllowRotating = false;
                break;
            case MotionEvent.ACTION_MOVE:
                double currentAngle = getAngle(eventX, eventY);
                mRotationAngle += mStartAngle - currentAngle;


                if (mRotationAngle >= 0 && mRotationAngle <= MAX_ROTATION_ANGLE) {
                    mStartAngle = currentAngle;
                }
                break;
            case MotionEvent.ACTION_UP:
                mAllowRotating = true;
                Log.d("snap", "Snap touch");
                if (mRotationAngle > 0 && mRotationAngle <= MAX_ROTATION_ANGLE) {
                    flingRunnable = new FlingRunnable(0, true);
                    handler.postDelayed(flingRunnable, 300);
                }
                //RotaryWheelView.this.postDelayed(new FlingRunnable(0, true), 1000);
                break;
        }

        //Log.d("snap", "mRotationAngle " + mRotationAngle + " sa " + mStartAngle);
        if (mRotationAngle >= 0 && mRotationAngle <= MAX_ROTATION_ANGLE) {
            mDetector.onTouchEvent(e);

            invalidate();
            return true;
        } else {
            if (mRotationAngle > MAX_ROTATION_ANGLE) {
                mRotationAngle = MAX_ROTATION_ANGLE;
            } else {
                mRotationAngle = 0;
                mStartAngle = 0;
            }
            return false;
        }
    }


    /**
     * @return The angle of the unit circle with the image view's center
     */
    private double getAngle(double xTouch, double yTouch) {
        double x = xTouch - (RADIUS * 2 / 2d);
        double y = (RADIUS / 2) - yTouch - ((RADIUS / 2) / 2d);
        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 3:
                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            default:
                return 0;
        }
    }

    /**
     * @return The selected quadrant.
     */
    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    private static final int SWIPE_MIN_DISTANCE = 0;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private class MyGestureDetector extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            handler.removeCallbacks(flingRunnable);
            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE) {
                RotaryWheelView.this.post(new FlingRunnable(-1 * (velocityX + velocityY), true));
            }
            return true;
        }
    }


    private static class Container {
        public String[] arr = new String[6];
    }

    /**
     * A {@link Runnable} for animating the the dialer's fling.
     */
    private class FlingRunnable implements Runnable {
        private float velocity;
        private double closestInteger = 0;
        private boolean isFling;

        public FlingRunnable(float velocity, boolean isFling) {
            this.velocity = velocity;
            this.isFling = isFling;
            if (isFling) {
                closestInteger = 0;
            }
        }

        @Override
        public void run() {
            if (Math.abs(velocity) > 5 && mAllowRotating) {
                mRotationAngle += velocity / 75;
                if (mRotationAngle < 0) {
                    mRotationAngle = 0;
                    velocity = 0;
                    invalidate();
                } else if (mRotationAngle > MAX_ROTATION_ANGLE) {
                    mRotationAngle = MAX_ROTATION_ANGLE;
                    velocity = 0;
                    invalidate();
                } else {
                    invalidate();
                    velocity /= 1.0666F;
                    // post this instance again
                    RotaryWheelView.this.post(this);
                }
                //Log.d("rotating ", "velocity "+velocity);
            } else if (isFling && mAllowRotating && mRotationAngle >= 0 && mRotationAngle <= MAX_ROTATION_ANGLE) {
                if (closestInteger == 0) {
                    closestInteger = closestInteger(mRotationAngle, 60);
                }
                Log.d("snap", "Difference closest "+closestInteger+ " real "+mRotationAngle);
                final double diff = closestInteger - mRotationAngle;
                if (Math.abs(diff) > 5) {
                    if (diff > 0) {
                        mRotationAngle += 5;
                        //closestInteger += -10;
                    } else {
                        mRotationAngle += -5;
                        //closestInteger += 10;
                    }
                    Log.d("snap", "Difference real "+mRotationAngle);
                    invalidate();
                    RotaryWheelView.this.post(this);
                } else {
                    Log.d("snap", "before  "+mRotationAngle);
                    mRotationAngle += diff;
                    Log.d("snap", "after  "+mRotationAngle);
                    invalidate();
                }
            }
        }
    }

    static double closestInteger(double a, double b) {
        double c1 = a - (a % b);
        double c2 = (a + b) - (a % b);
        if (a - c1 > c2 - a) {
            return c2;
        } else {
            return c1;
        }
    }

}