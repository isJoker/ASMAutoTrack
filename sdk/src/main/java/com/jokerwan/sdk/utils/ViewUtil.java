package com.jokerwan.sdk.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jokerwan.sdk.AppStateManager;
import com.jokerwan.sdk.ViewNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class ViewUtil {

    private static boolean sHaveCustomRecyclerView = false;
    private static boolean sHaveRecyclerView = haveRecyclerView();
    private static Method sRecyclerViewGetChildAdapterPositionMethod;
    private static Class sRecyclerViewClass;
    private static LruCache<Class, String> sClassNameCache;
    private static SparseArray sViewCache;

    /**
     * 获取 class name
     */
    private static String getCanonicalName(Class clazz) {
        String name = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            if (sClassNameCache == null) {
                sClassNameCache = new LruCache<Class, String>(100);
            }
            name = sClassNameCache.get(clazz);
        }
        if (TextUtils.isEmpty(name)) {
            name = clazz.getCanonicalName();
            if (TextUtils.isEmpty(name)) {
                name = "Anonymous";
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                synchronized (ViewUtil.class) {
                    sClassNameCache.put(clazz, name);
                }
            }
            checkCustomRecyclerView(clazz, name);
        }
        return name;
    }

    private static boolean instanceOfSupportViewPager(Object view) {
        Class clazz;
        try {
            clazz = Class.forName("android.support.v4.view.ViewPager");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return clazz.isInstance(view);
    }

    private static boolean instanceOfAndroidXViewPager(Object view) {
        Class clazz;
        try {
            clazz = Class.forName("androidx.viewpager.widget.ViewPager");
        } catch (ClassNotFoundException e2) {
            return false;
        }
        return clazz.isInstance(view);
    }

    public static boolean instanceOfRecyclerView(Object view) {
        Class clazz;
        try {
            clazz = Class.forName("android.support.v7.widget.RecyclerView");
        } catch (ClassNotFoundException th) {
            try {
                clazz = Class.forName("androidx.recyclerview.widget.RecyclerView");
            } catch (ClassNotFoundException e2) {
                return sHaveCustomRecyclerView
                        && view != null
                        && sRecyclerViewClass != null
                        && sRecyclerViewClass.isAssignableFrom(view.getClass());
            }
        }
        return clazz.isInstance(view);
    }

    private static boolean instanceOfSupportSwipeRefreshLayout(Object view) {
        Class clazz;
        try {
            clazz = Class.forName("android.support.v4.widget.SwipeRefreshLayout");
        } catch (ClassNotFoundException th) {
            try {
                clazz = Class.forName("androidx.swiperefreshlayout.widget.SwipeRefreshLayout");
            } catch (ClassNotFoundException e2) {
                return false;
            }
        }
        return clazz.isInstance(view);
    }

    static boolean instanceOfSupportListMenuItemView(Object view) {
        Class clazz = null;
        try {
            clazz = Class.forName("android.support.v7.view.menu.ListMenuItemView");
            return clazz.isInstance(view);
        } catch (ClassNotFoundException th) {
            //ignored
        }
        return false;
    }

    static boolean instanceOfAndroidXListMenuItemView(Object view) {
        Class clazz = null;
        try {
            clazz = Class.forName("androidx.appcompat.view.menu.ListMenuItemView");
            return clazz.isInstance(view);
        } catch (ClassNotFoundException th) {
            //ignored
        }
        return false;
    }

    static boolean instanceOfBottomNavigationItemView(Object view) {
        Class clazz = null;
        try {
            clazz = Class.forName("com.google.android.material.bottomnavigation.BottomNavigationItemView");
            return clazz.isInstance(view);
        } catch (ClassNotFoundException e) {
            //ignored
        }
        return false;
    }

    static boolean instanceOfNavigationView(Object view) {
        Class clazz = null;
        try {
            clazz = Class.forName("android.support.design.widget.NavigationView");
        } catch (ClassNotFoundException th) {
            try {
                clazz = Class.forName("com.google.android.material.navigation.NavigationView");
            } catch (ClassNotFoundException e2) {
                return false;
            }
        }
        return clazz.isInstance(view);
    }

    /**
     * view 是否为 Fragment 中的顶层 View
     */
    private static Object instanceOfFragmentRootView(View parentView, View childView) {
        Object parentFragment = AopUtil.getFragmentFromView(parentView);
        Object childFragment = AopUtil.getFragmentFromView(childView);
        if (parentFragment == null && childFragment != null) {
            return childFragment;
        }
        return null;
    }

    public static boolean instanceOfX5WebView(Object view) {
        Class clazz = null;
        try {
            clazz = Class.forName("com.tencent.smtt.sdk.WebView");
            return clazz.isInstance(view);
        } catch (ClassNotFoundException th) {
            //ignored
        }
        return false;
    }

    /**
     * position RecyclerView item
     */
    private static int getChildAdapterPositionInRecyclerView(View childView, ViewGroup parentView) {
        if (instanceOfRecyclerView(parentView)) {
            try {
                sRecyclerViewGetChildAdapterPositionMethod = parentView.getClass().getDeclaredMethod("getChildAdapterPosition", new Class[]{View.class});
            } catch (NoSuchMethodException e) {
                //ignored
            }
            if (sRecyclerViewGetChildAdapterPositionMethod == null) {
                try {
                    sRecyclerViewGetChildAdapterPositionMethod = parentView.getClass().getDeclaredMethod("getChildPosition", new Class[]{View.class});
                } catch (NoSuchMethodException e2) {
                    //ignored
                }
            }
            try {
                if (sRecyclerViewGetChildAdapterPositionMethod != null) {
                    Object object = sRecyclerViewGetChildAdapterPositionMethod.invoke(parentView, childView);
                    if (object != null) {
                        return (Integer) object;
                    }
                }
            } catch (IllegalAccessException e) {
                //ignored
            } catch (InvocationTargetException e) {
                //ignored
            }
        } else if (sHaveCustomRecyclerView) {
            return invokeCRVGetChildAdapterPositionMethod(parentView, childView);
        }
        return -1;
    }

    private static int getCurrentItem(View view) {
        try {
            Method method = view.getClass().getMethod("getCurrentItem");
            Object object = method.invoke(view);
            if (object != null) {
                return (Integer) object;
            }
        } catch (IllegalAccessException e) {
            //ignored
        } catch (InvocationTargetException e2) {
            //ignored
        } catch (NoSuchMethodException e) {
            //ignored
        }
        return -1;
    }

    static Object getItemData(View view) {
        try {
            Method method = view.getClass().getMethod("getItemData");
            return method.invoke(view);
        } catch (IllegalAccessException e) {
            //ignored
        } catch (InvocationTargetException e2) {
            //ignored
        } catch (NoSuchMethodException e) {
            //ignored
        }
        return null;
    }

    private static boolean haveRecyclerView() {
        try {
            Class.forName("android.support.v7.widget.RecyclerView");
            return true;
        } catch (ClassNotFoundException th) {
            try {
                Class.forName("androidx.recyclerview.widget.RecyclerView");
                return true;
            } catch (ClassNotFoundException e2) {
                return false;
            }
        }
    }

    @TargetApi(12)
    private static void checkCustomRecyclerView(Class<?> viewClass, String viewName) {
        if (!sHaveRecyclerView && !sHaveCustomRecyclerView && viewName != null && viewName.contains("RecyclerView")) {
            try {
                if (findRecyclerInSuper(viewClass) != null && sRecyclerViewGetChildAdapterPositionMethod != null) {
                    sRecyclerViewClass = viewClass;
                    sHaveCustomRecyclerView = true;
                }
            } catch (Exception e) {
                //ignored
            }
        }
    }

    private static Class<?> findRecyclerInSuper(Class<?> viewClass) {
        while (viewClass != null && !viewClass.equals(ViewGroup.class)) {
            try {
                sRecyclerViewGetChildAdapterPositionMethod = viewClass.getDeclaredMethod("getChildAdapterPosition", new Class[]{View.class});
            } catch (NoSuchMethodException e) {
                //ignored
            }
            if (sRecyclerViewGetChildAdapterPositionMethod == null) {
                try {
                    sRecyclerViewGetChildAdapterPositionMethod = viewClass.getDeclaredMethod("getChildPosition", new Class[]{View.class});
                } catch (NoSuchMethodException e2) {
                    //ignored
                }
            }
            if (sRecyclerViewGetChildAdapterPositionMethod != null) {
                return viewClass;
            }
            viewClass = viewClass.getSuperclass();
        }
        return null;
    }

    private static int invokeCRVGetChildAdapterPositionMethod(View customRecyclerView, View childView) {
        try {
            if (customRecyclerView.getClass() == sRecyclerViewClass) {
                return (Integer) sRecyclerViewGetChildAdapterPositionMethod.invoke(customRecyclerView, new Object[]{childView});
            }
        } catch (IllegalAccessException e) {
            //ignored
        } catch (InvocationTargetException e2) {
            //ignored
        }
        return -1;
    }

    private static boolean isListView(View view) {
        return (view instanceof AdapterView) || ViewUtil.instanceOfRecyclerView(view) || ViewUtil.instanceOfAndroidXViewPager(view) || ViewUtil.instanceOfSupportViewPager(view);
    }

    @SuppressLint("NewApi")
    public static boolean isViewSelfVisible(View view) {
        if (view == null || view.getWindowVisibility() == View.GONE) {
            return false;
        }
        if (WindowHelper.isDecorView(view.getClass())) {
            return true;
        }
        if (view.getWidth() <= 0 || view.getHeight() <= 0 || view.getAlpha() <= 0.0f || !view.getLocalVisibleRect(new Rect())) {
            return false;
        }
        if ((view.getVisibility() == View.VISIBLE || view.getAnimation() == null || !view.getAnimation().getFillAfter()) && view.getVisibility() != View.VISIBLE) {
            return false;
        }
        return true;
    }

    private static boolean viewVisibilityInParents(View view) {
        if (view == null) {
            return false;
        }
        if (!ViewUtil.isViewSelfVisible(view)) {
            return false;
        }
        ViewParent viewParent = view.getParent();
        while (viewParent instanceof View) {
            if (!ViewUtil.isViewSelfVisible((View) viewParent)) {
                return false;
            }
            viewParent = viewParent.getParent();
            if (viewParent == null) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("NewApi")
    public static void invalidateLayerTypeView(View[] views) {
        for (View view : views) {
            if (ViewUtil.viewVisibilityInParents(view) && view.isHardwareAccelerated()) {
                checkAndInvalidate(view);
                if (view instanceof ViewGroup) {
                    invalidateViewGroup((ViewGroup) view);
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private static void checkAndInvalidate(View view) {
        if (view.getLayerType() != 0) {
            view.invalidate();
        }
    }

    private static void invalidateViewGroup(ViewGroup viewGroup) {
        for (int index = 0; index < viewGroup.getChildCount(); index++) {
            View child = viewGroup.getChildAt(index);
            if (ViewUtil.isViewSelfVisible(child)) {
                checkAndInvalidate(child);
                if (child instanceof ViewGroup) {
                    invalidateViewGroup((ViewGroup) child);
                }
            }
        }
    }

    public static int getMainWindowCount(View[] windowRootViews) {
        int mainWindowCount = 0;
        WindowHelper.init();
        for (View windowRootView : windowRootViews) {
            if (windowRootView != null) {
                mainWindowCount += WindowHelper.getWindowPrefix(windowRootView).equals(WindowHelper.getMainWindowPrefix()) ? 1 : 0;
            }
        }
        return mainWindowCount;
    }

    public static boolean isWindowNeedTraverse(View root, String prefix, boolean skipOtherActivity) {
        if (root.hashCode() == AppStateManager.getInstance().getCurrentRootWindowsHashCode()) {
            return true;
        }
        if (root instanceof ViewGroup) {
            if (!skipOtherActivity) {
                return true;
            }
            if (!(root.getWindowVisibility() == View.GONE || root.getVisibility() != View.VISIBLE || TextUtils.equals(prefix, WindowHelper.getMainWindowPrefix()) || root.getWidth() == 0 || root.getHeight() == 0)) {
                return true;
            }
        }
        return false;
    }

    public static ViewNode getViewPathAndPosition(View clickView) {
        ArrayList<View> arrayList = new ArrayList<View>(8);
        arrayList.add(clickView);
        for (ViewParent parent = clickView.getParent(); parent instanceof ViewGroup; parent = parent.getParent()) {
            arrayList.add((ViewGroup) parent);
        }
        int endIndex = arrayList.size() - 1;
        View rootView = arrayList.get(endIndex);
        String listPosition = null;
        StringBuilder opx = new StringBuilder();
        StringBuilder px = new StringBuilder();
        if (rootView instanceof ViewGroup) {
            ViewGroup parentView = (ViewGroup) rootView;
            for (int i = endIndex - 1; i >= 0; i--) {
                final View childView = arrayList.get(i);
                final int viewPosition = parentView.indexOfChild(childView);
                final ViewNode viewNode = getViewNode(childView, viewPosition);
                if (viewNode != null) {
                    if (!TextUtils.isEmpty(viewNode.getViewPath()) && viewNode.getViewPath().contains("-") && !TextUtils.isEmpty(listPosition)) {
                        int replacePosition = px.indexOf("-");
                        if (replacePosition != -1) {
                            px.replace(replacePosition, replacePosition + 1, String.valueOf(listPosition));
                        }
                    }
                    opx.append(viewNode.getViewOriginalPath());
                    px.append(viewNode.getViewPath());
                    listPosition = viewNode.getViewPosition();
                }
                if (!(childView instanceof ViewGroup)) {
                    break;
                }
                parentView = (ViewGroup) childView;
            }
            return new ViewNode(listPosition, opx.toString(), px.toString());
        }
        return null;
    }

    static String getElementSelector(View view) {
        ViewParent viewParent;
        List<String> viewPath = new ArrayList<>();
        do {
            viewParent = view.getParent();
            int index = AopUtil.getChildIndex(viewParent, view);
            viewPath.add(view.getClass().getCanonicalName() + "[" + index + "]");
            if (viewParent instanceof ViewGroup) {
                view = (ViewGroup) viewParent;
            }
        } while (viewParent instanceof ViewGroup);

        Collections.reverse(viewPath);
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 1; i < viewPath.size(); i++) {
            stringBuffer.append(viewPath.get(i));
            if (i != (viewPath.size() - 1)) {
                stringBuffer.append("/");
            }
        }
        return stringBuffer.toString();
    }

    private static int getViewPosition(View view, int viewIndex) {
        int idx = viewIndex;
        if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (ViewUtil.instanceOfAndroidXViewPager(parent) || ViewUtil.instanceOfSupportViewPager(parent)) {
                idx = ViewUtil.getCurrentItem(parent);
            } else if (parent instanceof AdapterView) {
                idx += ((AdapterView) parent).getFirstVisiblePosition();
            } else if (ViewUtil.instanceOfRecyclerView(parent)) {
                int adapterPosition = ViewUtil.getChildAdapterPositionInRecyclerView(view, parent);
                if (adapterPosition >= 0) {
                    idx = adapterPosition;
                }
            }
        }
        return idx;
    }

    private static ViewNode getViewNode(View view, int viewIndex) {
        int viewPosition = getViewPosition(view, viewIndex);
        ViewParent parentObject = view.getParent();
        if (parentObject == null) {
            return null;
        }
        if (!WindowHelper.isDecorView(view.getClass()) || (parentObject instanceof View)) {
            if (parentObject instanceof View) {
                View parentView = (View) parentObject;
                String listPos = null;
                StringBuilder opx = new StringBuilder();
                StringBuilder px = new StringBuilder();
                String viewName = ViewUtil.getCanonicalName(view.getClass());
                Object fragment = null;
                if (parentView instanceof ExpandableListView) {
                    ExpandableListView listParent = (ExpandableListView) parentView;
                    long elp = listParent.getExpandableListPosition(viewPosition);
                    if (ExpandableListView.getPackedPositionType(elp) != 2) {
                        int groupIdx = ExpandableListView.getPackedPositionGroup(elp);
                        int childIdx = ExpandableListView.getPackedPositionChild(elp);
                        if (childIdx != -1) {
                            listPos = String.format(Locale.CHINA, "%d:%d", groupIdx, childIdx);
                            px.append(opx).append("/ELVG[").append(groupIdx).append("]/ELVC[-]/").append(viewName).append("[0]");
                            opx.append("/ELVG[").append(groupIdx).append("]/ELVC[").append(childIdx).append("]/").append(viewName).append("[0]");
                        } else {
                            listPos = String.format(Locale.CHINA, "%d", groupIdx);
                            px.append(opx).append("/ELVG[-]/").append(viewName).append("[0]");
                            opx.append("/ELVG[").append(groupIdx).append("]/").append(viewName).append("[0]");
                        }
                    } else if (viewPosition < listParent.getHeaderViewsCount()) {
                        opx.append("/ELH[").append(viewPosition).append("]/").append(viewName).append("[0]");
                        px.append("/ELH[").append(viewPosition).append("]/").append(viewName).append("[0]");
                    } else {
                        int footerIndex = viewPosition - (listParent.getCount() - listParent.getFooterViewsCount());
                        opx.append("/ELF[").append(footerIndex).append("]/").append(viewName).append("[0]");
                        px.append("/ELF[").append(footerIndex).append("]/").append(viewName).append("[0]");
                    }
                } else if (ViewUtil.isListView(parentView)) {
                    listPos = String.format(Locale.CHINA, "%d", viewPosition);
                    px.append(opx).append("/").append(viewName).append("[-]");
                    opx.append("/").append(viewName).append("[").append(listPos).append("]");
                } else if (ViewUtil.instanceOfSupportSwipeRefreshLayout(parentView)) {
                    opx.append("/").append(viewName).append("[0]");
                    px.append("/").append(viewName).append("[0]");
                } else if ((fragment = ViewUtil.instanceOfFragmentRootView(parentView, view)) != null) {
                    viewName = ViewUtil.getCanonicalName(fragment.getClass());
                    opx.append("/").append(viewName).append("[0]");
                    px.append("/").append(viewName).append("[0]");
                } else {
                    ViewParent listParent = parentView.getParent();
                    if (listParent instanceof View) {
                        View listParentView = (View) listParent;
                        if (sViewCache == null) {
                            sViewCache = new SparseArray<String>();
                        }
                        String parentPos = (String) sViewCache.get(listParentView.hashCode());
                        if (!TextUtils.isEmpty(parentPos)) {
                            listPos = parentPos;
                        }
                    }
                    viewPosition = AopUtil.getChildIndex(parentObject, view);
                    opx.append("/").append(viewName).append("[").append(viewPosition).append("]");
                    px.append("/").append(viewName).append("[").append(viewPosition).append("]");
                }
                if (WindowHelper.isDecorView(parentView.getClass())) {
                    if (opx.length() > 0) {
                        opx.deleteCharAt(0);
                    }
                    if (px.length() > 0) {
                        px.deleteCharAt(0);
                    }
                }
                if (!TextUtils.isEmpty(listPos)) {
                    if (sViewCache == null) {
                        sViewCache = new SparseArray<String>();
                    }
                    sViewCache.put(parentView.hashCode(), listPos);
                }
                ViewNode viewNode = getViewContentAndType(view);
                return new ViewNode(listPos, opx.toString(), px.toString(), viewNode.getViewContent(), viewNode.getViewType());
            }
        }
        return null;
    }

    public static void clear() {
        if (sViewCache != null) {
            sViewCache.clear();
        }
    }

    static boolean isTrackEvent(View view, boolean isFromUser) {
        if (view instanceof CheckBox) {
            if (!isFromUser) {
                return false;
            }
        } else if (view instanceof RadioButton) {
            if (!isFromUser) {
                return false;
            }
        } else if (view instanceof ToggleButton) {
            if (!isFromUser) {
                return false;
            }
        } else if (view instanceof CompoundButton) {
            if (!isFromUser) {
                return false;
            }
        }
        if (view instanceof RatingBar) {
            if (!isFromUser) {
                return false;
            }
        }
        return true;
    }

    public static ViewNode getViewContentAndType(View view) {
        String viewType = view.getClass().getCanonicalName();
        CharSequence viewText = null;
        Object tab = null;
        if (view instanceof CheckBox) { // CheckBox
            viewType = AopUtil.getViewType(viewType, "CheckBox");
            CheckBox checkBox = (CheckBox) view;
            viewText = checkBox.getText();
        } else if (view instanceof RadioButton) { // RadioButton
            viewType = AopUtil.getViewType(viewType, "RadioButton");
            RadioButton radioButton = (RadioButton) view;
            viewText = radioButton.getText();
        } else if (view instanceof ToggleButton) { // ToggleButton
            viewType = AopUtil.getViewType(viewType, "ToggleButton");
            viewText = AopUtil.getCompoundButtonText(view);
        } else if (view instanceof CompoundButton) {
            viewType = AopUtil.getViewTypeByReflect(view);
            viewText = AopUtil.getCompoundButtonText(view);
        } else if (view instanceof Button) { // Button
            viewType = AopUtil.getViewType(viewType, "Button");
            Button button = (Button) view;
            viewText = button.getText();
        } else if (view instanceof CheckedTextView) { // CheckedTextView
            viewType = AopUtil.getViewType(viewType, "CheckedTextView");
            CheckedTextView textView = (CheckedTextView) view;
            viewText = textView.getText();
        } else if (view instanceof TextView) { // TextView
            viewType = AopUtil.getViewType(viewType, "TextView");
            TextView textView = (TextView) view;
            viewText = textView.getText();
        } else if (view instanceof ImageView) { // ImageView
            viewType = AopUtil.getViewType(viewType, "ImageView");
            ImageView imageView = (ImageView) view;
            if (!TextUtils.isEmpty(imageView.getContentDescription())) {
                viewText = imageView.getContentDescription().toString();
            }
        } else if (view instanceof RatingBar) {
            viewType = AopUtil.getViewType(viewType, "RatingBar");
            RatingBar ratingBar = (RatingBar) view;
            viewText = String.valueOf(ratingBar.getRating());
        } else if (view instanceof SeekBar) {
            viewType = AopUtil.getViewType(viewType, "SeekBar");
            SeekBar seekBar = (SeekBar) view;
            viewText = String.valueOf(seekBar.getProgress());
        } else if (view instanceof Spinner) {
            viewType = AopUtil.getViewType(viewType, "Spinner");
            try {
                StringBuilder stringBuilder = new StringBuilder();
                viewText = AopUtil.traverseView(stringBuilder, (ViewGroup) view);
                if (!TextUtils.isEmpty(viewText)) {
                    viewText = viewText.toString().substring(0, viewText.length() - 1);
                }
            } catch (Exception ignored) {
            }
        } else if ((tab = instanceOfTabView(view)) != null) {
            viewText = getTabLayoutContent(tab);
            viewType = AopUtil.getViewType(viewType, "TabLayout");
        } else if (ViewUtil.instanceOfBottomNavigationItemView(view)) {
            Object itemData = ViewUtil.getItemData(view);
            if (itemData != null) {
                try {
                    Class<?> menuItemImplClass = ReflectUtil.getCurrentClass(new String[]{"androidx.appcompat.view.menu.MenuItemImpl"});
                    if (menuItemImplClass != null) {
                        String title = null;
                        title = ReflectUtil.findField(menuItemImplClass, itemData, new String[]{"mTitle"});
                        if (!TextUtils.isEmpty(title)) {
                            viewText = title;
                        }
                    }
                } catch (Exception e) {
                    //ignored
                }
            }
        } else if (ViewUtil.instanceOfNavigationView(view)) {
            viewText = ViewUtil.isViewSelfVisible(view) ? "Open" : "Close";
            viewType = AopUtil.getViewType(viewType, "NavigationView");
        } else if (view instanceof ViewGroup) {
            viewType = AopUtil.getViewGroupTypeByReflect(view);
            viewText = view.getContentDescription();
            if (TextUtils.isEmpty(viewText)) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    viewText = AopUtil.traverseView(stringBuilder, (ViewGroup) view);
                    if (!TextUtils.isEmpty(viewText)) {
                        viewText = viewText.toString().substring(0, viewText.length() - 1);
                    }
                } catch (Exception e) {
                    //ignored
                }
            }
        }

        if (TextUtils.isEmpty(viewText) && view instanceof TextView) {
            viewText = ((TextView) view).getHint();
        }

        if (TextUtils.isEmpty(viewText)) {
            viewText = view.getContentDescription();
        }

        if (view instanceof EditText) {
            viewText = "";
        }

        if (TextUtils.isEmpty(viewText)) {
            viewText = "";
        }
        return new ViewNode(viewText.toString(), viewType);
    }

    private static Object instanceOfTabView(View tabView) {
        try {
            Class<?> currentTabViewClass = ReflectUtil.getCurrentClass(new String[]{"android.support.design.widget.TabLayout$TabView", "com.google.android.material.tabs.TabLayout$TabView"});
            if (currentTabViewClass != null && currentTabViewClass.isAssignableFrom(tabView.getClass())) {
                return ReflectUtil.findField(currentTabViewClass, tabView, new String[]{"mTab", "tab"});
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String getTabLayoutContent(Object tab) {
        String viewText = null;
        Class<?> currentTabClass = null;
        try {
            currentTabClass = ReflectUtil.getCurrentClass(new String[]{"android.support.design.widget.TabLayout$Tab", "com.google.android.material.tabs.TabLayout$Tab"});
            if (currentTabClass != null) {
                Object text = null;
                text = ReflectUtil.callMethod(tab, "getText");
                if (text != null) {
                    viewText = text.toString();
                }
                View customView = null;
                customView = ReflectUtil.findField(currentTabClass, tab, new String[]{"mCustomView", "customView"});
                if (customView != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (customView instanceof ViewGroup) {
                        viewText = AopUtil.traverseView(stringBuilder, (ViewGroup) customView);
                        if (!TextUtils.isEmpty(viewText)) {
                            viewText = viewText.toString().substring(0, viewText.length() - 1);
                        }
                    } else {
                        viewText = AopUtil.getViewText(customView);
                    }
                }
            }
        } catch (Exception e) {
            //ignored
        }
        return viewText;
    }
}