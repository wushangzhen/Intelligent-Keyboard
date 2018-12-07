package com.example.shenyi.inputmethodalpha;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

import static com.example.shenyi.inputmethodalpha.R.id.btn;
import static com.example.shenyi.inputmethodalpha.R.id.btn1;
import static com.example.shenyi.inputmethodalpha.R.id.btn2;
import static com.example.shenyi.inputmethodalpha.R.id.btn3;
import static com.example.shenyi.inputmethodalpha.R.id.btn4;
import static com.example.shenyi.inputmethodalpha.R.id.btn5;
import static com.example.shenyi.inputmethodalpha.R.id.btn6;
//import static com.example.shenyi.inputmethodalpha.R.id.s0;
//import static com.example.shenyi.inputmethodalpha.R.id.s1;
//import static com.example.shenyi.inputmethodalpha.R.id.s2;
//import static com.example.shenyi.inputmethodalpha.R.id.s3;
//import static com.example.shenyi.inputmethodalpha.R.id.s4;
//import static com.example.shenyi.inputmethodalpha.R.id.s5;
//import static com.example.shenyi.inputmethodalpha.R.id.s6;

/**
 * Created by shenyi on 2017/11/5.
 */

public class InputMethodAlpha extends InputMethodService implements OnKeyboardActionListener {
    protected KeyboardView keyboardView; // 对应keyboard.xml中定义的KeyboardView
    protected Keyboard keyboard;         // 对应qwerty.xml中定义的Keyboard
    public MySQLiteHelper myHelper;
    public DBManager dbHelper;
    public SQLiteDatabase db;
    Button[] buttons = new Button[50];
    Button[] signals = new Button[50];
    EditText eT ;
    private int current_number = 6; // 当前闪动的按键编号
    private int level = 1;//当前所处的层级
    protected String current_consonant; // 当前的声母
    private int button_maxNumber = 6;

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public View onCreateInputView() {
        // keyboard被创建后，将调用onCreateInputView函数
        dbHelper = new DBManager(getApplicationContext());
        dbHelper.openDatabase();
        dbHelper.closeDatabase();
        myHelper = new MySQLiteHelper(getApplicationContext(), DBManager.DB_PATH + "/" + DBManager.DB_NAME, null, 1);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                current_number = current_number>=button_maxNumber?0:current_number+1;
                msg.what = current_number;
                handler.sendMessage(msg);
            }
        }, 1000, 2000);

        keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);  // 此处使用了keyboard.xml
        keyboard = new Keyboard(this, R.xml.qwerty);  // 此处使用了qwerty.xml
        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        final RelativeLayout layout_bt = new RelativeLayout(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.button, null);//导入button.xml

        buttons[0] = view.findViewById(btn);
        buttons[1] = view.findViewById(btn1);
        buttons[2] = view.findViewById(btn2);
        buttons[3] = view.findViewById(btn3);
        buttons[4] = view.findViewById(btn4);
        buttons[5] = view.findViewById(btn5);
        buttons[6] = view.findViewById(btn6);

        eT = view.findViewById(R.id.eT);

        setFlash(6);
        setText_mainMenu();

        //layout_bt.setOrientation(LinearLayout.HORIZONTAL);
        layout_bt.addView(view);

        //final LinearLayout layout = new LinearLayout(this);
        //layout.setOrientation(LinearLayout.VERTICAL);
        final RelativeLayout layout = new RelativeLayout(this);
        keyboardView.setAlpha((float)0);//设置透明度
        keyboardView.setPreviewEnabled(false);//设置预览不可见
        layout_bt.setBackgroundColor(Color.parseColor("#FFFFFF"));
        layout.addView(layout_bt);
        layout.addView(keyboardView);


        return layout;
    }


    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setFlash(msg.what);
        }
    };

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();//输入交互
        switch (primaryCode) {
            case Keyboard.KEYCODE_DONE:
                switch (level) {
                    case 1: // 一级菜单选择声母集合
                    {
                        String[] strings = buttons[current_number].getText().toString().split("\n");
                        cleanButton();
                        switch (current_number) {
                            case 0:
                                level = 11; // 特殊的二级菜单
                                setText_subMenu();
                                break;
                            default:
                                level = 2;//普通二级菜单（选声母）
                                setButton0();
                                setNewText(strings, 1);
                                break;
                        }
                    }
                    break;
                    case 11: // 特殊的二级菜单
                    {
                        cleanButton();
                        switch (current_number) {
                            case 0: //"数符"
                                level = 111;
                                findCharacter();
                                break;
                            case 1: //"返回"
                                level = 1;
                                setText_mainMenu();
                                break;
                            case 2: //"删除"
                                level = 11;
                                ic.deleteSurroundingText(1, 0);
                                setText_subMenu();
                                break;
                            case 3: //"换行"
                                level = 11;
                                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                                setText_subMenu();
                                break;
                            case 4://"左移"
                                level = 11;
                                //ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK));
                                ic.commitText("",-1);
                                setText_subMenu();
                                break;
                            case 5://"右移"
                                level = 11;
                                ic.commitText("",2);
                                setText_subMenu();
                                break;
                            default:
                                level = 1;
                                setText_mainMenu();
                                break;
                        }
                    }
                    break;
                    case 111: {//数符选择主界面
                        String[] str = buttons[current_number].getText().toString().split("\n");// 当前选中光标上的文字
                        cleanButton();
                        switch (current_number) {
                            case 0: //"数符"
                                level = 11;
                                setNewText(str, 0);
                                break;
                            default:
                                level = 1111;
                                setButton0();
                                setNewText(str, 1);
                                break;
                        }
                    }
                    break;
                    case 1111: {
                        String str1111 = buttons[current_number].getText().toString();
                        cleanButton();
                        switch (current_number) {
                            case 0: //"数符"
                                level = 11;
                                setText_subMenu();
                                break;
                            default://输入符号
                                level = 1;
                                ic.commitText(str1111, 1);
                                setText_mainMenu();
                                break;
                        }
                    }
                    break;
                    case 2: {//选择声母
                        String[] str = buttons[current_number].getText().toString().split("\n");
                        cleanButton();
                        switch (current_number) {
                            case 0://数符  返回  删除  换行  保存  退出
                                level = 11;
                                setText_subMenu();
                                break;
                            default://选中一个声母
                                level = 3;
                                current_consonant = str[0];
                                findByConsonant(current_consonant);
                                setButton0();
                                setButton_textsize(1,6,20);
                                break;
                        }
                    }
                    break;
                    case 3: {//选择拼音集合
                        String[] str = buttons[current_number].getText().toString().split("\n");
                        cleanButton();
                        switch (current_number) {
                            case 0://数符  返回  删除  换行  保存  退出
                                level = 11;
                                setText_subMenu();
                                break;
                            default://选中一个拼音
                                level = 4;
                                findByPinyin(str,str.length);
                                //查拼音
                                setButton0();
                                setButton_textsize(1,6,35);
                                break;
                        }
                    }
                    break;
                    case 4:{//候选字集合
                        String[] str = buttons[current_number].getText().toString().split("\n");
                        cleanButton();
                        switch (current_number) {
                            case 0://数符  返回  删除  换行  保存  退出
                                level = 11;
                                setText_subMenu();
                                break;
                            default://选中一个候选集合
                                if(str.length==1)
                                {
                                    level = 1;
                                    ic.commitText(str[0],1);
                                    setText_mainMenu();
                                }
                                else if(str.length<=6)
                                {
                                    level = 5;
                                    setNewText(str,1);
                                    setButton0();
                                }
                                else
                                {
                                    level = 4;
                                    setExtraText(str);
                                    setButton0();
                                }
                                break;
                        }
                    }
                    break;

                    case 5://
                    {
                        String str = buttons[current_number].getText().toString();

                        cleanButton();
                        switch (current_number) {
                            case 0://数符  返回  删除  换行  保存  退出
                                level = 11;
                                setNewText(str.split("\n"), 0);
                                break;
                            default://选中一个拼音
                                level = 1;
                                //ic.commitText(str,1);

                                eT.setText(eT.getText()+str);
                                eT.setSelection(eT.getText().length());
                                setText_mainMenu();
                                break;
                        }
                    }
                    break;
                }
                setFlash(0);
                setButton_maxNumber();
                break;
            default:
                char code = (char) primaryCode;
                ic.commitText(String.valueOf(code), 1);
        }
    }

    public void cleanButton() {//清除所有button的文字
        for (int i = 0; i < 7; i++) {
            buttons[i].setText("");
        }
    }

    public void setNewText(String[] string, int start) {//将string数组中的字符串赋给button，从button[start]开始
        int number = string.length;
        for (int i = start; i < start + number; i++) {
            buttons[i].setText(string[i - start]);
        }
    }

    public void setExtraText(String[] string)
    {
        int number = string.length;
        int min = number/6;// 1
        int max = min+1;
        int longnum = number%6;
        int str_num = 0;

        for(int i=1;i<=longnum;i++)
        {
            String str="";
            for(int j=0;j<max;j++)
            {
                str+=string[str_num];
                str+="\n";
                str_num++;
            }
            buttons[i].setText(str);
        }
        for(int i=longnum+1;i<7;i++)
        {
            String str="";
            for(int j=0;j<min;j++)
            {
                str+=string[str_num];
                str+="\n";
                str_num++;
            }
            buttons[i].setText(str);
        }
    }

    public void setText_mainMenu()
    {
        buttons[0].setText("数符\n返回\n删除\n换行\n左移\n右移");
        buttons[1].setText("b\np\nm\nr");
        buttons[2].setText("f\ng\nk\nh");
        buttons[3].setText("d\nt\nn\nl");
        buttons[4].setText("无声\nj\nq\nx");
        buttons[5].setText("zh\nch\ny\nsh");
        buttons[6].setText("z\nc\nw\ns");
        setButton_textsize(0,0,25);
        setButton_textsize(1,6,30);

    }

    public void setText_subMenu(){
        buttons[0].setText("数符");
        buttons[1].setText("返回");
        buttons[2].setText("删除");
        buttons[3].setText("换行");
        buttons[4].setText("左移");
        buttons[5].setText("右移");
        buttons[6].setText("");
        setButton_textsize(0,6,30);
    }

    public void setButton0(){
        buttons[0].setText("数符\n返回\n删除\n换行\n左移\n右移");
    }

    public void setFlash(int number){
        final Animation anim = AnimationUtils.loadAnimation(InputMethodAlpha.this, R.anim.flash);
        for(int i=0;i<7;i++)
        {
                buttons[i].clearAnimation();
        }
        buttons[number].setAnimation(anim);
        //signals[number].setBackgroundColor(Color.parseColor("#FF0000"));
        current_number = number;
    }

    public void setButton_maxNumber() {
        for(int i =0;i<7;i++)
        {
            if(buttons[i].getText()!="") {
                button_maxNumber = i;
            }
        }

    }

    public void setButton_blank(int start,int end,int blank_number){
        for(int i = start;i<=end;i++)
        {
            int len = buttons[i].getText().toString().split("\n").length-1;// \n数量
            if( len< blank_number)
            {
                int res = blank_number-len;
                String a= "";
                for(int j = 0;j<res;j++)
                {
                    a = a + "\n";
                }
                buttons[i].setText(buttons[i].getText()+ a);
            }
        }
    }

    public void setButton_textsize(int start ,int end, int size)
    {
        for(int i = start;i<=end;i++)
        {
            buttons[i].setTextSize(size);
        }
    }


    public void findCharacter() {
        db = myHelper.getReadableDatabase();
        String sql = "select * from num where descriptor1='shuzi'or descriptor2='fuhao'";
        Cursor c = db.rawQuery(sql, null);
        int index = 0;
        int number = 0;
        String[] ShuFu = new String[100];
        String[] Display_Button = new String[100];
        while (c.moveToNext()) {
            ShuFu[index] = c.getString(c.getColumnIndex("number"));
            String string = ShuFu[index];
            if (number < 6) {
                int index_temp = index;
                Display_Button[index_temp] = string;
                number++;
                index++;
            } else {
                int number_temp = number - (number / 6) * 6;
                Display_Button[number_temp] = Display_Button[number_temp] + "\n" + string;
                number++;
                index++;
            }
            for (int i = 1; i < 7; i++) {
                buttons[i].setText(Display_Button[i - 1]);
            }
            setButton0();
        }
    }

    public void findByConsonant(String consonant)
    {
        db = myHelper.getReadableDatabase();
        String sql = "select * from Demo1 where fuyin='" + consonant + "'";
        Cursor c1 = db.rawQuery(sql, null);
        int index1 = 0;
        int number1 = 0;
        String[] PINYIN = new String[100];
        String[] Display_Button1 = new String[100];
        while (c1.moveToNext()) {
            if (index1 == 0) {
                PINYIN[index1] = c1.getString(c1.getColumnIndex("pinyin"));
                Display_Button1[0] = PINYIN[index1];
                index1++;
                number1++;
            } else {
                PINYIN[index1] = c1.getString(c1.getColumnIndex("pinyin"));
                if (!PINYIN[index1].equals(PINYIN[index1 - 1])) {
                    if (number1 < 6) {
                        int index_temp = index1;
                        Display_Button1[index_temp] = PINYIN[index_temp];
                        number1++;
                        index1++;
                    } else {
                        int number_temp = number1 - (number1 / 6) * 6;
                        Display_Button1[number_temp] = Display_Button1[number_temp] + "\n" + PINYIN[index1];
                        number1++;
                        index1++;
                    }
                }
            }
        }
        for (int i = 1; i < 7; i++) {
            buttons[i].setText(Display_Button1[i - 1]);
        }
        c1.close();
    }

    public void findByPinyin(String[]string_separated,int num){
        if (num == 0) {
        } else if (num == 1) {
            String sql1 = "select * from Demo1 where pinyin='" + string_separated[0] + " '";
            Cursor c1 = db.rawQuery(sql1, null);
            int index1 = 0;
            int number1 = 0;
            String[] PINYIN = new String[100];
            String[] Display_Button1 = new String[100];
            if (c1.moveToNext()) {
                while (c1.moveToNext()) {
                    if (index1 == 0) {
                        PINYIN[index1] = c1.getString(c1.getColumnIndex("hanzi"));
                        Display_Button1[0] = PINYIN[index1];
                        index1++;
                        number1++;
                    } else {
                        PINYIN[index1] = c1.getString(c1.getColumnIndex("hanzi"));
                        if (!PINYIN[index1].equals(PINYIN[index1 - 1])) {
                            if (number1 < 6) {
                                int index_temp = index1;
                                Display_Button1[index_temp] = PINYIN[index_temp];
                                number1++;
                                index1++;
                            } else {
                                int number_temp = number1 - (number1 / 6) * 6;
                                Display_Button1[number_temp] = Display_Button1[number_temp] + "\n" + PINYIN[index1];
                                number1++;
                                index1++;
                            }
                        }
                    }
                }
                for (int i = 1; i < 7; i++) {
                    buttons[i].setText(Display_Button1[i - 1]);
                }
                c1.close();
            } else {
            }
        } else if (num == 2) {
            String sql2 = "select * from Demo1 where pinyin='" + string_separated[0] + "'or pinyin='" + string_separated[1] + "'";
            Cursor c2 = db.rawQuery(sql2, null);
            int index2 = 0;
            int number2 = 0;
            String[] PINYIN = new String[100];
            String[] Display_Button2 = new String[100];
            if (c2.moveToNext()) {
                while (c2.moveToNext()) {
                    if (index2 == 0) {
                        PINYIN[index2] = c2.getString(c2.getColumnIndex("hanzi"));
                        Display_Button2[0] = PINYIN[index2];
                        index2++;
                        number2++;
                    } else {
                        PINYIN[index2] = c2.getString(c2.getColumnIndex("hanzi"));
                        if (!PINYIN[index2].equals(PINYIN[index2 - 1])) {
                            if (number2 < 6) {
                                int index_temp = index2;
                                Display_Button2[index_temp] = PINYIN[index_temp];
                                number2++;
                                index2++;
                            } else {
                                int number_temp = number2 - (number2 / 6) * 6;
                                Display_Button2[number_temp] = Display_Button2[number_temp] + "\n" + PINYIN[index2];
                                number2++;
                                index2++;
                            }
                        }

                    }
                }
                for (int i = 1; i < 7; i++) {
                    buttons[i].setText(Display_Button2[i - 1]);
                }
                c2.close();
            } else {
                for (int k = 1, v = 0; k < 7 && v < num; v++, k++) {
                    {
                        buttons[k].setText(string_separated[v]);
                    }
                }
            }
        } else if (num == 3) {
            String sql11 = "select * from Demo1 where pinyin='" + string_separated[0] + "'";
            String sql22 = "select * from Demo1 where pinyin='" + string_separated[1] + "'";
            String sql33 = "select * from Demo1 where pinyin='" + string_separated[2] + "'";
            Cursor c11 = db.rawQuery(sql11, null);
            Cursor c22 = db.rawQuery(sql22, null);
            Cursor c33 = db.rawQuery(sql33, null);
            String[] button = new String[100];
            for(int i = 1;i<=6;i++)
            {
                button[i]="";
            }
//            button[1]=button[4]=string_separated[0]+"\n\n\n";
//            button[2]=button[5]=string_separated[1]+"\n\n\n";
//            button[3]=button[6]=string_separated[2]+"\n\n\n";
            if(c11.moveToNext())
            {
                int number = 0;
                while(c11.moveToNext()){
                    if(number<5)
                        button[1]+=c11.getString(c11.getColumnIndex("hanzi"))+"\n";
                    if(number>=5)
                        button[4]+=c11.getString(c11.getColumnIndex("hanzi"))+"\n";
                    number ++;
                    if(number==10)
                        break;
                }
                if(button[4].equals(string_separated[0]+"\n\n\n"))
                {
                    button[4]="";
                }
            }
            if(c22.moveToNext())
            {
                int number = 0;
                while(c22.moveToNext()){
                    if(number<5)
                        button[2]+=c22.getString(c22.getColumnIndex("hanzi"))+"\n";
                    if(number>=5)
                        button[5]+=c22.getString(c22.getColumnIndex("hanzi"))+"\n";
                    number ++;
                    if(number==10)
                        break;
                }
                if(button[5].equals(string_separated[1]+"\n\n\n"))
                {
                    button[5]="";
                }
            }
            if(c33.moveToNext())
            {
                int number = 0;
                while(c33.moveToNext()&&number != 10){
                    if(number<5)
                        button[3]+=c33.getString(c33.getColumnIndex("hanzi"))+"\n";
                    if(number>=5)
                        button[6]+=c33.getString(c33.getColumnIndex("hanzi"))+"\n";
                    number ++;
                }
                if(button[6].equals(string_separated[2]+"\n\n\n"))
                {
                    button[6]="";
                }
            }
            for(int i = 1;i<=6;i++)
            {
                buttons[i].setText(button[i]);
            }
            setButton_blank(1,6,5);
        } else if (num == 4) {
            String sql4 = "select * from Demo1 where pinyin='" + string_separated[0] + "'or pinyin='" + string_separated[1]
                    + "'or pinyin='" + string_separated[2] + "'or pinyin='" + string_separated[3] + "'";
            Cursor c4 = db.rawQuery(sql4, null);
            int index4 = 0;
            int number4 = 0;
            String[] PINYIN = new String[100];
            String[] Display_Button4 = new String[100];
            if (c4.moveToNext()) {
                while (c4.moveToNext()) {
                    if (index4 == 0) {
                        PINYIN[index4] = c4.getString(c4.getColumnIndex("hanzi"));
                        Display_Button4[0] = PINYIN[index4];
                        index4++;
                        number4++;
                    } else {
                        PINYIN[index4] = c4.getString(c4.getColumnIndex("hanzi"));
                        if (!PINYIN[index4].equals(PINYIN[index4 - 1])) {
                            if (number4 < 6) {
                                int index_temp = index4;
                                Display_Button4[index_temp] = PINYIN[index_temp];
                                number4++;
                                index4++;
                            } else {
                                int number_temp = number4 - (number4 / 6) * 6;
                                Display_Button4[number_temp] = Display_Button4[number_temp] + "\n" + PINYIN[index4];
                                number4++;
                                index4++;
                            }
                        }

                    }
                }
                for (int i = 1; i < 7; i++) {
                    buttons[i].setText(Display_Button4[i - 1]);
                }
                c4.close();
            } else {
                for (int k = 1, v = 0; k < 7 && v < num; v++, k++) {
                    {
                        buttons[k].setText(string_separated[v]);
                    }
                }
            }

        }else if (num == 5) {
            String sql4 = "select * from Demo1 where pinyin='" + string_separated[0] + "'or pinyin='" + string_separated[1]
                    + "'or pinyin='" + string_separated[2] + "'or pinyin='" + string_separated[3] + "'or pinyin='"
                    + string_separated[4] + "'";
            Cursor c4 = db.rawQuery(sql4, null);
            int index4 = 0;
            int number4 = 0;
            String[] PINYIN = new String[100];
            String[] Display_Button4 = new String[100];
            if (c4.moveToNext()) {
                while (c4.moveToNext()) {
                    if (index4 == 0) {
                        PINYIN[index4] = c4.getString(c4.getColumnIndex("hanzi"));
                        Display_Button4[0] = PINYIN[index4];
                        index4++;
                        number4++;
                    } else {
                        PINYIN[index4] = c4.getString(c4.getColumnIndex("hanzi"));
                        if (!PINYIN[index4].equals(PINYIN[index4 - 1])) {
                            if (number4 < 6) {
                                int index_temp = index4;
                                Display_Button4[index_temp] = PINYIN[index_temp];
                                number4++;
                                index4++;
                            } else {
                                int number_temp = number4 - (number4 / 6) * 6;
                                Display_Button4[number_temp] = Display_Button4[number_temp] + "\n" + PINYIN[index4];
                                number4++;
                                index4++;
                            }
                        }
                    }
                }
                for (int i = 1; i < 7; i++) {
                    buttons[i].setText(Display_Button4[i - 1]);
                }
                c4.close();
            } else {
                for (int k = 1, v = 0; k < 7 && v < num; v++, k++) {
                    {
                        buttons[k].setText(string_separated[v]);
                    }
                }
            }
        }
    }
}
