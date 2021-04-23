package de.timokl.bluetoothestw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Arrays;

public class MyCanvas extends View {
    private static final int green = Color.rgb(0, 190, 0);
    private static final int yellow = Color.YELLOW;
    private static final int red = Color.RED;
    private static final int white = Color.WHITE;
    private static final int black = Color.BLACK;

    Paint paint;
    Paint.FontMetrics fontMetrics;
    int multiplier;
    Double result;
    int xM;
    int trackWidth = 4;
    boolean updateOn = true;
    private static TouchListener pTouchListener;
    String LOG_TAG = "Canvas";

    boolean [] isTrackOccupied = {true, true, true, true, true, true};
    boolean [] currentSwitchStates = {false, false, false, false};
    int [] signalStates = {0, 0, 0, 0, 0};
    boolean [] selectedSignals = {false, false, false, false, false, false};
    int [][] SigRects;
    int [] statusOfRoutes = {0, 0, 0, 0, 0, 0, 0, 0, 0};  // 1=wird eingestellt, 2=festgelegt, 3=in verwendung
    char [][] routes =   {{'a', 'c'}, {'b', 'c'},              // Ausfahrten aus Bahnhof
                          {'c', 'd'},                          // Signal C (Berg)
                          {'d', 'n'}, {'d', 'e'}, {'d', 'a'},  // Signal D
                          {'e', 'n'}, {'e', 'e'}, {'e', 'a'}}; // Signal E (Innenkreis)

    // 2=gerade, 1=abzweigend, 0=nicht gebraucht
    //                                    Signale    Weichen  Belegtmeldung/Gleis
    int [][] routesLockTable =  {{1,0,0,0,0,  2,0,0,0,  0,0,1,0,0,0}, // AC
                                 {0,1,0,0,0,  1,0,0,0,  0,0,1,0,0,0}, // BC
                                 {0,0,1,0,0,  0,0,2,0,  0,0,0,1,0,0}, // CD
                                 {0,0,0,1,0,  0,0,2,2,  0,0,0,0,1,0}, // DN
                                 {0,0,0,1,0,  0,1,2,1,  0,0,0,0,1,1}, // DE
                                 {0,0,0,1,0,  2,2,2,1,  1,0,0,0,1,0}, // DA
                                 {0,0,0,0,1,  0,0,1,2,  0,0,0,0,1,0}, // EN
                                 {0,0,0,0,1,  0,1,1,1,  0,0,0,0,1,1}, // EE
                                 {0,0,0,0,1,  2,2,1,1,  1,0,0,0,1,0}};// EA

    private void init() {
        paint = new Paint();
        fontMetrics = new Paint.FontMetrics();
        paint.setColor(red);
        paint.setStrokeWidth(trackWidth);
        //paint.setAntiAlias(true);
    }

    public MyCanvas(Context context) {
        super(context, null, 0);
        init();
    }

    public MyCanvas(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public MyCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /*--------------------------------------------------------------------------------*/

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        final int action = event.getAction();

        if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            final float x = event.getX();
            final float y = event.getY();

            int xClick = convertXToGrid(x);
            int yClick = convertYToGrid(y);

            char signal = checkPressedOnSignal(xClick, yClick);
            if (signal != '-') {
                onTouchDown(x, y, signal);
            } else {
                int weiche = checkPressedOnSwitch(xClick, yClick);
                if (weiche != -1) {
                    onTouchDown(x, y, weiche);
                } else {
                    resetSignals();
                }
            }
        }

