package com.jokerwan.sdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ToggleButton;

import androidx.annotation.Keep;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.jokerwan.sdk.utils.AopUtil;
import com.jokerwan.sdk.utils.ViewUtil;
import com.jokerwan.sdk.utils.VisualUtil;

import org.json.JSONObject;

import java.util.Locale;


public class JokerDataAutoTrackHelper {
    @Keep
    public static void trackViewOnClick(DialogInterface dialogInterface, int whichButton) {
        try {
            Dialog dialog = null;
            if (dialogInterface instanceof Dialog) {
                dialog = (Dialog) dialogInterface;
            }

            if (dialog == null) {
                return;
            }

            Context context = dialog.getContext();
            //将Context转成Activity
            Activity activity = JokerDataPrivate.getActivityFromContext(context);

            if (activity == null) {
                activity = dialog.getOwnerActivity();
            }

            JSONObject properties = new JSONObject();
            //$screen_name & $title
            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }

            Button button = null;
            if (dialog instanceof android.app.AlertDialog) {
                button = ((android.app.AlertDialog) dialog).getButton(whichButton);
            } else if (dialog instanceof AlertDialog) {
                button = ((AlertDialog) dialog).getButton(whichButton);
            }

            if (button != null) {
                properties.put("$element_content", button.getText());
            }

            properties.put("$element_type", "Dialog");

            JokerDataAPI.getInstance().track("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackViewOnClick(CompoundButton view, boolean isChecked) {
        try {
            Context context = view.getContext();
            if (context == null) {
                return;
            }

            JSONObject properties = new JSONObject();

            Activity activity = JokerDataPrivate.getActivityFromContext(context);

            try {
                String idString = context.getResources().getResourceEntryName(view.getId());
                if (!TextUtils.isEmpty(idString)) {
                    properties.put("$element_id", idString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }

            String viewText = null;
            String viewType;
            if (view instanceof CheckBox) {
                viewType = "CheckBox";
                CheckBox checkBox = (CheckBox) view;
                if (!TextUtils.isEmpty(checkBox.getText())) {
                    viewText = checkBox.getText().toString();
                }
            } else if (view instanceof SwitchCompat) {
                viewType = "SwitchCompat";
                SwitchCompat switchCompat = (SwitchCompat) view;
                if (isChecked) {
                    if (!TextUtils.isEmpty(switchCompat.getTextOn())) {
                        viewText = switchCompat.getTextOn().toString();
                    }
                } else {
                    if (!TextUtils.isEmpty(switchCompat.getTextOff())) {
                        viewText = switchCompat.getTextOff().toString();
                    }
                }
            } else if (view instanceof ToggleButton) {
                viewType = "ToggleButton";
                ToggleButton toggleButton = (ToggleButton) view;
                if (isChecked) {
                    if (!TextUtils.isEmpty(toggleButton.getTextOn())) {
                        viewText = toggleButton.getTextOn().toString();
                    }
                } else {
                    if (!TextUtils.isEmpty(toggleButton.getTextOff())) {
                        viewText = toggleButton.getTextOff().toString();
                    }
                }
            } else if (view instanceof RadioButton) {
                viewType = "RadioButton";
                RadioButton radioButton = (RadioButton) view;
                if (!TextUtils.isEmpty(radioButton.getText())) {
                    viewText = radioButton.getText().toString();
                }
            } else {
                viewType = view.getClass().getCanonicalName();
            }

            //Content
            if (!TextUtils.isEmpty(viewText)) {
                properties.put("$element_content", viewText);
            }

            if (!TextUtils.isEmpty(viewType)) {
                properties.put("$element_type", viewType);
            }

            properties.put("isChecked", isChecked);

            JokerDataAPI.getInstance().track("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Keep
    public static void trackViewOnClick(DialogInterface dialogInterface, int whichButton, boolean isChecked) {
        try {
            Dialog dialog = null;
            if (dialogInterface instanceof Dialog) {
                dialog = (Dialog) dialogInterface;
            }

            if (dialog == null) {
                return;
            }

            Context context = dialog.getContext();
            //将Context转成Activity
            Activity activity = JokerDataPrivate.getActivityFromContext(context);

            if (activity == null) {
                activity = dialog.getOwnerActivity();
            }

            JSONObject properties = new JSONObject();
            //$screen_name & $title
            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }

            ListView listView = null;
            if (dialog instanceof android.app.AlertDialog) {
                listView = ((android.app.AlertDialog) dialog).getListView();
            } else if (dialog instanceof AlertDialog) {
                listView = ((AlertDialog) dialog).getListView();
            }

            if (listView != null) {
                ListAdapter listAdapter = listView.getAdapter();
                Object object = listAdapter.getItem(whichButton);
                if (object != null) {
                    if (object instanceof String) {
                        properties.put("$element_content", object);
                    }
                }
            }

            properties.put("isChecked", isChecked);
            properties.put("$element_type", "Dialog");

            JokerDataAPI.getInstance().track("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MenuItem 被点击，自动埋点
     *
     * @param object   Object
     * @param menuItem MenuItem
     */
    @Keep
    public static void trackViewOnClick(Object object, MenuItem menuItem) {
        try {
            Context context = null;
            if (object instanceof Context) {
                context = (Context) object;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$element_type", "menuItem");

            jsonObject.put("$element_content", menuItem.getTitle());

            if (context != null) {
                String idString = null;
                try {
                    idString = context.getResources().getResourceEntryName(menuItem.getItemId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(idString)) {
                    jsonObject.put("$element_id", idString);
                }

                Activity activity = JokerDataPrivate.getActivityFromContext(context);
                if (activity != null) {
                    jsonObject.put("$activity", activity.getClass().getCanonicalName());
                }
            }

            JokerDataAPI.getInstance().track("$AppClick", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Keep
    public static void trackTabHost(String tabName) {
        try {
            JSONObject properties = new JSONObject();

            properties.put("$element_type", "TabHost");
            properties.put("$element_content", tabName);
            JokerDataAPI.getInstance().track("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Keep
    public static void trackExpandableListViewGroupOnClick(ExpandableListView expandableListView, View view,
                                                           int groupPosition) {
        trackExpandableListViewChildOnClick(expandableListView, view, groupPosition, -1);
    }

    @Keep
    public static void trackExpandableListViewChildOnClick(ExpandableListView expandableListView, View view,
                                                           int groupPosition, int childPosition) {
        try {
            Context context = expandableListView.getContext();
            if (context == null) {
                return;
            }

            JSONObject properties = new JSONObject();
            Activity activity = JokerDataPrivate.getActivityFromContext(context);
            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }

            if (childPosition != -1) {
                properties.put("$element_position", String.format(Locale.CHINA, "%d:%d", groupPosition, childPosition));
            } else {
                properties.put("$element_position", String.format(Locale.CHINA, "%d", groupPosition));
            }

            String idString = JokerDataPrivate.getViewId(expandableListView);
            if (!TextUtils.isEmpty(idString)) {
                properties.put("$element_id", idString);
            }

            properties.put("$element_type", "ExpandableListView");

            String viewText = null;
            if (view instanceof ViewGroup) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    viewText = JokerDataPrivate.traverseViewContent(stringBuilder, (ViewGroup) view);
                    if (!TextUtils.isEmpty(viewText)) {
                        viewText = viewText.substring(0, viewText.length() - 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(viewText)) {
                properties.put("$element_content", viewText);
            }

            JokerDataAPI.getInstance().track("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Keep
    public static void trackViewOnClick(android.widget.AdapterView adapterView, android.view.View view, int position) {
        try {
            Context context = adapterView.getContext();
            if (context == null) {
                return;
            }

            JSONObject properties = new JSONObject();

            Activity activity = JokerDataPrivate.getActivityFromContext(context);
            String idString = JokerDataPrivate.getViewId(adapterView);
            if (!TextUtils.isEmpty(idString)) {
                properties.put("$element_id", idString);
            }

            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }
            properties.put("$element_position", String.valueOf(position));

            if (adapterView instanceof Spinner) {
                properties.put("$element_type", "Spinner");
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    if (item instanceof String) {
                        properties.put("$element_content", item);
                    }
                }
            } else {
                if (adapterView instanceof ListView) {
                    properties.put("$element_type", "ListView");
                } else if (adapterView instanceof GridView) {
                    properties.put("$element_type", "GridView");
                }

                String viewText = null;
                if (view instanceof ViewGroup) {
                    try {
                        StringBuilder stringBuilder = new StringBuilder();
                        viewText = JokerDataPrivate.traverseViewContent(stringBuilder, (ViewGroup) view);
                        if (!TextUtils.isEmpty(viewText)) {
                            viewText = viewText.substring(0, viewText.length() - 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    viewText = JokerDataPrivate.getElementContent(view);
                }
                //$element_content
                if (!TextUtils.isEmpty(viewText)) {
                    properties.put("$element_content", viewText);
                }
            }
            JokerDataAPI.getInstance().track("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * View 被点击，自动埋点
     *
     * @param view View
     */
    @Keep
    public static void trackViewOnClick(View view) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$element_id", JokerDataPrivate.getViewId(view));

            // ViewType ViewContent
            ViewNode viewContentAndType = ViewUtil.getViewContentAndType(view);
            if (!TextUtils.isEmpty(viewContentAndType.getViewType())) {
                jsonObject.put("$element_type", viewContentAndType.getViewType());
            }
            if (!TextUtils.isEmpty(viewContentAndType.getViewContent()) && VisualUtil.isSupportElementContent(view)) {
                jsonObject.put("$element_content", viewContentAndType.getViewContent());
            }

            // ViewPath  ViewPosition
            ViewNode viewPathAndPosition = ViewUtil.getViewPathAndPosition(view);
            if (viewPathAndPosition != null) {
                String viewPath = viewPathAndPosition.getViewPath();
                if (!TextUtils.isEmpty(viewPath)) {
                    // 截取 View ID 为 content 的容器路径后的内容，也就是我们写在布局中的路径
                    String contentContainerPath = "androidx.appcompat.widget.ContentFrameLayout[0]/";
                    String substring = viewPath.substring(viewPath.indexOf(contentContainerPath) + contentContainerPath.length());
                    jsonObject.put("$element_path", substring);
                    Log.e("wjc", "trackViewOnClick: -->" + substring);
                }
                if (!TextUtils.isEmpty(viewPathAndPosition.getViewPosition())) {
                    jsonObject.put("$element_position", viewPathAndPosition.getViewPosition());
                }
            }


            String tagData = JokerDataPrivate.getTagData(view);
            if (!TextUtils.isEmpty(tagData)) {
                jsonObject.put("$features", tagData);
            }

            // ActivityName
            Activity activity = JokerDataPrivate.getActivityFromView(view);
            if (activity != null) {
                jsonObject.put("$activity", activity.getClass().getCanonicalName());
            }

            // FragmentName
            Object fragment = AopUtil.getFragmentFromView(view);
            if (fragment != null) {
                AopUtil.getScreenNameAndTitleFromFragment(jsonObject, fragment, activity);
            }

            JokerDataAPI.getInstance().track("$AppClick", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onFragmentViewCreated(Object object, View rootView) {
        try {
            if (!isFragment(object)) {
                return;
            }

            //Fragment名称
            String fragmentName = object.getClass().getName();
            rootView.setTag(R.id.joker_tag_view_fragment_name, fragmentName);

            if (rootView instanceof ViewGroup) {
                traverseView(fragmentName, (ViewGroup) rootView);
            }
        } catch (Exception ignored) {
        }
    }

    private static void traverseView(String fragmentName, ViewGroup root) {
        try {
            if (TextUtils.isEmpty(fragmentName)) {
                return;
            }

            if (root == null) {
                return;
            }

            final int childCount = root.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                final View child = root.getChildAt(i);
                child.setTag(R.id.joker_tag_view_fragment_name, fragmentName);
                if (child instanceof ViewGroup && !(child instanceof ListView ||
                        child instanceof GridView ||
                        child instanceof Spinner ||
                        child instanceof RadioGroup)) {
                    traverseView(fragmentName, (ViewGroup) child);
                }
            }
        } catch (Exception e) {
            //ignored
        }
    }

    private static boolean isFragment(Object object) {
        try {
            if (object == null) {
                return false;
            }
            Class<?> supportFragmentClass = null;
            Class<?> androidXFragmentClass = null;
            Class<?> fragment = null;
            try {
                fragment = Class.forName("android.app.Fragment");
            } catch (Exception e) {
                //ignored
            }
            try {
                supportFragmentClass = Class.forName("android.support.v4.app.Fragment");
            } catch (Exception e) {
                //ignored
            }

            try {
                androidXFragmentClass = Class.forName("androidx.fragment.app.Fragment");
            } catch (Exception e) {
                //ignored
            }

            if (supportFragmentClass == null && androidXFragmentClass == null && fragment == null) {
                return false;
            }

            if ((supportFragmentClass != null && supportFragmentClass.isInstance(object)) ||
                    (androidXFragmentClass != null && androidXFragmentClass.isInstance(object)) ||
                    (fragment != null && fragment.isInstance(object))) {
                return true;
            }
        } catch (Exception e) {
            //ignored
        }
        return false;
    }
}
