package com.facebook.conceal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.facebook.conceal.R;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private EditText mInputContentText;
    private TextView mShowContentText;
    private Button mSaveBtn;
    private Button mQueryBtn;
    
    private OnClickListener mClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.save_btn:
                saveContent();
                break;
            case R.id.query_btn:
                queryContent();
                break;
            }
        }
    };
    
    private void saveContent() {
        String content = mInputContentText.getText().toString();
        if(TextUtils.isEmpty(content)) {
            return;
        }
        
        try {
            encryptingContent(content);
            Toast.makeText(this, "Save Succe", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void queryContent() {
        try {
            String content = decryptingContent();
            if(TextUtils.isEmpty(content)) {
                return;
            }
            
            mShowContentText.setText(content);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getViews();
        setClickListeners();
    }

    private void setClickListeners() {
        mSaveBtn.setOnClickListener(mClickListener);
        mQueryBtn.setOnClickListener(mClickListener);
    }

    private void getViews() {
        mInputContentText = (EditText) findViewById(R.id.content_input);
        mShowContentText = (TextView) findViewById(R.id.content_show);
        mSaveBtn = (Button) findViewById(R.id.save_btn);
        mQueryBtn = (Button) findViewById(R.id.query_btn);
    }
    
    private void encryptingContent(String content) throws Exception {
        // Creates a new Crypto object with default implementations of 
        // a key chain as well as native library.
        Crypto crypto = new Crypto(
          new SharedPrefsBackedKeyChain(this),
          new SystemNativeCryptoLibrary());

        // Check for whether the crypto functionality is available
        // This might fail if android does not load libaries correctly.
        if (!crypto.isAvailable()) {
          return;
        }
        String path = Environment.getExternalStorageDirectory().getPath() + "/test.txt";
        File file = new File(path);
        if(!file.exists()) {
            file.createNewFile();
        }
        
        OutputStream fileStream = new BufferedOutputStream(
          new FileOutputStream(file));

        // Creates an output stream which encrypts the data as
        // it is written to it and writes it out to the file.
        OutputStream outputStream = crypto.getCipherOutputStream(
          fileStream,
          new Entity("test"));

        // Write plaintext to it.
        outputStream.write(content.getBytes());
        outputStream.close();
    }
    
    private String decryptingContent() throws Exception {
        // Creates a new Crypto object with default implementations of 
        // a key chain as well as native library.
        Crypto crypto = new Crypto(
          new SharedPrefsBackedKeyChain(this),
          new SystemNativeCryptoLibrary());

        // Check for whether the crypto functionality is available
        // This might fail if android does not load libaries correctly.
        if (!crypto.isAvailable()) {
          return "";
        }
        String path = Environment.getExternalStorageDirectory().getPath() + "/test.txt";
        File file = new File(path);
     // Get the file to which ciphertext has been written.
        FileInputStream fileStream = new FileInputStream(file);

        // Creates an input stream which decrypts the data as
        // it is read from it.
        InputStream inputStream = crypto.getCipherInputStream(
          fileStream,
          new Entity("test"));

        // Read into a byte array.
        int read;
        byte[] buffer = new byte[1024];

        // You must read the entire stream to completion.
        // The verification is done at the end of the stream.
        // Thus not reading till the end of the stream will cause
        // a security bug. 
        int total = 0;
        while ((read = inputStream.read(buffer)) != -1) {
            total += read;
        }

        inputStream.close();
        return new String(buffer,0, total);
    }
}
