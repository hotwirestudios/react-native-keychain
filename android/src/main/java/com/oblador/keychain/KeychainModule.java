package com.oblador.keychain;

import android.content.Context;
import android.content.SharedPreferences;
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

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

public class KeychainModule extends ReactContextBaseJavaModule {

  public static final String REACT_CLASS = "RNKeychainManager";
  public static final String KEYCHAIN_DATA = "RN_KEYCHAIN";

  private final Crypto crypto;
  private final SharedPreferences prefs;

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
    //TODO: Service name?
    Entity entity = new Entity(KEYCHAIN_DATA + ":" + service + ":" + password);
    byte[] encryptedPass = new byte[0];
    byte[] b = password.getBytes(Charset.forName("UTF-8"));
    try {
      encryptedPass = crypto.encrypt(b, entity);
      byte[] decryptedPass = crypto.decrypt(encryptedPass, entity);
      //TODO: Store both in SharedPreferences?

      SharedPreferences.Editor prefsEditor = prefs.edit();
      prefsEditor.putString()

      prefsEditor.commit();
      Log.e("Crypto Encrypted", new String(encryptedPass));
      Log.e("Crypto Decrypted ", new String(decryptedPass));
    } catch (KeyChainException e) {
      e.printStackTrace();
    } catch (CryptoInitializationException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @ReactMethod
  public void getGenericPasswordForService(String service, Callback successCallback, Callback errorCallback) {

    String username = prefs.getString(service+":username", "");
    String password = prefs.getString(service+":password", "");

    successCallback.invoke(username, password);
  }

  @ReactMethod
  public void resetGenericPasswordForService(String service) {

  }

  private String getSharedPrefKey(String service, )

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
