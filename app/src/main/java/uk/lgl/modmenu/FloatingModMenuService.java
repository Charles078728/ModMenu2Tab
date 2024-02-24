//Please don't replace listeners with lambda!

package uk.lgl.modmenu;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;



import androtrainer.MemoryScanner;
import androtrainer.TYPE;
import androtrainer.Ranges;
import androtrainer.MemoryPatcher;
import androtrainer.Natives;
import androtrainer.AddressWriter;
import androtrainer.ScannerAddress;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import android.widget.OverScroller;
import android.view.ViewDebug.FlagToString;

public class FloatingModMenuService extends Service {
    //********** Here you can easly change the menu appearance **********//

    //region Variable
    public static final String TAG = "Mod_Menu"; //Tag for logcat
    int TEXT_COLOR = Color.parseColor("#82CAFD");
    int TEXT_COLOR_2 = Color.parseColor("#FFFFFF");
    int BTN_COLOR = Color.parseColor("#1C262D");
    int MENU_BG_COLOR = Color.parseColor("#EE1C2A35"); //#AARRGGBB
    int MENU_FEATURE_BG_COLOR = Color.parseColor("#DD141C22"); //#AARRGGBB
    int MENU_WIDTH = 360;
    int MENU_HEIGHT = 200;
    float MENU_CORNER = 0f;
    int ICON_SIZE = 45; //Change both width and height of image
    float ICON_ALPHA = 0.7f; //Transparent
    int ToggleON = Color.GREEN;
    int ToggleOFF = Color.RED;
    int BtnON = Color.parseColor("#1b5e20");
    int BtnOFF = Color.parseColor("#7f0000");
    int CategoryBG = Color.parseColor("#2F3D4C");
    int SeekBarColor = Color.parseColor("#80CBC4");
    int SeekBarProgressColor = Color.parseColor("#80CBC4");
    int CheckBoxColor = Color.parseColor("#80CBC4");
    int RadioColor = Color.parseColor("#FFFFFF");
    String NumberTxtColor = "#41c300";
    //********************************************************************//
    RelativeLayout mCollapsed, mRootContainer;
    LinearLayout mExpanded, patches, mSettings, mCollapse;
    LinearLayout.LayoutParams scrlLLExpanded, scrlLL;
    WindowManager mWindowManager;
    WindowManager.LayoutParams params;
    ImageView startimage;
    FrameLayout rootFrame;
    ScrollView scrollView;

    boolean stopChecking;

    //initialize methods from the native library
    native void setTitleText(TextView textView);

    native void setHeadingText(TextView textView);

    native String Icon();

    native String IconWebViewData();

    native String[] getFeatureList();

    native String[] settingsList();

    native boolean isGameLibLoaded();
    
    
    private LinearLayout patches1;
    private LinearLayout patches2;
    private LinearLayout patches3;
    private LinearLayout patches4;
    private LinearLayout patches5;
    private LinearLayout patches6;
    private LinearLayout patch6;
    
    //endregion

