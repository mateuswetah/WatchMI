package mateuswetah.wearablebraille.BrailleÉcran;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.wearable.view.BoxInsetLayout;
import android.util.AttributeSet;

public class MyBoxInsetLayout extends BoxInsetLayout {
    public MyBoxInsetLayout(Context context) {
        super(context);
    }

    public MyBoxInsetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyBoxInsetLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        Path path = new Path();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.TRANSPARENT);
        canvas.drawPaint(paint);
        for (int i = 50; i < 100; i++) {
            path.moveTo(i, i-1);
            path.lineTo(i, i);
        }
        path.close();
        paint.setStrokeWidth(3);
        paint.setPathEffect(null);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
    }
}
