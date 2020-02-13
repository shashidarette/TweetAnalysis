package SOA.CW3;

import java.text.MessageFormat;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.UniformInterfaceException;


/**
 * 
 *  @author Shashidar Ette : se146
 * 
 * This is implemented as single abstracts the communication with FollowerWebService
 *    Overall it provides following functions:
 *    public String getUserName(String passCode)
 *    public String submitNumberOfFollowers(String passCode, String userName, int numberOfFollowers)
 *    public String submitNumberOfTweets(String passCode, String userName, int nTweets)
 *    public String submitNumberOfRetweets(String passCode, String userName, int nRTweets)
 *    public String submitMostActiveFollower(String passCode, String userName, String ActiveFollower)
 * 
 */
public class FollowerServiceClient {
	// BaseURL of the follower service
	private final String BASE_FOLLOWERS_SERVICE_URI = "https://campus.cs.le.ac.uk/tyche/CO7214Rest2/rest/soa/";

	// FollowerServiceClient instance
	private static FollowerServiceClient instance = null;
	
	private FollowerServiceClient() {
		
	}
	
	// Singleton implementation of the client service
	public static FollowerServiceClient getInstance() {
		if (instance == null) {
			instance = new FollowerServiceClient();
		}
		return instance;
	}
	
	/**
	 * 
	 * getUserName rest call
	 * @param passCode
	 * @return UserName
	 * @throws UniformInterfaceException
	 */
	public String getUserName(String passCode) throws UniformInterfaceException {
		Client client = ClientBuilder.newClient();

		WebTarget target = client.target(
				BASE_FOLLOWERS_SERVICE_URI + MessageFormat.format("getUserName/{0}", new Object[] { passCode }));

		return target.request(MediaType.APPLICATION_JSON).get(String.class);
	}

	/**
	 * submits the number of followers to Follower service
	 * @param passCode
	 * @param userName
	 * @param numberOfFollowers
	 * @return
	 * @throws UniformInterfaceException
	 * Reference of the rest call
	 * submitNumberOfFollowers(passCode, name: String; numberOfFollowers: Int): String
	 * submitNumberOfFollowers/{passCode}/{userName}/{nFollowers}
	 s*/
	public String submitNumberOfFollowers(String passCode, String userName, int numberOfFollowers)
			throws UniformInterfaceException {
		Client client = ClientBuilder.newClient();

		WebTarget target = client
				.target(BASE_FOLLOWERS_SERVICE_URI + MessageFormat.format("submitNumberOfFollowers/{0}/{1}/{2}",
						new Object[] { passCode, userName, numberOfFollowers }));

		return target.request(MediaType.APPLICATION_JSON).get(String.class);
	}

	/**
	 * Submits the number of tweets for the user to Follower service
	 * @param passCode
	 * @param userName
	 * @param nTweets
	 * @return
	 * @throws UniformInterfaceException
	 * Reference of the rest call
	 * submitNumberOfTweets(passCode, name: String; numberOfTweets : Int):
	 * String submitNumberOfTweets/{passCode}/{userName}/{nTweets}
	 * */
	public String submitNumberOfTweets(String passCode, String userName, int nTweets)
			throws UniformInterfaceException {
		Client client = ClientBuilder.newClient();

		WebTarget target = client.target(BASE_FOLLOWERS_SERVICE_URI + MessageFormat
				.format("submitNumberOfTweets/{0}/{1}/{2}", new Object[] { passCode, userName, nTweets }));

		return target.request(MediaType.APPLICATION_JSON).get(String.class);
	}
	
	/**
	 * Submits the number of retweets for the user to Follower service
	 * @param passCode
	 * @param userName
	 * @param nRTweets
	 * @return
	 * @throws UniformInterfaceException
	 * Reference of the rest call
	 * submitNumberOfRetweets (passCode, name: String; numberOfRetweets: Int): String
	 * submitNumberOfRetweets/{passCode}/{userName}/{nRTweets}
	 */
	public String submitNumberOfRetweets(String passCode, String userName, int nRTweets)
			throws UniformInterfaceException {
		Client client = ClientBuilder.newClient();

		WebTarget target = client.target(BASE_FOLLOWERS_SERVICE_URI + MessageFormat
				.format("submitNumberOfRetweets/{0}/{1}/{2}", new Object[] { passCode, userName, nRTweets }));

		return target.request(MediaType.APPLICATION_JSON).get(String.class);
	}
	
	/**
	 * submits the most active follower of the user to Follower service
	 * @param passCode
	 * @param userName
	 * @param ActiveFollower
	 * @return
	 * @throws UniformInterfaceException
	 * submitMostActiveFollower (passCode, name: String; mostActiveFollower: String): String
	 * submitMostActiveFollower/{passCode}/{userName}/{ActiveFollower}
	 */
	public String submitMostActiveFollower(String passCode, String userName, String ActiveFollower)
			throws UniformInterfaceException {
		Client client = ClientBuilder.newClient();

		WebTarget target = client
				.target(BASE_FOLLOWERS_SERVICE_URI + MessageFormat.format("submitMostActiveFollower/{0}/{1}/{2}",
						new Object[] { passCode, userName, ActiveFollower }));

		return target.request(MediaType.APPLICATION_JSON).get(String.class);
	}
}
