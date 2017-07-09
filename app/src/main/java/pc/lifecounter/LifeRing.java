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
    private int startLife = 20;
    private float viewHeight;
    private float viewWidth;
    private float radius;
    private float circlePortion = 1;
    private float currentPortion = 1;

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
        base.set(0, 0, radius * 2, radius * 2);
        base.offset(radius, (viewHeight - radius * 2)/2);
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
    }

    private float lerp(float value1, float value2, float amount) {
        return value1 * (1 - amount) + (value2 * amount);
    }
}
