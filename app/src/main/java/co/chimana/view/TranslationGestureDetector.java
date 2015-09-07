/*
*   Based on:
*   http://chicketen.blog.jp/archives/1579621.html
*   http://chicketen.blog.jp/archives/1622120.html
* */

package co.chimana.view;

import android.view.MotionEvent;
import android.view.View;

public class TranslationGestureDetector {
    private TranslationGestureListener mListener;
    private float mX, mY; // タッチイベント時の座標
    private int mPointerID1, mPointerID2; // ポインタID記憶用

    public TranslationGestureDetector(TranslationGestureListener listener)
    {
        mListener = listener;
    }

    /**
     * タッチ処理
     */
    public boolean onTouch(View v, MotionEvent event) {
        int eventAction = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                // 最初の指の設定
                mPointerID1 = pointerId;
                mPointerID2 = -1;
                mX = x;
                mY = y;
                mListener.onTranslationBegin(this);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                // 3本目の指以降は無視する
                if (mPointerID2 == -1)
                {
                    mPointerID2 = pointerId;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (mPointerID1 == pointerId)
                {
                    mPointerID1 = -1;
                    mX = x;
                    mY = y;
                    mListener.onTranslationEnd(this);
                }
                else if (mPointerID2 == pointerId)
                {
                    mPointerID2 = -1;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mPointerID1 != -1)
                {
                    mX = x;
                    mY = y;
                    mListener.onTranslationEnd(this);
                }
                mPointerID1 = -1;
                mPointerID2 = -1;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mPointerID1 >= 0 && mPointerID2 == -1)
                {
                    int ptrIndex = event.findPointerIndex(mPointerID1);
                    mX = event.getX(ptrIndex);
                    mY = event.getY(ptrIndex);

                    mListener.onTranslation(this);
                }

                if (mPointerID1 >= 0)
                {
                    int ptrIndex = event.findPointerIndex(mPointerID1);
                    mX = event.getX(ptrIndex);
                    mY = event.getY(ptrIndex);
                }
                break;
        }

        mX = x;
        mY = y;
        mListener.onTranslationBegin(this);

        return true;
    }

    public float getX()
    {
        return mX;
    }

    public float getY()
    {
        return mY;
    }
}
