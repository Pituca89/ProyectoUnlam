package com.example.adagiom.bepim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class PlataformaAdapter extends BaseAdapter{
    private LayoutInflater layoutInflater;
    private List<Plataforma> mData;
    private PlataformaAdapter.OnSelectPlataforma onSelectPlataforma;

    public PlataformaAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
    }

    public void setData(List<Plataforma> data){
        mData = data;
    }

    public void setListener(PlataformaAdapter.OnSelectPlataforma listener){
        onSelectPlataforma = listener;
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
        PlataformaAdapter.ViewPlataforma viewPlataforma = null;
        if(convertView == null){
            viewPlataforma = new PlataformaAdapter.ViewPlataforma();
            convertView = layoutInflater.inflate(R.layout.item_plataforma,null);

            viewPlataforma.plataforma_name = (TextView) convertView.findViewById(R.id.plataforma_name);
            viewPlataforma.plataforma_select = (Button) convertView.findViewById(R.id.plataformar_select);
            convertView.setTag(viewPlataforma);
        }else{
            viewPlataforma = (PlataformaAdapter.ViewPlataforma) convertView.getTag();
        }

        Plataforma plataforma = mData.get(position);
        viewPlataforma.plataforma_name.setText(Integer.toString(plataforma.getChipid()));
        if(plataforma.getDisponible() == 1) {
            viewPlataforma.plataforma_select.setEnabled(true);
            viewPlataforma.plataforma_select.setText("DISPONIBLE");
            viewPlataforma.plataforma_select.setBackgroundResource(R.drawable.sombra);
            viewPlataforma.plataforma_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onSelectPlataforma != null) {
                        onSelectPlataforma.selectPlataforma(position);
                    }
                }
            });
        }else{
            viewPlataforma.plataforma_select.setEnabled(false);
            viewPlataforma.plataforma_select.setText("OCUPADA");
            viewPlataforma.plataforma_select.setBackgroundResource(R.drawable.sombra_ocupado);
        }
        return convertView;
    }
    static class ViewPlataforma {
        TextView plataforma_name;
        Button plataforma_select;
    }

    public interface OnSelectPlataforma{
        public abstract void selectPlataforma(int position);
    }
}