        this.performClick ();
        return true;
    }

    private int convertXToGrid(float x) {
        x = Math.round(x);
        x = (x- xM) / multiplier;
        return (int) x;
    }

    private int convertYToGrid(float y) {
        y = Math.round(y);
        y /= multiplier;
        return (int) y;
    }

    private char checkPressedOnSignal (int clickedX, int clickedY) {
        if (70 <= clickedX && clickedX <= 80 && 72 <= clickedY && clickedY <= 78) {
            return 'a';
        } else if (70 <= clickedX && clickedX <= 80 && 82 <= clickedY && clickedY <= 88) {
            return 'b';
        } else if (90 <= clickedX && clickedX <= 100 && 12 <= clickedY && clickedY <= 18) {
            return 'c';
        } else if (50 <= clickedX && clickedX <= 60 && 12 <= clickedY && clickedY <= 18) {
            return 'd';
        } else if (47 <= clickedX && clickedX <= 53 && 26 <= clickedY && clickedY <= 36) {
            return 'e';
        } else if (1 <= clickedX && clickedX <= 9 && 12 <= clickedY && clickedY <= 18) {
            return 'n';
        } else {
            return '-';
        }
    }

    private int checkPressedOnSwitch (int clickedX, int clickedY) {
        if (80 <= clickedX && clickedX <= 90 && 70 <= clickedY && clickedY <= 80) {
            return 0;
        } else if (40 <= clickedX && clickedX <= 50 && 60 <= clickedY && clickedY <= 70) {
            return 1;
        } else if (40 <= clickedX && clickedX <= 50 && 20 <= clickedY && clickedY <= 30) {
            return 2;
        } else if (10 <= clickedX && clickedX <= 20 && 20 <= clickedY && clickedY <= 30) {
            return 3;
        } else {
            return -1;
        }
    }

    /*--------------------------------------------------------------------------------*/

    static void onTouchDown(float downX, float downY, int weiche) {
        if (pTouchListener == null) return;
        pTouchListener.onTouchDown ((int) downX, (int) downY, weiche);
    }
    static void onTouchDown(float downX, float downY, char signal) {
        if (pTouchListener == null) return;
        pTouchListener.onTouchDown ((int) downX, (int) downY, signal);
    }
    static void resetSignals() {
        if (pTouchListener == null) return;
        pTouchListener.resetSignals ();
    }
    /*--------------------------------------------------------------------------------*/
    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    /*--------------------------------------------------------------------------------*/

    public void setTouchListener (TouchListener newValue)
    {
        pTouchListener = newValue;
    }
    /*--------------------------------------------------------------------------------*/


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(black);
        Log.d(LOG_TAG, "Belegtmeldung "+ Arrays.toString(isTrackOccupied));
        multiplier = getHeight() / 90;
        xM = (getWidth() - multiplier * 110) / 2;


//////////////////////////////////////////////////////////////////      Signal Rahmen      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        paint.setColor(green);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i<6; i++){
            if (selectedSignals[i]){
                SigRects = new int[][]{
                        {pos("x", 70), pos("y", 69), pos("x", 78), pos("y", 78)},
                        {pos("x", 70), pos("y", 79), pos("x", 78), pos("y", 88)},
                        {pos("x", 92), pos("y", 12), pos("x", 100), pos("y", 21)},
                        {pos("x", 52), pos("y", 12), pos("x", 60), pos("y", 21)},
                        {pos("x", 44), pos("y", 28), pos("x", 53), pos("y", 36)},
                        {pos("x", 2.5), pos("y", 12), pos("x", 7.5), pos("y", 21)}

                };
                canvas.drawRect(SigRects[i][0], SigRects[i][1], SigRects[i][2], SigRects[i][3], paint);
            }
        }


        paint.setStyle(Paint.Style.FILL);

