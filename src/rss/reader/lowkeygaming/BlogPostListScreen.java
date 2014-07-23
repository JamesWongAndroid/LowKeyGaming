package rss.reader.lowkeygaming;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardListView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

public class BlogPostListScreen extends Activity {
	
	CardListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_post_list_screen);
		listView = (CardListView) findViewById(R.id.myList);
		retreivePostsTask retreiveTask = new retreivePostsTask();
		retreiveTask.execute();
			
	}
	
	private class retreivePostsTask extends AsyncTask<Void, Void, Void> {

		String response = null;
		
		@Override
		protected Void doInBackground(Void... params) {
			HttpURLConnection connection;
		    OutputStreamWriter request = null;
		    URL url = null;
		    
		    
		    try {
		    	
		    	url = new URL("http://www.lowkeygaming.com/?json=1&count=50");
					connection = (HttpURLConnection) url.openConnection();
		            connection.setDoOutput(true);
		            connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
		            connection.setRequestMethod("POST");  
		            request = new OutputStreamWriter(connection.getOutputStream());
		            request.flush();
		            request.close();
		            
		            String line = "";               
		            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
		            BufferedReader reader = new BufferedReader(isr);
		            StringBuilder sb = new StringBuilder();
		            while ((line = reader.readLine()) != null)
		            {
		                sb.append(line + "\n");
		            }
		            
		            response = sb.toString().replaceAll("&#8217;", "'").replaceAll("&#8211;", "-").replaceAll("&#8230;", "...").replaceAll("&#038;", "&");
		            
		            // You can perform UI operations here
		       //     Toast.makeText(this,"Message from Server: \n"+ response, 0).show();             
		            isr.close();
		            reader.close();
		            
				
			} catch (Exception e) {
				
			}
		    
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			
			ArrayList<Card> cards = new ArrayList<Card>();
			
			try {
				JSONArray jPosts = new JSONObject(response).getJSONArray("posts");
				for (int i = 0; i < jPosts.length(); i++) {
					JSONObject jsonArticle = jPosts.getJSONObject(i);
					JSONObject jThumbnails = jsonArticle.getJSONObject("thumbnail_images").getJSONObject("medium");
					
					String articleTitle = jsonArticle.getString("title_plain");
				//	String imageURL = jsonArticle.getString("thumbnail");
					String imageURL = jThumbnails.getString("url");
					String articleExcerpt = jsonArticle.getString("excerpt");
					String test = "test";
					Card articleCard = new Card(getApplicationContext());
					
					CardThumbnail cardBackground = new CardThumbnail(getApplicationContext());
					cardBackground.setUrlResource(imageURL);
				//	ViewHelper.setAlpha(cardBackground, 0.55f);
				//	cardBackground.setDrawableResource(R.drawable.ic_launcher);
					CardHeader header = new CardHeader(getApplicationContext());
					header.setTitle(articleTitle);
					//articleCard.setTitle(articleExcerpt);
					articleCard.addCardThumbnail(cardBackground);
					articleCard.addCardHeader(header);
					
					
					cards.add(articleCard);
				}
				
				String test = "test";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			CardArrayAdapter cardArrayAdapter = new CardArrayAdapter(getApplicationContext(), cards);
			if (listView != null) {
				listView.setAdapter(cardArrayAdapter);
			}
			
		}
		
		
		
	}
	
}
