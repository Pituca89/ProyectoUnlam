package com.example.adagiom.bepim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class SectorTrainingAdapter extends BaseAdapter{
    private LayoutInflater layoutInflater;
    private List<Sector> mData;
    private OnSelectFinDestino onSelectFinDestino;

    public SectorTrainingAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
    }

    public void setData(List<Sector> data){
        mData = data;
    }

    public List<Sector> getData() {
        return mData;
    }

    public void setListener(OnSelectFinDestino listener){
        onSelectFinDestino = listener;
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
            convertView = layoutInflater.inflate(R.layout.item_sector_training,null);

            viewSector.sector_name = (TextView) convertView.findViewById(R.id.sector_name);
            convertView.setTag(viewSector);
        }else{
            viewSector = (ViewSector) convertView.getTag();
        }

        Sector sector = mData.get(position);
        viewSector.sector_name.setText(sector.getNombre());
        viewSector.sector_name.setTag(sector.getId());
        viewSector.sector_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onSelectFinDestino != null){
                    onSelectFinDestino.enviarPlataformaClick(position);
                }
            }
        });
        return convertView;
    }
    static class ViewSector {
        TextView sector_name;
    }
    public interface OnSelectFinDestino{
        public abstract void enviarPlataformaClick(int position);
    }
}