//////////////////////////////////////////////////////////////////      Trennstriche      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        paint.setColor(yellow);

        paint.setStrokeWidth(3);
        seperatorColor(0, 2);
        canvas.drawLine(pos("x", 80), pos("y", 70), pos("x", 80), pos("y", 69), paint); //Gl 1-3



        seperatorColor(1, 2);
        canvas.drawLine(pos("x", 80), pos("y", 80), pos("x", 80), pos("y", 79), paint); //Gl 2-3



        seperatorColor(2, 3);
        canvas.drawLine(pos("x", 90), pos("y", 20), pos("x", 90), pos("y", 19), paint); //Gl 3-4



        if(isTrackOccupied[4]/* && Belegtmeldung[nP]*/){
            paint.setColor(red);
        } else {
            paint.setColor(yellow);
        }
        canvas.drawLine(pos("x", 10), pos("y", 20), pos("x", 10), pos("y", 19), paint); //Gl 5-nP



       seperatorColor(3, 4);
        canvas.drawLine(pos("x", 50), pos("y", 20), pos("x", 50), pos("y", 19), paint); //Gl 4-5



        seperatorColor(4, 5);
        canvas.drawLine(pos("x", 45), pos("y", 64), pos("x", 46), pos("y", 64), paint); //Gl 5-6 unten
        seperatorColor(5, 4);
        canvas.drawLine(pos("x", 45), pos("y", 26), pos("x", 46), pos("y", 26), paint); //Gl 5-6 oben



        seperatorColor(4, 0);
        canvas.drawLine(pos("x", 50), pos("y", 70), pos("x", 50), pos("y", 69), paint); //Gl 5-2


//////////////////////////////////////////////////////////////////      Gleise      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        paint.setStrokeWidth(trackWidth);


        //Gleis "1"
        trackColor(0);
        canvas.drawLine(pos("x", 50), pos("y", 70), pos("x", 80), pos("y", 70), paint);

        //Gleis "2"
        trackColor(1);
        canvas.drawLine(pos("x", 0), pos("y", 80), pos("x", 80), pos("y", 80), paint);


        //Gleis "101"
        trackColor(2);

        canvas.drawLine(pos("x", 90), pos("y", 70), pos("x", 100), pos("y", 70), paint);
        canvas.drawLine(pos("x", 100), pos("y", 70), pos("x", 105), pos("y", 60), paint);
        canvas.drawLine(pos("x", 105), pos("y", 60), pos("x", 105), pos("y", 30), paint);
        canvas.drawLine(pos("x", 105), pos("y", 30), pos("x", 100), pos("y", 20), paint);
        canvas.drawLine(pos("x", 100), pos("y", 20), pos("x", 90), pos("y", 20), paint);


        //Gleis "201"
        trackColor(3);
        canvas.drawLine(pos("x", 90), pos("y", 20), pos("x", 50), pos("y", 20), paint);


        //Gleis "102"
        trackColor(4);

        canvas.drawLine(pos("x", 40), pos("y", 20), pos("x", 20), pos("y", 20), paint);

        canvas.drawLine(pos("x", 15), pos("y", 26), pos("x", 15), pos("y", 60), paint);
        canvas.drawLine(pos("x", 15), pos("y", 60), pos("x", 20), pos("y", 70), paint);
        canvas.drawLine(pos("x", 20), pos("y", 70), pos("x", 40), pos("y", 70), paint);



        //Richtung NP
        paint.setColor(red);
        canvas.drawLine(pos("x", 10), pos("y", 20), pos("x", 0), pos("y", 20), paint);



        //Gleis "103"
        trackColor(5);
        canvas.drawLine(pos("x", 45), pos("y", 64), pos("x", 45), pos("y", 26), paint);


