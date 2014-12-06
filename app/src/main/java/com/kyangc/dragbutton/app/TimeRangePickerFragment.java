package com.kyangc.dragbutton.app;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Calendar;

/**
 * @author chengkangyang on 12/5/14
 */
public class TimeRangePickerFragment extends Fragment implements View.OnTouchListener {

    LockableScrollView svContainer;
    Button btnMarkerTop, btnMarkerBottom;
    LinearLayout llLastLine, llTimeMarker;
    RelativeLayout rlRectangle;
    TextView tvStartTime, tvEndTime, tvLastTime;

    //How long to the scroll end
    double endScrollDistance;

    //ScrollView layouts
    double svLeft, svTop, svRight, svButtom;

    //Top marker layouts
    int topLeft, topRight, topTop, topBottom;

    //Bottom marker layouts
    int bottomLeft, bottomRight, bottomTop, bottomBottom;

    //Rectangle layouts
    int rectangleLeft, rectangleRight, rectangleTop, rectangleBottom, rectangleHeight;

    //Touch start of the top marker
    float topStartX = 0, topStartY = 0;

    //Touch start of the bottom marker
    float bottomStartX = 0, bottomStartY = 0;

    //Touch start of the rectangle
    float rectangleStartX = 0, rectangleStartY = 0;

    //Prepare runnable
    Runnable prepareWidgets = new Runnable() {
        @Override
        public void run() {
            initWidgets();
        }
    };

    private boolean isTimeRangeDraggable = false;
    private int scrollSpeed = 10;

    private OnTimeChangeListener onTimeChangeListener;

