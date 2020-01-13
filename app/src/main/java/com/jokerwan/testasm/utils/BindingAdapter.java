package com.jokerwan.testasm.utils;

import android.view.View;

import com.google.gson.Gson;
import com.jokerwan.testasm.R;

/**
 * Created by JokerWan on 2020-01-13.
 * Function:
 */
public class BindingAdapter {

    @androidx.databinding.BindingAdapter(value = {"BindingTag"})
    public static void setTagData(View view, Object model) {
        view.setTag(R.id.key_binding_tag, new Gson().toJson(model));
    }
}