//////////////////////////////////////////////////////////////////      Weichen      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        //Weiche "1"
        if (currentSwitchStates[0]){
            //abzweigend
            switchColor(1, 1);
            canvas.drawLine(pos("x", 80), pos("y", 70), pos("x", 83), pos("y", 70), paint);
            switchColor(1, 2);
            canvas.drawLine(pos("x", 80), pos("y", 80), pos("x", 83), pos("y", 80), paint);
            canvas.drawLine(pos("x", 83), pos("y", 80), pos("x", 88), pos("y", 70), paint);
        } else {
            //gerade
            switchColor(1, 1);
            canvas.drawLine(pos("x", 80), pos("y", 70), pos("x", 86), pos("y", 70), paint);
            switchColor(1, 2);
            canvas.drawLine(pos("x", 80), pos("y", 80), pos("x", 83), pos("y", 80), paint);
            canvas.drawLine(pos("x", 83), pos("y", 80), pos("x", 88), pos("y", 70), paint);
        }

        //Spitze
        switchColor(1, 0);
        canvas.drawLine(pos("x", 87), pos("y", 70), pos("x", 89), pos("y", 70), paint);


        paint.setColor(black);
        if (currentSwitchStates[0]){
            paint.setStrokeWidth(trackWidth);
            canvas.drawLine(pos("x", 85), pos("y", 70) + trackWidth, pos("x", 89), pos("y", 70) + trackWidth, paint);
        } else {
            paint.setStrokeWidth(3 * multiplier);
            canvas.drawLine(pos("x", 85), pos("y", 70) + (float)(3* multiplier /2 + trackWidth /2), pos("x", 89), pos("y", 70) + (float)(3* multiplier /2 + trackWidth /2), paint);
            paint.setStrokeWidth(trackWidth);
        }





        //Weiche "2"
        if (currentSwitchStates[1]){
            //abzweigend
            switchColor(2, 1);
            canvas.drawLine(pos("x", 50), pos("y", 70), pos("x", 47), pos("y", 70), paint);
            switchColor(2, 2);
            canvas.drawLine(pos("x", 45), pos("y", 64), pos("x", 42), pos("y", 70), paint);
        } else {
            //gerade
            switchColor(2, 1);
            canvas.drawLine(pos("x", 50), pos("y", 70), pos("x", 44), pos("y", 70), paint);
            switchColor(2, 2);
            canvas.drawLine(pos("x", 45), pos("y", 64), pos("x", 42), pos("y", 70), paint);
        }

        //Spitze
        switchColor(2, 0);
        canvas.drawLine(pos("x", 41), pos("y", 70), pos("x", 43), pos("y", 70), paint);


        paint.setColor(black);
        if (currentSwitchStates[1]){
            paint.setStrokeWidth(trackWidth);
            canvas.drawLine(pos("x", 41), pos("y", 70) - trackWidth, pos("x", 45), pos("y", 70) - trackWidth, paint);
        } else {
            paint.setStrokeWidth(3 * multiplier);
            canvas.drawLine(pos("x", 41), pos("y", 70) - (float)(3* multiplier /2 + trackWidth /2), pos("x", 45), pos("y", 70) - (float)(3* multiplier /2 + trackWidth /2), paint);
            paint.setStrokeWidth(trackWidth);
        }





        //Weiche "3"
        if (currentSwitchStates[2]){
            //abzweigend
            switchColor(3, 1);
            canvas.drawLine(pos("x", 50), pos("y", 20), pos("x", 47), pos("y", 20), paint);
            switchColor(3, 2);
            canvas.drawLine(pos("x", 45), pos("y", 26), pos("x", 42), pos("y", 20), paint);
        } else {
            //gerade
            switchColor(3, 1);
            canvas.drawLine(pos("x", 50), pos("y", 20), pos("x", 44), pos("y", 20), paint);
            switchColor(3, 2);
            canvas.drawLine(pos("x", 45), pos("y", 26), pos("x", 42), pos("y", 20), paint);
        }

        //Spitze
        switchColor(3, 0);
        canvas.drawLine(pos("x", 41), pos("y", 20), pos("x", 43), pos("y", 20), paint);


        paint.setColor(black);
        if (currentSwitchStates[2]){
            paint.setStrokeWidth(trackWidth);
            canvas.drawLine(pos("x", 41), pos("y", 20) + trackWidth, pos("x", 45), pos("y", 20) + trackWidth, paint);
        } else {
            paint.setStrokeWidth(3 * multiplier);
            canvas.drawLine(pos("x", 41), pos("y", 20) + (float)(3* multiplier /2 + trackWidth /2), pos("x", 45), pos("y", 20) + (float)(3* multiplier /2 + trackWidth /2), paint);
            paint.setStrokeWidth(trackWidth);
        }





        //Weiche "4"
        if (currentSwitchStates[3]){
            //abzweigend
            switchColor(4, 1);
            canvas.drawLine(pos("x", 10), pos("y", 20), pos("x", 13), pos("y", 20), paint);
            switchColor(4, 2);
            canvas.drawLine(pos("x", 15), pos("y", 26), pos("x", 18), pos("y", 20), paint);
        } else {
            //gerade
            switchColor(4, 1);
            canvas.drawLine(pos("x", 10), pos("y", 20), pos("x", 16), pos("y", 20), paint);
            switchColor(4, 2);
            canvas.drawLine(pos("x", 15), pos("y", 26), pos("x", 18), pos("y", 20), paint);
        }

        //Spitze
        switchColor(4, 0);
        canvas.drawLine(pos("x", 17), pos("y", 20), pos("x", 19), pos("y", 20), paint);


        paint.setColor(black);
        if (currentSwitchStates[3]){
            paint.setStrokeWidth(trackWidth);
            canvas.drawLine(pos("x", 15), pos("y", 20) + trackWidth, pos("x", 19), pos("y", 20) + trackWidth, paint);
        } else {
            paint.setStrokeWidth(3 * multiplier);
            canvas.drawLine(pos("x", 15), pos("y", 20) + (float)(3* multiplier /2 + trackWidth /2), pos("x", 19), pos("y", 20) + (float)(3* multiplier /2 + trackWidth /2), paint);
            paint.setStrokeWidth(trackWidth);
        }


