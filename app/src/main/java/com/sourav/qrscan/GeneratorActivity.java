package com.sourav.qrscan;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sourav.qrscan.databinding.ActivityGeneratorBinding;

import java.io.IOException;
import java.io.OutputStream;

public class GeneratorActivity extends AppCompatActivity {

    private static final String TAG = "AdMobDebug";
    private ActivityGeneratorBinding binding;
    private Bitmap qrBitmap;
    private int selectedColor = Color.BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rgColors.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbBlack) selectedColor = Color.BLACK;
            else if (checkedId == R.id.rbBlue) selectedColor = Color.parseColor("#2196F3");
            else if (checkedId == R.id.rbRed) selectedColor = Color.parseColor("#F44336");
            else if (checkedId == R.id.rbGreen) selectedColor = Color.parseColor("#4CAF50");
            else if (checkedId == R.id.rbPurple) selectedColor = Color.parseColor("#9C27B0");
        });

        binding.btnGenerate.setOnClickListener(v -> {
            String input = binding.etInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter some text", Toast.LENGTH_SHORT).show();
                return;
            }
            generateQRCode(input);
        });

        binding.btnSave.setOnClickListener(v -> saveImageToGallery());
        binding.btnShare.setOnClickListener(v -> shareImage());

        loadBannerAd();
    }

    private void loadBannerAd() {
        AdView adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        adView.setAdSize(AdSize.BANNER);
        
        binding.adContainer.removeAllViews();
        binding.adContainer.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Generator Banner Ad Loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                Log.e(TAG, "Generator Banner Ad Failed: " + adError.getMessage());
            }
        });
    }

    private void generateQRCode(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? selectedColor : Color.WHITE);
                }
            }
            binding.ivQrCode.setImageBitmap(qrBitmap);
            binding.cvQr.setVisibility(View.VISIBLE);
            binding.llActions.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void saveImageToGallery() {
        if (qrBitmap == null) return;

        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "QR_" + System.currentTimeMillis() + ".png");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuickScanPro");
                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                java.io.File image = new java.io.File(imagesDir, "QR_" + System.currentTimeMillis() + ".png");
                fos = new java.io.FileOutputStream(image);
            }
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        if (qrBitmap == null) return;
        
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), qrBitmap, "QR Code", null);
        if (path == null) return;
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share QR Code"));
    }
}
