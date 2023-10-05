package com.skyblue.facerecognition.object;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.skyblue.facerecognition.R;
import com.skyblue.facerecognition.database.DatabaseManager;
import com.skyblue.facerecognition.helpers.MLVideoHelperActivity;
import com.skyblue.facerecognition.helpers.vision.VisionBaseProcessor;
import com.skyblue.facerecognition.helpers.vision.recogniser.FaceRecognitionProcessor;
import com.google.mlkit.vision.face.Face;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FaceRecognitionActivity extends MLVideoHelperActivity implements FaceRecognitionProcessor.FaceRecognitionCallback {

    private Interpreter faceNetInterpreter;
    private FaceRecognitionProcessor faceRecognitionProcessor;

    private Face face;
    private Bitmap faceBitmap;
    private float[] faceVector;
    private DatabaseManager databaseManager;
    private final Context context = this;
    public static final String APP_DATA_FOLDER = "/.saneforce1";
    String SAVE_DIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeAddFaceVisible();

        databaseManager = new DatabaseManager(context);
        createDirectory();
    }

    private void createDirectory() {
        File dir = getExternalFilesDir(APP_DATA_FOLDER);
        if(!dir.exists()) {
            if (!dir.mkdir()) {
                Log.e("camerax_", "The folder " + dir.getPath() + "was not created");
            }else {
                Log.e("camerax_", "The folder " + dir.getPath() + "was created success");
            }
        }
    }

    @Override
    protected VisionBaseProcessor setProcessor() {
        try {
            faceNetInterpreter = new Interpreter(FileUtil.loadMappedFile(this, "mobile_face_net.tflite"), new Interpreter.Options());
        } catch (IOException e) {
            e.printStackTrace();
        }

        faceRecognitionProcessor = new FaceRecognitionProcessor(
                faceNetInterpreter,
                graphicOverlay,
                this
        );
        faceRecognitionProcessor.activity = this;
        return faceRecognitionProcessor;
    }

    public void setTestImage(Bitmap cropToBBox) {
        if (cropToBBox == null) {
            return;
        }
        runOnUiThread(() -> ((ImageView) findViewById(R.id.testImageView)).setImageBitmap(cropToBBox));
    }

    @Override
    public void onFaceDetected(Face face, Bitmap faceBitmap, float[] faceVector) {
        this.face = face;
        this.faceBitmap = faceBitmap;
        this.faceVector = faceVector;
    }

    @Override
    public void onFaceRecognised(Face face, float probability, String name) {

    }

    @Override
    public void onAddFaceClicked(View view) {
        super.onAddFaceClicked(view);

        if (face == null || faceBitmap == null) {
            return;
        }

        Face tempFace = face;
        Bitmap tempBitmap = faceBitmap;
        float[] tempVector = faceVector;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.add_face_dialog, null);
        ((ImageView) dialogView.findViewById(R.id.dlg_image)).setImageBitmap(tempBitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            Editable input  = ((EditText) dialogView.findViewById(R.id.dlg_input)).getEditableText();
            Editable empId = ((EditText) dialogView.findViewById(R.id.emp_id)).getEditableText();

            String mBase64Image =  bitmapToBase64();

            if (input.length() > 0) {
                faceRecognitionProcessor.registerFace(input, tempVector);

                String mDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String mTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                databaseManager.registerNewFace(input.toString(), Arrays.toString(tempVector), mBase64Image, empId.toString(), mDate, mTime);
                Toast.makeText(context, "Saved success", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private String bitmapToBase64() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        faceBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume_", "onResumed: Just now");
    }
}
