package SOA.CW3;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.api.UsersResources;
import twitter4j.auth.AccessToken;

/**
 * 
 * @author Shashidar Ette : se146
 * 
 * TweetAnalysis.java - This is implemented as singleton abstracts the communication with Twitter4j and its API
 * Overall it provides following functions: (more details as part of each function)
 *    public int getNumberOfFollowers(String userName)
 *    public int getNumberOfTweets(String userName)
 *    public int getNumberOfRetweets(String userName)
 *    public String getMostActiveFollower(String userName)
 *    public int getMostActiveFollowerContribution()
 *    
 * APIs used for analysis purpose:
 * UsersResources users() 
 *    - This is used to get UserResources from twitter factory.
 * 
 * Interface UsersResources
 * User	showUser(long userId) 
 *    - This is used to get the user from user id.
 *    - Used to get User object from active follower id
 * User	showUser(java.lang.String screenName)
 *    - Used to get the User object for user under consideration of the analysis
 * 
 * Interface User
 * int	getFollowersCount() 
 *    - To get number of followers. 
 *    - Optimized operation instead of collecting followerIds in a list and get the count.
 * int	getStatusesCount() 
 *    - To get number of tweets in the users time line.
 * long	getId()
 *    - get the id of the user
 * java.lang.String	getScreenName()
 *    - get the screen name of the user, used to get the screen name from active follower user id.
 * 
 * 
 * Interface TweetsResources
 * IDs	getRetweeterIds(long statusId, long cursor)
 * IDs getRetweeterIds(long statusId, int count, long cursor)
 *     - Used to get retweeter IDs for each of the tweet present in user's status time line.
 *     - IDs.getNextCursor is used to iterate through the whole list of retweeter ids.
 *     - second variant is an optimized way allows a max. of 200 retweeter ids
 *       instead of 100 of the first one.
 *     
 * Interface IDs
 * long[]	getIDs() 
 *      - gets array of long of the objects under consideration from IDs
 * long	getNextCursor() 
 *      - Used to retrieve whole of the objects in list under consideration.
 *      - For ex: getFollowersIDs(), getRetweeterIDs().
 *      
 * Interface TimelinesResources
 * ResponseList<Status>	getUserTimeline(long userId, Paging paging)
 *      - Used to get whole of the user's status time line.
 *      - Used for retweet count and active follower analysis operation.
 *      - Paging mechanism is used to retrieve the information in chunks.
 *      - PageSize considered to 200 entries per opage.
 * 
 * Interface Status
 * boolean	isRetweet() 
 *      - Used for retweets count, to know whether a status is retweet by the user.
 * boolean	isRetweetedByMe()
 *      - same as above.
 * int	getRetweetCount()
 *      - used as a check if a tweet ids needs to be added in the tweetIds list.
 *      
 * Interface FriendsFollowersResources
 * IDs	getFollowersIDs(long userId, long cursor)
 * IDs getFollowersIDs(long userId, long cursor, int count)
 *      - used to get list of follower ids of the user.
 *      - cursor mechanism as descived for IDs interface to get the infromation
 *      iteratively.
 *      - second variant is an optimized way and 
 *      allows a max. of 5000 IDs instead of uncertain 5000 IDs of the first one.
 * 
 * Interface HelpResources
 * Map<java.lang.String,RateLimitStatus> getRateLimitStatus(java.lang.String... resources)
 *      - as part of the analysis, getFollowerIds and getRetweeterIds api calls are most 
 *      bulk used. So there are changes of reaching their rate limits. 
 *      Used this apis to check those limits and warn the user.
 *           
 * Relationship	showFriendship(long sourceId, long targetId)
 *     - Get the relationship between two user ids.
 *     - Used for calculation of activeFollower, to get the relationship between user under focus and
 *     retweeter id
 *     
 * Interface Relationship
 * boolean	isSourceFollowedByTarget()
 *     - This is used to check, indeed the source user is followed by target.
 *     - As part of the course-work 3, making this call turned to be expensive in time.
 *     So used the APIs to generate followerIds list for the user. 
 *     Use the list as basis to check whether the retweeter is indeed a follower of a user.
 *
 */
