package com.unistrong.demo.gps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.location.GpsSatellite;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * 该界面显示GPS和格莱纳斯的信号强度
 *
 * @author Administrator
 */
public class SingleView extends View {

    private final static int BOTTOM = 30;

    private int mWidth, mHeight;

    //private int mRow = 4, mColumn = 8; // 默认为6行6列
    private int mRow = 4, mColumn = 20; // 默认为6行6列
    private Paint mDefaultPaint;
    private Paint mBasePaint;
    private Paint mEffectPaint;
    private Paint mTextPaint;
    private int mLeftPadding = 0;
    private Paint mChartPaint;
    private Paint mTextPaintForSnr;

    private Iterable<GpsSatellite> sates;

    public SingleView(Context context) {
        this(context, null, 0);

    }

    public SingleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SingleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mDefaultPaint = new Paint();
        mDefaultPaint.setAntiAlias(true);

        mBasePaint = new Paint(mDefaultPaint);
        mBasePaint.setColor(Color.WHITE);
        mBasePaint.setStyle(Style.STROKE);

        mEffectPaint = new Paint(mBasePaint);
        DashPathEffect dp = new DashPathEffect(new float[]{2, 1, 2, 1}, 1);
        mEffectPaint.setPathEffect(dp);

        mTextPaint = new Paint(mDefaultPaint);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(15);

        mChartPaint = new Paint(mDefaultPaint);
        mChartPaint.setColor(Color.BLUE);
        mChartPaint.setStyle(Style.FILL);

        mTextPaintForSnr = new Paint(mDefaultPaint);
        mTextPaintForSnr.setTextSize(15);
        mTextPaintForSnr.setColor(Color.WHITE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBase(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = this.getMeasuredWidth();
        mHeight = this.getMeasuredHeight();
        //mWidth = 400;
        //mHeight = 400;
    }

    // 初始化界面，
    private void drawBase(Canvas canvas) {
        if (mWidth == 0 || mHeight == 0) {
            return;
        }
        drawEffectLine(canvas);
    }

    private void drawText(Canvas canvas) {
        int everyRowHeight = (mHeight - BOTTOM) / mRow;
        for (int i = 0; i < mRow; i++) {
            canvas.drawText((i * 15 <= 0 ? "0" + i * 15 : i * 15) + " db", 0,
                    mHeight - BOTTOM - everyRowHeight * i, mTextPaint);
        }
    }

    // 画虚线
    private void drawEffectLine(Canvas canvas) {
        int everyRowHeight = (mHeight - BOTTOM) / mRow;
        int startPosition = mHeight - BOTTOM - everyRowHeight;

        // 显示GPS卫星信号
        int gpsWidth = (mWidth - mLeftPadding) / mColumn;
        int gpsStartWidth = gpsWidth + mLeftPadding;
        int gpsChartWidth = mLeftPadding;

        if (sates != null) {
            for (GpsSatellite gs : sates) {

                int snr = (int) gs.getSnr();
                int prn = gs.getPrn();
                boolean fix = gs.usedInFix();

                //System.out.println("leilei:fix = " + fix);
                if (!fix) {
                    mChartPaint.setColor(Color.LTGRAY);
                } else {
                    if (snr < 15) {
                        mChartPaint.setColor(Color.rgb(254, 97, 3));
                    } else if (snr < 30 && snr >= 15) {
                        mChartPaint.setColor(Color.rgb(254, 168, 2));
                    } else if (snr < 45 && snr >= 30) {
                        mChartPaint.setColor(Color.rgb(252, 254, 3));
                    } else if (snr >= 45) {
                        mChartPaint.setColor(Color.rgb(27, 254, 3));

                    }
                }

                canvas.drawRect(new Rect(gpsChartWidth + 2, mHeight - BOTTOM
                        - (int) (snr * everyRowHeight / 15), gpsChartWidth
                        + gpsWidth - 2, mHeight - BOTTOM), mChartPaint);

                String snrStr = snr < 10 ? "0" + snr : snr + "";
                canvas.drawText(
                        snrStr,
                        gpsChartWidth
                                + (gpsWidth - mTextPaintForSnr
                                .measureText(snrStr)) / 2, mHeight
                                - BOTTOM - (int) (snr * everyRowHeight / 15)
                                - 10, mTextPaintForSnr);
                String prnStr = prn < 10 ? "0" + prn : prn + "";
                canvas.drawText(
                        prnStr,
                        gpsChartWidth
                                + (gpsWidth - mTextPaintForSnr
                                .measureText(prnStr)) / 2, mHeight
                                - BOTTOM + 15, mTextPaintForSnr);
                gpsChartWidth += gpsWidth;
                //Log.e("WGP","MMMMMMMMMMMMMMMMMM="+snrStr+","+prnStr);
            }
        }
        //if (sates != null)
        /*{
			for (int i = 0; i < 20; i++) {
				
				int snr = 30;
				int prn = 15;
				boolean fix = true;

				//System.out.println("leilei:fix = " + fix);
				if (!fix) {
					mChartPaint.setColor(Color.LTGRAY);
				} else {
					if (snr < 15) {
						mChartPaint.setColor(Color.rgb(254, 97, 3));
					} else if (snr < 30 && snr >= 15) {
						mChartPaint.setColor(Color.rgb(254, 168, 2));
					} else if (snr < 45 && snr >= 30) {
						mChartPaint.setColor(Color.rgb(252, 254, 3));
					} else if (snr >= 45) {
						mChartPaint.setColor(Color.rgb(27, 254, 3));

					}
				}

				canvas.drawRect(new Rect(gpsChartWidth + 2, mHeight - BOTTOM
						- (int) (snr * everyRowHeight / 15), gpsChartWidth
						+ gpsWidth - 2, mHeight - BOTTOM), mChartPaint);

				String snrStr = snr < 10 ? snr + "0" : snr + "";
				canvas.drawText(
						snrStr,
						gpsChartWidth
								+ (gpsWidth - mTextPaintForSnr
										.measureText(snrStr)) / 2, mHeight
								- BOTTOM - (int) (snr * everyRowHeight / 15)
								- 10, mTextPaintForSnr);
				String prnStr = prn < 10 ? "0" + prn : prn + "";
				canvas.drawText(
						prnStr,
						gpsChartWidth
								+ (gpsWidth - mTextPaintForSnr
										.measureText(prnStr)) / 2, mHeight
								- BOTTOM + 15, mTextPaintForSnr);
				gpsChartWidth += gpsWidth;

			}
		}*/
    }

    private int index = 0;

    public void updateView(List<GpsSatellite> sates) {
        if (sates == null) {
            return;
        }
        int size = sates.size();
        if (size > 20) {
            mColumn = size;
        }
        this.sates = sates;
        this.invalidate();
    }
}