    //When this Class is called the code in this function will be executed
    @Override
    public void onCreate() {
        super.onCreate();
        Preferences.context = this;

        //Create the menu
        initFloating();

        //Create a handler for this Class
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                Thread();
                handler.postDelayed(this, 1000);
            }
        });
    }

    //Here we write the code for our Menu
    // Reference: https://www.androidhive.info/2016/11/android-floating-widget-like-facebook-chat-head/
    private void initFloating() {
        rootFrame = new FrameLayout(this); // Global markup
        rootFrame.setOnTouchListener(onTouchListener());
        mRootContainer = new RelativeLayout(this); // Markup on which two markups of the icon and the menu itself will be placed
        mCollapsed = new RelativeLayout(this); // Markup of the icon (when the menu is minimized)
        mCollapsed.setVisibility(View.VISIBLE);
        mCollapsed.setAlpha(ICON_ALPHA);
        patches1 = new LinearLayout(getBaseContext());
        patches2 = new LinearLayout(getBaseContext());
        patches3 = new LinearLayout(getBaseContext());
        patches4 = new LinearLayout(getBaseContext());
        patches5 = new LinearLayout(getBaseContext());
        patch6 = new LinearLayout(getBaseContext());
        patches6 = new LinearLayout(getBaseContext());

        //********** The box of the mod menu **********
        mExpanded = new LinearLayout(this); // Menu markup (when the menu is expanded)
        mExpanded.setVisibility(View.GONE);
        mExpanded.setBackgroundColor(MENU_BG_COLOR);

        mExpanded.setOrientation(LinearLayout.VERTICAL);
        // mExpanded.setPadding(1, 1, 1, 1);
        mExpanded.setLayoutParams(new LinearLayout.LayoutParams(dp(MENU_WIDTH), WRAP_CONTENT));
        GradientDrawable gdMenuBody = new GradientDrawable();
        gdMenuBody.setCornerRadius(MENU_CORNER); //Set corner
        gdMenuBody.setColor(MENU_BG_COLOR); //Set background color
        gdMenuBody.setStroke(5, Color.parseColor("#32cb00")); //Set border
        gdMenuBody.setCornerRadii(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 });
        mExpanded.setBackground(gdMenuBody); //Apply GradientDrawable to it
        

        //********** The icon to open mod menu **********
        startimage = new ImageView(this);
        startimage.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension = (int) TypedValue.applyDimension(1, ICON_SIZE, getResources().getDisplayMetrics()); //Icon size
        startimage.getLayoutParams().height = applyDimension;
        startimage.getLayoutParams().width = applyDimension;
        //startimage.requestLayout();
        startimage.setScaleType(ImageView.ScaleType.FIT_XY);
        byte[] decode = Base64.decode(Icon(), 0);
        startimage.setImageBitmap(BitmapFactory.decodeByteArray(decode, 0, decode.length));
        ((ViewGroup.MarginLayoutParams) startimage.getLayoutParams()).topMargin = convertDipToPixels(10);
        //Initialize event handlers for buttons, etc.
        startimage.setOnTouchListener(onTouchListener());
        startimage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.GONE);
                mExpanded.setVisibility(View.VISIBLE);
            }
        });

        //********** The icon in Webview to open mod menu **********
        WebView wView = new WebView(this); //Icon size width=\"50\" height=\"50\"
        wView.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension2 = (int) TypedValue.applyDimension(1, ICON_SIZE, getResources().getDisplayMetrics()); //Icon size
        wView.getLayoutParams().height = applyDimension2;
        wView.getLayoutParams().width = applyDimension2;
        wView.loadData("<html>" +
                "<head></head>" +
                "<body style=\"margin: 0; padding: 0\">" +
                "<img src=\"" + IconWebViewData() + "\" width=\"" + ICON_SIZE + "\" height=\"" + ICON_SIZE + "\" >" +
                "</body>" +
                "</html>", "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setAlpha(ICON_ALPHA);
        wView.getSettings().setAppCacheEnabled(true);
        wView.setOnTouchListener(onTouchListener());

        //********** Settings icon **********
   /*     TextView settings = new TextView(this); //Android 5 can't show ⚙, instead show other icon instead
        settings.setText(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ? "⚙" : "\uD83D\uDD27");
        settings.setTextColor(TEXT_COLOR);
        settings.setTypeface(Typeface.DEFAULT_BOLD);
        settings.setTextSize(20.0f);
        RelativeLayout.LayoutParams rlsettings = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rlsettings.addRule(ALIGN_PARENT_RIGHT);
        settings.setLayoutParams(rlsettings);
        settings.setOnClickListener(new View.OnClickListener() {
            boolean settingsOpen;

            @Override
            public void onClick(View v) {
                try {
                    settingsOpen = !settingsOpen;
                    if (settingsOpen) {
                        scrollView.removeView(patches);
                        scrollView.addView(mSettings);
                        scrollView.scrollTo(0, 0);
                    } else {
                        scrollView.removeView(mSettings);
                        scrollView.addView(patches);
                    }
                } catch (IllegalStateException e) {
                }
            }
        });*/

        //********** Settings **********
        mSettings = new LinearLayout(this);
        mSettings.setOrientation(LinearLayout.VERTICAL);
        featureList(settingsList(), mSettings);

        //********** Title text **********
        RelativeLayout titleText = new RelativeLayout(this);
        titleText.setPadding(10, 5, 10, 5);
        titleText.setVerticalGravity(16);
        
        TextView title = new TextView(this);
        title.setTextColor(TEXT_COLOR);
        title.setTextSize(18.0f);
        title.setGravity(Gravity.CENTER);
        
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
        title.setLayoutParams(rl);
        setTitleText(title);
        title.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mCollapsed.setVisibility(View.VISIBLE);
                    mCollapsed.setAlpha(ICON_ALPHA);
                    mExpanded.setVisibility(View.GONE);
                }
            });

        //********** Heading text **********
        TextView heading = new TextView(this);
        //heading.setText(Html.fromHtml(Heading()));
        heading.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        heading.setMarqueeRepeatLimit(-1);
        heading.setSingleLine(true);
        heading.setSelected(true);
        heading.setTextColor(TEXT_COLOR);
        heading.setTextSize(10.0f);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, 0, 0, 5);
        setHeadingText(heading);

        //********** Mod menu feature list **********
        

        //********** RelativeLayout for buttons **********
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setPadding(10, 3, 10, 3);
        relativeLayout.setVerticalGravity(Gravity.CENTER);

        LinearLayout outeroption2 = new LinearLayout(this);
        outeroption2.setLayoutParams(new LinearLayout.LayoutParams(dp(-1), dp(180)));


        scrollView = new ScrollView(this);
        scrlLL = new LinearLayout.LayoutParams(dp(150), dp(160));
        scrlLLExpanded = new LinearLayout.LayoutParams(mExpanded.getLayoutParams());
        scrlLLExpanded.weight = 1f;
        scrollView.setBackgroundColor(MENU_FEATURE_BG_COLOR);
        scrollView.setLayoutParams(Preferences.isExpanded ? scrlLLExpanded : scrlLL);
        scrlLL.setMargins(9, 9, 9, 9);
        scrollView.setPadding(0,15,0,15);
        patches = new LinearLayout(this);
        patches.setOrientation(LinearLayout.VERTICAL);



        final ScrollView scrollView2 = new ScrollView(getBaseContext());
        //scrollView2.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scrollView2.setLayoutParams(Preferences.isExpanded ? scrlLLExpanded : scrlLL);
        scrollView2.setBackgroundColor(MENU_FEATURE_BG_COLOR);
        scrollView2.setPadding(0,15,0,15);
        
        patches2 = new LinearLayout(this);
        patches2.setOrientation(LinearLayout.VERTICAL);

        final ScrollView scrollView3 = new ScrollView(getBaseContext());
        //scrollView2.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scrollView3.setLayoutParams(Preferences.isExpanded ? scrlLLExpanded : scrlLL);
        scrollView3.setBackgroundColor(MENU_FEATURE_BG_COLOR);
        scrollView3.setPadding(0,15,0,15);
        //patches1.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        patches3 = new LinearLayout(this);
        patches3.setOrientation(LinearLayout.VERTICAL);

        final ScrollView scrollView4 = new ScrollView(getBaseContext());
        //scrollView2.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scrollView4.setLayoutParams(Preferences.isExpanded ? scrlLLExpanded : scrlLL);
        scrollView4.setBackgroundColor(MENU_FEATURE_BG_COLOR);
        scrollView4.setPadding(0,15,0,15);
        //patches1.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        patches4 = new LinearLayout(this);
        patches4.setOrientation(LinearLayout.VERTICAL);

        final ScrollView scrollView5 = new ScrollView(getBaseContext());
        //scrollView2.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scrollView5.setLayoutParams(Preferences.isExpanded ? scrlLLExpanded : scrlLL);
        scrollView5.setBackgroundColor(MENU_FEATURE_BG_COLOR);
        scrollView5.setPadding(0,15,0,15);
        //patches1.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        patches5 = new LinearLayout(this);
        patches5.setOrientation(LinearLayout.VERTICAL);

        final ScrollView scrollView6 = new ScrollView(getBaseContext());
        //scrollView2.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        scrollView6.setLayoutParams(Preferences.isExpanded ? scrlLLExpanded : scrlLL);
        scrollView6.setBackgroundColor(MENU_FEATURE_BG_COLOR);
        scrollView6.setPadding(0,15,0,15);
        //patches1.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        patch6 = new LinearLayout(this);
        patch6.setOrientation(LinearLayout.VERTICAL);


        patches6.setPadding(0, 0, 0, 0);


        //new Titanic().start(closeBtn);

        final Button Menu1 = new Button(this);
        final GradientDrawable SubMenu1 = new GradientDrawable();
        SubMenu1.setStroke(4, (Color.parseColor("#FFFFFF")));
        SubMenu1.setColor(Color.parseColor("#FFFF0000"));//FFFF0000
        SubMenu1.setCornerRadii(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 });
        Menu1.setBackground(SubMenu1);
        Menu1.setText("Lobby Menu");
        Menu1.setTextSize(8.0f); 
        Menu1.setTypeface(null, Typeface.BOLD);
        //Menu1.setTypeface(Typeface.createFromAsset(getAssets(), "MK4.ttf"));
        //Menu1.setPadding(40, 40, 40, 40);
        Menu1.setTextColor(Color.parseColor("#FFFFFF"));
        Menu1.setLayoutParams(new LinearLayout.LayoutParams(dp(60), dp(30)));//58), dp(40)));
        //byte[] arrby0 = Base64.decode((String)Español, (int)0);
        //Bitmap bitmap0 = BitmapFactory.decodeByteArray((byte[])arrby0, (int)0, (int)arrby0.length);
        //Menu1.setImageBitmap(bitmap0);


        //******** IMAG MENU 2 *********\\
        final Button Menu2 = new Button(this);
        final GradientDrawable SubMenu2 = new GradientDrawable();
        SubMenu2.setStroke(2, (Color.parseColor("#00000000")));
        SubMenu2.setColor(Color.parseColor("#00000000"));//FF00DB39
        SubMenu2.setCornerRadii(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 });
        Menu2.setBackground(SubMenu2);
        Menu2.setText("");
        Menu2.setTextSize(8.0f);
        Menu2.setTypeface(null, Typeface.BOLD);
        Menu2.setTextColor(Color.parseColor("#00000000"));
        Menu2.setLayoutParams(new LinearLayout.LayoutParams(dp(10), dp(0)));//65), dp(40)));
        //byte[] arrby1 = Base64.decode((String)Ingles, (int)0);
        //Bitmap bitmap1 = BitmapFactory.decodeByteArray((byte[])arrby1, (int)0, (int)arrby1.length);
        //Menu2.setImageBitmap(bitmap1);


        //******** IMAG MENU 3 *********\\
        final Button Menu3 = new Button(this);
        final GradientDrawable SubMenu3 = new GradientDrawable();
        SubMenu3.setStroke(2, (Color.parseColor("#FFFFFF")));
        SubMenu3.setColor(Color.parseColor("#FF60FF00"));
        SubMenu3.setCornerRadii(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 });
        Menu3.setBackground(SubMenu3);
        Menu3.setText("Game Menu");
        Menu3.setTextSize(8.0f);
        Menu3.setTypeface(null, Typeface.BOLD);
        Menu3.setTextColor(Color.parseColor("#000000"));
        Menu3.setLayoutParams(new LinearLayout.LayoutParams(dp(60), dp(30)));//73), dp(40)));
        //byte[] arrby2 = Base64.decode((String)Ruso, (int)0);
        //Bitmap bitmap2 = BitmapFactory.decodeByteArray((byte[])arrby2, (int)0, (int)arrby2.length);
        //Menu3.setImageBitmap(bitmap2);




        //******** IMAG MENU 4 *********\\
        final Button Menu4 = new Button(this);
        final GradientDrawable SubMenu4 = new GradientDrawable();
        SubMenu4.setStroke(2, (Color.parseColor("#00000000")));
        SubMenu4.setColor(Color.parseColor("#00000000"));
        SubMenu4.setCornerRadii(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 });
        Menu4.setBackground(SubMenu4);
        Menu4.setText("");
        Menu4.setTextSize(8.0f);
        Menu4.setTypeface(null, Typeface.BOLD);
        Menu4.setTextColor(Color.parseColor("#00000000"));
        Menu4.setLayoutParams(new LinearLayout.LayoutParams(dp(10), dp(0)));//45), dp(40)));
        //byte[] arrby3 = Base64.decode((String)Turco, (int)0);
        //Bitmap bitmap3 = BitmapFactory.decodeByteArray((byte[])arrby3, (int)0, (int)arrby3.length);
        //Menu4.setImageBitmap(bitmap3);




        //******** IMAG MENU 5 *********\\
        final Button Menu5 = new Button(this);
        final GradientDrawable SubMenu5 = new GradientDrawable();
        SubMenu5.setStroke(2, (Color.parseColor("#FFFFFF")));
        SubMenu5.setColor(Color.parseColor("#FF60FF00"));
        SubMenu5.setCornerRadii(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 });
        Menu5.setBackground(SubMenu5);
        Menu5.setText("Color Hacks");
        Menu5.setTextSize(8.0f);
        Menu5.setTypeface(null, Typeface.BOLD);
        Menu5.setTextColor(Color.parseColor("#000000"));
        Menu5.setLayoutParams(new LinearLayout.LayoutParams(dp(60), dp(30)));//55), dp(40)));
        //byte[] arrby4 = Base64.decode((String)Arabe, (int)0);
        //Bitmap bitmap4 = BitmapFactory.decodeByteArray((byte[])arrby4, (int)0, (int)arrby4.length);
        //Menu5.setImageBitmap(bitmap4);


        final Button Menu6 = new Button(this);
        final GradientDrawable SubMenu6 = new GradientDrawable();
        SubMenu6.setStroke(2, (Color.parseColor("#00000000")));
        SubMenu6.setColor(Color.parseColor("#00000000"));
        SubMenu6.setCornerRadii(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 });
        Menu6.setBackground(SubMenu4);
        Menu6.setText("");
        Menu6.setTextSize(8.0f);
        Menu6.setTypeface(null, Typeface.BOLD);
        Menu6.setTextColor(Color.parseColor("#00000000"));
        Menu6.setLayoutParams(new LinearLayout.LayoutParams(dp(10), dp(0)));






        //******** ON CLICK IMAG MENU 1 *********\\
        Menu1.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    //playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
                    //imag menu 1
                    final GradientDrawable SubMenu1 = new GradientDrawable();
                    SubMenu1.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu1.setStroke(4, (Color.parseColor("#FFFFFF")));
                    SubMenu1.setColor(Color.parseColor("#FFFF0000"));
                    Menu1.setBackground(SubMenu1); 
                    Menu1.setTextColor(Color.parseColor("#FFFFFF"));
                    //imag menu 2
                    final GradientDrawable SubMenu2 = new GradientDrawable();
                    SubMenu2.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu2.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu2.setColor(Color.parseColor("#FF60FF00"));
                    Menu2.setBackground(SubMenu2); 
                    Menu2.setTextColor(Color.parseColor("#000000"));
                    //imag menu 3
                    final GradientDrawable SubMenu3 = new GradientDrawable();
                    SubMenu3.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu3.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu3.setColor(Color.parseColor("#FF60FF00"));
                    Menu3.setBackground(SubMenu3);  
                    Menu3.setTextColor(Color.parseColor("#000000"));
                    //imag menu 4
                    final GradientDrawable SubMenu4 = new GradientDrawable();
                    SubMenu4.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu4.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu4.setColor(Color.parseColor("#FF60FF00"));
                    Menu4.setBackground(SubMenu4);  
                    Menu4.setTextColor(Color.parseColor("#000000"));
                    //imag menu 5
                    final GradientDrawable SubMenu5 = new GradientDrawable();
                    SubMenu5.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu5.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu5.setColor(Color.parseColor("#FF60FF00"));
                    Menu5.setBackground(SubMenu5);  
                    Menu5.setTextColor(Color.parseColor("#000000"));

                    final GradientDrawable SubMenu6 = new GradientDrawable();
                    SubMenu6.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu6.setStroke(2, (Color.parseColor("#00000000")));
                    SubMenu6.setColor(Color.parseColor("#00000000"));
                    Menu6.setBackground(SubMenu6);  
                    Menu6.setTextColor(Color.parseColor("#00000000"));

                    //scrollView.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.VISIBLE);
                    scrollView2.setVisibility(View.VISIBLE);
                    scrollView3.setVisibility(View.GONE);
                    scrollView4.setVisibility(View.GONE);
                    scrollView5.setVisibility(View.GONE);
                    scrollView6.setVisibility(View.GONE);
                }                   
            });
        patches6.addView(Menu1);



        //******** ON CLICK IMAG MENU 2 *********\\
        Menu2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v){
                    //playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
                    //img menu 1

                }                   
            });

        patches6.addView(Menu2);


        //******** ON CLICK IMAG MENU 3 *********\\
        Menu3.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v){
                    //playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
                    //img menu 1
                    final GradientDrawable SubMenu1 = new GradientDrawable();
                    SubMenu1.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu1.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu1.setColor(Color.parseColor("#FF60FF00"));
                    Menu1.setBackground(SubMenu1);       
                    Menu1.setTextColor(Color.parseColor("#000000"));
                    //img menu 2
                    final GradientDrawable SubMenu2 = new GradientDrawable();
                    SubMenu2.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu2.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu2.setColor(Color.parseColor("#FF60FF00"));
                    Menu2.setBackground(SubMenu2); 
                    Menu2.setTextColor(Color.parseColor("#000000"));
                    //img menu 3
                    final GradientDrawable SubMenu3 = new GradientDrawable();
                    SubMenu3.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu3.setStroke(4, (Color.parseColor("#FFFFFF")));
                    SubMenu3.setColor(Color.parseColor("#FF0000"));
                    Menu3.setBackground(SubMenu3); 
                    Menu3.setTextColor(Color.parseColor("#FFFFFF"));
                    //imag menu 4
                    final GradientDrawable SubMenu4 = new GradientDrawable();
                    SubMenu4.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu4.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu4.setColor(Color.parseColor("#FF60FF00"));
                    Menu4.setBackground(SubMenu4);  
                    Menu4.setTextColor(Color.parseColor("#000000"));
                    //imag menu 5
                    final GradientDrawable SubMenu5 = new GradientDrawable();
                    SubMenu5.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu5.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu5.setColor(Color.parseColor("#FF60FF00"));
                    Menu5.setBackground(SubMenu5);  
                    Menu5.setTextColor(Color.parseColor("#000000"));
                    final GradientDrawable SubMenu6 = new GradientDrawable();
                    SubMenu6.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu6.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu6.setColor(Color.parseColor("#FF60FF00"));
                    Menu6.setBackground(SubMenu6);  
                    Menu6.setTextColor(Color.parseColor("#000000"));

                    //scrollView.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    scrollView2.setVisibility(View.GONE);
                    scrollView3.setVisibility(View.VISIBLE);
                    scrollView4.setVisibility(View.VISIBLE);
                    scrollView5.setVisibility(View.GONE);
                    scrollView6.setVisibility(View.GONE);
                }                   
            });

        patches6.addView(Menu3);




        //******** ON CLICK IMAG MENU 4 *********\\
        Menu4.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v){
                    //playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
                    //img menu 1

                }                   
            });

        patches6.addView(Menu4);





        //******** ON CLICK IMAG MENU 5 *********\\
        Menu5.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v){
                    //playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
                    //img menu 1
                    final GradientDrawable SubMenu1 = new GradientDrawable();
                    SubMenu1.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu1.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu1.setColor(Color.parseColor("#FF60FF00"));
                    Menu1.setBackground(SubMenu1);   
                    Menu1.setTextColor(Color.parseColor("#000000"));    
                    //img menu 2
                    final GradientDrawable SubMenu2 = new GradientDrawable();
                    SubMenu2.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu2.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu2.setColor(Color.parseColor("#FF60FF00"));
                    Menu2.setBackground(SubMenu2); 
                    Menu2.setTextColor(Color.parseColor("#000000"));
                    //img menu 3
                    final GradientDrawable SubMenu3 = new GradientDrawable();
                    SubMenu3.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu3.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu3.setColor(Color.parseColor("#FF60FF00"));
                    Menu3.setBackground(SubMenu3);  
                    Menu3.setTextColor(Color.parseColor("#000000"));
                    //imag menu 4
                    final GradientDrawable SubMenu4 = new GradientDrawable();
                    SubMenu4.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu4.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu4.setColor(Color.parseColor("#FF60FF00"));
                    Menu4.setBackground(SubMenu4);  
                    Menu4.setTextColor(Color.parseColor("#000000"));
                    //imag menu 5
                    final GradientDrawable SubMenu5 = new GradientDrawable();
                    SubMenu5.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu5.setStroke(4, (Color.parseColor("#FFFFFF")));
                    SubMenu5.setColor(Color.parseColor("#FFFF0000"));
                    Menu5.setBackground(SubMenu5); 
                    Menu5.setTextColor(Color.parseColor("#FFFFFF"));
                    final GradientDrawable SubMenu6 = new GradientDrawable();
                    SubMenu6.setCornerRadii(new float[] {20, 20, 20, 20, 20, 20, 20, 20 });
                    SubMenu6.setStroke(2, (Color.parseColor("#FFFFFF")));
                    SubMenu6.setColor(Color.parseColor("#FF60FF00"));
                    Menu6.setBackground(SubMenu6);  
                    Menu6.setTextColor(Color.parseColor("#000000"));
                    
                    //scrollView.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    scrollView2.setVisibility(View.GONE);
                    scrollView3.setVisibility(View.GONE);
                    scrollView4.setVisibility(View.GONE);
                    scrollView5.setVisibility(View.VISIBLE);
                    scrollView6.setVisibility(View.VISIBLE);
                }                   
            });

            
            
            
            
            
        patches6.addView(Menu5);

        Menu6.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v){
                    //playSound(Uri.fromFile(new File(cacheDir + "Select.ogg")));
                    //img menu 1
                }
            });
        patches6.addView(Menu6);

        //********** Params **********
        //Variable to check later if the phone supports Draw over other apps permission
        int iparams = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ? 2038 : 2002;
        params = new WindowManager.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, iparams, 8, -3);
        params.gravity = 51;
        params.x = 0;
        params.y = 100;

        //********** Adding view components **********
        rootFrame.addView(mRootContainer);
        mRootContainer.addView(mCollapsed);
        mRootContainer.addView(mExpanded);
        if (IconWebViewData() != null) {
            mCollapsed.addView(wView);
        } else {
            mCollapsed.addView(startimage);
        }
        titleText.addView(title);
       // titleText.addView(settings);
        mExpanded.addView(titleText);
        mExpanded.addView(heading);
        mExpanded.addView(patches6); 
        mExpanded.addView(outeroption2);
        outeroption2.addView(scrollView);
        scrollView.addView(patches);
        outeroption2.addView(scrollView2);
        scrollView2.addView(patches2);
        outeroption2.addView(scrollView3);
        scrollView3.addView(patches3);
        outeroption2.addView(scrollView4);
        scrollView4.addView(patches4);
        outeroption2.addView(scrollView5);
        scrollView5.addView(patches5); 
        outeroption2.addView(scrollView6);
        scrollView6.addView(patch6);
        mExpanded.addView(relativeLayout);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(rootFrame, params);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            boolean viewLoaded = false;

            @Override
            public void run() {
                //If the save preferences is enabled, it will check if game lib is loaded before starting menu
                //Comment the if-else code out except startService if you want to run the app and test preferences
                if (Preferences.loadPref && !isGameLibLoaded() && !stopChecking) {
                    if (!viewLoaded) {
                        patches.addView(Category("Save preferences was been enabled. Waiting for game lib to be loaded...\n\nForce load menu may not apply mods instantly. You would need to reactivate them again"));
                        patches.addView(Button(-100, "Force load menu"));
                        viewLoaded = true;
                    }
                    handler.postDelayed(this, 600);
                } else {
                    patches.removeAllViews();
                    featureList(getFeatureList(), patches);
                    patches2.removeAllViews();
                    featureList2(getFeatureList(), patches2);
                    patches3.removeAllViews();
                    featureList3(getFeatureList(), patches3);
                    patches4.removeAllViews();
                    featureList4(getFeatureList(), patches4);
                    patches5.removeAllViews();
                    featureList5(getFeatureList(), patches5);
                    patch6.removeAllViews();
                    featureList6(getFeatureList(), patch6);
					
                }
            }
        }, 500);
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            final View collapsedView = mCollapsed;
            final View expandedView = mExpanded;
            private float initialTouchX, initialTouchY;
            private int initialX, initialY;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - initialTouchY);
                        mExpanded.setAlpha(1f);
                        mCollapsed.setAlpha(1f);
                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (rawX < 10 && rawY < 10 && isViewCollapsed()) {
                            //When user clicks on the image view of the collapsed layout,
                            //visibility of the collapsed layout will be changed to "View.GONE"
                            //and expanded view will become visible.
                            try {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            } catch (NullPointerException e) {

                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mExpanded.setAlpha(0.5f);
                        mCollapsed.setAlpha(0.5f);
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + ((int) (motionEvent.getRawX() - initialTouchX));
                        params.y = initialY + ((int) (motionEvent.getRawY() - initialTouchY));
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(rootFrame, params);
                        return true;
                    default:
                        return false;
                }
            }
        };
    }

 
    private void featureList(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            String feature = listFT[i];
            if (feature.contains("True_")) {
                switchedOn = true;
                feature = feature.replaceFirst("True_", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd_")) {
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd_", "");
            }

            String[] str = feature.split("_");
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                featNum = i - subFeat;
            }

            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle":
                    linearLayout.addView(Switch(featNum, strSplit[1], switchedOn));
                    break;
                case "SeekBar":
                    linearLayout.addView(SeekBar(featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3])));
                    break;
                case "Button":
                    linearLayout.addView(Button(featNum, strSplit[1]));
                    break;
                case "ButtonOnOff":
                    linearLayout.addView(ButtonOnOff(featNum, strSplit[1], switchedOn));
                    break;
                case "Spinner":
                    linearLayout.addView(RichTextView(strSplit[1]));
                    linearLayout.addView(Spinner(featNum, strSplit[1], strSplit[2]));
                    break;
                case "InputText":
                    linearLayout.addView(TextField(featNum, strSplit[1], false, 0));
                    break;
                case "InputValue":
                    if (strSplit.length == 3)
                        linearLayout.addView(TextField(featNum, strSplit[2], true, Integer.parseInt(strSplit[1])));
                    if (strSplit.length == 2)
                        linearLayout.addView(TextField(featNum, strSplit[1], true, 0));
                    break;
                case "CheckBox":
                    linearLayout.addView(CheckBox(featNum, strSplit[1], switchedOn));
                    break;
                case "RadioButton":
                    linearLayout.addView(RadioButton(featNum, strSplit[1], strSplit[2]));
                    break;
                case "Collapse":
                    Collapse(linearLayout, strSplit[1]);
                    subFeat++;
                    break;
                case "ButtonLink":
                    subFeat++;
                    linearLayout.addView(ButtonLink(strSplit[1], strSplit[2]));
                    break;
                case "Category":
                    subFeat++;
                    linearLayout.addView(Category(strSplit[1]));
                    break;
                case "RichTextView":
                    subFeat++;
                    linearLayout.addView(RichTextView(strSplit[1]));
                    break;
                case "RichWebView":
                    subFeat++;
                    linearLayout.addView(RichWebView(strSplit[1]));
                    break;
            }
        }
    }




    private void featureList2(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            String feature = listFT[i];
            if (feature.contains("True_")) {
                switchedOn = true;
                feature = feature.replaceFirst("True_", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd2_")) {
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd2_", "");
            }

            String[] str = feature.split("_");
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                featNum = i - subFeat;
            }

            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle2":
                    linearLayout.addView(Switch(featNum, strSplit[1], switchedOn));
                    break;
                case "SeekBar2":
                    linearLayout.addView(SeekBar(featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3])));
                    break;
                case "Button2":
                    linearLayout.addView(Button(featNum, strSplit[1]));
                    break;
                case "ButtonOnOff2":
                    linearLayout.addView(ButtonOnOff(featNum, strSplit[1], switchedOn));
                    break;
                case "Spinner2":
                    linearLayout.addView(RichTextView(strSplit[1]));
                    linearLayout.addView(Spinner(featNum, strSplit[1], strSplit[2]));
                    break;
                case "InputText2":
                    linearLayout.addView(TextField(featNum, strSplit[1], false, 0));
                    break;
                case "InputValue2":
                    if (strSplit.length == 3)
                        linearLayout.addView(TextField(featNum, strSplit[2], true, Integer.parseInt(strSplit[1])));
                    if (strSplit.length == 2)
                        linearLayout.addView(TextField(featNum, strSplit[1], true, 0));
                    break;
                case "CheckBox2":
                    linearLayout.addView(CheckBox(featNum, strSplit[1], switchedOn));
                    break;
                case "RadioButton2":
                    linearLayout.addView(RadioButton(featNum, strSplit[1], strSplit[2]));
                    break;
                case "Collapse2":
                    Collapse(linearLayout, strSplit[1]);
                    subFeat++;
                    break;
                case "ButtonLink2":
                    subFeat++;
                    linearLayout.addView(ButtonLink(strSplit[1], strSplit[2]));
                    break;
                case "Category2":
                    subFeat++;
                    linearLayout.addView(Category(strSplit[1]));
                    break;
                case "RichTextView2":
                    subFeat++;
                    linearLayout.addView(RichTextView(strSplit[1]));
                    break;
                case "RichWebView2":
                    subFeat++;
                    linearLayout.addView(RichWebView(strSplit[1]));
                    break;
            }
        }
    }

    private void featureList3(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            String feature = listFT[i];
            if (feature.contains("True_")) {
                switchedOn = true;
                feature = feature.replaceFirst("True_", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd3_")) {
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd3_", "");
            }

            String[] str = feature.split("_");
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                featNum = i - subFeat;
            }

            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle3":
                    linearLayout.addView(Switch(featNum, strSplit[1], switchedOn));
                    break;
                case "SeekBar3":
                    linearLayout.addView(SeekBar(featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3])));
                    break;
                case "Button3":
                    linearLayout.addView(Button(featNum, strSplit[1]));
                    break;
                case "ButtonOnOff3":
                    linearLayout.addView(ButtonOnOff(featNum, strSplit[1], switchedOn));
                    break;
                case "Spinner3":
                    linearLayout.addView(RichTextView(strSplit[1]));
                    linearLayout.addView(Spinner(featNum, strSplit[1], strSplit[2]));
                    break;
                case "InputText3":
                    linearLayout.addView(TextField(featNum, strSplit[1], false, 0));
                    break;
                case "InputValue3":
                    if (strSplit.length == 3)
                        linearLayout.addView(TextField(featNum, strSplit[2], true, Integer.parseInt(strSplit[1])));
                    if (strSplit.length == 2)
                        linearLayout.addView(TextField(featNum, strSplit[1], true, 0));
                    break;
                case "CheckBox3":
                    linearLayout.addView(CheckBox(featNum, strSplit[1], switchedOn));
                    break;
                case "RadioButton3":
                    linearLayout.addView(RadioButton(featNum, strSplit[1], strSplit[2]));
                    break;
                case "Collapse3":
                    Collapse(linearLayout, strSplit[1]);
                    subFeat++;
                    break;
                case "ButtonLink3":
                    subFeat++;
                    linearLayout.addView(ButtonLink(strSplit[1], strSplit[2]));
                    break;
                case "Category3":
                    subFeat++;
                    linearLayout.addView(Category(strSplit[1]));
                    break;
                case "RichTextView3":
                    subFeat++;
                    linearLayout.addView(RichTextView(strSplit[1]));
                    break;
                case "RichWebView3":
                    subFeat++;
                    linearLayout.addView(RichWebView(strSplit[1]));
                    break;
            }
        }
    }



    private void featureList4(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            String feature = listFT[i];
            if (feature.contains("True_")) {
                switchedOn = true;
                feature = feature.replaceFirst("True_", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd4_")) {
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd4_", "");
            }

            String[] str = feature.split("_");
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                featNum = i - subFeat;
            }

            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle4":
                    linearLayout.addView(Switch(featNum, strSplit[1], switchedOn));
                    break;
                case "SeekBar4":
                    linearLayout.addView(SeekBar(featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3])));
                    break;
                case "Button4":
                    linearLayout.addView(Button(featNum, strSplit[1]));
                    break;
                case "ButtonOnOff4":
                    linearLayout.addView(ButtonOnOff(featNum, strSplit[1], switchedOn));
                    break;
                case "Spinner4":
                    linearLayout.addView(RichTextView(strSplit[1]));
                    linearLayout.addView(Spinner(featNum, strSplit[1], strSplit[2]));
                    break;
                case "InputText4":
                    linearLayout.addView(TextField(featNum, strSplit[1], false, 0));
                    break;
                case "InputValue4":
                    if (strSplit.length == 3)
                        linearLayout.addView(TextField(featNum, strSplit[2], true, Integer.parseInt(strSplit[1])));
                    if (strSplit.length == 2)
                        linearLayout.addView(TextField(featNum, strSplit[1], true, 0));
                    break;
                case "CheckBox4":
                    linearLayout.addView(CheckBox(featNum, strSplit[1], switchedOn));
                    break;
                case "RadioButton4":
                    linearLayout.addView(RadioButton(featNum, strSplit[1], strSplit[2]));
                    break;
                case "Collapse4":
                    Collapse(linearLayout, strSplit[1]);
                    subFeat++;
                    break;
                case "ButtonLink4":
                    subFeat++;
                    linearLayout.addView(ButtonLink(strSplit[1], strSplit[2]));
                    break;
                case "Category4":
                    subFeat++;
                    linearLayout.addView(Category(strSplit[1]));
                    break;
                case "RichTextView4":
                    subFeat++;
                    linearLayout.addView(RichTextView(strSplit[1]));
                    break;
                case "RichWebView4":
                    subFeat++;
                    linearLayout.addView(RichWebView(strSplit[1]));
                    break;
            }
        }
    }

    private void featureList5(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            String feature = listFT[i];
            if (feature.contains("True_")) {
                switchedOn = true;
                feature = feature.replaceFirst("True_", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd5_")) {
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd5_", "");
            }

            String[] str = feature.split("_");
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                featNum = i - subFeat;
            }

            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle5":
                    linearLayout.addView(Switch(featNum, strSplit[1], switchedOn));
                    break;
                case "SeekBar5":
                    linearLayout.addView(SeekBar(featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3])));
                    break;
                case "Button5":
                    linearLayout.addView(Button(featNum, strSplit[1]));
                    break;
                case "ButtonOnOff5":
                    linearLayout.addView(ButtonOnOff(featNum, strSplit[1], switchedOn));
                    break;
                case "Spinner5":
                    linearLayout.addView(RichTextView(strSplit[1]));
                    linearLayout.addView(Spinner(featNum, strSplit[1], strSplit[2]));
                    break;
                case "InputText5":
                    linearLayout.addView(TextField(featNum, strSplit[1], false, 0));
                    break;
                case "InputValue5":
                    if (strSplit.length == 3)
                        linearLayout.addView(TextField(featNum, strSplit[2], true, Integer.parseInt(strSplit[1])));
                    if (strSplit.length == 2)
                        linearLayout.addView(TextField(featNum, strSplit[1], true, 0));
                    break;
                case "CheckBox5":
                    linearLayout.addView(CheckBox(featNum, strSplit[1], switchedOn));
                    break;
                case "RadioButton5":
                    linearLayout.addView(RadioButton(featNum, strSplit[1], strSplit[2]));
                    break;
                case "Collapse5":
                    Collapse(linearLayout, strSplit[1]);
                    subFeat++;
                    break;
                case "ButtonLink5":
                    subFeat++;
                    linearLayout.addView(ButtonLink(strSplit[1], strSplit[2]));
                    break;
                case "Category5":
                    subFeat++;
                    linearLayout.addView(Category(strSplit[1]));
                    break;
                case "RichTextView5":
                    subFeat++;
                    linearLayout.addView(RichTextView(strSplit[1]));
                    break;
                case "RichWebView5":
                    subFeat++;
                    linearLayout.addView(RichWebView(strSplit[1]));
                    break;
            }
        }
    }



    private void featureList6(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            String feature = listFT[i];
            if (feature.contains("True_")) {
                switchedOn = true;
                feature = feature.replaceFirst("True_", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd6_")) {
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd6_", "");
            }

            String[] str = feature.split("_");
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                featNum = i - subFeat;
            }

            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle6":
                    linearLayout.addView(Switch(featNum, strSplit[1], switchedOn));
                    break;
                case "SeekBar6":
                    linearLayout.addView(SeekBar(featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3])));
                    break;
                case "Button6":
                    linearLayout.addView(Button(featNum, strSplit[1]));
                    break;
                case "ButtonOnOff6":
                    linearLayout.addView(ButtonOnOff(featNum, strSplit[1], switchedOn));
                    break;
                case "Spinner6":
                    linearLayout.addView(RichTextView(strSplit[1]));
                    linearLayout.addView(Spinner(featNum, strSplit[1], strSplit[2]));
                    break;
                case "InputText6":
                    linearLayout.addView(TextField(featNum, strSplit[1], false, 0));
                    break;
                case "InputValue6":
                    if (strSplit.length == 6)
                        linearLayout.addView(TextField(featNum, strSplit[2], true, Integer.parseInt(strSplit[1])));
                    if (strSplit.length == 2)
                        linearLayout.addView(TextField(featNum, strSplit[1], true, 0));
                    break;
                case "CheckBox6":
                    linearLayout.addView(CheckBox(featNum, strSplit[1], switchedOn));
                    break;
                case "RadioButton6":
                    linearLayout.addView(RadioButton(featNum, strSplit[1], strSplit[2]));
                    break;
                case "Collapse6":
                    Collapse(linearLayout, strSplit[1]);
                    subFeat++;
                    break;
                case "ButtonLink6":
                    subFeat++;
                    linearLayout.addView(ButtonLink(strSplit[1], strSplit[2]));
                    break;
                case "Category6":
                    subFeat++;
                    linearLayout.addView(Category(strSplit[1]));
                    break;
                case "RichTextView6":
                    subFeat++;
                    linearLayout.addView(RichTextView(strSplit[1]));
                    break;
                case "RichWebView6":
                    subFeat++;
                    linearLayout.addView(RichWebView(strSplit[1]));
                    break;
            }
        }
    }

    final MemoryScanner gg=new MemoryScanner();
    private View Switch(final int featureNum, final String featureName, boolean swiOn) {

        final  GradientDrawable lindfxMods = new  GradientDrawable();
        lindfxMods.setSize(30,30);
        lindfxMods.setShape(1);
        lindfxMods.setStroke(4,Color.parseColor("#FF00506D"));
        lindfxMods.setColor(Color.WHITE);

        final GradientDrawable lindfxModsm = new GradientDrawable();
        lindfxModsm.setColor(Color.parseColor("#CED9D9D9"));
        
        lindfxModsm.setStroke(4,Color.parseColor("#FF00506D"));
        lindfxModsm.setCornerRadius(50);

        final Switch switchR = new Switch(this);
        switchR.setText(featureName);
        switchR.setTextColor(TEXT_COLOR_2);
        switchR.setTextSize(15f);
        
        switchR.setPadding(0, 0, 0, 0);
        switchR.setBackgroundColor(Color.parseColor("#00000000"));
        switchR.setThumbDrawable(lindfxMods);
        switchR.setTrackDrawable(lindfxModsm);
        switchR.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(30)));
        switchR.setTypeface(switchR.getTypeface(), Typeface.NORMAL);
        switchR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean bool) {
                    Preferences.changeFeatureBool(featureName, featureNum, bool);

                    switch (featureNum) 
                    {
                        
                        case 2001: //GG Value
                            if (bool) {
                                gg.setRanges(new int[]{Ranges.ANONYMOUS});
                                gg.searchNumber("0.1", Flag.FLOAT);
                                gg.refineNumber("0.1",Flags.FLOAT,0xa8);
                                gg.refineNumber("45",Flags.DWORD,0xb0);
                                gg.refineNumber("30",Flags.DWORD,0xb4);
                                gg.refineNumber("7",Flags.DWORD,0xb8);
                                gg.refineNumber("10",Flags.FLOAT,0xc4);
                                gg.refineNumber("25",Flags.FLOAT,0xcc);
                                gg.refineNumber("1",Flags.DWORD,0x12c);
                                gg.editAll("0.1", Flags.FLOAT,0x0);
                                gg.editAll("0.1", Flags.DWORD,0xa8);
                                gg.editAll("99999991", Flags.DWORD,0xb0);
                                gg.editAll("99999991", Flags.DWORD,0xb4);
                                gg.editAll("99999991", Flags.DWORD,0xb8);
                                gg.editAll("10",Flags.FLOAT,0xc4);
                                gg.editAll("25",Flags.FLOAT,0xcc);
                                gg.editAll("50",Flags.DWORD,0x12c);
                                gg.clearResults();
                            } else {
                                gg.setRanges(new int[]{Ranges.ANONYMOUS});
                                gg.searchNumber("0.1", Flags.FLOAT);
                                gg.refineNumber("0.1", Flags.DWORD,0xa8);
                                gg.refineNumber("99999991", Flags.DWORD,0xb0);
                                gg.refineNumber("30", Flags.DWORD,0xb4);
                                gg.refineNumber("99999991", Flags.DWORD,0xb8);
                                gg.refineNumber("10",Flags.FLOAT,0xc4);
                                gg.refineNumber("25",Flags.FLOAT,0xcc);
                                gg.refineNumber("50",Flags.DWORD,0x12c);
                                gg.editAll("0.1", Flags.FLOAT,0x0);
                                gg.editAll("0.1",Flags.FLOAT,0xa8);
                                gg.editAll("45",Flags.DWORD,0xb0);
                                gg.editAll("30",Flags.DWORD,0xb4);
                                gg.editAll("7",Flags.DWORD,0xb8);
                                gg.editAll("10",Flags.FLOAT,0xc4);
                                gg.editAll("25",Flags.FLOAT,0xcc);
                                gg.editAll("1",Flags.DWORD,0x12c);
                                gg.clearResults();
                            }
                            break;
                        }
                    
                    if(bool) {
                        lindfxModsm.setColor(Color.parseColor("#FF000000"));
                        lindfxModsm.setStroke(4,Color.parseColor("#ffffff"));
                        lindfxMods.setColor(Color.parseColor("#FF000000"));
                        lindfxMods.setStroke(4,Color.parseColor("#ffffff"));
                    } else {
                        lindfxModsm.setColor(Color.parseColor("#CED9D9D9"));
                        lindfxModsm.setStroke(4,Color.parseColor("#FF00506D"));
                        lindfxMods.setColor(Color.parseColor("#ffffff"));
                        lindfxMods.setStroke(4,Color.parseColor("#FF00506D"));
                    }


                }
            });
            
        return switchR;
	}

    private View SeekBar(final int featNum, final String featName, final int min, int max) {
        int loadedProg = Preferences.loadPrefInt(featName, featNum);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(10, 5, 0, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        final TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + ((loadedProg == 0) ? min : loadedProg)));
        textView.setTextColor(TEXT_COLOR_2);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setPadding(25, 10, 35, 10);
        seekBar.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            seekBar.setMin(min); //setMin for Oreo and above
        seekBar.setProgress((loadedProg == 0) ? min : loadedProg);
        seekBar.getThumb().setColorFilter(SeekBarColor, PorterDuff.Mode.SRC_ATOP);
        seekBar.getProgressDrawable().setColorFilter(SeekBarProgressColor, PorterDuff.Mode.SRC_ATOP);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                //if progress is greater than minimum, don't go below. Else, set progress
                seekBar.setProgress(i < min ? min : i);
                Preferences.changeFeatureInt(featName, featNum, i < min ? min : i);
                textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + (i < min ? min : i)));
            }
        });
        linearLayout.addView(textView);
        linearLayout.addView(seekBar);

        return linearLayout;
    }

    private View Button(final int featNum, final String featName) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);
        button.setAllCaps(false); //Disable caps to support html
        button.setText(Html.fromHtml(featName));
        button.setBackgroundColor(BTN_COLOR);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (featNum) {
                    case -4:
                        Logcat.Save(getApplicationContext());
                        break;
                    case -5:
                        Logcat.Clear(getApplicationContext());
                        break;
                    case -6:
                        scrollView.removeView(mSettings);
                        scrollView.addView(patches);
                        break;
                    case -100:
                        stopChecking = true;
                        break;
                }
                Preferences.changeFeatureInt(featName, featNum, 0);
            }
        });

        return button;
    }

    private View ButtonLink(final String featName, final String url) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setAllCaps(false); //Disable caps to support html
        button.setTextColor(TEXT_COLOR_2);
        button.setText(Html.fromHtml(featName));
        button.setBackgroundColor(BTN_COLOR);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        return button;
    }

    private View ButtonOnOff(final int featNum, String featName, boolean switchedOn) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);
        button.setAllCaps(false); //Disable caps to support html

        final String finalfeatName = featName.replace("OnOff_", "");
        boolean isOn = Preferences.loadPrefBool(featName, featNum, switchedOn);
        if (isOn) {
            button.setText(Html.fromHtml(finalfeatName + ": ON"));
            button.setBackgroundColor(BtnON);
            isOn = false;
        } else {
            button.setText(Html.fromHtml(finalfeatName + ": OFF"));
            button.setBackgroundColor(BtnOFF);
            isOn = true;
        }
        final boolean finalIsOn = isOn;
        button.setOnClickListener(new View.OnClickListener() {
            boolean isOn = finalIsOn;

            public void onClick(View v) {
                Preferences.changeFeatureBool(finalfeatName, featNum, isOn);
                //Log.d(TAG, finalfeatName + " " + featNum + " " + isActive2);
                if (isOn) {
                    button.setText(Html.fromHtml(finalfeatName + ": ON"));
                    button.setBackgroundColor(BtnON);
                    isOn = false;
                } else {
                    button.setText(Html.fromHtml(finalfeatName + ": OFF"));
                    button.setBackgroundColor(BtnOFF);
                    isOn = true;
                }
            }
        });
        return button;
    }

    private View Spinner(final int featNum, final String featName, final String list) {
        Log.d(TAG, "spinner " + featNum + " " + featName + " " + list);
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        // Create another LinearLayout as a workaround to use it as a background
        // to keep the down arrow symbol. No arrow symbol if setBackgroundColor set
        LinearLayout linearLayout2 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layoutParams2.setMargins(7, 2, 7, 5);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setBackgroundColor(BTN_COLOR);
        linearLayout2.setLayoutParams(layoutParams2);

        final Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
        spinner.setLayoutParams(layoutParams2);
        spinner.getBackground().setColorFilter(1, PorterDuff.Mode.SRC_ATOP); //trick to show white down arrow color
        //Creating the ArrayAdapter instance having the list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lists);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner'
        spinner.setAdapter(aa);
        spinner.setSelection(Preferences.loadPrefInt(featName, featNum));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Preferences.changeFeatureInt(spinner.getSelectedItem().toString(), featNum, position);
                ((TextView) parentView.getChildAt(0)).setTextColor(TEXT_COLOR_2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        linearLayout2.addView(spinner);
        return linearLayout2;
    }

    private View TextField(final int featNum, final String featName, final boolean numOnly, final int maxValue) {
        final EditTextString edittextstring = new EditTextString();
        final EditTextNum edittextnum = new EditTextNum();
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);

        final Button button = new Button(this);
        if (numOnly) {
            int num = Preferences.loadPrefInt(featName, featNum);
            edittextnum.setNum((num == 0) ? 1 : num);
            button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + ((num == 0) ? 1 : num) + "</font>"));
        } else {
            String string = Preferences.loadPrefString(featName, featNum);
            edittextstring.setString((string == "") ? "" : string);
            button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + string + "</font>"));
        }
        button.setAllCaps(false);
        button.setLayoutParams(layoutParams);
        button.setBackgroundColor(BTN_COLOR);
        button.setTextColor(TEXT_COLOR_2);
        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog alert = new AlertDialog.Builder(getApplicationContext(), 2).create();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Objects.requireNonNull(alert.getWindow()).setType(Build.VERSION.SDK_INT >= 26 ? 2038 : 2002);
                    }
                    alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }
                        });

                    //LinearLayout
                    LinearLayout linearLayout1 = new LinearLayout(getApplicationContext());
                    linearLayout1.setPadding(5, 5, 5, 5);
                    linearLayout1.setOrientation(LinearLayout.VERTICAL);
                    linearLayout1.setBackgroundColor(MENU_FEATURE_BG_COLOR);

                    //TextView
                    final TextView TextViewNote = new TextView(getApplicationContext());
                    TextViewNote.setText("Tap OK to apply changes. Tap outside to cancel");
                    if (maxValue != 0)
                        TextViewNote.setText("Tap OK to apply changes. Tap outside to cancel\nMax value: " + maxValue);
                    TextViewNote.setTextColor(TEXT_COLOR_2);

                    //Edit text
                    final EditText edittext = new EditText(getApplicationContext());
                    edittext.setMaxLines(1);
                    edittext.setWidth(convertDipToPixels(300));
                    edittext.setTextColor(TEXT_COLOR_2);
                    if (numOnly) {
                        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
                        edittext.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
                        InputFilter[] FilterArray = new InputFilter[1];
                        FilterArray[0] = new InputFilter.LengthFilter(10);
                        edittext.setFilters(FilterArray);
                    } else {
                        edittext.setText(edittextstring.getString());
                    }
                    edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                                if (hasFocus) {
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                } else {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                }
                            }
                        });
                    edittext.requestFocus();

                    //Button
                    Button btndialog = new Button(getApplicationContext());
                    btndialog.setBackgroundColor(BTN_COLOR);
                    btndialog.setTextColor(TEXT_COLOR_2);
                    btndialog.setText("OK");
                    btndialog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (numOnly) {
                                    int num;
                                    try {
                                        num = Integer.parseInt(TextUtils.isEmpty(edittext.getText().toString()) ? "0" : edittext.getText().toString());
                                        if (maxValue != 0 &&  num >= maxValue)
                                            num = maxValue;
                                    } catch (NumberFormatException ex) {
                                        num = 2147483640;
                                    }
                                    edittextnum.setNum(num);
                                    button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + num + "</font>"));
                                    alert.dismiss();
                                    Preferences.changeFeatureInt(featName, featNum, num);
                                } else {
                                    String str = edittext.getText().toString();
                                    edittextstring.setString(edittext.getText().toString());
                                    button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + str + "</font>"));
                                    alert.dismiss();
                                    Preferences.changeFeatureString(featName, featNum, str);
                                }
                                edittext.setFocusable(false);
                            }
                        });

                    linearLayout1.addView(TextViewNote);
                    linearLayout1.addView(edittext);
                    linearLayout1.addView(btndialog);
                    alert.setView(linearLayout1);
                    alert.show();
                }
            });

        linearLayout.addView(button);
        return linearLayout;
    }

    private View CheckBox(final int featNum, final String featName, boolean switchedOn) {
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText(featName);
        checkBox.setTextColor(TEXT_COLOR_2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            checkBox.setButtonTintList(ColorStateList.valueOf(CheckBoxColor));
        checkBox.setChecked(Preferences.loadPrefBool(featName, featNum, switchedOn));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBox.isChecked()) {
                    Preferences.changeFeatureBool(featName, featNum, isChecked);
                } else {
                    Preferences.changeFeatureBool(featName, featNum, isChecked);
                }
            }
        });
        return checkBox;
    }

    private View RadioButton(final int featNum, String featName, final String list) {
        //Credit: LoraZalora
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        final TextView textView = new TextView(this);
        textView.setText(featName + ":");
        textView.setTextColor(TEXT_COLOR_2);

        final RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setPadding(10, 5, 10, 5);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        radioGroup.addView(textView);

        for (int i = 0; i < lists.size(); i++) {
            final RadioButton Radioo = new RadioButton(this);
            final String finalfeatName = featName, radioName = lists.get(i);
            View.OnClickListener first_radio_listener = new View.OnClickListener() {
                public void onClick(View v) {
                    textView.setText(Html.fromHtml(finalfeatName + ": <font color='" + NumberTxtColor + "'>" + radioName));
                    Preferences.changeFeatureInt(finalfeatName, featNum, radioGroup.indexOfChild(Radioo));
                }
            };
            System.out.println(lists.get(i));
            Radioo.setText(lists.get(i));
            Radioo.setTextColor(Color.LTGRAY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Radioo.setButtonTintList(ColorStateList.valueOf(RadioColor));
            Radioo.setOnClickListener(first_radio_listener);
            radioGroup.addView(Radioo);
        }

        int index = Preferences.loadPrefInt(featName, featNum);
        if (index > 0) { //Preventing it to get an index less than 1. below 1 = null = crash
            textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + lists.get(index - 1)));
            ((RadioButton) radioGroup.getChildAt(index)).setChecked(true);
        }

        return radioGroup;
    }

    private void Collapse(LinearLayout linLayout, final String text) {
        LinearLayout.LayoutParams layoutParamsLL = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParamsLL.setMargins(0, 5, 0, 0);

        LinearLayout collapse = new LinearLayout(this);
        collapse.setLayoutParams(layoutParamsLL);
        collapse.setVerticalGravity(16);
        collapse.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout collapseSub = new LinearLayout(this);
        collapseSub.setVerticalGravity(16);
        collapseSub.setPadding(0, 5, 0, 5);
        collapseSub.setOrientation(LinearLayout.VERTICAL);
        collapseSub.setBackgroundColor(Color.parseColor("#222D38"));
        collapseSub.setVisibility(View.GONE);
        mCollapse = collapseSub;

        final TextView textView = new TextView(this);
        textView.setBackgroundColor(CategoryBG);
        textView.setText("▽ " + text + " ▽");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(TEXT_COLOR_2);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 20, 0, 20);
        textView.setOnClickListener(new View.OnClickListener() {
            boolean isChecked;

            @Override
            public void onClick(View v) {

                boolean z = !this.isChecked;
                this.isChecked = z;
                if (z) {
                    collapseSub.setVisibility(View.VISIBLE);
                    textView.setText("△ " + text + " △");
                    return;
                }
                collapseSub.setVisibility(View.GONE);
                textView.setText("▽ " + text + " ▽");
            }
        });
        collapse.addView(textView);
        collapse.addView(collapseSub);
        linLayout.addView(collapse);
    }

    private View Category(String text) {
        TextView textView = new TextView(this);
        textView.setBackgroundColor(CategoryBG);
        textView.setText(Html.fromHtml(text));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(TEXT_COLOR_2);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 5, 0, 5);
        return textView;
    }

    private View RichTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(text));
        textView.setTextColor(TEXT_COLOR_2);
        textView.setPadding(10, 5, 10, 5);
        return textView;
    }

    private View RichWebView(String text) {
        WebView wView = new WebView(this);
        wView.loadData(text, "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setPadding(0, 5, 0, 5);
        wView.getSettings().setAppCacheEnabled(false);
        return wView;
    }

    //Override our Start Command so the Service doesnt try to recreate itself when the App is closed
    public int onStartCommand(Intent intent, int i, int i2) {
        return Service.START_NOT_STICKY;
    }

    private boolean isViewCollapsed() {
        return rootFrame == null || mCollapsed.getVisibility() == View.VISIBLE;
    }

    //For our image a little converter
    private int convertDipToPixels(int i) {
        return (int) ((((float) i) * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int dp(int i) {
        return (int) TypedValue.applyDimension(1, (float) i, getResources().getDisplayMetrics());
    }

    //Check if we are still in the game. If now our menu and menu button will dissapear
    private boolean isNotInGame() {
        RunningAppProcessInfo runningAppProcessInfo = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    //Destroy our View
    public void onDestroy() {
        super.onDestroy();
        if (rootFrame != null) {
            mWindowManager.removeView(rootFrame);
        }
    }

    //Same as above so it wont crash in the background and therefore use alot of Battery life
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopSelf();
    }

    private void Thread() {
        if (rootFrame == null) {
            return;
        }
        if (isNotInGame()) {
            rootFrame.setVisibility(View.INVISIBLE);
        } else {
            rootFrame.setVisibility(View.VISIBLE);
        }
    }

    private class EditTextString {
        private String text;

        public void setString(String s) {
            text = s;
        }

        public String getString() {
            return text;
        }
    }

    private class EditTextNum {
        private int val;

        public void setNum(int i) {
            val = i;
        }

        public int getNum() {
            return val;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
