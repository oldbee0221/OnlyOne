package mms5.onepagebook.com.onlyonesms.common;

import android.support.v4.view.ViewPager;
import android.view.View;

public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-무한대,-1)
            // 이 페이지는 left off-screen
            view.setAlpha(0);

        } else if (position <= 1) { // [-1,1]
            // 기본 슬라이드 전환효과를 페이지가 축소되는 효과로 수정
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;

            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // 페이지 배율을 작게 조정 (MIN_SCALE과 1 사이)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // 페이지 크기에 따라 페이드 효과를 줌.
            view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else { // (1,+Infinity]
            // 이 페이지는 right off-screen
            view.setAlpha(0);
        }
    }
}

