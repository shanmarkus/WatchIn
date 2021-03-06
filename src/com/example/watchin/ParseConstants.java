package com.example.watchin;

public final class ParseConstants {

	// Class name
	public static final String TABLE_USER = "_User";
	public static final String TABLE_PLACE = "Place";
	public static final String TABLE_ITEM = "Item";
	public static final String TABLE_ITEM_REVIEW = "ItemReview";
	public static final String TABLE_ITEM_LOVED = "ItemLoved";
	public static final String TABLE_PROMOTION = "Promotion";
	public static final String TABLE_PROMOTION_CATEGORY = "PromotionCategory";
	public static final String TABLE_PROMOTION_QUOTA = "PromotionQuota";

	// User Activity class
	public static final String TABLE_ACTV_USER_CLAIM_PROMOTION = "Activity_User_Claim_Promotion";
	public static final String TABLE_ACTV_USER_CHECK_IN_PLACE = "Activity_User_Check_In_Place";

	// Relational Class Name
	public static final String TABLE_REL_PLACE_ITEM = "Rel_Place_Item";
	public static final String TABLE_REL_PROMOTION_PLACE = "Rel_Promotion_Place";
	public static final String TABLE_REL_USER_USER = "Rel_User_User";
	public static final String TABLE_REL_USER_REWARD = "Rel_User_Reward";

	// Field names
	public static final String KEY_USERNAME = "username";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_NAME = "name";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_OBJECT_ID = "objectId";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_RATING = "rating";
	public static final String KEY_ITEM_ID = "itemId";
	public static final String KEY_PLACE_ID = "placeId";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_USER_ID = "userId";
	public static final String KEY_FOLLOWING_ID = "followingId";
	public static final String KEY_REVIEW = "review";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_CATEGORY_ID = "categoryId";
	public static final String KEY_PROMOTION_ID = "promotionId";
	public static final String KEY_CREATED_AT = "createdAt";
	public static final String KEY_REQUIREMENT = "requirement";
	public static final String KEY_START_DATE = "startDate";
	public static final String KEY_END_DATE = "endDate";
	public static final String KEY_CLAIMABLE = "claimable";
	public static final String KEY_REWARD_POINT = "rewardPoint";
	public static final String KEY_QUOTA = "quota";
	public static final String KEY_TOTAL_CHECK_IN = "totalCheckIn";
	public static final String KEY_TOTAL_CLAIMED = "totalClaimed";
	public static final String KEY_LIST_PROMOTION = "listPromotion";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_OPERATIONAL_HOUR = "operationalHour";
	public static final String KEY_TOTAL_LOVED = "totalLoved";
	public static final String KEY_FOLLOWER = "follower";
	public static final String KEY_FOLLOWING = "following";
	public static final String KEY_IS_CLAIMED = "isClaimed";
	public static final String KEY_TOTAL_CLAIMED_PROMOTION = "totalClaimedPromotion";

	// Relational names
	public static final String KEY_RELATION_ITEMS_CATALOG = "itemsCatalog";

	// Pointer names
	public static final String KEY_POINTER_ITEM = "item";

}
