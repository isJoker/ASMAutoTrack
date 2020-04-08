package com.jokerwan.testasm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.jokerwan.sdk.ScreenAutoTracker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by JokerWan on 2019-12-10.
 * Function:
 */
public class MainFragment extends Fragment implements ScreenAutoTracker {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        AppCompatButton button = view.findViewById(R.id.btn_fragment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public String getScreenUrl() {
        return "MainActivity/MainFragment";
    }

    @Override
    public JSONObject getTrackProperties() throws JSONException {
        return null;
    }
}
