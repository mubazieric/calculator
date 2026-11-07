package com.iwat.scientific;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.ClipData;
import android.content.ActivityNotFoundException;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;
import androidx.core.content.FileProvider;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int INK = 0xfff1f0f5, MUTED = 0xffa3a4b0, BG = 0xff101116;
    private static final int CARD = 0xff34343e, KEY = 0xff252831, TEAL = 0xffe9bf52, PURPLE = 0xffb899fa;
    private static final int SCREEN = 0xffddebe9, SCREEN_INK = 0xff1c2834, ORANGE = 0xfff09a5b;
    private static final int CAMERA = 4;
    private LinearLayout root, body, nav, calculatorKeys, memoryStrip, adSlot;
    private ConsentInformation consentInformation;
    private AdView adView;
    private boolean adsStarted;
    private EditText input;
    private TextView preview, angleBadge;
    private Uri photo;
    private boolean degrees = true;
    private boolean secondLayer = false;
    private boolean engineering = false;
    private double answer = 0, memory = 0;
    private String expression = "", screen = "Calculate";
    private JSONArray history = new JSONArray();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        degrees = getPreferences(0).getBoolean("degrees", true);
        memory = Double.longBitsToDouble(getPreferences(0).getLong("memory", Double.doubleToRawLongBits(0)));
        try { history = new JSONArray(getPreferences(0).getString("history", "[]")); } catch (Exception ignored) { }
        if (state != null) {
            expression = state.getString("expression", "");
            answer = state.getDouble("answer", 0);
            secondLayer = state.getBoolean("secondLayer", false);
            engineering = state.getBoolean("engineering", false);
            photo = state.getParcelable("photo");
        }
        show(state == null ? "Calculate" : state.getString("screen", "Calculate"));
        if (BuildConfig.ADS_ENABLED) requestAdConsent();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        if (input != null && screen.equals("Calculate")) expression = input.getText().toString();
        state.putString("expression", expression);
        state.putString("screen", screen);
        state.putDouble("answer", answer);
        state.putBoolean("secondLayer", secondLayer);
        state.putBoolean("engineering", engineering);
        state.putParcelable("photo", photo);
        super.onSaveInstanceState(state);
    }
    @Override protected void onResume() { super.onResume(); if(adView!=null) adView.resume(); }
    @Override protected void onPause() { if(adView!=null) adView.pause(); super.onPause(); }
    @Override protected void onDestroy() { if(adView!=null) { adView.destroy(); adView=null; } super.onDestroy(); }

    private void requestAdConsent() {
        consentInformation=UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(this,new ConsentRequestParameters.Builder().build(),()->{
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,error->{ if(consentInformation.canRequestAds()) startAds(); });
            if(consentInformation.canRequestAds()) startAds();
        },error->{ if(consentInformation.canRequestAds()) startAds(); });
    }
    private void startAds() {
        if(adsStarted || isFinishing() || isDestroyed()) return;
        adsStarted=true;
        new Thread(()->MobileAds.initialize(getApplicationContext(),status->runOnUiThread(()->{
            if(!isFinishing() && !isDestroyed()) showBanner();
        }))).start();
    }
    private void showBanner() {
        if(!adsStarted || adSlot==null || adView!=null || !consentInformation.canRequestAds()) return;
        adView=new AdView(this); adView.setAdSize(AdSize.BANNER); adView.setAdUnitId(BuildConfig.AD_BANNER_ID);
        adSlot.addView(adView,new LinearLayout.LayoutParams(-2,dp(50)));
        adView.loadAd(new AdRequest.Builder().build());
    }

    private int dp(float n) { return (int)(getResources().getDisplayMetrics().density * n + .5f); }
    private GradientDrawable shape(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private GradientDrawable gradient(int a, int b, int radius) { GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a,b}); d.setCornerRadius(dp(radius)); return d; }
    private Drawable texture() {
        return new Drawable() {
            final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override public void draw(Canvas canvas) {
                canvas.drawColor(BG); paint.setColor(0xff25262d);
                int step=dp(9), width=getBounds().width(), height=getBounds().height();
                for(int y=0;y<height;y+=step) for(int x=0;x<width;x+=step)
                    canvas.drawCircle(x+((y/step)%2)*step/2f,y,dp(.65f),paint);
            }
            @Override public void setAlpha(int alpha) { }
            @Override public void setColorFilter(android.graphics.ColorFilter filter) { }
            @Override public int getOpacity() { return PixelFormat.OPAQUE; }
        };
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextColor(color); t.setTextSize(size);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private void pad(View v, int h, int vspace) { v.setPadding(dp(h),dp(vspace),dp(h),dp(vspace)); }
    private void gap(LinearLayout parent, int height) { View v = new View(this); parent.addView(v, new LinearLayout.LayoutParams(1,dp(height))); }
    private void show(String page) {
        if (input != null && screen.equals("Calculate")) expression = input.getText().toString();
        if(adView!=null) { adView.destroy(); adView=null; }
        screen = page;
        calculatorKeys = null; memoryStrip = null; adSlot=null;
        root = column(); root.setBackground(texture());
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                // Android 15+ draws behind system bars; typing hides the keypad
                // instead of shrinking its buttons beneath the on-screen keyboard.
                if (Build.VERSION.SDK_INT >= 35) {
                    android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                    view.setPadding(0, bars.top, 0, bars.bottom);
                }
                if (calculatorKeys != null) calculatorKeys.setVisibility(insets.isVisible(WindowInsets.Type.ime()) ? View.GONE : View.VISIBLE);
                if (memoryStrip != null) memoryStrip.setVisibility(insets.isVisible(WindowInsets.Type.ime()) ? View.GONE : View.VISIBLE);
                return insets;
            });
        }
        body = column();
        if (page.equals("Calculate")) {
            // Keep the whole keypad in the available viewport, even on compact phones.
            body.setPadding(dp(12),dp(7),dp(12),dp(7));
            root.addView(body, new LinearLayout.LayoutParams(-1,0,1));
        } else {
            ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setVerticalScrollBarEnabled(false);
            root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
            body.setPadding(dp(20),dp(20),dp(20),dp(24)); scroll.addView(body);
        }
        header();
        switch (page) {
            case "Convert": converter(); break;
            case "History": history(); break;
            case "Scan": scan(); break;
            case "Contact": contact(); break;
            default: calculator();
        }
        if(!page.equals("Calculate") && BuildConfig.ADS_ENABLED) {
            adSlot=row(); adSlot.setGravity(Gravity.CENTER); root.addView(adSlot);
            if(adsStarted) showBanner();
        }
        navigation();
        if (Build.VERSION.SDK_INT >= 30) root.requestApplyInsets();
    }
    private void header() {
        LinearLayout top = row(); body.addView(top);
        boolean compact = screen.equals("Calculate");
        TextView mark = text("∑",compact?20:24,BG,true); mark.setGravity(Gravity.CENTER);
        mark.setBackground(gradient(PURPLE,0xffe8c9ff,12)); top.addView(mark,new LinearLayout.LayoutParams(dp(compact?38:50),dp(compact?38:50)));
        LinearLayout titles = column(); titles.setPadding(dp(10),0,0,0); top.addView(titles,new LinearLayout.LayoutParams(0,-2,1));
        titles.addView(text("IWAT",compact?17:21,INK,true)); titles.addView(text("SCIENTIFIC CALCULATOR",compact?9:10,PURPLE,true));
        TextView ornament = text("✦",compact?21:27,PURPLE,true); top.addView(ornament);
        if (compact) { gap(body,6); return; }
        gap(body,26);
        body.addView(text(screen.equals("Calculate") ? "Your pocket laboratory." : screen.equals("Scan") ? "Solve from a photo." : screen.equals("History") ? "Every answer, saved." : screen.equals("Contact") ? "We'd love to hear from you." : "Convert with confidence.",24,INK,true));
        gap(body,5);
        body.addView(text(screen.equals("Calculate") ? "Scientific precision, beautifully simple." : screen.equals("Scan") ? "Capture an expression. Check it. Solve it." : screen.equals("History") ? "Tap an entry to calculate it again." : screen.equals("Contact") ? "Questions, feedback, and suggestions." : "Everyday units, instant results.",13,MUTED,false));
        gap(body,22);
    }
    private void navigation() {
        nav = row(); pad(nav,8,4); nav.setBackgroundColor(0xff1b1c23); root.addView(nav);
        String[] items = {"Calculate","Convert","History","Scan","Contact"}; String[] icons = {"▦","⇄","◷","◎","✉"};
        for (int i=0;i<items.length;i++) {
            final String destination = items[i];
            LinearLayout cell=column(); cell.setGravity(Gravity.CENTER);
            if (screen.equals(destination)) cell.setBackground(shape(0xff3a334b,16));
            TextView symbol=text(icons[i],20,screen.equals(destination)?PURPLE:MUTED,true); symbol.setGravity(Gravity.CENTER); cell.addView(symbol);
            TextView label=text(destination,10,screen.equals(destination)?PURPLE:MUTED,true); label.setGravity(Gravity.CENTER); cell.addView(label);
            nav.addView(cell,new LinearLayout.LayoutParams(0,dp(48),1)); cell.setOnClickListener(v->show(destination));
        }
    }
    private void calculator() {
        LinearLayout flags=row(); body.addView(flags);
        angleBadge=text(degrees?"● DEG":"● RAD",11,TEAL,true); flags.addView(angleBadge,new LinearLayout.LayoutParams(0,-2,1));
        TextView mode=text((secondLayer?"2ND":"1ST")+"   MATH   •   NORM",10,secondLayer?PURPLE:MUTED,true); flags.addView(mode);
        gap(body,6);
        LinearLayout frame=row(); frame.setBackground(gradient(SCREEN,0xffd1e1e0,18));
        int screenHeight=(int)(getResources().getDisplayMetrics().heightPixels/getResources().getDisplayMetrics().density);
        body.addView(frame,new LinearLayout.LayoutParams(-1,dp(Math.max(108,Math.min(158,screenHeight*.19f)))));
        View stripe=new View(this); stripe.setBackground(gradient(PURPLE,0xff765cf4,3));
        LinearLayout.LayoutParams stripeParams=new LinearLayout.LayoutParams(dp(4),-1); stripeParams.setMargins(dp(8),dp(12),0,dp(12)); frame.addView(stripe,stripeParams);
        LinearLayout display=column(); display.setPadding(dp(12),dp(10),dp(16),dp(9)); frame.addView(display,new LinearLayout.LayoutParams(0,-1,1));
        LinearLayout displayTop=row(); display.addView(displayTop);
        displayTop.addView(text("IWAT  /  SCIENTIFIC",10,0xff546671,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView menu=text("⋮",25,SCREEN_INK,true); menu.setGravity(Gravity.CENTER); displayTop.addView(menu,new LinearLayout.LayoutParams(dp(28),dp(28))); menu.setOnClickListener(v->toolsMenu());
        input=new EditText(this); input.setSingleLine(false); input.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        input.setTextSize(24); input.setTextColor(SCREEN_INK); input.setHintTextColor(0xff778b90); input.setHint("0");
        input.setBackgroundColor(Color.TRANSPARENT); input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        display.addView(input,new LinearLayout.LayoutParams(-1,0,1)); input.setText(expression); input.setSelection(input.length());
        preview=text("= 0",28,0xff59479b,true); preview.setSingleLine(true); preview.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        preview.setEllipsize(TextUtils.TruncateAt.START);
        display.addView(preview,new LinearLayout.LayoutParams(-1,dp(35)));
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) { }
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                updatePreview(s.toString());
            }
            public void afterTextChanged(Editable e) { }
        });
        updatePreview(expression);
        gap(body,7);
        LinearLayout strip=row(); memoryStrip=strip; body.addView(strip);
        small(strip,"MODE",()->toolsMenu());
        small(strip,"SOLVE",()->solvePrompt());
        small(strip,"CONST",()->constantsMenu());
        small(strip,"S⇔D",()->fractionDisplay());
        small(strip,"MEM",()->memoryMenu());
        small(strip,degrees?"DEG":"RAD",()->{ degrees=!degrees; getPreferences(0).edit().putBoolean("degrees",degrees).apply(); show("Calculate"); });
        gap(body,6);
        LinearLayout keypad=column(); calculatorKeys=keypad; body.addView(keypad,new LinearLayout.LayoutParams(-1,0,1));
        String[][] scientific=secondLayer
            ? new String[][]{{"asin(","acos(","atan(","sinh(","cosh("},{"tanh(","abs(","∛(","eˣ","10ˣ"},{"n√","π","e","Ans","1st"}}
            : new String[][]{{"sin(","cos(","tan(","log(","ln("},{"√(","^","x²","x!","a/b"},{"π","e","nCr","nPr","2nd"}};
        for(String[] keys:scientific) keyRow(keypad,keys,true);
        String[][] number={{"AC","(",")","⌫","±"},{"7","8","9","÷","%"},{"4","5","6","×","1/x"},{"1","2","3","−","Ans"},{"0",".","EXP","+","="}};
        for(String[] keys:number) keyRow(keypad,keys,false);
        // The editor does not summon the keyboard just because the activity opened.
        body.setFocusableInTouchMode(true); body.requestFocus();
    }
    private void updatePreview(String value) {
        try { preview.setText(getString(R.string.result_format, formatResult(new Expression(value,degrees,answer).evaluate()))); preview.setTextColor(0xff59479b); }
        catch(Exception e) { preview.setText(value.isEmpty()?"= 0":"…"); preview.setTextColor(MUTED); }
    }
    private String formatResult(double value) {
        if (!engineering || value==0) return Expression.format(value);
        int exponent=(int)Math.floor(Math.log10(Math.abs(value))/3)*3;
        return Expression.format(value/Math.pow(10,exponent))+"E"+exponent;
    }
    private void saveMemory() { getPreferences(0).edit().putLong("memory",Double.doubleToRawLongBits(memory)).apply(); }
    private double current() { try { return new Expression(input.getText().toString(),degrees,answer).evaluate(); } catch(Exception e) { return answer; } }
    private void small(LinearLayout parent,String label,Runnable action) {
        TextView button=text(label,label.length()>4?10:12,INK,true); button.setGravity(Gravity.CENTER); button.setBackground(shape(CARD,14));
        button.setSingleLine(true); button.setIncludeFontPadding(false);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(42),1); p.setMargins(dp(2),0,dp(2),0); parent.addView(button,p); button.setOnClickListener(v->action.run());
    }
    private void keyRow(LinearLayout keypad,String[] keys,boolean science) {
        LinearLayout line=row(); keypad.addView(line,new LinearLayout.LayoutParams(-1,0,1));
        for(String key:keys) {
            boolean equals=key.equals("=");
            boolean layer=key.equals("2nd") || key.equals("1st");
            boolean warm=key.equals("AC") || key.equals("⌫");
            boolean operation=key.equals("+") || key.equals("−") || key.equals("×") || key.equals("÷");
            TextView button=text(key,science?14:18,(equals||warm||layer)?BG:(science?PURPLE:INK),true); button.setGravity(Gravity.CENTER);
            button.setIncludeFontPadding(false); button.setSingleLine(true);
            button.setBackground(equals?gradient(TEAL,0xffffdf7d,12):(warm?gradient(ORANGE,0xfff7ad6e,12):(layer?gradient(PURPLE,0xffd0aaff,12):shape(science?CARD:(operation?0xff43444e:KEY),12))));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,1); p.setMargins(dp(2),dp(1),dp(2),dp(1)); line.addView(button,p);
            button.setOnClickListener(v->press(key));
        }
    }
    private void press(String key) {
        switch(key) {
            case "AC": input.setText(""); return;
            case "⌫": int pos=input.getSelectionStart(); if(pos>0) input.getText().delete(pos-1,pos); return;
            case "=": calculate(); return;
            case "2nd": case "1st": secondLayer=!secondLayer; show("Calculate"); return;
            case "x²": insert("^2"); return;
            case "x!": insert("!"); return;
            case "a/b": insert("/"); return;
            case "∛(": insert("cbrt("); return;
            case "n√": insert("^(1/"); return;
            case "eˣ": insert("exp("); return;
            case "10ˣ": insert("10^("); return;
            case "1/x": insert("^(-1)"); return;
            case "±":
                String value=input.getText().toString();
                if (value.startsWith("-(") && value.endsWith(")")) input.setText(value.substring(2,value.length()-1));
                else if (value.startsWith("-")) input.setText(value.substring(1));
                else input.setText(value.isEmpty()?"-":"-("+value+")");
                input.setSelection(input.length()); return;
            case "EXP": insert("E"); return;
            default: insert(key);
        }
    }
    private void insert(String value) { int pos=Math.max(0,input.getSelectionStart()); input.getText().insert(pos,value); input.setSelection(pos+value.length()); }
    private void calculate() {
        String query=input.getText().toString().trim();
        try {
            double result=new Expression(query,degrees,answer).evaluate();
            String formatted=formatResult(result); answer=result;
            preview.setText(getString(R.string.result_format, formatted)); preview.setTextColor(0xff59479b);
            saveHistory(query,formatted,query);
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);
        } catch(Exception e) { preview.setText(e.getMessage()); preview.setTextColor(0xffffa6a6); }
    }
    private void saveHistory(String label,String result,String replay) throws Exception {
        JSONObject entry=new JSONObject(); entry.put("expression",label); entry.put("result",result);
        entry.put("replay",replay); entry.put("mode",degrees?"DEG":"RAD");
        JSONArray next=new JSONArray(); next.put(entry); for(int i=0;i<Math.min(99,history.length());i++) next.put(history.get(i));
        history=next; getPreferences(0).edit().putString("history",history.toString()).apply();
    }
    private void resultDialog(String label,String value) {
        TextView detail=text(value,23,PURPLE,true); pad(detail,22,20);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(detail)
            .setPositiveButton("USE RESULT",(d,w)->{ input.setText(value); input.setSelection(input.length()); })
            .setNegativeButton("CLOSE",null).create(); openDialog(dialog,label);
    }
    private void infoDialog(String label,String detail) {
        TextView content=text(detail,17,INK,false); pad(content,22,18);
        ScrollView scroll=new ScrollView(this); scroll.addView(content);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(scroll).setPositiveButton("DONE",null).create();
        openDialog(dialog,label);
    }
    private void publish(String label,double result) {
        AdvancedMath.finite(result);
        String value=formatResult(result); answer=result;
        try { saveHistory(label,value,Expression.format(result)); } catch(Exception e) { message("Unable to save result"); }
        updatePreview(input.getText().toString());
        resultDialog(label,value);
    }
    private void menu(String title,String[] choices,java.util.function.IntConsumer action) {
        LinearLayout content=column(); pad(content,18,10);
        ScrollView scroll=new ScrollView(this); scroll.addView(content);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(scroll).create();
        for(int i=0;i<choices.length;i++) {
            final int selected=i;
            TextView option=text(choices[i],16,INK,true); pad(option,15,13); option.setBackground(shape(i%2==0?KEY:CARD,12));
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2); params.bottomMargin=dp(5); content.addView(option,params);
            option.setOnClickListener(v->{ dialog.dismiss(); action.accept(selected); });
        }
        openDialog(dialog,title);
    }
    private void openDialog(AlertDialog dialog,String title) {
        TextView heading=text(title,20,INK,true); pad(heading,22,18); dialog.setCustomTitle(heading);
        dialog.show(); dialog.getWindow().setBackgroundDrawable(shape(0xff20222b,20));
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels-dp(36),-2);
        if(dialog.getButton(AlertDialog.BUTTON_POSITIVE)!=null) dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(PURPLE);
        if(dialog.getButton(AlertDialog.BUTTON_NEGATIVE)!=null) dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(MUTED);
    }
    private interface ToolAction { void run(String[] values); }
    private void prompt(String title,String[] labels,String[] defaults,ToolAction action) {
        ScrollView scroll=new ScrollView(this); LinearLayout content=column(); pad(content,20,12); scroll.addView(content);
        EditText[] fields=new EditText[labels.length];
        for(int i=0;i<labels.length;i++) {
            content.addView(text(labels[i],12,PURPLE,true)); gap(content,5);
            EditText field=new EditText(this); field.setText(defaults[i]); field.setSingleLine(true);
            field.setSelectAllOnFocus(true); field.setTextColor(INK); field.setTextSize(17);
            field.setHintTextColor(MUTED); field.setBackground(shape(KEY,11)); pad(field,12,9);
            content.addView(field,new LinearLayout.LayoutParams(-1,dp(49))); gap(content,14); fields[i]=field;
        }
        AlertDialog dialog=new AlertDialog.Builder(this).setView(scroll).setPositiveButton("CALCULATE",null).setNegativeButton("CANCEL",null).create();
        openDialog(dialog,title);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String[] values=new String[fields.length]; for(int i=0;i<fields.length;i++) values[i]=fields[i].getText().toString().trim();
            try { action.run(values); dialog.dismiss(); }
            catch(Exception e) { message(e.getMessage()==null?"Check the input":e.getMessage()); }
        });
    }
    private double numeric(String value) { return new Expression(value,degrees,answer).evaluate(); }
    private double[] numbers(String csv) {
        String[] parts=csv.split(","); double[] values=new double[parts.length];
        for(int i=0;i<parts.length;i++) values[i]=numeric(parts[i].trim()); return values;
    }
    private void toolsMenu() {
        String[] choices={"d/dx  ·  Derivative at x","∫  ·  Definite integral","SOLVE  ·  Root in an interval",
            "STAT  ·  Sum, mean, median, deviation","MATRIX  ·  2×2 to 6×6 operations","VECTOR  ·  Dot, cross, magnitude",
            "CMPLX  ·  Complex arithmetic","Pol / Rec  ·  Coordinate conversion","GCD / LCM  ·  Integers",
            "ENG  ·  Engineering notation","CONST  ·  Scientific constants","FUNC  ·  Floor, ceil, cot, mod","RAN  ·  Random numbers",
            "LIMIT  ·  Two-sided limit","TABLE  ·  Function values","POLY  ·  Quadratic roots",
            "SIMUL  ·  Linear equations","DIST  ·  Probability","BASE  ·  BIN / OCT / DEC / HEX",
            "SYMBOLIC  ·  Polynomial derivative","SYMBOLIC  ·  Polynomial integral"};
        menu("SCIENTIFIC TOOLS",choices,index->{
            String formula=input.getText().toString().trim(); if(formula.isEmpty()) formula="x^2";
            String initial=formula;
            switch(index) {
                case 0: prompt("DERIVATIVE",new String[]{"f(x)","AT x"},new String[]{initial,"1"},v->publish("d/dx " + v[0] + " @ " + v[1],AdvancedMath.derivative(v[0],numeric(v[1]),degrees))); break;
                case 1: prompt("DEFINITE INTEGRAL",new String[]{"f(x)","LOWER BOUND","UPPER BOUND"},new String[]{initial,"0","1"},v->publish("∫ " + v[0] + " ["+v[1]+","+v[2]+"]",AdvancedMath.integral(v[0],numeric(v[1]),numeric(v[2]),degrees))); break;
                case 2: solvePrompt(); break;
                case 3: prompt("STATISTICS",new String[]{"VALUES, SEPARATED BY COMMAS"},new String[]{"1, 2, 3, 4"},v->{ double[] stats=AdvancedMath.statistics(numbers(v[0]));
                    infoDialog("STATISTICS", "n = " + numbers(v[0]).length + "\nΣ = " + Expression.format(stats[0]) + "\nMean = " + Expression.format(stats[1]) + "\nMedian = " + Expression.format(stats[2]) + "\nσ = " + Expression.format(stats[3]));
                    try { saveHistory("STAT mean: " + v[0],Expression.format(stats[1]),Expression.format(stats[1])); } catch(Exception ignored) { }
                }); break;
                case 4: matrixMenu(); break;
                case 5: vectorMenu(); break;
                case 6: complexMenu(); break;
                case 7: coordinateMenu(); break;
                case 8: prompt("GCD / LCM",new String[]{"FIRST INTEGER","SECOND INTEGER"},new String[]{"54","24"},v->{
                    long a=wholeNumber(v[0]),b=wholeNumber(v[1]),gcd=AdvancedMath.gcd(a,b),lcm=AdvancedMath.lcm(a,b);
                    try { saveHistory("GCD("+a+","+b+")",Long.toString(gcd),Long.toString(gcd)); } catch(Exception ignored) { }
                    infoDialog("GCD / LCM","GCD = "+gcd+"\nLCM = "+lcm);
                }); break;
                case 9: engineering=!engineering; updatePreview(input.getText().toString()); message(engineering?"Engineering notation on":"Engineering notation off"); break;
                case 10: constantsMenu(); break;
                case 11: menu("MORE FUNCTIONS",new String[]{"floor(x)","ceil(x)","cot(x)","acot(x)","mod(a,b)"},choice->{
                    insert(new String[]{"floor(","ceil(","cot(","acot(","mod("}[choice]);
                }); break;
                case 12: menu("RANDOM",new String[]{"Random decimal in [0,1)","Random integer in a range"},choice->{
                    if(choice==0) insert(Expression.format(Math.random()));
                    else prompt("RANDOM INTEGER",new String[]{"MINIMUM","MAXIMUM"},new String[]{"1","100"},v->{
                        long a=wholeNumber(v[0]),b=wholeNumber(v[1]);
                        if(a>b || b-a>1000000000L) throw new IllegalArgumentException("Use a valid range up to 1 billion");
                        insert(Long.toString(a+(long)(Math.random()*(b-a+1))));
                    });
                }); break;
                case 13: prompt("TWO-SIDED LIMIT",new String[]{"f(x)","AS x APPROACHES"},new String[]{initial.equals("x^2")?"sin(x)/x":initial,"0"},v->publish("lim " + v[0] + " as x→" + v[1],AdvancedMath.limit(v[0],numeric(v[1]),degrees))); break;
                case 14: prompt("FUNCTION TABLE",new String[]{"f(x)","START","END","STEP"},new String[]{initial,"0","10","1"},v->{
                    double[][] rows=AdvancedMath.table(v[0],numeric(v[1]),numeric(v[2]),numeric(v[3]),degrees);
                    StringBuilder output=new StringBuilder("x            f(x)\n");
                    for(double[] row:rows) output.append(Expression.format(row[0])).append("             ").append(Expression.format(row[1])).append('\n');
                    infoDialog("TABLE  ·  "+rows.length+" ROWS",output.toString());
                }); break;
                case 15: prompt("QUADRATIC  ax²+bx+c=0",new String[]{"a","b","c"},new String[]{"1","-3","2"},v->infoDialog("QUADRATIC ROOTS",AdvancedMath.quadratic(numeric(v[0]),numeric(v[1]),numeric(v[2])))); break;
                case 16: prompt("SIMULTANEOUS EQUATIONS",new String[]{"COEFFICIENTS (ROWS SEPARATED BY ;)","RESULTS (SEPARATED BY ,)"},new String[]{"2, 1; 1, -1","5, 1"},v->{
                    double[] solution=AdvancedMath.linearSystem(matrix(v[0]),numbers(v[1]));
                    StringBuilder output=new StringBuilder(); String[] names={"x","y","z","u","v","w"};
                    for(int i=0;i<solution.length;i++) output.append(names[i]).append(" = ").append(Expression.format(solution[i])).append('\n');
                    infoDialog("UNIQUE SOLUTION",output.toString());
                }); break;
                case 17: distributionMenu(); break;
                case 18: basePrompt(); break;
                case 19: prompt("SYMBOLIC DERIVATIVE",new String[]{"POLYNOMIAL IN x (UP TO DEGREE 8)"},new String[]{"3x^2-4x+1"},v->infoDialog("d/dx",Polynomial.differentiate(v[0]))); break;
                case 20: prompt("SYMBOLIC ANTIDERIVATIVE",new String[]{"POLYNOMIAL IN x (UP TO DEGREE 8)"},new String[]{"3x^2-4x+1"},v->infoDialog("∫ f(x) dx",Polynomial.integrate(v[0]))); break;
            }
        });
    }
    private void solvePrompt() {
        String formula=input.getText().toString().trim();
        prompt("SOLVE  f(x)=0",new String[]{"f(x)","LEFT BOUND","RIGHT BOUND"},new String[]{formula.isEmpty()?"x-2":formula,"-10","10"},
            v->publish("SOLVE " + v[0],AdvancedMath.solve(v[0],numeric(v[1]),numeric(v[2]),degrees)));
    }
    private double[][] matrix(String raw) {
        String[] rows=raw.split(";"); double[][] values=new double[rows.length][];
        for(int i=0;i<rows.length;i++) values[i]=numbers(rows[i]);
        // Validate dimensions before any operation, including linear systems.
        if(rows.length<2 || rows.length>6) throw new IllegalArgumentException("Enter 2 to 6 rows separated by semicolons");
        for(double[] row:values) if(row.length!=rows.length) throw new IllegalArgumentException("Each row needs "+rows.length+" entries");
        return values;
    }
    private String matrixText(double[][] matrix) {
        StringBuilder result=new StringBuilder();
        for(double[] row:matrix) {
            result.append("[ ");
            for(int i=0;i<row.length;i++) { if(i>0) result.append("     "); result.append(Expression.format(row[i])); }
            result.append(" ]\n");
        }
        return result.toString();
    }
    private void matrixMenu() {
        menu("MATRIX  ·  2×2 TO 6×6",new String[]{"Determinant","Inverse","Multiply A × B"},index->{
            String[] labels=index==2?new String[]{"MATRIX A (ROWS SEPARATED BY ;)","MATRIX B (ROWS SEPARATED BY ;)"}:new String[]{"MATRIX (ROWS SEPARATED BY ;)"};
            String[] defaults=index==2?new String[]{"1, 2; 3, 4","5, 6; 7, 8"}:new String[]{"1, 2; 3, 4"};
            prompt("MATRIX  ·  "+new String[]{"DETERMINANT","INVERSE","MULTIPLY"}[index],labels,defaults,v->{
                double[][] a=matrix(v[0]);
                if(index==0) publish("det("+v[0]+")",AdvancedMath.matrixDeterminant(a));
                else if(index==1) infoDialog("INVERSE MATRIX",matrixText(AdvancedMath.matrixInverse(a)));
                else infoDialog("MATRIX PRODUCT",matrixText(AdvancedMath.matrixMultiply(a,matrix(v[1]))));
            });
        });
    }
    private void vectorMenu() {
        menu("VECTOR  ·  2D / 3D",new String[]{"Dot product A · B","Cross product A × B (3D)","Magnitude |A|"},index->{
            String[] labels=index==2?new String[]{"VECTOR A (COMMA SEPARATED)"}:new String[]{"VECTOR A (COMMA SEPARATED)","VECTOR B (SAME DIMENSION)"};
            String[] defaults=index==2?new String[]{"3, 4"}:new String[]{"1, 2, 3","4, 5, 6"};
            prompt("VECTOR  ·  "+new String[]{"DOT","CROSS","MAGNITUDE"}[index],labels,defaults,v->{
                double[] a=numbers(v[0]);
                if(index==0) publish("A · B",AdvancedMath.dot(a,numbers(v[1])));
                else if(index==1) {
                    double[] result=AdvancedMath.cross(a,numbers(v[1]));
                    infoDialog("A × B","[ "+Expression.format(result[0])+", "+Expression.format(result[1])+", "+Expression.format(result[2])+" ]");
                } else publish("|A|",AdvancedMath.magnitude(a));
            });
        });
    }
    private void distributionMenu() {
        menu("PROBABILITY DISTRIBUTIONS",new String[]{"Normal PDF  ·  f(x)","Normal CDF  ·  P(X ≤ x)","Binomial PMF  ·  P(X = k)","Poisson PMF  ·  P(X = k)",
            "Binomial CDF  ·  P(X ≤ k)","Poisson CDF  ·  P(X ≤ k)","Hypergeometric PMF"},index->{
            if(index<2) prompt(index==0?"NORMAL PDF":"NORMAL CDF",new String[]{"x","MEAN  μ","STANDARD DEVIATION  σ"},new String[]{"0","0","1"},v->publish(index==0?"Normal PDF":"Normal CDF",index==0?
                AdvancedMath.normalPdf(numeric(v[0]),numeric(v[1]),numeric(v[2])):AdvancedMath.normalCdf(numeric(v[0]),numeric(v[1]),numeric(v[2]))));
            else if(index==2) prompt("BINOMIAL PMF",new String[]{"NUMBER OF TRIALS  n","SUCCESSES  k","SUCCESS PROBABILITY  p"},new String[]{"4","2","0.5"},v->publish("Binomial P(X=k)",AdvancedMath.binomial(nonnegativeInt(v[0]),nonnegativeInt(v[1]),numeric(v[2]))));
            else if(index==3) prompt("POISSON PMF",new String[]{"EVENTS  k","AVERAGE RATE  λ"},new String[]{"2","3"},v->publish("Poisson P(X=k)",AdvancedMath.poisson(nonnegativeInt(v[0]),numeric(v[1]))));
            else if(index==4) prompt("BINOMIAL CDF",new String[]{"TRIALS  n","UP TO  k","PROBABILITY  p"},new String[]{"4","2","0.5"},v->publish("Binomial P(X≤k)",AdvancedMath.binomialCdf(nonnegativeInt(v[0]),nonnegativeInt(v[1]),numeric(v[2]))));
            else if(index==5) prompt("POISSON CDF",new String[]{"UP TO  k","AVERAGE RATE  λ"},new String[]{"2","3"},v->publish("Poisson P(X≤k)",AdvancedMath.poissonCdf(nonnegativeInt(v[0]),numeric(v[1]))));
            else prompt("HYPERGEOMETRIC PMF",new String[]{"POPULATION  N","SUCCESSES IN POPULATION  K","DRAWS  n","OBSERVED SUCCESSES  k"},new String[]{"8","4","4","2"},v->publish("Hypergeometric P(X=k)",AdvancedMath.hypergeometric(nonnegativeInt(v[0]),nonnegativeInt(v[1]),nonnegativeInt(v[2]),nonnegativeInt(v[3]))));
        });
    }
    private void basePrompt() {
        prompt("BASE CONVERSION",new String[]{"INTEGER (NO PREFIX)","FROM BASE  2, 8, 10, 16","TO BASE  2, 8, 10, 16"},new String[]{"255","10","16"},v->{
            int from=Integer.parseInt(v[1]),to=Integer.parseInt(v[2]);
            if(!validBase(from) || !validBase(to)) throw new IllegalArgumentException("Use base 2, 8, 10 or 16");
            long number=Long.parseLong(v[0],from);
            infoDialog("BASE  "+from+" → "+to,Long.toString(number,to).toUpperCase(Locale.US));
        });
    }
    private boolean validBase(int base) { return base==2 || base==8 || base==10 || base==16; }
    private long wholeNumber(String value) {
        double number=numeric(value); if(number!=Math.rint(number) || Math.abs(number)>9e15) throw new IllegalArgumentException("Enter a whole number within 9 quadrillion");
        return (long)number;
    }
    private int nonnegativeInt(String value) {
        long number=wholeNumber(value);
        if(number<0 || number>Integer.MAX_VALUE) throw new IllegalArgumentException("Enter a nonnegative whole number");
        return (int)number;
    }
    private void coordinateMenu() {
        menu("COORDINATES",new String[]{"Rectangular → Polar","Polar → Rectangular"},index->prompt(index==0?"RECTANGULAR → POLAR":"POLAR → RECTANGULAR",
            index==0?new String[]{"x","y"}:new String[]{"r","θ ("+(degrees?"degrees":"radians")+")"},new String[]{"3","4"},v->{
                double a=numeric(v[0]),b=numeric(v[1]);
                if(index==0) infoDialog("POLAR","r = "+Expression.format(Math.hypot(a,b))+"\nθ = "+Expression.format(degrees?Math.toDegrees(Math.atan2(b,a)):Math.atan2(b,a)));
                else { double angle=degrees?Math.toRadians(b):b; infoDialog("RECTANGULAR","x = "+Expression.format(a*Math.cos(angle))+"\ny = "+Expression.format(a*Math.sin(angle))); }
            }));
    }
    private void complexMenu() {
        menu("COMPLEX NUMBERS",new String[]{"Add  (+)","Subtract  (−)","Multiply  (×)","Divide  (÷)"},operation->prompt("COMPLEX  a+bi  &  c+di",
            new String[]{"a (REAL 1)","b (IMAGINARY 1)","c (REAL 2)","d (IMAGINARY 2)"},new String[]{"1","2","3","4"},v->{
                double a=numeric(v[0]),b=numeric(v[1]),c=numeric(v[2]),d=numeric(v[3]),real,imag;
                switch(operation) {
                    case 0: real=a+c; imag=b+d; break;
                    case 1: real=a-c; imag=b-d; break;
                    case 2: real=a*c-b*d; imag=a*d+b*c; break;
                    default: double divisor=c*c+d*d; if(divisor==0) throw new IllegalArgumentException("Cannot divide by zero");
                        real=(a*c+b*d)/divisor; imag=(b*c-a*d)/divisor;
                }
                String result=Expression.format(AdvancedMath.finite(real))+(imag<0?" − ":" + ")+Expression.format(Math.abs(AdvancedMath.finite(imag)))+"i";
                try { saveHistory("COMPLEX " + new String[]{"+","−","×","÷"}[operation],result,Expression.format(real)); } catch(Exception ignored) { }
                infoDialog("COMPLEX RESULT",result);
            }));
    }
    private void constantsMenu() {
        String[] names={"Speed of light  c","Planck constant  h","Gravitational constant  G","Avogadro constant  Nₐ","Elementary charge  e","Boltzmann constant  k"};
        String[] values={"299792458","6.62607015E-34","6.67430E-11","6.02214076E23","1.602176634E-19","1.380649E-23"};
        menu("SCIENTIFIC CONSTANTS",names,index->{ insert(values[index]); message(names[index]+" inserted (SI units)"); });
    }
    private void memoryMenu() {
        menu("MEMORY  ·  "+Expression.format(memory),new String[]{"M+  Add display to memory","M−  Subtract display from memory","MR  Recall memory","MC  Clear memory"},index->{
            if(index==0) memory+=current(); if(index==1) memory-=current();
            if(index==2) insert(Expression.format(memory)); if(index==3) memory=0;
            saveMemory(); message("Memory: "+Expression.format(memory));
        });
    }
    private void fractionDisplay() {
        try { double value=current(); preview.setText(getString(R.string.result_format,AdvancedMath.fraction(value))); preview.setTextColor(0xff59479b); }
        catch(Exception e) { message(e.getMessage()); }
    }
    private void history() {
        if(history.length()==0) { empty("◷","No calculations yet","Your solved expressions will appear here."); return; }
        TextView clear=text("CLEAR HISTORY  ↗",11,TEAL,true); clear.setGravity(Gravity.END); body.addView(clear);
        clear.setOnClickListener(v->{ history=new JSONArray(); getPreferences(0).edit().remove("history").apply(); show("History"); }); gap(body,12);
        for(int i=0;i<history.length();i++) {
            JSONObject item=history.optJSONObject(i); if(item==null) continue;
            LinearLayout card=column(); pad(card,18,16); card.setBackground(shape(CARD,18));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(10); body.addView(card,lp);
            card.addView(text(item.optString("mode","DEG")+"  •  CALCULATION",10,TEAL,true)); gap(card,8);
            card.addView(text(item.optString("expression"),19,INK,false)); gap(card,5);
            card.addView(text("= "+item.optString("result"),23,TEAL,true));
            card.setOnClickListener(v->{ expression=item.optString("expression"); degrees=item.optString("mode").equals("DEG"); show("Calculate"); });
        }
    }
    private void empty(String icon,String title,String subtitle) {
        LinearLayout card=column(); pad(card,22,42); card.setGravity(Gravity.CENTER); card.setBackground(shape(CARD,24)); body.addView(card);
        TextView symbol=text(icon,44,TEAL,true); symbol.setGravity(Gravity.CENTER); card.addView(symbol);
        gap(card,12); card.addView(text(title,20,INK,true)); gap(card,6); card.addView(text(subtitle,13,MUTED,false));
    }
    private void converter() {
        String[] categories={"Length","Mass","Temperature","Area","Volume","Time","Speed","Data"};
        String[][] units={{"Meter","Kilometer","Centimeter","Millimeter","Mile","Yard","Foot","Inch"},
            {"Kilogram","Gram","Milligram","Pound","Ounce","Tonne"},{"Celsius","Fahrenheit","Kelvin"},
            {"Square meter","Square kilometer","Square foot","Acre","Hectare"},
            {"Liter","Milliliter","Cubic meter","Gallon (US)","Fluid ounce (US)"},
            {"Second","Minute","Hour","Day","Week","Year"},
            {"Meter/second","Kilometer/hour","Mile/hour","Knot"},
            {"Byte","Kilobyte","Megabyte","Gigabyte","Bit"}};
        double[][] factors={{1,1000,.01,.001,1609.344,.9144,.3048,.0254},
            {1,.001,.000001,.45359237,.028349523125,1000},{1,1,1},
            {1,1000000,.09290304,4046.8564224,10000},{1,.001,1000,3.785411784,.0295735295625},
            {1,60,3600,86400,604800,31557600},{1,1/3.6,.44704,.5144444444},
            {1,1000,1000000,1000000000,.125}};
        LinearLayout card=column(); pad(card,20,22); card.setBackground(gradient(0xff1c3446,0xff19223e,23)); body.addView(card);
        card.addView(text("CATEGORY",11,TEAL,true)); gap(card,8);
        Spinner category=spinner(categories); card.addView(category); gap(card,22);
        card.addView(text("FROM",11,MUTED,true)); gap(card,7);
        Spinner from=spinner(units[0]); card.addView(from); gap(card,9);
        EditText amount=new EditText(this); amount.setText("1"); amount.setSelectAllOnFocus(true); amount.setSingleLine(true); amount.setInputType(8194); amount.setTextColor(INK); amount.setTextSize(27); amount.setBackground(shape(KEY,12)); pad(amount,14,12); card.addView(amount); gap(card,22);
        card.addView(text("TO",11,MUTED,true)); gap(card,7);
        Spinner to=spinner(units[0]); card.addView(to); gap(card,17);
        TextView result=text("= 1",31,TEAL,true); result.setGravity(Gravity.END); card.addView(result);
        Runnable update=()->{
            try {
                int c=category.getSelectedItemPosition(), a=from.getSelectedItemPosition(), b=to.getSelectedItemPosition();
                double value=Double.parseDouble(amount.getText().toString());
                double converted=c==2?temperature(value,a,b):value*factors[c][a]/factors[c][b];
                if (!Double.isFinite(converted)) throw new IllegalArgumentException("Result is too large");
                result.setText(getString(R.string.result_format, Expression.format(converted)));
            } catch(Exception e) { result.setText("= —"); }
        };
        category.setOnItemSelectedListener(listener(()->{
            int c=category.getSelectedItemPosition(); from.setAdapter(adapter(units[c])); to.setAdapter(adapter(units[c])); if(units[c].length>1) to.setSelection(1); update.run();
        }));
        from.setOnItemSelectedListener(listener(update)); to.setOnItemSelectedListener(listener(update));
        amount.addTextChangedListener(new TextWatcher() { public void beforeTextChanged(CharSequence s,int a,int c,int f){} public void onTextChanged(CharSequence s,int a,int b,int c){update.run();} public void afterTextChanged(Editable e){} });
        gap(body,20);
        TextView note=text("✦  Conversions run entirely on your device.\n     Temperature uses the correct offset formulas.",13,MUTED,false); body.addView(note);
    }
    private double temperature(double v,int a,int b) { double c=a==0?v:a==1?(v-32)*5/9:v-273.15; return b==0?c:b==1?c*9/5+32:c+273.15; }
    private ArrayAdapter<String> adapter(String[] values) {
        return new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values) {
            @Override public View getView(int position,View convertView,android.view.ViewGroup parent) {
                TextView view=text(getItem(position),17,INK,false); view.setPadding(dp(8),dp(5),dp(8),dp(5)); return view;
            }
            @Override public View getDropDownView(int position,View convertView,android.view.ViewGroup parent) {
                TextView view=text(getItem(position),16,BG,false); view.setBackgroundColor(Color.WHITE); pad(view,16,12); return view;
            }
        };
    }
    private Spinner spinner(String[] values) { Spinner s=new Spinner(this); s.setAdapter(adapter(values)); s.setBackground(shape(KEY,12)); s.setPadding(dp(12),dp(9),dp(8),dp(9)); return s; }
    private android.widget.AdapterView.OnItemSelectedListener listener(Runnable action) { return new android.widget.AdapterView.OnItemSelectedListener() {
        public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){action.run();}
        public void onNothingSelected(android.widget.AdapterView<?> parent){} }; }

    private EditText contactField(LinearLayout parent,String label,String hint,boolean multiline) {
        parent.addView(text(label,11,PURPLE,true)); gap(parent,8);
        EditText field=new EditText(this); field.setHint(hint); field.setHintTextColor(MUTED);
        field.setTextColor(INK); field.setTextSize(16); field.setBackground(shape(KEY,13)); pad(field,14,11);
        if(multiline) { field.setMinLines(5); field.setGravity(Gravity.TOP); field.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); }
        else field.setSingleLine(true);
        parent.addView(field,new LinearLayout.LayoutParams(-1,multiline?-2:dp(52))); gap(parent,17);
        return field;
    }
    private void contact() {
        LinearLayout card=column(); pad(card,18,21); card.setBackground(gradient(0xff242332,0xff1e242c,20)); body.addView(card);
        card.addView(text("CONTACT THE DEVELOPER",12,TEAL,true)); gap(card,7);
        card.addView(text("Your message will open in your email app for review before sending.",13,MUTED,false)); gap(card,20);
        EditText name=contactField(card,"YOUR NAME","Name",false);
        EditText email=contactField(card,"EMAIL ADDRESS","you@example.com",false);
        email.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText subject=contactField(card,"SUBJECT","Feedback about IWAT",false);
        EditText message=contactField(card,"YOUR MESSAGE","Tell us what you think…",true);
        Button send=new Button(this); send.setText(R.string.open_email_to_send); send.setAllCaps(false);
        send.setTextColor(BG); send.setTextSize(14); send.setBackground(gradient(TEAL,0xffffdc7d,13));
        card.addView(send,new LinearLayout.LayoutParams(-1,dp(52)));
        send.setOnClickListener(v->{
            String sender=name.getText().toString().trim(),address=email.getText().toString().trim();
            String title=subject.getText().toString().trim(),bodyText=message.getText().toString().trim();
            if(sender.isEmpty()) { name.setError("Enter your name"); return; }
            if(!Patterns.EMAIL_ADDRESS.matcher(address).matches()) { email.setError("Enter a valid email"); return; }
            if(title.isEmpty()) { subject.setError("Enter a subject"); return; }
            if(bodyText.isEmpty()) { message.setError("Enter your message"); return; }
            Intent intent=new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:ericmubazi2010@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT,"IWAT: "+title);
            intent.putExtra(Intent.EXTRA_TEXT,"From: "+sender+"\nReply to: "+address+"\n\n"+bodyText);
            try { startActivity(Intent.createChooser(intent,"Send with an email app")); }
            catch(ActivityNotFoundException e) { message("No email app found. Write to ericmubazi2010@gmail.com"); }
        });
        gap(body,18);
        body.addView(text("✉  ericmubazi2010@gmail.com",15,PURPLE,true)); gap(body,10);
        body.addView(text("Your form is not uploaded by IWAT. Sending requires your email app and your confirmation. Optional ads may use Google services after consent.",12,MUTED,false));
        gap(body,16);
        TextView notice=text("READ PRIVACY NOTICE  ↗",12,PURPLE,true); body.addView(notice);
        notice.setOnClickListener(v->{
            try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/mubazieric/calculator/blob/main/PRIVACY.md"))); }
            catch(ActivityNotFoundException e) { message("No browser found"); }
        });
        if(BuildConfig.ADS_ENABLED) {
            gap(body,18);
            TextView privacy=text("AD PRIVACY OPTIONS  ↗",12,TEAL,true); body.addView(privacy);
            privacy.setOnClickListener(v->{
                if(consentInformation!=null && consentInformation.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED)
                    UserMessagingPlatform.showPrivacyOptionsForm(this,error->{ if(error!=null) message("Privacy settings unavailable right now"); });
                else message("Ad privacy options are not required for this device");
            });
        }
    }

    private void scan() {
        LinearLayout panel=column(); pad(panel,22,28); panel.setGravity(Gravity.CENTER); panel.setBackground(gradient(0xff1e3d4d,0xff272449,24)); body.addView(panel);
        TextView art=text("  ◈  ",70,TEAL,true); art.setGravity(Gravity.CENTER); panel.addView(art); gap(panel,4);
        TextView title=text("Point. Capture. Calculate.",21,INK,true); title.setGravity(Gravity.CENTER); panel.addView(title); gap(panel,7);
        TextView subtitle=text("On-device OCR reads printed expressions.\nYou always review before solving.",13,MUTED,false); subtitle.setGravity(Gravity.CENTER); panel.addView(subtitle); gap(panel,22);
        Button camera=new Button(this); camera.setText(R.string.open_camera); camera.setTextColor(BG); camera.setTextSize(14); camera.setAllCaps(false); camera.setBackground(gradient(TEAL,0xff72cfe8,14)); panel.addView(camera,new LinearLayout.LayoutParams(-1,dp(52)));
        camera.setOnClickListener(v->launchCamera()); gap(body,17);
        LinearLayout info=column(); pad(info,18,18); info.setBackground(shape(CARD,18)); body.addView(info);
        info.addView(text("HOW IT WORKS",11,TEAL,true)); gap(info,12);
        info.addView(text("01   Photograph a clear, well-lit expression\n\n02   Review and correct the recognized text\n\n03   Tap solve in the calculator",14,INK,false));
        gap(body,14);
        body.addView(text("Privacy first: images are processed on-device. No photo or answer is uploaded.",12,MUTED,false));
    }
    private void launchCamera() {
        try {
            File folder=new File(getCacheDir(),"photos"); if(!folder.exists() && !folder.mkdirs()) throw new IllegalStateException("Cannot create photo folder");
            File file=File.createTempFile("iwat_",".jpg",folder);
            photo=FileProvider.getUriForFile(this,getPackageName()+".files",file);
            Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            camera.putExtra(MediaStore.EXTRA_OUTPUT,photo);
            camera.setClipData(ClipData.newUri(getContentResolver(),"IWAT photo",photo));
            camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(camera,CAMERA);
        } catch(ActivityNotFoundException e) { message("No camera app found");
        } catch(Exception e) { message("Camera unavailable: " + e.getMessage()); }
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=CAMERA || result!=RESULT_OK || photo==null) return;
        try {
            InputImage image=InputImage.fromFilePath(this,photo);
            var recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            message("Reading photo on this device…");
            recognizer.process(image).addOnSuccessListener(recognized->{
                recognizer.close();
                if (isFinishing() || isDestroyed()) return;
                String candidate="";
                for(var block:recognized.getTextBlocks()) for(var line:block.getLines()) {
                    String raw=line.getText().split("=",2)[0].replace('×','*').replace('÷','/').replace('−','-');
                    raw=raw.replaceAll("[^0-9+*/().^% !π√eE−a-zA-Z-]", "").trim();
                    if(raw.length()>candidate.length() && raw.matches(".*[0-9].*")) candidate=raw;
                }
                if(candidate.isEmpty()) { message("No expression found. Try a clearer photo."); return; }
                expression=candidate; show("Calculate");
                input.requestFocus(); message("Review the extracted expression, then tap =");
            }).addOnFailureListener(e->{ recognizer.close(); if (!isFinishing() && !isDestroyed()) message("Couldn't read photo. Please try again."); });
        } catch(Exception e) { message("Couldn't open photo: " + e.getMessage()); }
    }
    private void message(String text) { android.widget.Toast.makeText(this,text,android.widget.Toast.LENGTH_LONG).show(); }
}