//////////////////////////////////////////////////////////////////      Linien  Aussen (Wenn FA)     /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        paint.setColor(green);
        paint.setStrokeWidth(1);
        for(int i=0; i<9; i++){
            // W1
            if(routesLockTable[i][5] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 80), pos("y", 82), pos("x", 84), pos("y", 82), paint);
                canvas.drawLine(pos("x", 80), pos("y", 78), pos("x", 82), pos("y", 78), paint);
                canvas.drawLine(pos("x", 89), pos("y", 72), pos("x", 90), pos("y", 72), paint);
                canvas.drawLine(pos("x", 87), pos("y", 68), pos("x", 90), pos("y", 68), paint);
                canvas.drawLine(pos("x", 84), pos("y", 82), pos("x", 89), pos("y", 72), paint);
                canvas.drawLine(pos("x", 82), pos("y", 78), pos("x", 87), pos("y", 68), paint);
            }else if(routesLockTable[i][5] == 2 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 80), pos("y", 72), pos("x", 90), pos("y", 72), paint);
                canvas.drawLine(pos("x", 80), pos("y", 68), pos("x", 90), pos("y", 68), paint);
            }
            // W2
            if(routesLockTable[i][6] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 72), pos("x", 43), pos("y", 72), paint);
                canvas.drawLine(pos("x", 40), pos("y", 68), pos("x", 41), pos("y", 68), paint);
                canvas.drawLine(pos("x", 43), pos("y", 72), pos("x", 47), pos("y", 64), paint);
                canvas.drawLine(pos("x", 41), pos("y", 68), pos("x", 43), pos("y", 64), paint);
            }else if(routesLockTable[i][6] == 2 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 72), pos("x", 50), pos("y", 72), paint);
                canvas.drawLine(pos("x", 40), pos("y", 68), pos("x", 50), pos("y", 68), paint);
            }
            // W3
            if(routesLockTable[i][7] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 22), pos("x", 41), pos("y", 22), paint);
                canvas.drawLine(pos("x", 40), pos("y", 18), pos("x", 43), pos("y", 18), paint);
                canvas.drawLine(pos("x", 41), pos("y", 22), pos("x", 43), pos("y", 26), paint);
                canvas.drawLine(pos("x", 43), pos("y", 18), pos("x", 47), pos("y", 26), paint);
            }else if(routesLockTable[i][7] == 2 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 22), pos("x", 50), pos("y", 22), paint);
                canvas.drawLine(pos("x", 40), pos("y", 18), pos("x", 50), pos("y", 18), paint);
            }
            // W4
            if(routesLockTable[i][8] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 20), pos("y", 22), pos("x", 19), pos("y", 22), paint);
                canvas.drawLine(pos("x", 20), pos("y", 18), pos("x", 17), pos("y", 18), paint);
                canvas.drawLine(pos("x", 19), pos("y", 22), pos("x", 17), pos("y", 26), paint);
                canvas.drawLine(pos("x", 17), pos("y", 18), pos("x", 13), pos("y", 26), paint);
            }else if(routesLockTable[i][8] == 2 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 0), pos("y", 22), pos("x", 20), pos("y", 22), paint);
                canvas.drawLine(pos("x", 0), pos("y", 18), pos("x", 20), pos("y", 18), paint);
            }
            // A
            if(routesLockTable[i][9] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 50), pos("y", 72), pos("x", 80), pos("y", 72), paint);
                canvas.drawLine(pos("x", 50), pos("y", 68), pos("x", 80), pos("y", 68), paint);
            }
            // B
            if(routesLockTable[i][10] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 0), pos("y", 82), pos("x", 80), pos("y", 82), paint);
                canvas.drawLine(pos("x", 0), pos("y", 78), pos("x", 80), pos("y", 78), paint);
            }
            // C
            if(routesLockTable[i][11] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 90), pos("y", 72), pos("x", 101), pos("y", 72), paint);
                canvas.drawLine(pos("x", 90), pos("y", 68), pos("x", 99), pos("y", 68), paint);
                canvas.drawLine(pos("x", 101), pos("y", 72), pos("x", 107), pos("y", 60), paint);
                canvas.drawLine(pos("x", 99), pos("y", 68), pos("x", 103), pos("y", 60), paint);
                canvas.drawLine(pos("x", 107), pos("y", 60), pos("x", 107), pos("y", 30), paint);
                canvas.drawLine(pos("x", 103), pos("y", 60), pos("x", 103), pos("y", 30), paint);
                canvas.drawLine(pos("x", 103), pos("y", 30), pos("x", 99), pos("y", 22), paint);
                canvas.drawLine(pos("x", 107), pos("y", 30), pos("x", 101), pos("y", 18), paint);
                canvas.drawLine(pos("x", 99), pos("y", 22), pos("x", 90), pos("y", 22), paint);
                canvas.drawLine(pos("x", 101), pos("y", 18), pos("x", 90), pos("y", 18), paint);
            }
            // D
            if(routesLockTable[i][12] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 90), pos("y", 22), pos("x", 50), pos("y", 22), paint);
                canvas.drawLine(pos("x", 90), pos("y", 18), pos("x", 50), pos("y", 18), paint);
            }
            // T
            if(routesLockTable[i][13] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 22), pos("x", 20), pos("y", 22), paint);
                canvas.drawLine(pos("x", 40), pos("y", 18), pos("x", 20), pos("y", 18), paint);

                if(i != 3 && i != 6){
                    canvas.drawLine(pos("x", 17), pos("y", 26), pos("x", 17), pos("y", 60), paint);
                    canvas.drawLine(pos("x", 13), pos("y", 26), pos("x", 13), pos("y", 60), paint);
                    canvas.drawLine(pos("x", 17), pos("y", 60), pos("x", 21), pos("y", 68), paint);
                    canvas.drawLine(pos("x", 13), pos("y", 60), pos("x", 19), pos("y", 72), paint);
                    canvas.drawLine(pos("x", 19), pos("y", 72), pos("x", 40), pos("y", 72), paint);
                    canvas.drawLine(pos("x", 21), pos("y", 68), pos("x", 40), pos("y", 68), paint);
                }
            }
            // E
            if(routesLockTable[i][14] == 1 && statusOfRoutes[i] == 1){
                canvas.drawLine(pos("x", 47), pos("y", 64), pos("x", 47), pos("y", 26), paint);
                canvas.drawLine(pos("x", 43), pos("y", 64), pos("x", 43), pos("y", 26), paint);
            }
        }
        paint.setStrokeWidth(trackWidth);


