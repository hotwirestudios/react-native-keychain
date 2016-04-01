package com.oblador.keychain;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.nio.charset.Charset;

public class KeychainModule extends ReactContextBaseJavaModule {

    public static final String REACT_CLASS = "RNKeychainManager";
    public static final String KEYCHAIN_DATA = "RN_KEYCHAIN";

    private final Crypto crypto;
    private final SharedPreferences prefs;

    private final Charset charset = Charset.forName("UTF-8");

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public KeychainModule(ReactApplicationContext reactContext) {
        super(reactContext);
        crypto = new Crypto(
                new SharedPrefsBackedKeyChain(getReactApplicationContext()),
                new SystemNativeCryptoLibrary());
        prefs = this.getReactApplicationContext().getSharedPreferences(KEYCHAIN_DATA, Context.MODE_PRIVATE);
    }

    @ReactMethod
    public void setGenericPasswordForService(String service, String username, String password, Callback callback) {
        if (!crypto.isAvailable()) {
            Log.e("Crypto", "Crypto is missing");
        }

        Entity entity = new Entity(KEYCHAIN_DATA + ":" + service);

        try {
            String encryptedUsername = encryptWithEntity(username, entity);
            String encryptedPassword = encryptWithEntity(password, entity);

            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString(service + ":u", encryptedUsername);
            prefsEditor.putString(service + ":p", encryptedPassword);
            prefsEditor.apply();
            callback.invoke(new Object[] {null});
        } catch (Exception e) {
            e.printStackTrace();
            callback.invoke(exceptionToErrorObj(e));
        }
    }

    private String encryptWithEntity(String string, Entity entity) throws KeyChainException, CryptoInitializationException, IOException {
        byte[] bytes = string.getBytes(charset);
        return Base64.encodeToString(crypto.encrypt(bytes, entity), Base64.DEFAULT);
    }

    private String decryptWithEntity(String string, Entity entity) throws KeyChainException, CryptoInitializationException, IOException {
        return new String(crypto.decrypt(Base64.decode(string.getBytes(charset), Base64.DEFAULT), entity), charset);
    }

    @ReactMethod
    public void getGenericPasswordForService(String service, Callback callback) {

        String username = prefs.getString(service + ":u", "");
        String password = prefs.getString(service + ":p", "");

        if (username.isEmpty() || password.isEmpty()) {
            callback.invoke(new Object[] {null});
            return;
        }

        Entity entity = new Entity(KEYCHAIN_DATA + ":" + service);

        try {
            String decryptedUsername = decryptWithEntity(username, entity);
            String decryptedPass = decryptWithEntity(password, entity);

            callback.invoke(null, decryptedUsername, decryptedPass);
        } catch (Exception e) {
            e.printStackTrace();
            callback.invoke(exceptionToErrorObj(e));
        }
    }

    @ReactMethod
    public void resetGenericPasswordForService(String service, Callback callback) {
        try {
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.remove(service + ":u");
            prefsEditor.remove(service + ":p");
            prefsEditor.apply();
            callback.invoke(new Object[] {null});
        } catch (Exception e) {
            e.printStackTrace();
            callback.invoke(exceptionToErrorObj(e));
        }
    }

    private WritableNativeMap exceptionToErrorObj(Exception e) {
        WritableNativeMap map = new WritableNativeMap();
        map.putString("key", e.getClass().toString());
        map.putString("message", e.getMessage());
        return map;
    }


//    @ReactMethod
//    public void setInternetCredentialsForServer(String server, String username, String password, Callback callback) {
//
//    }
//
//    @ReactMethod
//    public void getInternetCredentialsForServer(String server, Callback callback) {
//
//    }
//
//    @ReactMethod
//    public void resetInternetCredentialsForServer(String server, Callback callback) {
//
//    }


}
