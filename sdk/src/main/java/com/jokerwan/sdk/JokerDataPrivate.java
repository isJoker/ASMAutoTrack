package com.jokerwan.sdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.jokerwan.sdk.utils.DateFormatUtils;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class JokerDataPrivate {

    public static Map<String, Object> getDeviceInfo(Context context) {
        final Map<String, Object> deviceInfo = new HashMap<>();
        {
            deviceInfo.put("$lib_version", JokerDataAPI.SDK_VERSION);
            deviceInfo.put("$os", "Android");
            deviceInfo.put("$os_version",
                    Build.VERSION.RELEASE == null ? "UNKNOWN" : Build.VERSION.RELEASE);
            deviceInfo
                    .put("$manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER);
            if (TextUtils.isEmpty(Build.MODEL)) {
                deviceInfo.put("$model", "UNKNOWN");
            } else {
                deviceInfo.put("$model", Build.MODEL.trim());
            }

            try {
                final PackageManager manager = context.getPackageManager();
                final PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
                deviceInfo.put("$app_version", packageInfo.versionName);

                int labelRes = packageInfo.applicationInfo.labelRes;
                deviceInfo.put("$app_name", context.getResources().getString(labelRes));
            } catch (final Exception e) {
                e.printStackTrace();
            }

            final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            deviceInfo.put("$screen_height", displayMetrics.heightPixels);
            deviceInfo.put("$screen_width", displayMetrics.widthPixels);

            return Collections.unmodifiableMap(deviceInfo);
        }
    }

    /**
     * 获取 Android ID
     *
     * @param mContext Context
     * @return String
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context mContext) {
        String androidID = "";
        try {
            androidID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return androidID;
    }

    /**
     * 获取 view 的 anroid:id 对应的字符串
     *
     * @param view View
     * @return String
     */
    public static String getViewId(View view) {
        String idString = null;
        try {
            if (view.getId() != View.NO_ID) {
                idString = view.getContext().getResources().getResourceEntryName(view.getId());
            }
        } catch (Exception e) {
            //ignore
        }
        return idString;
    }

    public static String getElementType(View view) {
        if (view == null) {
            return null;
        }

        String viewType = null;
        if (view instanceof CheckBox) { // CheckBox
            viewType = "CheckBox";
        } else if (view instanceof SwitchCompat) {
            viewType = "SwitchCompat";
        } else if (view instanceof RadioButton) { // RadioButton
            viewType = "RadioButton";
        } else if (view instanceof ToggleButton) { // ToggleButton
            viewType = "ToggleButton";
        } else if (view instanceof Button) { // Button
            viewType = "Button";
        } else if (view instanceof CheckedTextView) { // CheckedTextView
            viewType = "CheckedTextView";
        } else if (view instanceof TextView) { // TextView
            viewType = "TextView";
        } else if (view instanceof ImageButton) { // ImageButton
            viewType = "ImageButton";
        } else if (view instanceof ImageView) { // ImageView
            viewType = "ImageView";
        } else if (view instanceof RatingBar) {
            viewType = "RatingBar";
        } else if (view instanceof SeekBar) {
            viewType = "SeekBar";
        } else if (view instanceof LinearLayout) {
            viewType = "LinearLayout";
        } else if (view instanceof RelativeLayout) {
            viewType = "RelativeLayout";
        } else if (view instanceof FrameLayout) {
            viewType = "FrameLayout";
        } else if (view instanceof ConstraintLayout) {
            viewType = "ConstraintLayout";
        }
        return viewType;
    }

    public static String traverseViewContent(StringBuilder stringBuilder, ViewGroup root) {
        try {
            if (root == null) {
                return stringBuilder.toString();
            }

            final int childCount = root.getChildCount();
            if (childCount <= 0) {
                if (!TextUtils.isEmpty(root.getContentDescription())) {
                    stringBuilder.append(root.getContentDescription());
                }
                return stringBuilder.toString();
            }
            for (int i = 0; i < childCount; ++i) {
                final View child = root.getChildAt(i);

                if (child.getVisibility() != View.VISIBLE) {
                    continue;
                }

                if (child instanceof ViewGroup) {
                    traverseViewContent(stringBuilder, (ViewGroup) child);
                } else {
                    CharSequence viewText = null;
                    if (child instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) child;
                        viewText = checkBox.getText();
                    } else if (child instanceof SwitchCompat) {
                        SwitchCompat switchCompat = (SwitchCompat) child;
                        viewText = switchCompat.getTextOn();
                    } else if (child instanceof RadioButton) {
                        RadioButton radioButton = (RadioButton) child;
                        viewText = radioButton.getText();
                    } else if (child instanceof ToggleButton) {
                        ToggleButton toggleButton = (ToggleButton) child;
                        boolean isChecked = toggleButton.isChecked();
                        if (isChecked) {
                            viewText = toggleButton.getTextOn();
                        } else {
                            viewText = toggleButton.getTextOff();
                        }
                    } else if (child instanceof Button) {
                        Button button = (Button) child;
                        viewText = button.getText();
                    } else if (child instanceof CheckedTextView) {
                        CheckedTextView textView = (CheckedTextView) child;
                        viewText = textView.getText();
                    } else if (child instanceof TextView) {
                        TextView textView = (TextView) child;
                        viewText = textView.getText();
                    } else {
                        if (!TextUtils.isEmpty(child.getContentDescription())) {
                            viewText = child.getContentDescription().toString();
                        }
                    }

                    if (!TextUtils.isEmpty(viewText)) {
                        stringBuilder.append(viewText.toString());
                        stringBuilder.append("-");
                    }
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return stringBuilder.toString();
        }
    }

    /**
     * 获取 View 上显示的文本
     *
     * @param view View
     * @return String
     */
    public static String getElementContent(View view) {
        if (view == null) {
            return null;
        }

        CharSequence viewText = null;
        if (view instanceof CheckBox) { // CheckBox
            CheckBox checkBox = (CheckBox) view;
            viewText = checkBox.getText();
        } else if (view instanceof SwitchCompat) {
            SwitchCompat switchCompat = (SwitchCompat) view;
            viewText = switchCompat.getTextOn();
        } else if (view instanceof RadioButton) { // RadioButton
            RadioButton radioButton = (RadioButton) view;
            viewText = radioButton.getText();
        } else if (view instanceof ToggleButton) { // ToggleButton
            ToggleButton toggleButton = (ToggleButton) view;
            boolean isChecked = toggleButton.isChecked();
            if (isChecked) {
                viewText = toggleButton.getTextOn();
            } else {
                viewText = toggleButton.getTextOff();
            }
        } else if (view instanceof Button) { // Button
            Button button = (Button) view;
            viewText = button.getText();
        } else if (view instanceof CheckedTextView) { // CheckedTextView
            CheckedTextView textView = (CheckedTextView) view;
            viewText = textView.getText();
        } else if (view instanceof TextView) { // TextView
            TextView textView = (TextView) view;
            viewText = textView.getText();
        } else if (view instanceof SeekBar) {
            SeekBar seekBar = (SeekBar) view;
            viewText = String.valueOf(seekBar.getProgress());
        } else if (view instanceof RatingBar) {
            RatingBar ratingBar = (RatingBar) view;
            viewText = String.valueOf(ratingBar.getRating());
        } else {
            viewText = view.getContentDescription();
        }
        if (viewText != null) {
            return viewText.toString();
        }
        return null;
    }

    /**
     * 获取 View 所属 Activity
     *
     * @param view View
     * @return Activity
     */
    public static Activity getActivityFromView(View view) {
        if (view == null) {
            return null;
        }
        return getActivityFromContext(view.getContext());
    }

    public static Activity getActivityFromContext(Context context) {
        Activity activity = null;
        try {
            if (context != null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }

    private static void addIndentBlank(StringBuilder sb, int indent) {
        try {
            for (int i = 0; i < indent; i++) {
                sb.append('\t');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String formatJson(String jsonStr) {
        try {
            if (null == jsonStr || "".equals(jsonStr)) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            char last;
            char current = '\0';
            int indent = 0;
            boolean isInQuotationMarks = false;
            for (int i = 0; i < jsonStr.length(); i++) {
                last = current;
                current = jsonStr.charAt(i);
                switch (current) {
                    case '"':
                        if (last != '\\') {
                            isInQuotationMarks = !isInQuotationMarks;
                        }
                        sb.append(current);
                        break;
                    case '{':
                    case '[':
                        sb.append(current);
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent++;
                            addIndentBlank(sb, indent);
                        }
                        break;
                    case '}':
                    case ']':
                        if (!isInQuotationMarks) {
                            sb.append('\n');
                            indent--;
                            addIndentBlank(sb, indent);
                        }
                        sb.append(current);
                        break;
                    case ',':
                        sb.append(current);
                        if (last != '\\' && !isInQuotationMarks) {
                            sb.append('\n');
                            addIndentBlank(sb, indent);
                        }
                        break;
                    default:
                        sb.append(current);
                }
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取 Activity 的 title
     *
     * @param activity Activity
     * @return Activity 的 title
     */
    public static String getActivityTitle(Activity activity) {
        try {
            if (activity != null) {
                try {
                    String activityTitle = null;

                    if (Build.VERSION.SDK_INT >= 11) {
                        String toolbarTitle = getToolbarTitle(activity);
                        if (!TextUtils.isEmpty(toolbarTitle)) {
                            activityTitle = toolbarTitle;
                        }
                    }

                    if (TextUtils.isEmpty(activityTitle)) {
                        activityTitle = activity.getTitle().toString();
                    }

                    if (TextUtils.isEmpty(activityTitle)) {
                        PackageManager packageManager = activity.getPackageManager();
                        if (packageManager != null) {
                            ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                            if (!TextUtils.isEmpty(activityInfo.loadLabel(packageManager))) {
                                activityTitle = activityInfo.loadLabel(packageManager).toString();
                            }
                        }
                    }

                    return activityTitle;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @TargetApi(11)
    public static String getToolbarTitle(Activity activity) {
        try {
            if ("com.tencent.connect.common.AssistActivity".equals(activity.getClass().getCanonicalName())) {
                if (!TextUtils.isEmpty(activity.getTitle())) {
                    return activity.getTitle().toString();
                }
                return null;
            }
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (!TextUtils.isEmpty(actionBar.getTitle())) {
                    return actionBar.getTitle().toString();
                }
            } else {
                try {
                    Class<?> appCompatActivityClass = compatActivity();
                    if (appCompatActivityClass != null && appCompatActivityClass.isInstance(activity)) {
                        Method method = activity.getClass().getMethod("getSupportActionBar");
                        Object supportActionBar = method.invoke(activity);
                        if (supportActionBar != null) {
                            method = supportActionBar.getClass().getMethod("getTitle");
                            CharSequence charSequence = (CharSequence) method.invoke(supportActionBar);
                            if (charSequence != null) {
                                return charSequence.toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    //ignored
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Class<?> compatActivity() {
        Class<?> appCompatActivityClass = null;
        try {
            appCompatActivityClass = Class.forName("android.support.v7.app.AppCompatActivity");
        } catch (Exception e) {
            //ignored
        }
        if (appCompatActivityClass == null) {
            try {
                appCompatActivityClass = Class.forName("androidx.appcompat.app.AppCompatActivity");
            } catch (Exception e) {
                //ignored
            }
        }
        return appCompatActivityClass;
    }

    public static void mergeJSONObject(final JSONObject source, JSONObject dest) {
        try {
            Iterator<String> superPropertiesIterator = source.keys();

            while (superPropertiesIterator.hasNext()) {
                String key = superPropertiesIterator.next();
                Object value = source.get(key);
                if (value instanceof Date && !"$time".equals(key)) {
                    dest.put(key, DateFormatUtils.formatDate((Date) value, Locale.CHINA));
                } else {
                    dest.put(key, value);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static String getTagData(View view) {
        String tagData = null;
        String tag = (String) view.getTag(R.id.key_binding_tag);
        if (tag != null) {
            tagData = tag;
        }
        return tagData;
    }
}
