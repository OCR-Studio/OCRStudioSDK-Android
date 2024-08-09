/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.R;

import java.util.Map;

/**
 * Adapter for ListView
 */
public  class ResultAdapter extends BaseAdapter {

    private ResultModel.ResultData data = new ResultModel.ResultData(); // can't be null
    public void setData(ResultModel.ResultData data){this.data = data;}

    private final Context context;
    private final LayoutInflater inflater;

    // must be called in Activity.onCreate
    public ResultAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    /**
     * We use a special view template for each item type
     * so we must implement this method
     */
    @Override
    public int getItemViewType(int position) {
        ResultItem item = data.mData.get(position);
        if(item instanceof ResultItemField)     return  1;
        if(item instanceof ResultItemImage)     return  2;
        if(item instanceof ResultItemSection)   return  3;
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return data.mData.size();
    }

    @Override
    public Object getItem(int position) {
        return data.mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ResultItem item = data.mData.get(position);

        // Fill template
        if (item instanceof ResultItemField) {
            // Get/create view
            if (view == null)
                view = inflater.inflate(R.layout.result_row_field, null);
            // Get data
            ResultItemField field = (ResultItemField) item;
            // DATA => VIEW
            ((TextView) view.findViewById(R.id.name)).setText(field.name);
            ((TextView) view.findViewById(R.id.val)).setText(field.value);
            ((TextView) view.findViewById(R.id.isAccepted)).setText(
                    field.isAccepted != null ? String.valueOf(field.isAccepted) : ""
            );
            // Load Attributes
            LinearLayout l = view.findViewById(R.id.attributes);
            l.removeAllViews();
            if (field.attr != null) {
                for (Map.Entry<String, String> attr : field.attr.entrySet()) {
                    TextView at = new TextView(context);
                    at.setText(attr.getKey() + ":" + attr.getValue());
                    at.setTextSize(10);
                    l.addView(at);
                }
            }
        }

        if (item instanceof ResultItemImage) {
            // Get/create view
            if (view == null)
                view = inflater.inflate(R.layout.result_row_image, null);
            // Get data
            ResultItemImage img = (ResultItemImage) item;
            // DATA => VIEW
            // name
            ((TextView) view.findViewById(R.id.name)).setText(img.name);
            // Bitmap
            Bitmap bitmap = img.bitmap;
            if (bitmap != null)
                ((ImageView) view.findViewById(R.id.imageView)).setImageBitmap(bitmap);
            // Attributes
            LinearLayout ll = view.findViewById(R.id.imgAttributes);
            ll.removeAllViews();
            if (img.attr != null) {
                for (Map.Entry<String, String> attr : img.attr.entrySet()) {
                    TextView at = new TextView(context);
                    at.setText(attr.getKey() + ":" + attr.getValue());
                    at.setTextSize(10);
                    ll.addView(at);
                }
            }
        }

        if (item instanceof ResultItemSection) {
            // Get/create view
            if (view == null)
                view = inflater.inflate(R.layout.result_header, null);
            // Get data
            ResultItemSection section = (ResultItemSection) item;
            // DATA => VIEW
            ((TextView) view.findViewById(R.id.header)).setText(section.name);
        }

        return view;
    }

}
