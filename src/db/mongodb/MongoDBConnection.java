package db.mongodb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.eq;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MongoDBConnection implements DBConnection {

	private MongoClient mongoClient;
	private MongoDatabase db;

	public MongoDBConnection() {
		// Connects to local mongodb server.
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection error");
			return;
		}
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$push", new Document("favorite", new Document("$each", itemIds))));
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection error");
			return;
		}
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$pullAll", new Document("favorite", itemIds)));
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection error");
			return new HashSet<>();
		}
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		Set<String> favoriteIds = new HashSet<>();

		if (iterable.first() != null && iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteIds.addAll(list);
		}
		return favoriteIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection error");
			return new HashSet<>();
		}

		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);

		for (String itemId : itemIds) {
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
			if (iterable.first() != null) {
				Document doc = iterable.first();

				ItemBuilder builder = new ItemBuilder();
				builder.setItemId(doc.getString("item_id"));
				builder.setName(doc.getString("name"));
				builder.setRating(doc.getDouble("rating"));
				builder.setAddress(doc.getString("address"));
				builder.setCategories(getCategories(itemId));
				builder.setImageUrl(doc.getString("image_url"));
				builder.setUrl(doc.getString("url"));

				favoriteItems.add(builder.build());
			}
		}

		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection error");
			return new HashSet<>();
		}
		Set<String> categories = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
		if (iterable.first() != null) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("categories");
			categories.addAll(list);
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// TODO Auto-generated method stub
		TicketMasterAPI ticketMasterAPI = new TicketMasterAPI();
		List<Item> items = ticketMasterAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		// TODO Auto-generated method stub
		// Step1 check if the item already exists
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", item.getItemId()));
		// Step2 save if not exists
		if (iterable.first() == null) {
			db.getCollection("items")
					.insertOne(new Document().append("item_id", item.getItemId()).append("name", item.getName())
							.append("rating", item.getRating()).append("address", item.getAddress())
							.append("categories", item.getCategories()).append("image_url", item.getImageUrl())
							.append("url", item.getUrl()));
		}

	}

	@Override
	public String getFullname(String userId) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection error");
			return "";
		}
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null) {
			return iterable.first().getString("first_name") + " " + iterable.first().getString("last_name");
		}
		return "";
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection error");
			return false;
		}
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null) {
			Document doc = iterable.first();
			return doc.getString("password").equals(password);
		}
		return false;
	}

	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		// TODO Auto-generated method stub
		if (mongoClient == null) {
			System.err.println("DB connection error");
			return false;
		}
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() == null) {
			db.getCollection("users").insertOne(new Document().append("user_id", userId).append("password", password)
					.append("first_name", firstname).append("last_name", lastname));
			return true;
		}
		return false;

	}

}
