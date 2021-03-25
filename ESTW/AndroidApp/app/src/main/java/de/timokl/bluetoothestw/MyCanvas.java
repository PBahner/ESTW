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
    Paint.FontMetrics fm;
    int m;
    Double ergebnis;
    int xM;
    int gleisBreite = 4;
    boolean updateOn = true;
    private static TouchListener pTouchListener;
    String LOG_TAG = "Canvas";

    boolean [] Belegtmeldung = {true, true, true, true, true, true};
    boolean [] Weichen = {false, false, false, false};
    int [] Signale = {0, 0, 0, 0, 0};
    boolean [] SigAuswahl = {false, false, false, false, false, false};
    int [][] SigRects;
    int [] einzustellendeFahrstrassen = {0, 0, 0, 0, 0, 0, 0, 0, 0};  // 1=wird eingestellt, 2=festgelegt, 3=in verwendung
    char [][] fahrstrassen =   {{'a', 'c'}, {'b', 'c'},              // Ausfahrten aus Bahnhof
                                {'c', 'd'},                          // Signal C (Berg)
                                {'d', 'n'}, {'d', 'e'}, {'d', 'a'},  // Signal D
                                {'e', 'n'}, {'e', 'e'}, {'e', 'a'}}; // Signal E (Innenkreis)

    // 2=gerade, 1=abzweigend, 0=nicht gebraucht
    //                                    Signale    Weichen  Belegtmeldung/Gleis
    int [][] fahrstrassenVerschluss =  {{1,0,0,0,0,  2,0,0,0,  0,0,1,0,0,0}, // AC
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
        fm = new Paint.FontMetrics();
        paint.setColor(red);
        paint.setStrokeWidth(gleisBreite);
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
            int xClick = Math.round(event.getX());
            int yClick = Math.round(event.getY());

            xClick = (xClick - xM) / m;
            yClick /= m;

            if (70 <= xClick && xClick <= 80 && 72 <= yClick && yClick <= 78) {
                onTouchDown(x, y, "a");
            } else if (70 <= xClick && xClick <= 80 && 82 <= yClick && yClick <= 88) {
                onTouchDown(x, y, "b");
            } else if (90 <= xClick && xClick <= 100 && 12 <= yClick && yClick <= 18) {
                onTouchDown(x, y, "c");
            } else if (50 <= xClick && xClick <= 60 && 12 <= yClick && yClick <= 18) {
                onTouchDown(x, y, "d");
            } else if (47 <= xClick && xClick <= 53 && 26 <= yClick && yClick <= 36) {
                onTouchDown(x, y, "e");
            } else if (1 <= xClick && xClick <= 9 && 12 <= yClick && yClick <= 18) {
                onTouchDown(x, y, "n");
            } else if (80 <= xClick && xClick <= 90 && 70 <= yClick && yClick <= 80) {
                onTouchDown(x, y, 0);
            } else if (40 <= xClick && xClick <= 50 && 60 <= yClick && yClick <= 70) {
                onTouchDown(x, y, 1);
            } else if (40 <= xClick && xClick <= 50 && 20 <= yClick && yClick <= 30) {
                onTouchDown(x, y, 2);
            } else if (10 <= xClick && xClick <= 20 && 20 <= yClick && yClick <= 30) {
                onTouchDown(x, y, 3);
            } else {
                resetSignals();
            }
        }

        this.performClick ();
        return true;
    }

    /*--------------------------------------------------------------------------------*/

    static void onTouchDown(float downX, float downY, int weiche) {
        if (pTouchListener == null) return;
        pTouchListener.onTouchDown ((int) downX, (int) downY, weiche);
    }
    static void onTouchDown(float downX, float downY, String signal) {
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
        Log.d(LOG_TAG, "Belegtmeldung "+ Arrays.toString(Belegtmeldung));
        m = getHeight() / 90;
        xM = (getWidth() - m * 110) / 2;


//////////////////////////////////////////////////////////////////      Signal Rahmen      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        paint.setColor(green);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i<6; i++){
            if (SigAuswahl[i]){
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
        TrColor(0, 2);
        canvas.drawLine(pos("x", 80), pos("y", 70), pos("x", 80), pos("y", 69), paint); //Gl 1-3



        TrColor(1, 2);
        canvas.drawLine(pos("x", 80), pos("y", 80), pos("x", 80), pos("y", 79), paint); //Gl 2-3



        TrColor(2, 3);
        canvas.drawLine(pos("x", 90), pos("y", 20), pos("x", 90), pos("y", 19), paint); //Gl 3-4



        if(Belegtmeldung[4]/* && Belegtmeldung[nP]*/){
            paint.setColor(red);
        } else {
            paint.setColor(yellow);
        }
        canvas.drawLine(pos("x", 10), pos("y", 20), pos("x", 10), pos("y", 19), paint); //Gl 5-nP



       TrColor(3, 4);
        canvas.drawLine(pos("x", 50), pos("y", 20), pos("x", 50), pos("y", 19), paint); //Gl 4-5



        TrColor(4, 5);
        canvas.drawLine(pos("x", 45), pos("y", 64), pos("x", 46), pos("y", 64), paint); //Gl 5-6 unten
        TrColor(5, 4);
        canvas.drawLine(pos("x", 45), pos("y", 26), pos("x", 46), pos("y", 26), paint); //Gl 5-6 oben



        TrColor(4, 0);
        canvas.drawLine(pos("x", 50), pos("y", 70), pos("x", 50), pos("y", 69), paint); //Gl 5-2


//////////////////////////////////////////////////////////////////      Gleise      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        paint.setStrokeWidth(gleisBreite);


        //Gleis "1"
        GlColor(0);
        canvas.drawLine(pos("x", 50), pos("y", 70), pos("x", 80), pos("y", 70), paint);

        //Gleis "2"
        GlColor(1);
        canvas.drawLine(pos("x", 0), pos("y", 80), pos("x", 80), pos("y", 80), paint);


        //Gleis "101"
        GlColor(2);

        canvas.drawLine(pos("x", 90), pos("y", 70), pos("x", 100), pos("y", 70), paint);
        canvas.drawLine(pos("x", 100), pos("y", 70), pos("x", 105), pos("y", 60), paint);
        canvas.drawLine(pos("x", 105), pos("y", 60), pos("x", 105), pos("y", 30), paint);
        canvas.drawLine(pos("x", 105), pos("y", 30), pos("x", 100), pos("y", 20), paint);
        canvas.drawLine(pos("x", 100), pos("y", 20), pos("x", 90), pos("y", 20), paint);


        //Gleis "201"
        GlColor(3);
        canvas.drawLine(pos("x", 90), pos("y", 20), pos("x", 50), pos("y", 20), paint);


        //Gleis "102"
        GlColor(4);

        canvas.drawLine(pos("x", 40), pos("y", 20), pos("x", 20), pos("y", 20), paint);

        canvas.drawLine(pos("x", 15), pos("y", 26), pos("x", 15), pos("y", 60), paint);
        canvas.drawLine(pos("x", 15), pos("y", 60), pos("x", 20), pos("y", 70), paint);
        canvas.drawLine(pos("x", 20), pos("y", 70), pos("x", 40), pos("y", 70), paint);



        //Richtung NP
        paint.setColor(red);
        canvas.drawLine(pos("x", 10), pos("y", 20), pos("x", 0), pos("y", 20), paint);



        //Gleis "103"
        GlColor(5);
        canvas.drawLine(pos("x", 45), pos("y", 64), pos("x", 45), pos("y", 26), paint);


//////////////////////////////////////////////////////////////////      Weichen      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        //Weiche "1"
        if (Weichen[0]){
            //abzweigend
            SwitchColor(1, 1);
            canvas.drawLine(pos("x", 80), pos("y", 70), pos("x", 83), pos("y", 70), paint);
            SwitchColor(1, 2);
            canvas.drawLine(pos("x", 80), pos("y", 80), pos("x", 83), pos("y", 80), paint);
            canvas.drawLine(pos("x", 83), pos("y", 80), pos("x", 88), pos("y", 70), paint);
        } else {
            //gerade
            SwitchColor(1, 1);
            canvas.drawLine(pos("x", 80), pos("y", 70), pos("x", 86), pos("y", 70), paint);
            SwitchColor(1, 2);
            canvas.drawLine(pos("x", 80), pos("y", 80), pos("x", 83), pos("y", 80), paint);
            canvas.drawLine(pos("x", 83), pos("y", 80), pos("x", 88), pos("y", 70), paint);
        }

        //Spitze
        SwitchColor(1, 0);
        canvas.drawLine(pos("x", 87), pos("y", 70), pos("x", 89), pos("y", 70), paint);


        paint.setColor(black);
        if (Weichen[0]){
            paint.setStrokeWidth(gleisBreite);
            canvas.drawLine(pos("x", 85), pos("y", 70) + gleisBreite, pos("x", 89), pos("y", 70) + gleisBreite, paint);
        } else {
            paint.setStrokeWidth(3 * m);
            canvas.drawLine(pos("x", 85), pos("y", 70) + (float)(3*m/2 + gleisBreite/2), pos("x", 89), pos("y", 70) + (float)(3*m/2 + gleisBreite/2), paint);
            paint.setStrokeWidth(gleisBreite);
        }





        //Weiche "2"
        if (Weichen[1]){
            //abzweigend
            SwitchColor(2, 1);
            canvas.drawLine(pos("x", 50), pos("y", 70), pos("x", 47), pos("y", 70), paint);
            SwitchColor(2, 2);
            canvas.drawLine(pos("x", 45), pos("y", 64), pos("x", 42), pos("y", 70), paint);
        } else {
            //gerade
            SwitchColor(2, 1);
            canvas.drawLine(pos("x", 50), pos("y", 70), pos("x", 44), pos("y", 70), paint);
            SwitchColor(2, 2);
            canvas.drawLine(pos("x", 45), pos("y", 64), pos("x", 42), pos("y", 70), paint);
        }

        //Spitze
        SwitchColor(2, 0);
        canvas.drawLine(pos("x", 41), pos("y", 70), pos("x", 43), pos("y", 70), paint);


        paint.setColor(black);
        if (Weichen[1]){
            paint.setStrokeWidth(gleisBreite);
            canvas.drawLine(pos("x", 41), pos("y", 70) - gleisBreite, pos("x", 45), pos("y", 70) - gleisBreite, paint);
        } else {
            paint.setStrokeWidth(3 * m);
            canvas.drawLine(pos("x", 41), pos("y", 70) - (float)(3*m/2 + gleisBreite/2), pos("x", 45), pos("y", 70) - (float)(3*m/2 + gleisBreite/2), paint);
            paint.setStrokeWidth(gleisBreite);
        }





        //Weiche "3"
        if (Weichen[2]){
            //abzweigend
            SwitchColor(3, 1);
            canvas.drawLine(pos("x", 50), pos("y", 20), pos("x", 47), pos("y", 20), paint);
            SwitchColor(3, 2);
            canvas.drawLine(pos("x", 45), pos("y", 26), pos("x", 42), pos("y", 20), paint);
        } else {
            //gerade
            SwitchColor(3, 1);
            canvas.drawLine(pos("x", 50), pos("y", 20), pos("x", 44), pos("y", 20), paint);
            SwitchColor(3, 2);
            canvas.drawLine(pos("x", 45), pos("y", 26), pos("x", 42), pos("y", 20), paint);
        }

        //Spitze
        SwitchColor(3, 0);
        canvas.drawLine(pos("x", 41), pos("y", 20), pos("x", 43), pos("y", 20), paint);


        paint.setColor(black);
        if (Weichen[2]){
            paint.setStrokeWidth(gleisBreite);
            canvas.drawLine(pos("x", 41), pos("y", 20) + gleisBreite, pos("x", 45), pos("y", 20) + gleisBreite, paint);
        } else {
            paint.setStrokeWidth(3 * m);
            canvas.drawLine(pos("x", 41), pos("y", 20) + (float)(3*m/2 + gleisBreite/2), pos("x", 45), pos("y", 20) + (float)(3*m/2 + gleisBreite/2), paint);
            paint.setStrokeWidth(gleisBreite);
        }





        //Weiche "4"
        if (Weichen[3]){
            //abzweigend
            SwitchColor(4, 1);
            canvas.drawLine(pos("x", 10), pos("y", 20), pos("x", 13), pos("y", 20), paint);
            SwitchColor(4, 2);
            canvas.drawLine(pos("x", 15), pos("y", 26), pos("x", 18), pos("y", 20), paint);
        } else {
            //gerade
            SwitchColor(4, 1);
            canvas.drawLine(pos("x", 10), pos("y", 20), pos("x", 16), pos("y", 20), paint);
            SwitchColor(4, 2);
            canvas.drawLine(pos("x", 15), pos("y", 26), pos("x", 18), pos("y", 20), paint);
        }

        //Spitze
        SwitchColor(4, 0);
        canvas.drawLine(pos("x", 17), pos("y", 20), pos("x", 19), pos("y", 20), paint);


        paint.setColor(black);
        if (Weichen[3]){
            paint.setStrokeWidth(gleisBreite);
            canvas.drawLine(pos("x", 15), pos("y", 20) + gleisBreite, pos("x", 19), pos("y", 20) + gleisBreite, paint);
        } else {
            paint.setStrokeWidth(3 * m);
            canvas.drawLine(pos("x", 15), pos("y", 20) + (float)(3*m/2 + gleisBreite/2), pos("x", 19), pos("y", 20) + (float)(3*m/2 + gleisBreite/2), paint);
            paint.setStrokeWidth(gleisBreite);
        }


//////////////////////////////////////////////////////////////////      Linien  Aussen (Wenn FA)     /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        paint.setColor(green);
        paint.setStrokeWidth(1);
        for(int i=0; i<9; i++){
            // W1
            if(fahrstrassenVerschluss[i][5] == 1 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 80), pos("y", 82), pos("x", 84), pos("y", 82), paint);
                canvas.drawLine(pos("x", 80), pos("y", 78), pos("x", 82), pos("y", 78), paint);
                canvas.drawLine(pos("x", 89), pos("y", 72), pos("x", 90), pos("y", 72), paint);
                canvas.drawLine(pos("x", 87), pos("y", 68), pos("x", 90), pos("y", 68), paint);
                canvas.drawLine(pos("x", 84), pos("y", 82), pos("x", 89), pos("y", 72), paint);
                canvas.drawLine(pos("x", 82), pos("y", 78), pos("x", 87), pos("y", 68), paint);
            }else if(fahrstrassenVerschluss[i][5] == 2 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 80), pos("y", 72), pos("x", 90), pos("y", 72), paint);
                canvas.drawLine(pos("x", 80), pos("y", 68), pos("x", 90), pos("y", 68), paint);
            }
            // W2
            if(fahrstrassenVerschluss[i][6] == 1 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 72), pos("x", 43), pos("y", 72), paint);
                canvas.drawLine(pos("x", 40), pos("y", 68), pos("x", 41), pos("y", 68), paint);
                canvas.drawLine(pos("x", 43), pos("y", 72), pos("x", 47), pos("y", 64), paint);
                canvas.drawLine(pos("x", 41), pos("y", 68), pos("x", 43), pos("y", 64), paint);
            }else if(fahrstrassenVerschluss[i][6] == 2 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 72), pos("x", 50), pos("y", 72), paint);
                canvas.drawLine(pos("x", 40), pos("y", 68), pos("x", 50), pos("y", 68), paint);
            }
            // W3
            if(fahrstrassenVerschluss[i][7] == 1 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 22), pos("x", 41), pos("y", 22), paint);
                canvas.drawLine(pos("x", 40), pos("y", 18), pos("x", 43), pos("y", 18), paint);
                canvas.drawLine(pos("x", 41), pos("y", 22), pos("x", 43), pos("y", 26), paint);
                canvas.drawLine(pos("x", 43), pos("y", 18), pos("x", 47), pos("y", 26), paint);
            }else if(fahrstrassenVerschluss[i][7] == 2 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 40), pos("y", 22), pos("x", 50), pos("y", 22), paint);
                canvas.drawLine(pos("x", 40), pos("y", 18), pos("x", 50), pos("y", 18), paint);
            }
            // W4
            if(fahrstrassenVerschluss[i][8] == 1 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 20), pos("y", 22), pos("x", 19), pos("y", 22), paint);
                canvas.drawLine(pos("x", 20), pos("y", 18), pos("x", 17), pos("y", 18), paint);
                canvas.drawLine(pos("x", 19), pos("y", 22), pos("x", 17), pos("y", 26), paint);
                canvas.drawLine(pos("x", 17), pos("y", 18), pos("x", 13), pos("y", 26), paint);
            }else if(fahrstrassenVerschluss[i][8] == 2 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 0), pos("y", 22), pos("x", 20), pos("y", 22), paint);
                canvas.drawLine(pos("x", 0), pos("y", 18), pos("x", 20), pos("y", 18), paint);
            }
            // A
            if(fahrstrassenVerschluss[i][9] == 1 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 50), pos("y", 72), pos("x", 80), pos("y", 72), paint);
                canvas.drawLine(pos("x", 50), pos("y", 68), pos("x", 80), pos("y", 68), paint);
            }
            // B
            if(fahrstrassenVerschluss[i][10] == 1 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 0), pos("y", 82), pos("x", 80), pos("y", 82), paint);
                canvas.drawLine(pos("x", 0), pos("y", 78), pos("x", 80), pos("y", 78), paint);
            }
            // C
            if(fahrstrassenVerschluss[i][11] == 1 && einzustellendeFahrstrassen[i] == 1){
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
            if(fahrstrassenVerschluss[i][12] == 1 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 90), pos("y", 22), pos("x", 50), pos("y", 22), paint);
                canvas.drawLine(pos("x", 90), pos("y", 18), pos("x", 50), pos("y", 18), paint);
            }
            // T
            if(fahrstrassenVerschluss[i][13] == 1 && einzustellendeFahrstrassen[i] == 1){
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
            if(fahrstrassenVerschluss[i][14] == 1 && einzustellendeFahrstrassen[i] == 1){
                canvas.drawLine(pos("x", 47), pos("y", 64), pos("x", 47), pos("y", 26), paint);
                canvas.drawLine(pos("x", 43), pos("y", 64), pos("x", 43), pos("y", 26), paint);
            }
        }
        paint.setStrokeWidth(gleisBreite);


