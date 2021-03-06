package noclay.treehole3.ActivityCollect;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;
import noclay.treehole3.OtherPackage.SignUserBaseClass;
import noclay.treehole3.R;

/**
 * Created by 寒 on 2016/7/25.
 */
public class ChangePassWord extends AppCompatActivity implements View.OnClickListener{
    private EditText signedPhoneNumber;
    private Button sendMessage;
    private EditText checkNumber;
    private EditText newPassWord;
    private ImageView cancelButton;
    private Button completeChangePassWord;
    private boolean isFromLogin;
    private int i = 30;
    private static final int MSG_WHAT_FOR_THREAD = 0;
    private static final int MSG_WHAT_FOR_THREAD_DEATH = 2;
    private static final int MSG_WHAT_FOT_SHORT_MESSAGE = 1;
    private String objectId;
    private static final String TAG = "ChangePassWord";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_activity_layout);
        initView();
        Bmob.initialize(this, "e7a1bf15265fddb02517d7d9181fe6a6");
        //初始化短信验证
        SMSSDK.initSDK(ChangePassWord.this, "1559e5fc73570","8a88fdb37b3887daa07b4074a1b9b66b");
        EventHandler eh = new EventHandler(){
            @Override
            public void afterEvent(int i, int i1, Object o) {
                Message msg = new Message();
                msg.arg1 = i;
                msg.arg2 = i1;
                msg.what = MSG_WHAT_FOT_SHORT_MESSAGE;
                handler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);
        signedPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isMobileNum(signedPhoneNumber.getText().toString())) {
                    //发送短信
                    sendMessage.setClickable(true);
                    sendMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_button_def));
                } else {
                    sendMessage.setClickable(false);
                    sendMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_button_2));

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
    public static boolean isMobileNum(String mobiles) {
        String regex = "1[3|5|7|8|][0-9]{9}";
        return mobiles.matches(regex);
    }
    private void initView() {
        signedPhoneNumber = (EditText) findViewById(R.id.signed_phoneNumber);
        sendMessage = (Button) findViewById(R.id.send_message_button);
        newPassWord = (EditText) findViewById(R.id.new_password);
        cancelButton = (ImageView) findViewById(R.id.cancel_button);
        completeChangePassWord = (Button) findViewById(R.id.complete_change);
        checkNumber = (EditText) findViewById(R.id.input_checkNumber);
        sendMessage.setOnClickListener(this);
        completeChangePassWord.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        Intent intent = getIntent();
        isFromLogin = intent.getBooleanExtra("isLogin", true);
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_WHAT_FOR_THREAD:{
                    sendMessage.setClickable(false);
                    sendMessage.setText(msg.arg1 + "秒后可获取验证码");
                    sendMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_button_2));
                    break;
                }
                case MSG_WHAT_FOR_THREAD_DEATH:{
                    sendMessage.setClickable(true);
                    sendMessage.setText("获取验证码");
                    sendMessage.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_button_def));
                    break;
                }
                case MSG_WHAT_FOT_SHORT_MESSAGE:{
                    int event = msg.arg1;
                    int result = msg.arg2;
                    switch(event){
                        case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE:{
                            if(result == SMSSDK.RESULT_COMPLETE){

                                //验证成功，在这里进行修改密码
                                SignUserBaseClass newUser = new SignUserBaseClass();
                                newUser.setPassWord(newPassWord.getText().toString());
                                newUser.update(objectId, new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if(e == null){
                                            Toast.makeText(ChangePassWord.this, "修改成功,即将跳转到登录界面",
                                                    Toast.LENGTH_SHORT).show();
                                            setResultBack(true);
                                        }else{
                                            Toast.makeText(ChangePassWord.this, "修改失败",
                                                    Toast.LENGTH_SHORT).show();
                                            setResultBack(false);
                                        }
                                    }
                                });

                            }else{
                                Toast.makeText(ChangePassWord.this, "验证码错误",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case SMSSDK.EVENT_GET_VERIFICATION_CODE:{
                            if(result == SMSSDK.RESULT_COMPLETE){
                                Toast.makeText(ChangePassWord.this, "验证码发送成功，请等待", Toast.LENGTH_SHORT).show();
                                i = 30;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while(i > 0){
                                            i--;
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            Message message = new Message();
                                            if(i == 0){
                                                message.what = ChangePassWord.MSG_WHAT_FOR_THREAD_DEATH;
                                            }else{
                                                message.what = ChangePassWord.MSG_WHAT_FOR_THREAD;
                                            }
                                            message.arg1 = i;
                                            handler.sendMessage(message);
                                        }
                                    }
                                }).start();
                                //验证码已发送
                            }else{
                                Toast.makeText(ChangePassWord.this, "验证码发送失败", Toast.LENGTH_SHORT).show();
                                //获取验证码失败
                            }
                        }
                        break;
                    }
                }
            }
        }
    };

    private void setResultBack(boolean isSuccess) {
        if(isFromLogin){
            Intent intent = new Intent();
            intent.putExtra("userName", signedPhoneNumber.getText().toString());
            if(isSuccess){
                setResult(RESULT_OK, intent);
            }else{
                setResult(RESULT_CANCELED, intent);
            }
        }else{
            if(isSuccess){
                Intent intent = new Intent(ChangePassWord.this, LoginActivity.class);
                intent.putExtra("userName", signedPhoneNumber.getText().toString());
                startActivity(intent);
            }
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.send_message_button:{
                BmobQuery<SignUserBaseClass> query = new BmobQuery<>();
                query.addWhereEqualTo("phoneNumber",signedPhoneNumber.getText().toString());
                query.findObjects(new FindListener<SignUserBaseClass>() {
                    @Override
                    public void done(List<SignUserBaseClass> list, BmobException e) {
                        if(!list.isEmpty()){
                            //获取用户的Id
                            SignUserBaseClass signUserBaseClass = list.get(0);
                            objectId = signUserBaseClass.getObjectId();

                            SMSSDK.getVerificationCode("86", signedPhoneNumber.getText().toString(),
                                    new OnSendMessageHandler() {
                                        @Override
                                        public boolean onSendMessage(String s, String s1) {
                                            return false;
                                        }
                                    });
                        }else{
                            Toast.makeText(ChangePassWord.this, "未查询到此用户", Toast.LENGTH_SHORT).show();
                        }
                        if(e != null){
                            Toast.makeText(ChangePassWord.this, "数据库异常",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            }
            case R.id.cancel_button:{
                setResultBack(false);
                break;
            }
            case R.id.complete_change:{
                SignUserBaseClass signUserBaseClass = new SignUserBaseClass();
                if(checkNumber.getText().toString().isEmpty()){
                    Toast.makeText(this, "验证码不能为空",Toast.LENGTH_SHORT).show();
                }else if(newPassWord.getText().toString().length() > 16 || newPassWord.getText().
                        toString().length() < 6){
                    Toast.makeText(this, "密码过长或过短", Toast.LENGTH_SHORT).show();
                }else{
                    SMSSDK.submitVerificationCode("86", signedPhoneNumber.getText().toString(),
                            checkNumber.getText().toString());
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResultBack(false);
        super.onBackPressed();
    }
}
