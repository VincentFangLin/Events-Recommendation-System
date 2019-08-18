package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicketMasterAPI {

	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final String API_KEY = "t1Z0pEo75PM8418p6Euf34yIBtRHjbuP";

	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword, "UTF-8"); // "R S" => "R%20S"
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);

		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash, keyword, 50);

		String url = URL + "?" + query;

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();
			System.out.println("send request to url: " + url);
			System.out.println("Response code: " + responseCode);

			if (responseCode != 200) {
				System.out.println("error status code is " + responseCode);
				return new ArrayList<>();
			}

			// read input stream (use InputStreamReader class and BufferedReader, save in a
			// sb)
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();

			JSONObject object = new JSONObject(builder.toString());

			if (!object.isNull("_embedded")) {
				JSONObject embeddedJsonObject = object.getJSONObject("_embedded");
				return getItemList(embeddedJsonObject.getJSONArray("events"));
			}

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return new ArrayList<>();

	}

	private List<Item> getItemList(JSONArray events) throws JSONException {
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < events.length(); i++) {
			JSONObject event = events.getJSONObject(i);

			ItemBuilder builder = new ItemBuilder();
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			builder.setAddress(getAddress(event));
			builder.setImageUrl(getImageUrl(event));
			builder.setCategories(getCategories(event));

			items.add(builder.build());
		}

		return items;
	}

	private String getAddress(JSONObject event) throws JSONException {
		if (!event.isNull("_embedded")) {
			JSONObject embedded = event.getJSONObject("_embedded");
			if (!embedded.isNull("venues")) {
				JSONArray venues = embedded.getJSONArray("venues");
				for (int i = 0; i < venues.length(); ++i) {
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder builder = new StringBuilder();
					if (!venue.isNull("address")) {
						JSONObject address = venue.getJSONObject("address");
						if (!address.isNull("line1")) {
							builder.append(address.getString("line1"));
						}
						if (!address.isNull("line2")) {
							builder.append(" ,");
							builder.append(address.getString("line2"));
						}
						if (!address.isNull("line3")) {
							builder.append(" ,");
							builder.append(address.getString("line3"));
						}
					}
					if (!venue.isNull("city")) {
						JSONObject city = venue.getJSONObject("city");
						if (!city.isNull("name")) {
							builder.append(" ,");
							builder.append(city.getString("name"));
						}
					}
					if (!venue.isNull("state")) {
						JSONObject state = venue.getJSONObject("state");
						if (!state.isNull("name")) {
							builder.append(" ,");
							builder.append(state.getString("name"));
						}
					}
					if (!venue.isNull("country")) {
						JSONObject country = venue.getJSONObject("country");
						if (!country.isNull("name")) {
							builder.append(" ,");
							builder.append(country.getString("name"));
						}
					}
					String addressStr = builder.toString();
					if (!addressStr.isEmpty()) {
						return addressStr;
					}

				}
			}
		}
		return "";
	}

	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images");
			for (int i = 0; i < array.length(); i++) {
				JSONObject image = array.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}

	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<>();
		if (!event.isNull("classifications")) {
			JSONArray classifications = event.getJSONArray("classifications");
			for (int i = 0; i < classifications.length(); i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull("segment")) {
					JSONObject segment = classification.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}

	private void queryAPI(double lat, double lon) {
		List<Item> events = search(lat, lon, null);

		try {
			for (Item event : events) {
				System.out.println(event.toJsonObject());
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		tmApi.queryAPI(37.38, -122.08); // Mountain View, CA
	}

}