//////////////////////////////////////////////////////////////////      Signale      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        //Signal "N1"
        signalColor(0);
        canvas.drawCircle(pos("x", 76), pos("y", 75), (float) 1.5 * multiplier, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 76), pos("y", 75), pos("x", 71), pos("y", 75), paint);
        paint.setStrokeWidth(trackWidth);
        canvas.drawLine(pos("x", 71), pos("y", 76), pos("x", 71), pos("y", 74), paint);


        //Signal "N2"
        signalColor(1);
        canvas.drawCircle(pos("x", 76), pos("y", 85), (float) 1.5 * multiplier, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 76), pos("y", 85), pos("x", 71), pos("y", 85), paint);
        paint.setStrokeWidth(trackWidth);
        canvas.drawLine(pos("x", 71), pos("y", 86), pos("x", 71), pos("y", 84), paint);


        //Signal "101"
        signalColor(2);
        canvas.drawCircle(pos("x", 94), pos("y", 15), (float) 1.5 * multiplier, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 94), pos("y", 15), pos("x", 99), pos("y", 15), paint);
        paint.setStrokeWidth(trackWidth);
        canvas.drawLine(pos("x", 99), pos("y", 16), pos("x", 99), pos("y", 14), paint);


        //Signal "102"
        signalColor(3);
        canvas.drawCircle(pos("x", 54), pos("y", 15), (float) 1.5 * multiplier, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 54), pos("y", 15), pos("x", 59), pos("y", 15), paint);
        paint.setStrokeWidth(trackWidth);
        canvas.drawLine(pos("x", 59), pos("y", 16), pos("x", 59), pos("y", 14), paint);


        //Signal "103"
        signalColor(4);
        canvas.drawCircle(pos("x", 50), pos("y", 30), (float) 1.5 * multiplier, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 50), pos("y", 30), pos("x", 50), pos("y", 35), paint);
        paint.setStrokeWidth(trackWidth);
        canvas.drawLine(pos("x", 51), pos("y", 35), pos("x", 49), pos("y", 35), paint);


        //Pfeil Neueplatte
        paint.setColor(green);
        Path triangle = new Path();
        triangle.moveTo(pos("x", 6.5), pos("y", 13.5));
        triangle.lineTo(pos("x", 3), pos("y", 15));
        triangle.lineTo(pos("x", 6.5), pos("y", 16.5));
        triangle.lineTo(pos("x", 6.5), pos("y", 13.5));
        canvas.drawPath(triangle, paint);