public class TweetAnalysis {
	// Twitter API constants for communication
	// Twitter Consumer Key
	private final String consumerKey = "2y7gH8p271emSIsnhiIYbLcSs";
	
	// Twitter Consumer Secret
	private final String consumerSecret = "9mpBwrQOh8HadsbJSTcmQ96XK5EorkzqdiAe3Uc6VP0fdX84I5";
	
	// Twitter Access Token
	private final String accessToken = "839072503641288704-bBMB139ApFTxeMRdNu6eBeU8DXEySQK";
	
	// Twitter Access Token Secret
	private final String accessTokenSecret = "lAuOEFMTqKqAnm3h6i1W2aPaiRPJRx2HtdLlqCZbhpCnd";
	
	// Maximum number of contributions by active Follower
	private int activeFollowerContribution;
	
	// maximum size of the status per page
	private final int pageSize = 5000;
	
	// maximum number of result count per request
	private final int retweeterIDsResultCount = 200;
	private final int followerIDsResultCount = 200;
	
	// Twitter instance
	private Twitter twitter;
	
	// Tweets Time Line - cached considering it's an expensive operation
	// to get the tweets status time line
	private List<Status> tweetsTimeLine;
	private String userNameOfTweetsTimeLine;
	
	// Singleton instance of TweetAnalysis
	private static TweetAnalysis instance = null;
	
	// Private constructor of TweetAnalysis
	private TweetAnalysis() {
		intializeTwitterInstance();
	}
	
	// Singleton class
	public static TweetAnalysis getTweetAnalysisInstance() {
		if (instance == null) {
			instance = new TweetAnalysis();
		}
		return instance;
	}
	
