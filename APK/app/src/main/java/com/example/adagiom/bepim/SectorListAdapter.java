package com.example.adagiom.bepim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

public class SectorListAdapter extends BaseAdapter{
    private LayoutInflater layoutInflater;
    private List<Sector> mData;
    private OnActionSector actionSector;
    private int selectedPosition = 0;

    public SectorListAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
    }

    public void setData(List<Sector> data){
        mData = data;
    }

    public List<Sector> getData() {
        return mData;
    }

    public void setListener(OnActionSector listener){
        actionSector = listener;
    }

    @Override
    public int getCount() {
        return (mData == null) ? 0: mData.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewSector viewSector = null;
        if(convertView == null){
            viewSector = new ViewSector();
            convertView = layoutInflater.inflate(R.layout.item_sector_list,null);

            viewSector.radioButton = (RadioButton) convertView.findViewById(R.id.radioButton);
            viewSector.sector_list_name = (TextView) convertView.findViewById(R.id.sector_list_name);
            viewSector.delete = (ImageView) convertView.findViewById(R.id.delete);
            viewSector.edit = (ImageView) convertView.findViewById(R.id.edit);
            convertView.setTag(viewSector);
        }else{
            viewSector = (ViewSector) convertView.getTag();
        }
        Sector sector = mData.get(position);
        viewSector.radioButton.setChecked(sector.getActual() == 1);
        viewSector.radioButton.setTag(position);
        viewSector.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //selectedPosition = (int) view.getTag();
                if(actionSector != null) {
                    actionSector.onActionChange(position);
                    notifyDataSetChanged();
                }
            }
        });


        viewSector.sector_list_name.setText(sector.getNombre());
        viewSector.sector_list_name.setTag(sector.getId());
        viewSector.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionSector != null){
                    actionSector.onActionDelete(position);
                }
            }
        });
        viewSector.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionSector != null){
                    actionSector.onActionEdit(position);
                }
            }
        });
        return convertView;
    }
    static class ViewSector {
        TextView sector_list_name;
        ImageView delete;
        ImageView edit;
        RadioButton radioButton;
    }
    public interface OnActionSector{
        public abstract void onActionEdit(int position);
        public abstract void onActionDelete(int position);
        public abstract void onActionChange(int position);
    }

}