    public TimeRangePickerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initViews(inflater.inflate(R.layout.fragment_time_range_picker, container, false));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.btn_mark_top:
                onTopMarkerTouched(event);
                break;
            case R.id.btn_mark_bottom:
                onBottomMarkerTouched(event);
                break;
            case R.id.rl_rectangle:
                return onTimeRangeRectangleTouched(event);
            default:
                break;
        }
        return false;
    }

    private View initViews(View view) {

        //Main container
        svContainer = (LockableScrollView) view.findViewById(R.id.sv_container);

        //Last line
        llLastLine = (LinearLayout) view.findViewById(R.id.ll_last_line);

        //Time marker
        llTimeMarker = (LinearLayout) view.findViewById(R.id.ll_time_marker);

        //Time range marker
        rlRectangle = (RelativeLayout) view.findViewById(R.id.rl_rectangle);
        btnMarkerTop = (Button) view.findViewById(R.id.btn_mark_top);
        btnMarkerBottom = (Button) view.findViewById(R.id.btn_mark_bottom);

        //Text views
        tvStartTime = (TextView) view.findViewById(R.id.tv_start_time);
        tvEndTime = (TextView) view.findViewById(R.id.tv_end_time);
        tvLastTime = (TextView) view.findViewById(R.id.tv_last_time);

        //Set onTouch listener
        btnMarkerTop.setOnTouchListener(this);
        btnMarkerBottom.setOnTouchListener(this);
        if (isTimeRangeDraggable) rlRectangle.setOnTouchListener(this);

        //Prepare widget
        rlRectangle.post(prepareWidgets);

        //Return view
        return view;
    }

    private void initWidgets() {
        //Get the scrollView container's layouts
        int[] svLayouts = {0, 0};
        svContainer.getLocationOnScreen(svLayouts);
        svLeft = 0;
        svTop = 0;
        svRight = svContainer.getWidth();
        svButtom = llLastLine.getBottom();

        //Scroll distance to the end
        endScrollDistance = svButtom - svContainer.getHeight();

        //Get current time
        Calendar c = Calendar.getInstance();
        int currentMin = c.get(Calendar.MINUTE);
        int startHour = c.get(Calendar.HOUR_OF_DAY) % 24;
        int endHour = startHour + 1;

        //Set time line
        setTimeLine(llTimeMarker, startHour, currentMin);

        //Set initial marks
        setPoint(btnMarkerTop,
                (int) (svRight * 0.9 - btnMarkerTop.getHeight()),
                (int) (((double) startHour / 24) * (svButtom - btnMarkerTop.getHeight())));
        setPoint(btnMarkerBottom,
                (int) (svRight * 0.25),
                (int) (((double) endHour / 24) * (svButtom - btnMarkerBottom.getHeight())));

        //Set initial rectangle
        setRectangle(rlRectangle,
                (int) (((double) startHour / 24) * (svButtom - btnMarkerTop.getHeight()) + btnMarkerTop.getHeight() / 2),
                (int) (((double) endHour / 24) * (svButtom - btnMarkerBottom.getHeight()) + btnMarkerBottom.getHeight() / 2));

        //Scroll to the current time
        scrollToCurrent();

        //Set current time display
        tvStartTime.setText(startHour + ":00");
        tvEndTime.setText(endHour + ":00");
        tvLastTime.setText(1 + "小时");
    }

    private void setPoint(Button point, int left, int top) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) point.getLayoutParams();
        params.setMargins(left, top, (int) (svRight - left), (int) (svButtom - top - point.getHeight()));
        point.setLayoutParams(params);
    }

    private void setRectangle(RelativeLayout rectangle, int top, int bottom) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) rectangle.getLayoutParams();
        params.width = (int) (svRight * 0.85);
        params.height = bottom - top;
        params.setMargins((int) (svRight * 0.15), top, 0, (int) svButtom - top - params.height);
        rectangle.setLayoutParams(params);
    }

    private void setTimeLine(LinearLayout timeLine, int hour, int minute) {
        double dayInMinute = 24 * 60;
        double currentMunite = hour * 60 + minute;
        int marginTop = (int) (btnMarkerTop.getHeight() / 2
                + currentMunite / dayInMinute * (svButtom - btnMarkerTop.getHeight())
                - timeLine.getHeight() / 2);
        int marginBottom = (int) (svButtom - marginTop - timeLine.getHeight());
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) timeLine.getLayoutParams();
        params.setMargins(
                0,
                marginTop,
                0,
                marginBottom
        );
    }

    private void scrollToCurrent() {
        int rectangleHeight = (int) ((svButtom - btnMarkerBottom.getHeight()) / 24);
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int rectanglePosition = hour * rectangleHeight + rectangleHeight / 2;
        int screeHeight = svContainer.getHeight();
        int upperEndScrollPosition = screeHeight / 2;
        int lowerEndScrollPosition = (int) svButtom - screeHeight / 2;

        Log.d("see scroll",
                rectangleHeight + "," +
                        rectanglePosition + "," +
                        screeHeight + "," +
                        upperEndScrollPosition + "," +
                        lowerEndScrollPosition);

        if (rectanglePosition <= upperEndScrollPosition) {
            //No scroll
        } else if (rectanglePosition > upperEndScrollPosition && rectanglePosition < lowerEndScrollPosition) {
            //Scroll up
            svContainer.scrollBy(0, (rectanglePosition - upperEndScrollPosition));
        } else {
            //Scroll up to the most
            svContainer.scrollBy(0, ((int) svButtom - screeHeight));
        }
    }

    private void displayTimeRange() {
        int rectangleTop = rlRectangle.getTop() - btnMarkerTop.getHeight() / 2;
        int rectangleBottom = rlRectangle.getBottom() - btnMarkerBottom.getHeight() / 2;
        int totalHeight = (int) svButtom - btnMarkerBottom.getHeight();
        int totalMinutes = 24 * 60;
        int startMinutes = (int) (totalMinutes * (double) rectangleTop / (double) totalHeight);
        int endMinutes = (int) (totalMinutes * (double) rectangleBottom / (double) totalHeight);
        int startHour = startMinutes / 60;
        int endHour = endMinutes / 60;
        int startMinute = startMinutes % 60;
        int endMinute = endMinutes % 60;
        int lastMinutes = endMinutes - startMinutes;
        int lastHour = lastMinutes / 60;
        int lastMinute = lastMinutes % 60;

        //Call back
        if (onTimeChangeListener != null) onTimeChangeListener.onTimeRangeChanged(startMinutes, endMinutes);

        String startTime = startHour + ":" + (startMinute >= 10 ? startMinute : "0" + startMinute);
        String endTime = endHour + ":" + (endMinute >= 10 ? endMinute : "0" + endMinute);
        String lastTime;
        if (lastHour == 0) {
            lastTime = lastMinute + "分钟";
        } else {
            if (lastMinute == 0) {
                lastTime = lastHour + "小时";
            } else {
                lastTime = lastHour + "小时" + lastMinute + "分钟";
            }
        }

        tvStartTime.setText(startTime);
        tvEndTime.setText(endTime);
        tvLastTime.setText(lastTime);
    }

    private void seeCordinates(View v) {
        int[] cors = {0, 0};

        v.getLocationOnScreen(cors);
        Log.d("location on screen:", cors[0] + "," + cors[1]);
        Log.d("getx, gety", v.getX() + "," + v.getY());
        Log.d("getLeft, getRight, getTop, getbottom", v.getLeft() + "," + v.getRight() + "," + v.getTop() + "," + v.getBottom());
        Log.d("getHeight", v.getHeight() + "");
        Log.d("SV container layouts", svLeft + "," + svRight + "," + svTop + "," + svButtom);
    }

    private void onTopMarkerTouched(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                topStartX = event.getRawX();
                topStartY = event.getRawY();
                bottomTop = btnMarkerBottom.getTop();
                bottomBottom = btnMarkerBottom.getBottom();
                svContainer.setScrollingEnabled(false);
                break;

            case MotionEvent.ACTION_MOVE:
                int dy = (int) (event.getRawY() - topStartY);

                topLeft = btnMarkerTop.getLeft();
                topRight = btnMarkerTop.getRight();
                topTop = btnMarkerTop.getTop() + dy;
                topBottom = btnMarkerTop.getBottom() + dy;

                if (topTop < svTop) {
                    topTop = (int) svTop;
                    topBottom = (int) (svTop + btnMarkerTop.getHeight());
                }
                if (topBottom > bottomTop - btnMarkerTop.getHeight()) {
                    topBottom = bottomTop - btnMarkerTop.getHeight();
                    topTop = topBottom - btnMarkerTop.getHeight();
                }

                setPoint(btnMarkerTop, topLeft, topTop);
                setRectangle(rlRectangle,
                        (btnMarkerTop.getTop() + (btnMarkerTop.getHeight() / 2)),
                        (btnMarkerBottom.getTop() + (btnMarkerBottom.getHeight() / 2)));

                topStartX = event.getRawX();
                topStartY = event.getRawY();

                int[] btnLocation = {0, 0};
                int[] svLocation = {0, 0};

                btnMarkerTop.getLocationOnScreen(btnLocation);
                svContainer.getLocationOnScreen(svLocation);

                if (svContainer.getScrollY() > scrollSpeed
                        && btnLocation[1] < svLocation[1] + 80) {
                    svContainer.smoothScrollBy(0, -scrollSpeed);
                    topTop = topTop - scrollSpeed;
                    topBottom = topBottom - scrollSpeed;
                    setPoint(btnMarkerTop, topLeft, topTop);
                    setRectangle(rlRectangle,
                            (btnMarkerTop.getTop() + (btnMarkerTop.getHeight() / 2)),
                            (btnMarkerBottom.getTop() + (btnMarkerBottom.getHeight() / 2)));
                }

                if (svContainer.getScrollY() + scrollSpeed < endScrollDistance
                        && btnLocation[1] + btnMarkerTop.getHeight() > svLocation[1] + svContainer.getHeight() - 80) {
                    svContainer.smoothScrollBy(0, +scrollSpeed);
                    topTop = topTop + scrollSpeed;
                    topBottom = topBottom + scrollSpeed;
                    setPoint(btnMarkerTop, topLeft, topTop);
                    setRectangle(rlRectangle,
                            (btnMarkerTop.getTop() + (btnMarkerTop.getHeight() / 2)),
                            (btnMarkerBottom.getTop() + (btnMarkerBottom.getHeight() / 2)));
                }
                displayTimeRange();

                break;
            case MotionEvent.ACTION_UP:
                svContainer.setScrollingEnabled(true);
                setPoint(btnMarkerTop, topLeft, topTop);
                setRectangle(rlRectangle,
                        (btnMarkerTop.getTop() + (btnMarkerTop.getHeight() / 2)),
                        (btnMarkerBottom.getTop() + (btnMarkerBottom.getHeight() / 2)));
                displayTimeRange();
                btnMarkerTop.setPressed(false);
                break;

            default:
                break;
        }
    }

    private void onBottomMarkerTouched(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                topTop = btnMarkerTop.getTop();
                topBottom = btnMarkerTop.getBottom();
                bottomStartX = event.getRawX();
                bottomStartY = event.getRawY();
                svContainer.setScrollingEnabled(false);
                break;

            case MotionEvent.ACTION_MOVE:
                int dy = (int) (event.getRawY() - bottomStartY);

                bottomLeft = btnMarkerBottom.getLeft();
                bottomRight = btnMarkerBottom.getRight();
                bottomTop = btnMarkerBottom.getTop() + dy;
                bottomBottom = btnMarkerBottom.getBottom() + dy;

                if (bottomTop < topBottom + btnMarkerTop.getHeight()) {
                    bottomTop = topBottom + btnMarkerTop.getHeight();
                    bottomBottom = bottomTop + btnMarkerBottom.getHeight();
                }
                if (bottomBottom > svButtom) {
                    bottomBottom = (int) svButtom;
                    bottomTop = (int) (svButtom - btnMarkerBottom.getHeight());
                }

                setPoint(btnMarkerBottom, bottomLeft, bottomTop);

                bottomStartX = event.getRawX();
                bottomStartY = event.getRawY();

                int[] btnLocation = {0, 0};
                int[] svLocation = {0, 0};

                btnMarkerBottom.getLocationOnScreen(btnLocation);
                svContainer.getLocationOnScreen(svLocation);

                if (svContainer.getScrollY() > scrollSpeed && btnLocation[1] < svLocation[1] + 80) {
                    svContainer.smoothScrollBy(0, -scrollSpeed);
                    bottomTop = bottomTop - scrollSpeed;
                    bottomBottom = bottomBottom - scrollSpeed;
                    setPoint(btnMarkerBottom, bottomLeft, bottomTop);
                }

                if (svContainer.getScrollY() + scrollSpeed < endScrollDistance
                        && btnLocation[1] + btnMarkerBottom.getHeight() > svLocation[1] + svContainer.getHeight() - 80) {
                    svContainer.smoothScrollBy(0, +scrollSpeed);
                    bottomTop = bottomTop + scrollSpeed;
                    bottomBottom = bottomBottom + scrollSpeed;
                    setPoint(btnMarkerBottom, bottomLeft, bottomTop);
                }

                setRectangle(rlRectangle,
                        (btnMarkerTop.getTop() + (btnMarkerTop.getHeight() / 2)),
                        (btnMarkerBottom.getTop() + (btnMarkerBottom.getHeight() / 2)));

                displayTimeRange();

                break;
            case MotionEvent.ACTION_UP:
                svContainer.setScrollingEnabled(true);
                setPoint(btnMarkerBottom, bottomLeft, bottomTop);
                setRectangle(rlRectangle,
                        (btnMarkerTop.getTop() + (btnMarkerTop.getHeight() / 2)),
                        (btnMarkerBottom.getTop() + (btnMarkerBottom.getHeight() / 2)));
                displayTimeRange();
                break;

            default:
                break;
        }
    }

    private boolean onTimeRangeRectangleTouched(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Set start
                topTop = btnMarkerTop.getTop();
                topBottom = btnMarkerTop.getBottom();
                topLeft = btnMarkerTop.getLeft();
                topRight = btnMarkerTop.getRight();

                bottomTop = btnMarkerBottom.getTop();
                bottomBottom = btnMarkerBottom.getBottom();
                bottomLeft = btnMarkerBottom.getLeft();
                bottomRight = btnMarkerBottom.getRight();

                //Set touch start
                rectangleStartX = event.getRawX();
                rectangleStartY = event.getRawY();

                //Set rect height
                rectangleHeight = rlRectangle.getHeight();

                //Lock scroll view
                svContainer.setScrollingEnabled(false);
                break;

            case MotionEvent.ACTION_MOVE:
                int dy = (int) (event.getRawY() - rectangleStartY);

                rectangleLeft = rlRectangle.getLeft();
                rectangleRight = rlRectangle.getRight();
                rectangleTop = rlRectangle.getTop() + dy;
                rectangleBottom = rlRectangle.getBottom() + dy;

                //Top boundary
                if (rectangleTop < (int) svTop + btnMarkerTop.getHeight() / 2) {
                    rectangleTop = (int) svTop + btnMarkerTop.getHeight() / 2;
                    rectangleBottom = rectangleTop + rectangleHeight;
                }

                //Bottom boundary
                if (rectangleBottom > (int) svButtom - btnMarkerBottom.getHeight() / 2) {
                    rectangleBottom = (int) svButtom - btnMarkerBottom.getHeight() / 2;
                    rectangleTop = rectangleBottom - rectangleHeight;
                }

                setPoint(btnMarkerTop, topLeft, rectangleTop - btnMarkerTop.getHeight() / 2);
                setPoint(btnMarkerBottom, bottomLeft, rectangleBottom - btnMarkerBottom.getHeight() / 2);
                setRectangle(rlRectangle, rectangleTop, rectangleBottom);

                rectangleStartX = event.getRawX();
                rectangleStartY = event.getRawY();

                int[] svLocation = {0, 0};
                int[] rectLocation = {0, 0};

                svContainer.getLocationOnScreen(svLocation);
                rlRectangle.getLocationOnScreen(rectLocation);

                //Scroll down
                if (rectLocation[1] + rlRectangle.getHeight() > svLocation[1] + svContainer.getHeight() - 80
                        && svContainer.getScrollY() + scrollSpeed < endScrollDistance) {
                    svContainer.smoothScrollBy(0, scrollSpeed);
                    rectangleTop += scrollSpeed;
                    rectangleBottom = rectangleTop + rlRectangle.getHeight();
                    setPoint(btnMarkerTop, topLeft, rectangleTop - btnMarkerTop.getHeight() / 2);
                    setPoint(btnMarkerBottom, bottomLeft, rectangleBottom - btnMarkerBottom.getHeight() / 2);
                    setRectangle(rlRectangle, rectangleTop, rectangleBottom);
                }

                //Scroll up
                if (rectLocation[1] < svLocation[1] + 80
                        && svContainer.getScrollY() > scrollSpeed) {
                    svContainer.smoothScrollBy(0, -scrollSpeed);
                    rectangleTop -= scrollSpeed;
                    rectangleBottom = rectangleTop + rlRectangle.getHeight();
                    topTop = rectangleTop - btnMarkerTop.getHeight() / 2;
                    bottomTop = rectangleBottom - btnMarkerBottom.getHeight() / 2;

                    setPoint(btnMarkerBottom, bottomLeft, bottomTop);
                    setPoint(btnMarkerTop, topLeft, topTop);
                    setRectangle(rlRectangle, rectangleTop, rectangleBottom);
                }

                displayTimeRange();

                break;
            case MotionEvent.ACTION_UP:
                svContainer.setScrollingEnabled(true);
                setPoint(btnMarkerTop, topLeft, rectangleTop - btnMarkerTop.getHeight() / 2);
                setPoint(btnMarkerBottom, bottomLeft, rectangleBottom - btnMarkerBottom.getHeight() / 2);
                setRectangle(rlRectangle,
                        rectangleTop,
                        rectangleBottom);
                displayTimeRange();
                break;

            default:
                break;
        }
        return true;
    }

    public TimeRangePickerFragment setIsRectangleDraggable(boolean isRectangleDraggable) {
        this.isTimeRangeDraggable = isRectangleDraggable;
        return this;
    }

    public TimeRangePickerFragment setScrollSpeed(int speed) {
        this.scrollSpeed = speed;
        return this;
    }

    public interface OnTimeChangeListener {
        public void onTimeRangeChanged(int startMinute, int endMinute);
    }

    public TimeRangePickerFragment setOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
        this.onTimeChangeListener = onTimeChangeListener;
        return this;
    }
}
