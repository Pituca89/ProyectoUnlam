package com.example.adagiom.bepim;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SectorFragment extends Fragment {

    public SectorFragment() {
        // Required empty public constructor
    }


    public static SectorFragment newInstance(String param1, String param2) {
        SectorFragment fragment = new SectorFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sector, container, false);
    }

}
