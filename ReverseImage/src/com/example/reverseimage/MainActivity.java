package com.example.reverseimage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
public class MainActivity extends Activity {


	private static final String apiKey = "0287787c683b3dab54d5f3eb8b0d8cb0"; //$NON-NLS-1$
	public static final String sharedSecret = "bd121e52304f9dd4"; //$NON-NLS-1$
	public static final String FLICKR_POST_URL= "http://api.flickr.com/services/upload/";

	private static final int CAMERA_PIC_REQUEST = 1337;  


	static String restHost = "www.flickr.com";
	Flickr flickr;
	REST rest;
	String token;	// Generated via authentication process, for tracking the session and making signed calls
	RequestContext requestContext;	// Used for making signed calls to Flickr



	//	
	//
	//
	//	Flickr f;
	//	RequestContext requestContext;
	//	String frob = "";
	//	String token = "";
	//	Properties properties = null;
	//
	//


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onClickBtn(View v)
	{
		Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);  
	} 	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAMERA_PIC_REQUEST) {  
			// do something  
			Bitmap thumbnail = (Bitmap) data.getExtras().get("data");  

			ImageView image = (ImageView) findViewById(R.id.imageView1);  
			image.setImageBitmap(thumbnail);
			Log.i("Main", "Start Auth");
			try {
				auth();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}  
	}


	private void auth() {


		REST rest = new REST();
		rest.setHost(restHost);
		Flickr flickr = new Flickr (apiKey, rest);

		/* Flickr requires the shared secret for any signed calls. RequestContext will store and send it. */
		requestContext = RequestContext.getRequestContext();
		requestContext.setSharedSecret(sharedSecret);

		/* To authorize this application, generate an authentication URL and direct the user to it. */
		// First, generate a frob...
		AuthInterface authInterface = flickr.getAuthInterface();
		String firstFrob = "";
		try {
			firstFrob = authInterface.getFrob();
		} catch(FlickrException e) {
			e.printStackTrace();
		}
		/* ... then generate a URL with that frob, asking for a certain level of permissions ...
		 *    -- Permission.READ     (private photos)
		 *    -- Permission.WRITE    (edit info and photos; upload photos)
		 *    -- Permission.DELETE   (photos)
		 */
		URL url = authInterface.buildAuthenticationUrl(Permission.WRITE, firstFrob);
		// ... and direct the user to it.
		System.out.println("Authorize this application at the following URL:");
		System.out.println(url.toExternalForm());   // Make sure to use URL.toExternalForm()!

		// =================================================================================
		// If this were a real webapp, you would break into another servlet after this point
		// =================================================================================

		/* After the user authorizes the application, s/he will be redirected to the
		 * callback URL associated with the API key for the application; the URL
		 * will include a rehashed frob that the application can use for authentication.
		 * If this were a real web application, it would fetch the frob itself from that page.
		 * This demo merely prompts the user to fetch it for us.
		 */
		System.out.println("After you were redirected, what was the frob? (Check your URL: ...?frob=XXX)");
		BufferedReader userInput = new BufferedReader( new InputStreamReader(System.in));
		String fetchedFrob = userInput.readLine();

		/* This is the last bit of the authentication process.
		 * Take the frob that Flickr gave back to us and use it
		 * to generate an Auth(enticated widget).
		 */
		Auth auth;
		try {
			auth = authInterface.getToken(fetchedFrob);
			System.out.println("Authentication success");
		} catch(FlickrException e) {
			System.out.println("Authentication failed");
			e.printStackTrace();
		}

		/* That frob was only valid for one authentication.  The token is good for
		 * making new Flickr's in a single session.
		 * I don't really know how long a session lasts -- perhaps forever.
		 * To maintain the session, keep the token in memory: auth.getToken()
		 */
		System.out.println("Token: "+auth.getToken());
		System.out.println("nsid: "+auth.getUser().getId());
		/* In addition to authorizing the application, the authentication process
		 * gives us quick access to who is the logged-in user.
		 */
		System.out.println("Realname: "+auth.getUser().getRealName());
		System.out.println("Username: "+auth.getUser().getUsername());
		/* Flickr has three tiers of permissions:
		 *  -- read (private photos)
		 *  -- write (edit info and photos; upload photos)
		 *  -- delete (photos)
		 */
		System.out.println("Permission: "+auth.getPermission().getType());

		// =================================================================================
		// Use the token to create another Flickr object with this authentication
		// =================================================================================
		Flickr sameSession = new Flickr ();
		sameSession.getAuth().setToken(flickr.getAuth().getToken());











	}




	//	private void auth() throws ParserConfigurationException, IOException, SAXException {
	//
	//		InputStream in = null;
	//		try {
	//			in = getClass().getResourceAsStream("/setup.properties");
	//			properties = new Properties();
	//			properties.load(in);
	//		} finally {
	//			IOUtilities.close(in);
	//		}
	//		f = new Flickr(
	//				properties.getProperty(API_KEY),
	//				properties.getProperty(API_SEC),
	//				new REST()
	//				);
	//		Flickr.debugStream = false;
	//		requestContext = RequestContext.getRequestContext();
	//		AuthInterface authInterface = f.getAuthInterface();
	//		try {
	//			frob = authInterface.getFrob();
	//		} catch (FlickrException e) {
	//			e.printStackTrace();
	//		}
	//		Log.i("Main", "Start Auth");
	//		
	//		
	//		
	//		Log.i("Custom             :", ("frob: " + frob));
	//		URL url = authInterface.buildAuthenticationUrl(Permission.DELETE, frob);
	//		Log.i("Custom             :","Press return after you granted access at this URL:");
	//		Log.i("Custom             :",url.toExternalForm());
	//		BufferedReader infile =
	//				new BufferedReader ( new InputStreamReader (System.in) );
	//		String line = infile.readLine();
	//		try {
	//			Auth auth = authInterface.getToken(frob);
	//			Log.i("Custom             :","Authentication success");
	//			// This token can be used until the user revokes it.
	//			Log.i("Custom             :","Token: " + auth.getToken());
	//			Log.i("Custom             :","nsid: " + auth.getUser().getId());
	//			Log.i("Custom             :","Realname: " + auth.getUser().getRealName());
	//			Log.i("Custom             :","Username: " + auth.getUser().getUsername());
	//			Log.i("Custom             :","Permission: " + auth.getPermission().getType());
	//		} catch (FlickrException e) {
	//			Log.i("Custom             :","Authentication failed");
	//			e.printStackTrace();
	//		}
	//
	//
	//
	//	} 
}






