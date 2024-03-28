/*
  Copyright (c) 2024-2024, OCR Studio
  All rights reserved.
*/
package ai.ocrstudio.sdk.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.R;
import ai.ocrstudio.sdk.ResultStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public  class ResultAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SECTION = 1;
    private static final int TYPE_IMG = 2;
    private static final int TYPE_TABLE = 3;

    private Context _context;

    ArrayList <Object> mData = new ArrayList<>();
    ArrayList <String> types = new ArrayList<>();

    private LayoutInflater mInflater;

    public ResultAdapter(Context context) {

        _context = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        mData.clear();
        types.clear();
        notifyDataSetChanged();

    }

    public void addItem(Pair<String, ResultStore.FieldInfo> item, String type) {
        mData.add(item);
        types.add(type);
        notifyDataSetChanged();
    }

    public void addItem(String item, String type) {
        mData.add(item);
        types.add(type);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        switch (types.get(position)) {
            case "field":
                return TYPE_ITEM;
            case "section":
                return TYPE_SECTION;
            case "image":
                return TYPE_IMG;
            case "table":
                return TYPE_TABLE;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int rowType = getItemViewType(position);

        // Choose template
        if (convertView == null) {

            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.result_row_field, null);
                    break;
                case TYPE_SECTION:
                    convertView = mInflater.inflate(R.layout.result_header, null);
                    break;
                case TYPE_IMG:
                    convertView = mInflater.inflate(R.layout.result_row_image, null);
                    break;
                case TYPE_TABLE:
                    convertView = mInflater.inflate(R.layout.result_row_table, null);
                    break;
            }
        }

        // Fill template
        switch (rowType) {
            case TYPE_ITEM:

                Pair <String, ResultStore.FieldInfo> el = (Pair<String, ResultStore.FieldInfo>) mData.get(position);

                ((TextView) convertView.findViewById(R.id.name)).setText(el.first);
                ((TextView) convertView.findViewById(R.id.val)).setText(el.second.value);
                ((TextView) convertView.findViewById(R.id.isAccepted)).setText(String.valueOf(el.second.isAccepted));

                LinearLayout l = convertView.findViewById(R.id.attributes);
                l.removeAllViews();

                for (Map.Entry<String, String> attr : el.second.attr.entrySet()) {
                    TextView at = new TextView(_context);
                    at.setText(attr.getKey() +":"+attr.getValue());
                    at.setTextSize(10);
                    l.addView(at);
                }

                break;
            case TYPE_IMG:
                Pair<String, ResultStore.FieldInfo> img = (Pair<String, ResultStore.FieldInfo>) mData.get(position);

                ((TextView) convertView.findViewById(R.id.name)).setText(img.first);

                byte[] bytes = Base64.decode(img.second.value, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ((ImageView) convertView.findViewById(R.id.imageView)).setImageBitmap(bmp);

                LinearLayout ll = convertView.findViewById(R.id.imgAttributes);
                ll.removeAllViews();

                for (Map.Entry<String, String> attr : img.second.attr.entrySet()) {
                    TextView at = new TextView(_context);
                    at.setText(attr.getKey() +":"+attr.getValue());
                    at.setTextSize(10);
                    ll.addView(at);
                }

                break;

            case TYPE_TABLE:
                Pair<String, ResultStore.FieldInfo> table = (Pair<String, ResultStore.FieldInfo>) mData.get(position);
                String value = table.second.value;

                LinearLayout container = convertView.findViewById(R.id.container);
                container.removeAllViews();

                HorizontalScrollView horizontalScrollView = new HorizontalScrollView(_context);
                horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                try {
                    JSONObject json = new JSONObject(value);
                    JSONArray fields = json.getJSONArray("fields");

                    TableLayout tableLayout = new TableLayout(_context);
                    tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    tableLayout.setStretchAllColumns(true);
                    tableLayout.setDividerDrawable(ContextCompat.getDrawable(_context, R.drawable.divider));
                    tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);

                    for (int i = 0; i < fields.length(); i++) {
                        JSONArray row = fields.getJSONArray(i);

                        TableRow tableRow = new TableRow(_context);
                        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                        for (int j = 0; j < row.length(); j++) {
                            String cellText = row.getString(j);

                            TextView textView = new TextView(_context);
                            textView.setText(cellText);
                            textView.setPadding(16, 16, 16, 16);
                            textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                            textView.setSingleLine(false);
                            textView.setEllipsize(TextUtils.TruncateAt.END);
                            textView.setMaxLines(4);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

                            TableRow.LayoutParams params = (TableRow.LayoutParams) textView.getLayoutParams();
                            params.width = 500; // Set a fixed width for the columns
                            textView.setLayoutParams(params);

                            tableRow.addView(textView);
                        }

                        tableLayout.addView(tableRow);
                    }

                    horizontalScrollView.addView(tableLayout);


                    container.addView(horizontalScrollView);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;


            case TYPE_SECTION:
                ((TextView) convertView.findViewById(R.id.header)).setText(mData.get(position).toString());
                break;
        }

        return convertView;
    }

}
