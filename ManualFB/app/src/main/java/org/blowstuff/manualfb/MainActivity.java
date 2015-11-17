package org.blowstuff.manualfb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    // When you login, we will save the accessToken
    // If the user goes out of the app and then returns, the accessToken will be there.
    // In order to avoid problems, we logout automatically the user in the onCreate method
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // #### WARNING ####
        // FacebookSDK must initialize before the setContentView (
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        // Logs out the user.
        LoginManager.getInstance().logOut();
        callbackManager = CallbackManager.Factory.create();
        accessToken = AccessToken.getCurrentAccessToken();
    }

    private void publishOnMyFeed(){
        // Gets the current profile Logged in
        Profile pt = Profile.getCurrentProfile();

        // Creates a content to save a message. There are a lot of content types
        // This one, is to share a link. In that case, the link of yout facebook profile
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(pt.getLinkUri())
                .setContentDescription("Hello, I am the best!!!")
                .build();

        // Creates a bitmap image
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.meudrawable);
        // Creates an object with the bitmap image
        // This is important, because in the next step, the content receives only one SharePhoto and not a bitmap
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .build();

        // The content to share the photo
        SharePhotoContent photoContent = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        // For this example: ShareDialog.show(this, content); -> shares the link above
        // ShareDialog.show(this, photoContent); -> shares the photo
        // This is one way to publish.
        ShareDialog.show(this, photoContent);

        // The other way is direct
        // other way:
        // ShareApi.share(content, null);

    }

    private void getMyInformation(){
        // This will get the information about the user logged in
        // HARD WAY
        // This is with a Json Object.
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        // Application code
                        JSONArray jsonArray = response.getJSONArray();
                        try {
                            String name = object.getString("name");
                            String id = object.getString("id");
                            String link = object.getString("link");
                            TextView tv = (TextView) findViewById(R.id.info_user);
                            tv.setText("Nome: " + name + "\nID: " + id + "\nLink: " + link);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void LoginClick(View view) {
        // 1- Login Manager
        //  This class manages login and permissions for Facebook.
        // 2 - LogInWithReadPermissions
        //  Logs the user in with the requested read permissions.
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile, user_posts "));
        // Registers a login callback to the given callback manager.
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            // This method is when you have success login or logout
            @Override
            public void onSuccess(LoginResult loginResult) {

                //publishOnMyFeed();
                accessToken = AccessToken.getCurrentAccessToken();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void getPostsFromMyFeed(){
        // This will get all the posts from your feed
        // See the result in log cat
        // There is the need to handle with the JSON object
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            /* handle the result */
                        Log.i("fb", "Feeds :" +response.getJSONObject());
                    }
                }
        ).executeAsync();
    }

    private void getMyInformationByProfile(){
        // Get the information about the user logged in in the easiest way
        Profile profile = Profile.getCurrentProfile();
        TextView tv = (TextView)findViewById(R.id.info_user);
        tv.setText("Nome: " + profile.getName() + "\nID: " + profile.getId() + "\nLink: " + profile.getLinkUri().toString());

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(profile.getLinkUri())
                .setContentDescription("Olá, criei uma aplicação brutal mas não percebi nada do que me explicaram!!!")
                .build();

        MessageDialog.show(this, content);
        //ShareApi.share(content, null);
    }

    public void InfoUser(View view) {
        if(accessToken == null) {
            TextView tv = (TextView)findViewById(R.id.info_user);
            tv.setText("[ERRO] - Access Token Inválido");
            return;
        }

        getMyInformation();
        //getMyInformationByProfile();

    }
}