//////////////////////////////////////////////////////////////////      Signale      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        //Signal "N1"
        SigColor(0);
        canvas.drawCircle(pos("x", 76), pos("y", 75), (float) 1.5 * m, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 76), pos("y", 75), pos("x", 71), pos("y", 75), paint);
        paint.setStrokeWidth(gleisBreite);
        canvas.drawLine(pos("x", 71), pos("y", 76), pos("x", 71), pos("y", 74), paint);


        //Signal "N2"
        SigColor(1);
        canvas.drawCircle(pos("x", 76), pos("y", 85), (float) 1.5 * m, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 76), pos("y", 85), pos("x", 71), pos("y", 85), paint);
        paint.setStrokeWidth(gleisBreite);
        canvas.drawLine(pos("x", 71), pos("y", 86), pos("x", 71), pos("y", 84), paint);


        //Signal "101"
        SigColor(2);
        canvas.drawCircle(pos("x", 94), pos("y", 15), (float) 1.5 * m, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 94), pos("y", 15), pos("x", 99), pos("y", 15), paint);
        paint.setStrokeWidth(gleisBreite);
        canvas.drawLine(pos("x", 99), pos("y", 16), pos("x", 99), pos("y", 14), paint);


        //Signal "102"
        SigColor(3);
        canvas.drawCircle(pos("x", 54), pos("y", 15), (float) 1.5 * m, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 54), pos("y", 15), pos("x", 59), pos("y", 15), paint);
        paint.setStrokeWidth(gleisBreite);
        canvas.drawLine(pos("x", 59), pos("y", 16), pos("x", 59), pos("y", 14), paint);


        //Signal "103"
        SigColor(4);
        canvas.drawCircle(pos("x", 50), pos("y", 30), (float) 1.5 * m, paint);
        paint.setStrokeWidth(6);
        canvas.drawLine(pos("x", 50), pos("y", 30), pos("x", 50), pos("y", 35), paint);
        paint.setStrokeWidth(gleisBreite);
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



        GleisText(canvas, "1", 65, 71);
        GleisText(canvas,"2", 45, 81);
        GleisText(canvas, "101", 103, 46);
        GleisText(canvas, "201", 69, 21);
        GleisText(canvas, "102", 13, 46);
        GleisText(canvas, "103", 43, 46);

        paint.setTextSize(m*3);
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

    public void TrColor(int gl1, int gl2){
        paint.setColor(yellow);
        boolean abschnitt2FS = false;
        for(int i=0; i<9; i++) {
            if (fahrstrassenVerschluss[i][gl2 + 9] == 1 && einzustellendeFahrstrassen[i] == 3) {
                abschnitt2FS = true; break;
            }
        }
        if(Belegtmeldung[gl1] && (gl1 != 4 || Belegtmeldung[gl2] || abschnitt2FS)){  // && Belegtmeldung[gl2]
            paint.setColor(red);
        } else {
            for(int i=0; i<9; i++) {
                if (fahrstrassenVerschluss[i][gl1 + 9] == 1 && gl1 != 4 &&
                        (einzustellendeFahrstrassen[i] == 2 || (einzustellendeFahrstrassen[i] == 3 && !Belegtmeldung[gl1]))) {
                    paint.setColor(green);
                } else if (fahrstrassenVerschluss[i][gl1 + 9] == 1 && fahrstrassenVerschluss[i][gl2 + 9] == 1 && gl1 == 4 &&
                        (einzustellendeFahrstrassen[i] == 2 || (einzustellendeFahrstrassen[i] == 3 && !Belegtmeldung[gl1] && !Belegtmeldung[gl2]))) {
                    paint.setColor(green);
                }
            }
        }


    }

    public void GlColor(int gl){
        if (Belegtmeldung[gl]){
            paint.setColor(red);
        } else {
            paint.setColor(yellow);
        }
        for(int i=0; i<9; i++) {
            if (fahrstrassenVerschluss[i][gl + 9] == 1 && (einzustellendeFahrstrassen[i] == 2 || (einzustellendeFahrstrassen[i] == 3 && !Belegtmeldung[gl]))) {
                paint.setColor(green);
            } else if (fahrstrassenVerschluss[i][gl + 9] == 1 && einzustellendeFahrstrassen[i] == 3 && Belegtmeldung[gl]) {
                paint.setColor(red);
            }
        }
    }

    public void SigColor(int sig){
        if (Signale[sig] == 0){
            paint.setColor(red);
        } else if (Signale[sig] == 1) {
            paint.setColor(yellow);
        } else if (Signale[sig] == 2) {
            paint.setColor(green);
        }
    }

    public void SwitchColor(int sw, int p){
        if (p == 0){   // Farbe für Weichenspitze
            for (int fahrstrasse=0; fahrstrasse<fahrstrassenVerschluss.length; fahrstrasse++) {
                if ((einzustellendeFahrstrassen[fahrstrasse] == 2 || einzustellendeFahrstrassen[fahrstrasse] == 3) && (fahrstrassenVerschluss[fahrstrasse][4+sw] == 1 || fahrstrassenVerschluss[fahrstrasse][4 + sw] == 2)) {
                    paint.setColor(green);
                    break;
                } else {
                    paint.setColor(white);
                }
            }
        } else if (p == 1){ // Farbe für Weiche gerade
            for (int fahrstrasse=0; fahrstrasse<fahrstrassenVerschluss.length; fahrstrasse++) {
                if ((einzustellendeFahrstrassen[fahrstrasse] == 2 || einzustellendeFahrstrassen[fahrstrasse] == 3) && fahrstrassenVerschluss[fahrstrasse][4+sw] == 2){
                    paint.setColor(green);
                    break;
                } else if (!Weichen[sw-1]){
                    paint.setColor(yellow);
                } else {
                    paint.setColor(white);
                }
            }
        } else if (p == 2){ // Farbe für Weiche abzweigend
            for (int fahrstrasse=0; fahrstrasse<fahrstrassenVerschluss.length; fahrstrasse++) {
                if ((einzustellendeFahrstrassen[fahrstrasse] == 2 || einzustellendeFahrstrassen[fahrstrasse] == 3) && fahrstrassenVerschluss[fahrstrasse][4+sw] == 1){
                    paint.setColor(green);
                    break;
                } else if (Weichen[sw-1]){
                    paint.setColor(yellow);
                } else {
                    paint.setColor(white);
                }
            }
        }
    }

    private void GleisText(Canvas canvas, String text, int x, int y) {
        paint.setTextSize(m*3);
        paint.setColor(black);
        paint.getFontMetrics(fm);
        int margin = 5;

        canvas.drawRect(pos("x", x) - margin,
                pos("y", y) + fm.top - margin,
                pos("x", x) + paint.measureText(text) + margin,
                pos("y", y) + fm.bottom + margin, paint);


        paint.setColor(Color.WHITE);
        canvas.drawText(text, pos("x", x), pos("y", y), paint);
    }

    public int pos(String Achse, double posWert) {
        ergebnis = 0.0;
        if (Achse.equals("x")) {
            ergebnis = posWert * m + xM;
        } else if (Achse.equals("y")) {
            ergebnis = posWert * m;
        }
        return (int)Math.round(ergebnis);
    }
}