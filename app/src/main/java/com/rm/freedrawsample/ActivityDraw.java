package com.rm.freedrawsample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rm.freedrawview.FreeDrawView;
import com.rm.freedrawview.PathDrawnListener;
import com.rm.freedrawview.PathRedoUndoCountChangeListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityDraw extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        PathRedoUndoCountChangeListener, FreeDrawView.DrawCreatorListener, PathDrawnListener {

    private static final int THICKNESS_STEP = 1;
    private static final int THICKNESS_MAX = 30;
    private static final int THICKNESS_MIN = 1;

    private static final int ALPHA_STEP = 1;
    private static final int ALPHA_MAX = 255;
    private static final int ALPHA_MIN = 0;
    private static final String TAG = "ActivityDraw";

    private FreeDrawView mFreeDrawView;
    private View mSideView;
    private Button mBtnRandomColor, mBtnUndo, mBtnRedo, mBtnClearAll;
    private SeekBar mThicknessBar, mAlphaBar;
    private TextView mTxtRedoCount, mTxtUndoCount;

    private ImageView mImgScreen;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        setTitle(getString(R.string.app_name));

        mImgScreen = (ImageView) findViewById(R.id.img_screen);

        mTxtRedoCount = (TextView) findViewById(R.id.txt_redo_count);
        mTxtUndoCount = (TextView) findViewById(R.id.txt_undo_count);

        mFreeDrawView = (FreeDrawView) findViewById(R.id.free_draw_view);
        mFreeDrawView.setOnPathDrawnListener(this);
        mFreeDrawView.setPathRedoUndoCountChangeListener(this);

        mSideView = findViewById(R.id.side_view);
        mBtnRandomColor = (Button) findViewById(R.id.btn_color);
        mBtnUndo = (Button) findViewById(R.id.btn_undo);
        mBtnRedo = (Button) findViewById(R.id.btn_redo);
        mBtnClearAll = (Button) findViewById(R.id.btn_clear_all);
        mThicknessBar = (SeekBar) findViewById(R.id.slider_thickness);
        mAlphaBar = (SeekBar) findViewById(R.id.slider_alpha);

        mBtnRandomColor.setOnClickListener(this);
        mBtnUndo.setOnClickListener(this);
        mBtnRedo.setOnClickListener(this);
        mBtnClearAll.setOnClickListener(this);

        mAlphaBar.setMax((ALPHA_MAX - ALPHA_MIN) / ALPHA_STEP);
        mAlphaBar.setProgress(mFreeDrawView.getPaintAlpha());
        mAlphaBar.setOnSeekBarChangeListener(this);

        mThicknessBar.setMax((THICKNESS_MAX - THICKNESS_MIN) / THICKNESS_STEP);
        mThicknessBar.setProgress((int) mFreeDrawView.getPaintWidth());
        mThicknessBar.setOnSeekBarChangeListener(this);
        mSideView.setBackgroundColor(mFreeDrawView.getPaintColor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        mMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_screen) {
            takeAndShowScreenshot();
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void takeAndShowScreenshot() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFreeDrawView.getDrawScreenshot(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSideView.setBackgroundColor(mFreeDrawView.getPaintColor());
    }

    private void changeColor() {
        int color = ColorHelper.getRandomMaterialColor(this);

        mFreeDrawView.setPaintColor(color);

        mSideView.setBackgroundColor(mFreeDrawView.getPaintColor());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == mBtnRandomColor.getId()) {
            changeColor();
        }

        if (id == mBtnUndo.getId()) {
            mFreeDrawView.undoLast();
        }

        if (id == mBtnRedo.getId()) {
            mFreeDrawView.redoLast();
        }

        if (id == mBtnClearAll.getId()) {
            mFreeDrawView.undoAll();
        }
    }

    // SliderListener
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == mThicknessBar.getId()) {
            mFreeDrawView.setPaintWidthDp(THICKNESS_MIN + (progress * THICKNESS_STEP));
        } else {
            mFreeDrawView.setPaintAlpha(ALPHA_MIN + (progress * ALPHA_STEP));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onBackPressed() {
        if (mImgScreen.getVisibility() == View.VISIBLE) {
            mMenu.findItem(R.id.menu_screen).setVisible(true);
            mImgScreen.setImageBitmap(null);
            mImgScreen.setVisibility(View.GONE);

            mFreeDrawView.setVisibility(View.VISIBLE);
            mSideView.setVisibility(View.VISIBLE);

            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            moveTaskToBack(true);
        }
    }

    // PathRedoUndoCountChangeListener.
    @Override
    public void onUndoCountChanged(int undoCount) {
        mTxtUndoCount.setText(String.valueOf(undoCount));
    }

    @Override
    public void onRedoCountChanged(int redoCount) {
        mTxtRedoCount.setText(String.valueOf(redoCount));
    }

    // PathDrawnListener
    @Override
    public void onNewPathDrawn() {
        // The user has finished drawing a path
    }

    @Override
    public void onPathStart() {
        // The user has started drawing a path
    }


    // DrawCreatorListener
    @Override
    public void onDrawCreated(Bitmap draw) {
        mSideView.setVisibility(View.GONE);
        mFreeDrawView.setVisibility(View.GONE);

        mMenu.findItem(R.id.menu_screen).setVisible(false);

        mImgScreen.setVisibility(View.VISIBLE);

        mImgScreen.setImageBitmap(draw);

        savebitmap(draw);
    }

    @Override
    public void onDrawCreationError() {
        Toast.makeText(this, "Error, cannot create bitmap", Toast.LENGTH_SHORT).show();
    }

    public void savebitmap(Bitmap bmp){
        try{
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
            String timeStamp = new SimpleDateFormat("ddMMyy_HHmmss").format(new Date());
            String mImageName="coratcoret_"+ timeStamp +".jpg";
            String storagePath = Environment.getExternalStorageDirectory() + "/coratcoret";
            File sdStorageDir = new File(storagePath);
            sdStorageDir.mkdirs();
            File f = new File(Environment.getExternalStorageDirectory()
                    + "/coratcoret"
                    + File.separator + mImageName);

            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            Toast.makeText(this, "Your image sucsessfully saved to:\n"+storagePath+"/"+mImageName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