	/*
	 * Initializes twitter factory and sets authentication details
	 */
	public Twitter intializeTwitterInstance() {
		if (twitter == null) {
			// Create thread-safe factory
			TwitterFactory twitterFactory = new TwitterFactory();

			// Create new Twitter instance
			twitter = twitterFactory.getInstance();

			// setup OAuth Consumer Credentials
			twitter.setOAuthConsumer(consumerKey, consumerSecret);

			// setup OAuth Access Token
			twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));
		}
		return twitter;
	}
	
	// Resets the cached tweetsTimeLine and the user name of the tweets time line
	public void reset() {
		tweetsTimeLine = null;
		userNameOfTweetsTimeLine = "";
		activeFollowerContribution = 0;
	}
	
	/**
	 * This is first of the analysis operations in course-work 3.
	 * Gets number of followers of the given user
	 * @param userName
	 * @return the number of followers count
	 * @throws TwitterException
	 */
	public int getNumberOfFollowers(String userName) throws TwitterException {				
		try {
			// Twitter User resources
			UsersResources userResources = twitter.users();
			
			// UserResources.showUser(userName) is used to retrieve the User object
			User userUnderFocus = userResources.showUser(userName);
			
			// User.getFollowersCount returns the number of followers of a user
			return userUnderFocus.getFollowersCount();
		} catch (TwitterException tex) {
			throw tex;
		}
	}

	/**
	 * This is second of the analysis operations in course-work3
	 * Gets the number of tweets in the users time line
	 * @param userName
	 * @return the number of tweets of a user
	 * @throws TwitterException
	 */
	public int getNumberOfTweets(String userName) throws TwitterException {
		try {
			UsersResources userResources = twitter.users();
			User userUnderFocus = userResources.showUser(userName);
			return userUnderFocus.getStatusesCount();
		} catch (TwitterException tex) {
			throw tex;
		}
	}
	
	/**
	 * This is the third of the analysis operations in course-work 3\
	 * Gets the number of retweets present by user present in user's time line
	 * @param userName
	 * @return the number of retweets done by a user
	 * @throws TwitterException
	 */
	public int getNumberOfRetweets(String userName) throws TwitterException {
		int numberOfReTweets = 0;
		getUserTimeLine(userName);
		
		// traverse through all the Status and find out how many are
		// retweets by the user using isRetweet() on each Statuses
		for (Status tweetStatus : tweetsTimeLine) {
			// isRetweet() returns if the respective tweet is retweeted by User
			if (tweetStatus.isRetweet()) {
				numberOfReTweets++;
			}
		}

		return numberOfReTweets;
	}
	
	/**
	 * This is fourth of the analysis operations requested as part of course-work3.
	 * Gets the user name of the most active follower for a given user.
	 * 
	 * Go through all the collected tweets, find out retweeter ids
	 * using getRetweeterIds(long, long).
	 * for each tweet compute the maximum number of retweets by a
	 * each of the user who is essentially a follower
	 * 
	 * To filter the user which are followers There are 2 mechanisms
	 * either to followerIds list generated above or call
	 * twitter.showFriendship(sourceUserId, targetUserId).isSourceFollowedByTarget() under the
	 * relationship object of Twitter instance
	 *
	 * @param userName
	 * @return the user name of most active follower
	 * @throws TwitterException
	 */
	public String getMostActiveFollower(String userName) throws TwitterException {
		// checks whether analysis reached getFollowerIds and getRetweeterIds rate limits
		if (getRateLimitStatuses()) {
			// rate limits reached - return empty active follower name as failure
			return "";
		}
		
		// Stores the id of the active follower
		long activeFollowerUserId = 0;
		
		// Maximum RetweetCount - used for have the most active follower during analysis
		int maxRetweetCount = -1;
		
		// Hash table containing the mapping of follower id and the contribution of the follower
		// in the user's time line
		Hashtable<Long, Integer> followerIdsContributionTable = new Hashtable<Long, Integer>();
		
		
		// Get the list of all follower ids of the user
		List<Long> followerList = getFollowerIds(userName);
		
		// Get the list of all retweeter ids of the user
		ArrayList<IDs> retweeterIDsList = getRetweeterIDs(userName);
		
		// Go through each of the retweeterIds list
		// For each list, check if the each of the user is a follower
		// then, 
		//       check if the user is already present in followerIdsContributionMap
		//              if no - add the user into the table with start count of 1.
		//              if yes -update the count for the follower and update the table again
		// At the end of the iterating retweeterIDsList: 
		// - activeFollowerUserId is the most active follower
		// - maxRetweetCount is the contribution done by the active follower
		for (IDs userIds : retweeterIDsList) {
			for (long userId : userIds.getIDs()) {
				// Check if the retweeter user Id is indeed part of the followers list
				//
				// NOTE: alternate way to find if the user is a follower of certain user is by
				//  twitter.showFriendship(userUnderFocus.getId(), userId).isSourceFollowedByTarget()
				// But for analysis purposes, it could be expensive to make redudant calls for all retweet ids
				//
				if (followerList.contains(userId)) {
					// check if the follower is present in followerIdsContribitionTable
					if (!followerIdsContributionTable.containsKey(userId)) {
						// a new user id
						followerIdsContributionTable.put(userId, 1);

						// Initialize activeFollower - only for first user
						if (maxRetweetCount < 0) {
							maxRetweetCount = 1;
							activeFollowerUserId = userId;
						}
					} else {
						// the follower exists, update the count
						int currentCount = followerIdsContributionTable.get(userId);
						followerIdsContributionTable.put(userId, ++currentCount);

						// Compare the updated count with maxCount and update follower if needed.
						if (currentCount > maxRetweetCount) {
							maxRetweetCount = currentCount;
							activeFollowerUserId = userId;
						}
					}
				}
			}
		}

		// Update active follower and its contribution
		String activeFollower = "";
		activeFollowerContribution = maxRetweetCount;
		if (activeFollowerUserId > 0) {
			User follower = twitter.users().showUser(activeFollowerUserId);
			activeFollower = follower.getScreenName();
		} else {
			activeFollower = "user name for computed follower id could not be found.";
		}
		
		return activeFollower;
	}

	/**
	 * Gets the maximum contribution of the most active follower.
	 * This is computed whenever getMostActiveFollower() analysis operation
	 * is carried out.
	 * @return number of retweets done by most active follower
	 */
	public int getMostActiveFollowerContribution() {
		return activeFollowerContribution;
	}
	
	/**
	 * This is the utility function to get the list of all retweeter IDs.
	 * The information is stored as List<IDs>. 
	 * The information is processed further to get the acitve follower for a user.
	 * @param userName
	 * @return list of IDs containing retweeterIds for a user
	 * @throws TwitterException
	 */
	private ArrayList<IDs> getRetweeterIDs(String userName) throws TwitterException {
		ArrayList<IDs> retweeterIDsList = new ArrayList<>();
		if (getRetweeterIdsRateLimitStatus()) {
			System.out.println(RETWEETERIDS_RATE_LIMIT_MSG );
			return retweeterIDsList;
		}
		List<Long> tweetIds = collectTweetIds(userName);
		boolean reached_limit = false;
		
		for (long tweetId : tweetIds) {
			long cursor = -1;

			do {
				IDs userIds = twitter.getRetweeterIds(tweetId, retweeterIDsResultCount, cursor);
				if (userIds.getRateLimitStatus().getRemaining() == 0) {
					reached_limit = true;
				}
					
				retweeterIDsList.add(userIds);
				cursor = userIds.getNextCursor();
			} while (cursor != 0 && !reached_limit);
		}
		
		if (reached_limit) {
			System.out.println(RETWEETERIDS_RATE_LIMIT_MSG );
		}
		return retweeterIDsList;
	}

	/**
	 * This is a utlity function to retrieve the list of follower ids of a user.
	 * Uses getFollowersIDs(long, long) to get the required list iteratively. 
	 * @param userName
	 * @return list of follower ids
	 * @throws TwitterException
	 */
	private List<Long> getFollowerIds(String userName) throws TwitterException {
		List<Long> followerList = new ArrayList<Long>();
		
		if (getFollowerIdsRateLimitStatus()) {
			System.out.println(FOLLOWER_IDS_RATE_LIMIT_MSG);
			return followerList;
		}
		
		User userUnderFocus = twitter.users().showUser(userName);
		long fcursor = -1;
		boolean reached_limit = false;
		
		do {
			IDs followerIds = twitter.getFollowersIDs(userUnderFocus.getId(), 
					fcursor, followerIDsResultCount);
			if (followerIds.getRateLimitStatus().getRemaining() == 0) {
				reached_limit = true;
			}
			// List of follower ids is to used as a look up
			for (long fid : followerIds.getIDs()) {
				followerList.add(fid);
			}
			fcursor = followerIds.getNextCursor();
		} while (fcursor != 0  && !reached_limit);
		
		if (reached_limit) {
			System.out.println(FOLLOWER_IDS_RATE_LIMIT_MSG);
		}
		
		return followerList;
	}	
	
	/**
	 * Gets the total status time line for a given user
	 * This time line is useful to find the most active follower analysis operation
	 * @param userName
	 * @return the number of statuses in the time line
	 * @throws TwitterException
	 */
	private int getUserTimeLine(String userName) throws TwitterException {
		// Get the user object for the given userName
		User userUnderFocus = twitter.users().showUser(userName);
					
		// check if the tweetsTimeLine is not generated at all or
		// is the tweetTimeLine is requested for same user and status count is same
		if (tweetsTimeLine == null || (!userNameOfTweetsTimeLine.equals(userName)
				|| tweetsTimeLine.size() != userUnderFocus.getStatusesCount())) {
			tweetsTimeLine = new ArrayList<Status>();
		
			// Total number of pages to be fetched for the user
			int totalStatusCount = userUnderFocus.getStatusesCount();
			int numberOfPages = (totalStatusCount / pageSize) + 1;
			
			// Flag to decide whether to continue the fetch or not
			boolean fetch = true;
			
			for (int pageIndex = 1; pageIndex <= numberOfPages && fetch; pageIndex++) {
				ResponseList<Status> userTimeLineStatus = twitter.getUserTimeline(userName,
						new Paging(pageIndex, pageSize));
				int statusCount = userTimeLineStatus.size();
				
				// if fetch of a userTimeLine results in 0 statuses, then we can stop fetching processes
				fetch = statusCount > 0;
				if (fetch) {
					// adds the userTimeLine status into 
					tweetsTimeLine.addAll(userTimeLineStatus.subList(0, userTimeLineStatus.size() - 1));
				}
			}
		}
		return tweetsTimeLine.size();
	}
	
	/**
	 * This is a utility function which collects all the tweet ids present in 
	 * a user's status time line. This is used in turn to find out retweets in 
	 * the total timeline.
	 * @param userName
	 * @return list of tweet ids i.e. which is not retweets and are only updated 
	 * by the user under consideration
	 * @throws TwitterException
	 */
	private List<Long> collectTweetIds(String userName) throws TwitterException {
		getUserTimeLine(userName);
		ArrayList<Long> tweetIds = new ArrayList<Long>();
		
		for (Status tweetStatus : tweetsTimeLine) {
			long tweetId = tweetStatus.getId();

			// isRetweet() returns if the respective tweet is retweeted by
			// User
			if (!tweetStatus.isRetweet()) {
				// consider the tweet only if its from the user.
				// This is to avoid promotional tweets
				if (!tweetStatus.isRetweetedByMe()) {
					int retweetCount = tweetStatus.getRetweetCount();
					if (retweetCount > 0) {
						tweetIds.add(tweetId);
					}
				}
			}
		}
		
		return tweetIds;
	}
	
	/**
	 * Utility function to check whether rate limit of follower/ids call reached
	 * @return
	 * @throws TwitterException
	 */
	private boolean getFollowerIdsRateLimitStatus() throws TwitterException {
		Map<String, RateLimitStatus> map =
		twitter.getRateLimitStatus("followers");
		
		return map.get("/followers/ids").getRemaining() == 0 ;
	}
	
	// String message constants
	private static final String TRY_AGAIN_MSG = " Please try again after some time.";
	
	private static final String RETWEETERIDS_RATE_LIMIT_MSG 
	= "You have consumed the rate limits of getRetweeterIDs request,"
			+ TRY_AGAIN_MSG;

	private static final String FOLLOWER_IDS_RATE_LIMIT_MSG = "You have consumed the rate limits of getFollowerIDs request,"
			+ TRY_AGAIN_MSG;
	
	/**
	 * Utility function to check whether rate limit of /statuses/retweeters/ids call reached
	 * @return
	 * @throws TwitterException
	 */
	private boolean getRetweeterIdsRateLimitStatus() throws TwitterException {
		Map<String, RateLimitStatus> map =
		twitter.getRateLimitStatus("statuses");
		
		return map.get("/statuses/retweeters/ids").getRemaining() == 0;
	}
	
	/**
	 * Utility function for checking
	 * follower/ids and /statuses/retweeters/ids rate limits together
	 * @return
	 * @throws TwitterException
	 */
	private boolean getRateLimitStatuses() throws TwitterException {
		Map<String, RateLimitStatus> map =
		twitter.getRateLimitStatus("followers", "statuses");
		
		boolean reachedLimit = false;
		
		reachedLimit = map.get("/followers/ids").getRemaining() == 0 || map.get("/statuses/retweeters/ids").getRemaining() == 0;
		
		if (getFollowerIdsRateLimitStatus() || getRetweeterIdsRateLimitStatus()) {
			System.out.println("You have consumed the rate limits of getRetweeterIDs or getFolloweIDs request,"
					+ TRY_AGAIN_MSG);
		}
		return reachedLimit;
	}
}
