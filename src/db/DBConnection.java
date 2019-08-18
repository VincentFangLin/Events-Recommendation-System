package db;

import java.util.List;
import java.util.Set;

import entity.Item;

public interface DBConnection {
	/**
	 * Close the connection
	 */
	public void close();

	/**
	 * Insert the favorite items for a user
	 * 
	 * @param userId
	 * @param itemIds
	 */
	public void setFavoriteItems(String userId, List<String> itemIds);

	/**
	 * Delete the favorite items for a user
	 * 
	 * @param userId
	 * @param itemIds
	 */
	public void unsetFavoriteItems(String userId, List<String> itemIds);

	/**
	 * Get the favorite item id for a user
	 * 
	 * @param userId
	 * @return set of item ids
	 */
	public Set<String> getFavoriteItemIds(String userId);

	/**
	 * Get the favorite items for a user
	 * 
	 * @param userId
	 * @return set of items
	 */
	public Set<Item> getFavoriteItems(String userId);

	/**
	 * Gets categories based on item id
	 * 
	 * @param itemId
	 * @return set of categories
	 */
	public Set<String> getCategories(String itemId);

	/**
	 * Search items near a geolocation and a term(optional)
	 * 
	 * @param lat
	 * @param lon
	 * @param term
	 * @return
	 */
	public List<Item> searchItems(double lat, double lon, String term);

	/**
	 * Save item into db
	 * 
	 * @param item
	 */
	public void saveItem(Item item);

	/**
	 * Get full name of a user.(This is for demo and extension.)
	 * 
	 * @param userId
	 * @return
	 */
	public String getFullname(String userId);

	/**
	 * Return whether the credential is correct.
	 * 
	 * @param userId
	 * @param password
	 * @return
	 */
	public boolean verifyLogin(String userId, String password);

	/**
	 * Register one user
	 * 
	 * @param userId
	 * @param password
	 * @param firstname
	 * @param lastname
	 * @return boolean
	 */
	public boolean registerUser(String userId, String password, String firstname, String lastname);

}
