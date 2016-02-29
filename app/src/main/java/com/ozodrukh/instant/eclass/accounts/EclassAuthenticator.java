package com.ozodrukh.instant.eclass.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import com.ozodrukh.eclass.entity.User;
import com.ozodrukh.instant.eclass.BuildConfig;
import com.ozodrukh.instant.eclass.EclassApplication;

public class EclassAuthenticator extends AbstractAccountAuthenticator{

  public static final String ECLASS_USER = BuildConfig.APPLICATION_ID + ".auth.USER_ACCOUNT";

  public static Account[] getAccounts(){
    return AccountManager.get(EclassApplication.getContext())
        .getAccountsByType(ECLASS_USER);
  }

  public static Bundle getUserData(User user){
    Bundle userData = new Bundle();
    userData.putString("name", user.getName());
    userData.putString("gr_code", user.getGrCode());
    userData.putString("user_id", user.getUserId());
    return userData;
  }

  public static User loadUser(AccountManager am, Account account){
    User user = new User();
    user.setGrCode(am.getUserData(account, "gr_code"));
    user.setName(am.getUserData(account, "name"));
    user.setUserId(account.name);
    user.setPassword(am.getPassword(account));
    return user;
  }

  public EclassAuthenticator(Context context) {
    super(context);
  }

  @Override
  public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
    return null;
  }

  @Override public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
      String authTokenType, String[] requiredFeatures, Bundle options)
      throws NetworkErrorException {
    return null;
  }

  @Override public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
      Bundle options) throws NetworkErrorException {
    return null;
  }

  @Override public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
      String authTokenType, Bundle options) throws NetworkErrorException {
    return null;
  }

  @Override public String getAuthTokenLabel(String authTokenType) {
    return "E-class";
  }

  @Override public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
      String authTokenType, Bundle options) throws NetworkErrorException {
    return null;
  }

  @Override public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
      String[] features) throws NetworkErrorException {
    return null;
  }
}
