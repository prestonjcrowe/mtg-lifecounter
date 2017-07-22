package pc.lifecounter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import static android.graphics.Color.rgb;

/**
 * Created by prestoncrowe on 6/29/17.
 */

public class LifeRing extends View {
    private final Paint PAINT = new Paint();
    private RectF base = new RectF(0, 0, 0, 0);
    private RectF outerArc = new RectF(0, 0, 0, 0);
    private int startLife = 20;
    private float viewHeight;
    private float viewWidth;
    private float radius;
    private float outerRadius;
    private float circlePortion = 1;
    private float currentPortion = 1;
    private float recentPortion = 1;
    private boolean commanderMode = false;
    private int outerColor = rgb(255, 255, 255);

    public LifeRing(Context context) {
        super(context);
        setFocusable(true);
    }
    public LifeRing(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LifeRing(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setLife(int life) {
        circlePortion = (float) life / startLife;
    }

    public void setStart(int start) {
        startLife = start;
        circlePortion = 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        viewHeight = canvas.getHeight();
        viewWidth = canvas.getWidth();
        radius = viewWidth / 4;
        outerRadius = radius * 1.25f;
        base.set(0, 0, radius * 2, radius * 2);
        outerArc.set(0, 0, outerRadius * 2, outerRadius * 2);
        base.offset(radius, (viewHeight - radius * 2)/2);
        outerArc.offset(outerRadius, (viewHeight - outerRadius * 2)/2);
        outerArc = scale(base, 1.09f);
        canvas.drawColor(0); //
        PAINT.setAntiAlias(true);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(4);
        PAINT.setColor(Color.WHITE);

        currentPortion = lerp(currentPortion, circlePortion, 0.1f);
        if (Math.abs(circlePortion - currentPortion) <= 0.001f) {
            currentPortion = circlePortion;
        }
        if (currentPortion >= 1) {
            PAINT.setColor(rgb(35, 255, 123));
            canvas.drawOval(base, PAINT);
            PAINT.setColor(Color.WHITE);
            canvas.drawArc(base, 270, -(360 * currentPortion % 360), false, PAINT);
        } else {
            canvas.drawArc(base, 270, -(360 * currentPortion), false, PAINT);
        }
        if (commanderMode) {
            PAINT.setColor(outerColor);
            canvas.drawArc(outerArc, 270, 360, false, PAINT);
            //PAINT.setColor(Color.RED);
            // start at last currentPortion, save that
            // sweep angle -> (currentPortion
            System.out.println("recent: " + recentPortion);
            System.out.println("circle " + circlePortion);
            //canvas.drawArc(outerArc, recentPortion * 360,  (360 * (recentPortion - currentPortion)), false, PAINT);
        }
    }

    private float lerp(float value1, float value2, float amount) {
        return value1 * (1 - amount) + (value2 * amount);
    }

    private RectF scale(RectF rect, float factor){
        float diffHorizontal = (rect.right-rect.left) * (factor-1f);
        float diffVertical = (rect.bottom-rect.top) * (factor-1f);

        float top = rect.top - diffVertical/2f;
        float bottom = rect.bottom + diffVertical/2f;
        float left = rect.left - diffHorizontal/2f;
        float right = rect.right + diffHorizontal/2f;
        return new RectF(left, top, right, bottom);
    }

    public void setOuterColor(int c){
        outerColor = c;
    }

    public void setCommander(boolean mode) {
        System.out.println("recent: " + recentPortion);
        System.out.println("circle " + circlePortion);
        recentPortion = circlePortion;
        commanderMode = mode;
    }

    public boolean getCommander() {
        return commanderMode;
    }
}
