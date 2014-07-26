package com.lowkeygaming.app;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
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

import rss.reader.lowkeygaming.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class BlogPostListScreen extends Activity {

	private CardListView listView;
	private Card articleCard;
	private CardArrayAdapter cardArrayAdapter;
	private ArrayList<Card> cards;
	private OnCardClickListener cardClicks;
	private boolean isloading = false;
	private int pageCount = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blog_post_list_screen);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#f39c12")));
		actionBar.setIcon(R.drawable.lowkeyicon);

		listView = (CardListView) findViewById(R.id.myList);

		cardClicks = new OnCardClickListener() {

			@Override
			public void onClick(Card c, View v) {
				String postID = c.getId();
				Intent toArticleScreen = new Intent(getApplicationContext(),
						ArticleScreen.class);
				toArticleScreen.putExtra("articleID", postID);
				startActivity(toArticleScreen);
				finish();

			}
		};

		retreivePostsTask retreiveTask = new retreivePostsTask();
		retreiveTask.execute();

	}

	private class retreivePostsTask extends AsyncTask<Void, Void, Void> {

		String response = null;
		JSONObject jsonArticle;

		@Override
		protected Void doInBackground(Void... params) {
			HttpURLConnection connection;
			OutputStreamWriter request = null;
			URL url = null;

			try {
				isloading = true;
				url = new URL(
						"http://www.lowkeygaming.com/?json=1&count=10&page="
								+ pageCount);
				connection = (HttpURLConnection) url.openConnection();
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type",
						"text/plain; charset=utf-8");
				connection.setRequestMethod("POST");
				request = new OutputStreamWriter(connection.getOutputStream());
				request.flush();
				request.close();

				String line = "";
				InputStreamReader isr = new InputStreamReader(
						connection.getInputStream());
				BufferedReader reader = new BufferedReader(isr);
				StringBuilder sb = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				response = sb.toString().replaceAll("&#8217;", "'")
						.replaceAll("&#8211;", "-")
						.replaceAll("&#8230;", "...").replaceAll("&#038;", "&").replaceAll("&#8220;", "").replaceAll("&#8221;", "").replaceAll("&nbsp;", "");

				// You can perform UI operations here
				// Toast.makeText(this,"Message from Server: \n"+ response,
				// 0).show();
				isr.close();
				reader.close();

			} catch (Exception e) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			isloading = false;
			cards = new ArrayList<Card>();

			try {
				JSONArray jPosts = new JSONObject(response)
						.getJSONArray("posts");
				for (int i = 0; i < jPosts.length(); i++) {
					jsonArticle = jPosts.getJSONObject(i);
					JSONObject jThumbnails = jsonArticle.getJSONObject(
							"thumbnail_images").getJSONObject("medium");

					String articleTitle = jsonArticle.getString("title_plain");
					String imageURL = jThumbnails.getString("url");

					articleCard = new Card(getApplicationContext());

					CardThumbnail cardBackground = new CardThumbnail(
							getApplicationContext());
					cardBackground.setUrlResource(imageURL);
					CardHeader header = new CardHeader(getApplicationContext());
					header.setTitle(articleTitle);
					articleCard.addCardThumbnail(cardBackground);
					articleCard.addCardHeader(header);
					String postID = jsonArticle.getString("id");
					articleCard.setId(postID);
					articleCard.setOnClickListener(cardClicks);

					cards.add(articleCard);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			cardArrayAdapter = new CardArrayAdapter(getApplicationContext(),
					cards);
			if (listView != null) {
				
				SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(cardArrayAdapter);
				swingBottomInAnimationAdapter.setAbsListView(listView);
				listView.setExternalAdapter(swingBottomInAnimationAdapter, cardArrayAdapter);
				
		/*		AnimationAdapter animCardArrayAdapter = new AlphaInAnimationAdapter(cardArrayAdapter);
		        animCardArrayAdapter.setAbsListView(listView);
		        listView.setExternalAdapter(animCardArrayAdapter, cardArrayAdapter); */
		        
				listView.setItemsCanFocus(true);
				 
				listView.setOnScrollListener(new OnScrollListener() {

					@Override
					public void onScrollStateChanged(AbsListView view,
							int scrollState) {

					}
					
					

					@Override
					public void onScroll(AbsListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						int loadedItems = firstVisibleItem + visibleItemCount;
						if ((loadedItems == totalItemCount) && !isloading) {
							
							LoadPostTask loadPosttask = new LoadPostTask();
							loadPosttask.execute();
						} 

					}
				});

			}

		}
	}
	
	private class LoadPostTask extends AsyncTask<Void, Void, Void> {

		String response = null;
		JSONObject jsonArticle;
		
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			HttpURLConnection connection;
			OutputStreamWriter request = null;
			URL url = null;
			if (!isloading) {
				try {
					isloading = true;
					pageCount++;
					url = new URL(
							"http://www.lowkeygaming.com/?json=1&count=10&page="
									+ pageCount);
					connection = (HttpURLConnection) url.openConnection();
					connection.setDoOutput(true);
					connection.setRequestProperty("Content-Type",
							"text/plain; charset=utf-8");
					connection.setRequestMethod("POST");
					request = new OutputStreamWriter(connection.getOutputStream());
					request.flush();
					request.close();

					String line = "";
					InputStreamReader isr = new InputStreamReader(
							connection.getInputStream());
					BufferedReader reader = new BufferedReader(isr);
					StringBuilder sb = new StringBuilder();
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}

					response = sb.toString().replaceAll("&#8217;", "'")
							.replaceAll("&#8211;", "-")
							.replaceAll("&#8230;", "...").replaceAll("&#038;", "&").replaceAll("&#8220;", "").replaceAll("&#8221;", "").replaceAll("&nbsp;", "");
					
					isr.close();
					reader.close();

				} catch (Exception e) {

				}
				
				try {
					JSONArray jPosts = new JSONObject(response)
							.getJSONArray("posts");
					for (int i = 0; i < jPosts.length(); i++) {
						jsonArticle = jPosts.getJSONObject(i);
						JSONObject jThumbnails = jsonArticle.getJSONObject(
								"thumbnail_images").getJSONObject("medium");

						String articleTitle = jsonArticle.getString("title_plain");
						String imageURL = jThumbnails.getString("url");

						articleCard = new Card(getApplicationContext());

						CardThumbnail cardBackground = new CardThumbnail(
								getApplicationContext());
						cardBackground.setUrlResource(imageURL);
						CardHeader header = new CardHeader(getApplicationContext());
						header.setTitle(articleTitle);
						articleCard.addCardThumbnail(cardBackground);
						articleCard.addCardHeader(header);
						String postID = jsonArticle.getString("id");
						articleCard.setId(postID);
						articleCard.setOnClickListener(cardClicks);

						cards.add(articleCard);
						
						
					}
					
					

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			

			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			cardArrayAdapter.notifyDataSetChanged();
			isloading = false;
		}
		
	}

}
