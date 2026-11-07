package com.iwat.scientific;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import androidx.core.content.FileProvider;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;

public class MainActivity extends Activity {
    private static final int INK = 0xffe8efff, MUTED = 0xff9ba9c1, BG = 0xff0b1220;
    private static final int CARD = 0xff172238, KEY = 0xff202e46, TEAL = 0xff62e0c8, PURPLE = 0xffad9fff;
    private static final int CAMERA = 4;
    private LinearLayout root, body, nav;
    private EditText input;
    private TextView preview, angleBadge;
    private Uri photo;
    private boolean degrees = true;
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
        show("Calculate");
    }

    private int dp(float n) { return (int)(getResources().getDisplayMetrics().density * n + .5f); }
    private GradientDrawable shape(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private GradientDrawable gradient(int a, int b, int radius) { GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a,b}); d.setCornerRadius(dp(radius)); return d; }
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
        screen = page;
        root = column(); root.setBackgroundColor(BG);
        setContentView(root);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setVerticalScrollBarEnabled(false);
        root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        body = column(); body.setPadding(dp(20),dp(20),dp(20),dp(24)); scroll.addView(body);
        header();
        switch (page) {
            case "Convert": converter(); break;
            case "History": history(); break;
            case "Scan": scan(); break;
            default: calculator();
        }
        navigation();
    }
    private void header() {
        LinearLayout top = row(); body.addView(top);
        TextView mark = text("  ∑  ",24,BG,true); mark.setGravity(Gravity.CENTER);
        mark.setBackground(gradient(TEAL,0xff7cb7ee,16)); top.addView(mark,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout titles = column(); titles.setPadding(dp(12),0,0,0); top.addView(titles,new LinearLayout.LayoutParams(0,-2,1));
        titles.addView(text("IWAT",21,INK,true)); titles.addView(text("SCIENTIFIC CALCULATOR",10,TEAL,true));
        TextView ornament = text("✦",27,PURPLE,true); top.addView(ornament);
        gap(body,26);
        body.addView(text(screen.equals("Calculate") ? "Your pocket laboratory." : screen.equals("Scan") ? "Solve from a photo." : screen.equals("History") ? "Every answer, saved." : "Convert with confidence.",24,INK,true));
        gap(body,5);
        body.addView(text(screen.equals("Calculate") ? "Scientific precision, beautifully simple." : screen.equals("Scan") ? "Capture an expression. Check it. Solve it." : screen.equals("History") ? "Tap an entry to calculate it again." : "Everyday units, instant results.",13,MUTED,false));
        gap(body,22);
    }
    private void navigation() {
        nav = row(); pad(nav,8,8); nav.setBackgroundColor(0xff111c2e); root.addView(nav);
        String[] items = {"Calculate","Convert","History","Scan"}; String[] icons = {"▦","⇄","◷","◎"};
        for (int i=0;i<items.length;i++) {
            final String destination = items[i];
            LinearLayout cell=column(); cell.setGravity(Gravity.CENTER); pad(cell,0,5);
            if (screen.equals(destination)) cell.setBackground(shape(0xff20384a,16));
            TextView symbol=text(icons[i],22,screen.equals(destination)?TEAL:MUTED,true); symbol.setGravity(Gravity.CENTER); cell.addView(symbol);
            TextView label=text(destination,10,screen.equals(destination)?TEAL:MUTED,true); label.setGravity(Gravity.CENTER); cell.addView(label);
            nav.addView(cell,new LinearLayout.LayoutParams(0,dp(58),1)); cell.setOnClickListener(v->show(destination));
        }
    }
    private void calculator() {
        LinearLayout display=column(); pad(display,20,20); display.setBackground(gradient(0xff1c3446,0xff19223e,24)); body.addView(display);
        LinearLayout flags=row(); display.addView(flags);
        angleBadge=text(degrees?"● DEG":"● RAD",11,TEAL,true); flags.addView(angleBadge,new LinearLayout.LayoutParams(0,-2,1));
        TextView mode=text("fx-991  •  NATURAL DISPLAY",10,MUTED,true); flags.addView(mode);
        gap(display,20);
        input=new EditText(this); input.setSingleLine(false); input.setMinLines(2); input.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        input.setTextSize(25); input.setTextColor(INK); input.setHintTextColor(MUTED); input.setHint("0");
        input.setBackgroundColor(Color.TRANSPARENT); input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        display.addView(input,new LinearLayout.LayoutParams(-1,dp(82))); input.setText(expression); input.setSelection(input.length());
        preview=text("= 0",30,TEAL,true); preview.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); display.addView(preview);
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) { }
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                try { preview.setText("= " + Expression.format(new Expression(s.toString(),degrees,answer).evaluate())); preview.setTextColor(TEAL); }
                catch(Exception e) { preview.setText(s.length()==0?"= 0":"…"); preview.setTextColor(MUTED); }
            }
            public void afterTextChanged(Editable e) { }
        });
        gap(body,17);
        LinearLayout strip=row(); body.addView(strip);
        small(strip,degrees?"DEG":"RAD",()->{ degrees=!degrees; getPreferences(0).edit().putBoolean("degrees",degrees).apply(); angleBadge.setText(degrees?"● DEG":"● RAD"); show("Calculate"); });
        small(strip,"M+",()->{ memory+=current(); saveMemory(); });
        small(strip,"M−",()->{ memory-=current(); saveMemory(); });
        small(strip,"MR",()->insert(Expression.format(memory)));
        small(strip,"MC",()->{ memory=0; saveMemory(); });
        gap(body,14);
        String[][] scientific={{"sin(","cos(","tan(","log(","ln("},{"asin(","acos(","atan(","√(","^"},{"π","e","x²","x!","nCr"},{"sinh(","cosh(","tanh(","abs(","nPr"},{"a/b","∛","eˣ","10ˣ","n√"}};
        for(String[] keys:scientific) keyRow(keys,true);
        gap(body,8);
        String[][] number={{"AC","(",")","⌫"},{"7","8","9","÷"},{"4","5","6","×"},{"1","2","3","−"},{"0",".","Ans","+"},{"±","%","1/x","="}};
        for(String[] keys:number) keyRow(keys,false);
        gap(body,12);
        TextView hint=text("TIP  •  Try  sin(30) + √(16)  or  5 nCr 2",11,MUTED,false); hint.setGravity(Gravity.CENTER); body.addView(hint);
    }
    private void saveMemory() { getPreferences(0).edit().putLong("memory",Double.doubleToRawLongBits(memory)).apply(); }
    private double current() { try { return new Expression(input.getText().toString(),degrees,answer).evaluate(); } catch(Exception e) { return answer; } }
    private void small(LinearLayout parent,String label,Runnable action) {
        TextView button=text(label,11,TEAL,true); button.setGravity(Gravity.CENTER); button.setBackground(shape(CARD,12));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(37),1); p.setMargins(dp(2),0,dp(2),0); parent.addView(button,p); button.setOnClickListener(v->action.run());
    }
    private void keyRow(String[] keys,boolean science) {
        LinearLayout line=row(); body.addView(line);
        for(String key:keys) {
            boolean equals=key.equals("=");
            TextView button=text(key,science?14:19,equals?BG:(science?0xffc9c4ff:INK),true); button.setGravity(Gravity.CENTER);
            button.setBackground(equals?gradient(TEAL,0xff72cfe8,15):shape(science?CARD:KEY,15));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(science?48:55),1); p.setMargins(dp(3),dp(3),dp(3),dp(3)); line.addView(button,p);
            button.setOnClickListener(v->press(key));
        }
    }
    private void press(String key) {
        switch(key) {
            case "AC": input.setText(""); return;
            case "⌫": int pos=input.getSelectionStart(); if(pos>0) input.getText().delete(pos-1,pos); return;
            case "=": calculate(); return;
            case "x²": insert("^2"); return;
            case "x!": insert("!"); return;
            case "a/b": insert("/"); return;
            case "∛": insert("^(1/3)"); return;
            case "n√": insert("^(1/"); return;
            case "eˣ": insert("exp("); return;
            case "10ˣ": insert("10^("); return;
            case "1/x": insert("^(-1)"); return;
            case "±": insert("-"); return;
            default: insert(key);
        }
    }
    private void insert(String value) { int pos=Math.max(0,input.getSelectionStart()); input.getText().insert(pos,value); input.setSelection(pos+value.length()); }
    private void calculate() {
        String query=input.getText().toString().trim();
        try {
            double result=new Expression(query,degrees,answer).evaluate();
            String formatted=Expression.format(result); answer=result;
            preview.setText("= " + formatted); preview.setTextColor(TEAL);
            JSONObject entry=new JSONObject(); entry.put("expression",query); entry.put("result",formatted); entry.put("mode",degrees?"DEG":"RAD");
            JSONArray next=new JSONArray(); next.put(entry); for(int i=0;i<Math.min(99,history.length());i++) next.put(history.get(i));
            history=next; getPreferences(0).edit().putString("history",history.toString()).apply();
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);
        } catch(Exception e) { preview.setText(e.getMessage()); preview.setTextColor(0xffffa6a6); }
    }
    private void history() {
        if(history.length()==0) { empty("◷","No calculations yet","Your solved expressions will appear here."); return; }
        TextView clear=text("CLEAR HISTORY  ↗",11,TEAL,true); clear.setGravity(Gravity.RIGHT); body.addView(clear);
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
        TextView result=text("= 1",31,TEAL,true); result.setGravity(Gravity.RIGHT); card.addView(result);
        Runnable update=()->{
            try {
                int c=category.getSelectedItemPosition(), a=from.getSelectedItemPosition(), b=to.getSelectedItemPosition();
                double value=Double.parseDouble(amount.getText().toString());
                double converted=c==2?temperature(value,a,b):value*factors[c][a]/factors[c][b];
                result.setText("= " + Expression.format(converted));
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

    private void scan() {
        LinearLayout panel=column(); pad(panel,22,28); panel.setGravity(Gravity.CENTER); panel.setBackground(gradient(0xff1e3d4d,0xff272449,24)); body.addView(panel);
        TextView art=text("  ◈  ",70,TEAL,true); art.setGravity(Gravity.CENTER); panel.addView(art); gap(panel,4);
        TextView title=text("Point. Capture. Calculate.",21,INK,true); title.setGravity(Gravity.CENTER); panel.addView(title); gap(panel,7);
        TextView subtitle=text("On-device OCR reads printed expressions.\nYou always review before solving.",13,MUTED,false); subtitle.setGravity(Gravity.CENTER); panel.addView(subtitle); gap(panel,22);
        Button camera=new Button(this); camera.setText("◎   OPEN CAMERA"); camera.setTextColor(BG); camera.setTextSize(14); camera.setAllCaps(false); camera.setBackground(gradient(TEAL,0xff72cfe8,14)); panel.addView(camera,new LinearLayout.LayoutParams(-1,dp(52)));
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
            camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if(camera.resolveActivity(getPackageManager())==null) { message("No camera app found"); return; }
            startActivityForResult(camera,CAMERA);
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
                String candidate="";
                for(var block:recognized.getTextBlocks()) for(var line:block.getLines()) {
                    String raw=line.getText().split("=",2)[0].replace('×','*').replace('÷','/').replace('−','-');
                    raw=raw.replaceAll("[^0-9+*/().^% !π√eE−a-zA-Z-]", "").trim();
                    if(raw.length()>candidate.length() && raw.matches(".*[0-9].*")) candidate=raw;
                }
                if(candidate.isEmpty()) { message("No expression found. Try a clearer photo."); return; }
                expression=candidate; show("Calculate");
                input.requestFocus(); message("Review the extracted expression, then tap =");
            }).addOnFailureListener(e->{ recognizer.close(); message("Couldn't read photo. Please try again."); });
        } catch(Exception e) { message("Couldn't open photo: " + e.getMessage()); }
    }
    private void message(String text) { android.widget.Toast.makeText(this,text,android.widget.Toast.LENGTH_LONG).show(); }
}
