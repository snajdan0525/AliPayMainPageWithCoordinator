package com.snalopainen.coordinatorlayout.alipay.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by jinyan on 16/9/23.
 */

public class ImageSlider extends Slider {

    private int placeHolderId;
    private ImageView.ScaleType placeHolderScaleType = ImageView.ScaleType.CENTER_INSIDE;

    public ImageSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImages(ArrayList<String> imageUris) {
        final ArrayList<String> uris = imageUris == null ? null : new ArrayList<>(imageUris);

        setSliderProvider(new SliderProvider() {
            @Override
            public int getCount() {
                return uris == null ? 0 : uris.size();
            }

            @Override
            public View getView(Context context, int position, View convertView) {
                BqImageView bqImageView = new BqImageView(context);

                bqImageView.suggestResize(getWidth(), getHeight());
                if (placeHolderId != 0) {
                    bqImageView.placeholder(placeHolderId, placeHolderScaleType);
                }
                bqImageView.scaleType(ImageView.ScaleType.FIT_XY).load(uris.get(position));
                return bqImageView;
            }
        });
    }

    public void setPlaceHolderId(int placeHolderId) {
        this.placeHolderId = placeHolderId;
    }

    public void setPlaceHolderId(int placeHolderId, ImageView.ScaleType scaleType) {
        this.placeHolderId = placeHolderId;
        this.placeHolderScaleType = scaleType;
    }


    public void setImagesWithResIDs(ArrayList<Integer> imagesResID ) {
        final ArrayList<Integer> resIDs = imagesResID == null ? null : new ArrayList<>(imagesResID);

        setSliderProvider(new SliderProvider() {
            @Override
            public int getCount() {
                return resIDs == null ? 0 : resIDs.size();
            }

            @Override
            public View getView(Context context, int position, View convertView) {
                ImageView bqImageView = new ImageView(context);

//                bqImageView.suggestResize(getWidth(), getHeight());
//                if (placeHolderId != 0) {
//                    bqImageView.placeholder(placeHolderId, placeHolderScaleType);
//                }
//                bqImageView.scaleType(ImageView.ScaleType.FIT_XY).loadRes(resIDs.get(position));
                bqImageView.setImageResource(resIDs.get(position));
                bqImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                return bqImageView;
            }
        });
    }
}
