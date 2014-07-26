package com.lowkeygaming.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import rss.reader.lowkeygaming.R;

import com.koushikdutta.ion.Ion;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.TextView;

public class ArticleScreen extends FragmentActivity {

	private TextView contentTextView, titleTextView;
	private ImageView articleImage;
	private RetreiveArticleTask retreiveArticleTask;
	private Bundle extras;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article_screen);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#f39c12")));
		actionBar.setIcon(R.drawable.lowkeyiconhalf);

		extras = getIntent().getExtras();
		String postID = extras.getString("articleID");

		retreiveArticleTask = new RetreiveArticleTask();
		retreiveArticleTask.execute(postID);

		contentTextView = (TextView) findViewById(R.id.articleText);
		titleTextView = (TextView) findViewById(R.id.titleText);
		
		articleImage = (ImageView) findViewById(R.id.articlePicture);

	}

	@Override
	public void onBackPressed() {
		if (retreiveArticleTask.getStatus() == AsyncTask.Status.RUNNING) {
			retreiveArticleTask.cancel(true);
		}

		Intent closeArticle = new Intent(getApplicationContext(),
				BlogPostListScreen.class);
		extras.clear();
		startActivity(closeArticle);
		finish();
	}

	private class RetreiveArticleTask extends AsyncTask<String, Void, Void> {

		String response = null;

		@Override
		protected Void doInBackground(String... params) {
			HttpURLConnection connection;
			OutputStreamWriter request = null;
			URL url = null;
			String inputPostID = params[0];

			try {

				url = new URL(
						"http://www.lowkeygaming.com/?json=get_post&post_id="
								+ inputPostID);
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

				response = sb.toString();
				isr.close();
				reader.close();

			} catch (Exception e) {

			}

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {

			try {
				JSONObject jArticleObject = new JSONObject(response).getJSONObject("post");
				String titleContent = filterJson(jArticleObject.getString("title"));
				String articleContent = filterJson(jArticleObject.getString("content"));
			
				int indexOfGoogle = articleContent.indexOf("google_ad_client");
				String articleContentNoAds = articleContent.replace(
						articleContent.substring(indexOfGoogle,
								articleContent.length()), "");
				
				JSONObject jThumbnails = jArticleObject.getJSONObject(
						"thumbnail_images").getJSONObject("large");
				
				String imageURL = jThumbnails.getString("url");
				
				Ion.with(getApplicationContext()).load(imageURL).withBitmap().resize(800, 400).intoImageView(articleImage);
				
				
				titleTextView.setText(titleContent);
				contentTextView.setText(articleContentNoAds);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	protected String filterJson(String jsonString) {
		return jsonString.replaceAll("\n", "\n \n").replaceAll("\\<.*?>", "").replaceAll("&#8217;", "'")
				.replaceAll("&#8211;", "-").replaceAll("&#8242;", "'")
				.replaceAll("&#8230;", "...").replaceAll("&#038;", "&")
				.replaceAll("&nbsp;", "").replaceAll("&#8220;", "").replaceAll("&#8221;", "");
	}
}
