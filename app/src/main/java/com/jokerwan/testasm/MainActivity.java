package com.jokerwan.testasm;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.jokerwan.sdk.JokerTrackViewOnClick;
import com.jokerwan.testasm.databinding.ActivityMainBinding;
import com.jokerwan.testasm.model.UserModel;

public class MainActivity extends AppCompatActivity implements MainListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        binding.btnSetClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("携带业务参数");
            }
        });
        // 通过在 xml 里面 通过 bindingTag 方式设置需要收集的业务 model
        binding.btnModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("普通按钮点击");
            }
        });
        binding.setModel(new UserModel("张三","18","男"));

        binding.setListener(this);

        binding.btnLambdaSetClick.setOnClickListener(v -> showToast("lambdaSetOnClickListener"));

        binding.btnDialogClick.setOnClickListener(v -> showDialog(MainActivity.this));

        binding.llContainer.setOnClickListener(v -> showToast("线性布局容器点击"));
        binding.btn1.setOnClickListener(v -> showToast("Button1"));
        binding.btn2.setOnClickListener(v -> showToast("Button2"));
        binding.btn3.setOnClickListener(v -> showToast("Button3"));
        binding.btn4.setOnClickListener(v -> showToast("Button4"));
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 被 @JokerDataTrackViewOnClick 标记的方法需满足如下条件才会被插桩
     * 1：只有一个参数，而且参数是 android.view.View类型
     * 2：无返回值
     */
    @JokerTrackViewOnClick
    public void xmlClick(View v){
        showToast("xml绑定onClick");
    }

    @Override
    public void onMainClick() {
        showToast("DataBinding点击事件");
    }

    @Override
    public void onIncludeItemClick() {

    }

    private void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("弹框标题");
        builder.setMessage("这是内容");
        builder.setNegativeButton("取消", (dialog, which) -> showToast("取消"));
        builder.setPositiveButton("确定", (dialog, which) -> showToast("确定"));

        AlertDialog dialog = builder.create();

        dialog.show();
    }

}