/////////////////////////////////////////////////////////////////      Texte      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



        trackText(canvas, "1", 65, 71);
        trackText(canvas,"2", 45, 81);
        trackText(canvas, "101", 103, 46);
        trackText(canvas, "201", 69, 21);
        trackText(canvas, "102", 13, 46);
        trackText(canvas, "103", 43, 46);

        paint.setTextSize(multiplier *3);
        paint.setColor(yellow);

        canvas.drawText("1", pos("x", 90), pos("y", 67.5), paint);
        canvas.drawText("2", pos("x", 37), pos("y", 67.5), paint);
        canvas.drawText("3", pos("x", 40), pos("y", 17.5), paint);
        canvas.drawText("4", pos("x", 20), pos("y", 17.5), paint);

        paint.setColor(green);

        canvas.drawText("N1",  pos("x", 66), pos("y", 76), paint); // Signal Gl 1
        canvas.drawText("N2",  pos("x", 66), pos("y", 86), paint); // Signal Gl 2
        canvas.drawText("101",  pos("x", 100), pos("y", 16), paint); // Signal Gl 101
        canvas.drawText("102",  pos("x", 60), pos("y", 16), paint); // Signal Gl 201
        canvas.save();
        canvas.rotate(90, 50, 36);
        canvas.drawText("103",  pos("x", 50), pos("y", 50), paint); // Signal Gl 103 // 50,36
        canvas.restore();


        if(updateOn) {
            postInvalidateDelayed(50);
        }

    }

    public void seperatorColor(int track1, int track2){
        paint.setColor(yellow);
        boolean trainIsInImportantSection = false;
        for(int i=0; i<9; i++) {
            //     Gleis muss im Plan frei sein     und   Zug schon im Abschnitt
            if (routesLockTable[i][track2 + 9] == 1 && statusOfRoutes[i] == 3) {
                trainIsInImportantSection = true;
                break;
            }
        }
        if(isTrackOccupied[track1] && (track1 != 4 || isTrackOccupied[track2] || trainIsInImportantSection)){  // && isTrackOccupied[track2]
            paint.setColor(red);
        } else {
            for(int i=0; i<9; i++) {
                if (routesLockTable[i][track1 + 9] == 1 && track1 != 4 &&
                        (statusOfRoutes[i] == 2 || (statusOfRoutes[i] == 3 && !isTrackOccupied[track1]))) {
                    paint.setColor(green);
                } else if (routesLockTable[i][track1 + 9] == 1 && routesLockTable[i][track2 + 9] == 1 && track1 == 4 &&
                        (statusOfRoutes[i] == 2 || (statusOfRoutes[i] == 3 && !isTrackOccupied[track1] && !isTrackOccupied[track2]))) {
                    paint.setColor(green);
                }
            }
        }


    }

    public void trackColor(int gl){
        if (isTrackOccupied[gl]){
            paint.setColor(red);
        } else {
            paint.setColor(yellow);
        }
        for(int i=0; i<9; i++) {
            if (routesLockTable[i][gl + 9] == 1 && (statusOfRoutes[i] == 2 || (statusOfRoutes[i] == 3 && !isTrackOccupied[gl]))) {
                paint.setColor(green);
            } else if (routesLockTable[i][gl + 9] == 1 && statusOfRoutes[i] == 3 && isTrackOccupied[gl]) {
                paint.setColor(red);
            }
        }
    }

    public void signalColor(int sig){
        if (signalStates[sig] == 0){
            paint.setColor(red);
        } else if (signalStates[sig] == 1) {
            paint.setColor(yellow);
        } else if (signalStates[sig] == 2) {
            paint.setColor(green);
        }
    }

    public void switchColor(int sw, int p){
        if (p == 0){   // Farbe für Weichenspitze
            for (int route = 0; route< routesLockTable.length; route++) {
                if ((statusOfRoutes[route] == 2 || statusOfRoutes[route] == 3) && (routesLockTable[route][4+sw] == 1 || routesLockTable[route][4 + sw] == 2)) {
                    paint.setColor(green);
                    break;
                } else {
                    paint.setColor(white);
                }
            }
        } else if (p == 1){ // Farbe für Weiche gerade
            for (int route = 0; route < routesLockTable.length; route++) {
                if ((statusOfRoutes[route] == 2 || statusOfRoutes[route] == 3) && routesLockTable[route][4+sw] == 2){
                    paint.setColor(green);
                    break;
                } else if (!currentSwitchStates[sw-1]){
                    paint.setColor(yellow);
                } else {
                    paint.setColor(white);
                }
            }
        } else if (p == 2){ // Farbe für Weiche abzweigend
            for (int route = 0; route< routesLockTable.length; route++) {
                if ((statusOfRoutes[route] == 2 || statusOfRoutes[route] == 3) && routesLockTable[route][4+sw] == 1){
                    paint.setColor(green);
                    break;
                } else if (currentSwitchStates[sw-1]){
                    paint.setColor(yellow);
                } else {
                    paint.setColor(white);
                }
            }
        }
    }

    private void trackText(Canvas canvas, String text, int x, int y) {
        paint.setTextSize(multiplier *3);
        paint.setColor(black);
        paint.getFontMetrics(fontMetrics);
        int margin = 5;

        canvas.drawRect(pos("x", x) - margin,
                pos("y", y) + fontMetrics.top - margin,
                pos("x", x) + paint.measureText(text) + margin,
                pos("y", y) + fontMetrics.bottom + margin, paint);


        paint.setColor(Color.WHITE);
        canvas.drawText(text, pos("x", x), pos("y", y), paint);
    }

    public int pos(String axis, double valueOfPosition) {
        result = 0.0;
        if (axis.equals("x")) {
            result = valueOfPosition * multiplier + xM;
        } else if (axis.equals("y")) {
            result = valueOfPosition * multiplier;
        }
        return (int)Math.round(result);
    }
}