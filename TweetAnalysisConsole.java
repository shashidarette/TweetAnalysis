package SOA.CW3;

// Standard Java import
import java.io.IOException;
import java.util.Scanner;
import twitter4j.TwitterException;

/**
 * 
 * @author Shashidar Ette : se146
 * Brief Overview:
 * This is a tweet analytics tool developed based on SOA course-work 3
 * For the simplicity purposes all the required code of analysis, constants are included in 
 * TweetAnalysis.java class.
 * 
 * Overall this course-work submission contains 3 classes:
 * 1. TweetAnalysis.java - This is implemented as singleton abstracts the communication with Twitter4j and its API
 *    Overall it provides following functions:
 *    public int getNumberOfFollowers(String userName)
 *    public int getNumberOfTweets(String userName)
 *    public int getNumberOfRetweets(String userName)
 *    public String getMostActiveFollower(String userName)
 *    public int getMostActiveFollowerContribution()
 *    
 * 2. FollowerServiceClient.java - This is implemented as singleton, abstracts the communication with FollowerWebService
 *    Overall it provides following functions:
 *    public String getUserName(String passCode)
 *    public String submitNumberOfFollowers(String passCode, String userName, int numberOfFollowers)
 *    public String submitNumberOfTweets(String passCode, String userName, int nTweets)
 *    public String submitNumberOfRetweets(String passCode, String userName, int nRTweets)
 *    public String submitMostActiveFollower(String passCode, String userName, String ActiveFollower)
			
 * 3. TweetAnalysisConsole.java : This is a console application which communicates with TweetAnalysis 
 *    instance and submits the results to FollowerServiceClient instance. 
 *    It has references of both as instances. Overall the design of the course-work submission is as below:
 * 
 * 
 *  ----------------               ----------------               ---------------------
 *  |    Tweet    |   requests      |    Tweet    |  submits>     |    Follower       |
 *  |  Analysis   |<----------------|  Analysis   |-------------->|  Service Client   |    
 *  | (Singleton) |                 |   Console   |               |   (Singleton)     | 
 *  ---------------                ---------------                ---------------------
 * 
 * 
 * In addition, this console based application provides an option for user to select the operation(s)
 * to be carried for tweet analysis. The end-user of this application can choose any of the options below:
 * 1. Number of followers of the user
 * 2. Number of tweets of the user
 * 3. Number of the retweets of the user
 * 4. Most active follower of the user
 * 5. All operations
 * 
 * Apart from this, the application 2 alternatives to choose a twitter user to be analyzed for selected operations.
 * The options are as below
 * 1. Use default pass code of se146 : Shashidar Ette. => Dynamically username will be picked 
 *    using FollowerService.getUserName()
 * 2. Use any valid twitter user name
 *
 * Please refer to the following string constants for more details
 * - <OPERATION_OPTIONS> - details analysis operations 
 * - <USER_OPTIONS>      - details options to choose a twitter user 
 */
public class TweetAnalysisConsole {
	// Follower Service user pass code for se146 : Shashidar Ette
	private static final String PASSCODE_DEFAULT = "se1685";
	
	// String constant resources	
	static final String OPERATION_OPTIONS = 
			"\r\n[                          WELCOME TO TWITTER USER ANALYTICS                               ]\r\n"
			+ "This application helps to analyze a specific twitter user and get the below information:\r\n"
			+ "1 - Number of followers of the user\r\n" + "2 - Number of tweets of the user\r\n"
			+ "3 - Number of the retweets of the user\r\n" + "4 - Most active follower of the user\r\n"
			+ "5 - All the above \r\n"
			+ "HELP: Choose 5 to perform all operations or select the operations in following way 1 or 1,2,3 or 3,4\r\n"
			+ "Choose any of the analysis operations to be carried out (only positive integers):";
			
	static final String USER_OPTIONS = 
			"----------------------------------------------------------------------------------------\r\n"
			+ "Choose one of the options to choose a user to analyze:\r\n"
			+ "1. Use default pass code of se146: Shashidar Ette. \r\n"
			+ "2. Use any valid twitter user name\r\n" 
			+ "3. Exit\r\n" 
			+ "NOTE:\r\n"
			+ "- For option 1:\r\n"
			+ "   -> pass code will be used to retrieve the username from FollowerService.\r\n"
			+ "   -> generated results will be submitted to Follower Service for validation.\r\n"
			+ "- If an invalid option is chosen, by default option 1 will be selected.\r\n"
			+ "----------------------------------------------------------------------------------------\r\n"
			+ "Please select a valid option:";

	// Analyzer options
	static final int USE_DEFAULT_PASS_CODE = 1;
	static final int USER_TWITTER_USER = 2;
	static final int EXIT = 3;
	private static int selectedUserOption = -1;
	
	// User name from FollowerWebService
	private static String userName = "";

	// Boolean flag to control whether to submit the results to follower service or not. 
	// By default its disabled.
	private static boolean submitResponsesToFollowerService = false;
	
	// Boolean flag to control whether to request a user name for a pass code every-time
	private static boolean requestUserNameEveryOperation = false;
	
	// Operation selection from user, controlled by option selected by user in console
	private static boolean followerOperation = false;
	private static boolean tweetsOperation = false;
	private static boolean retweetsOperation = false;
	private static boolean activefollowerOperation = false;
	
	// TweetAnalysis instance - it used to send analysis request for a specific user
	private static TweetAnalysis tweetAnalysis  = TweetAnalysis.getTweetAnalysisInstance();
	
	// FollowerServiceClient instance - it used to submit the analysis results for a specific user
	private static FollowerServiceClient followerService = FollowerServiceClient.getInstance();
	
	/**
	 * Main function - it communicates with Follower service and calls various it utility function
	 * to do the twitter analytics
	 * @param args
	 * @throws IOException
	 * @throws TwitterException
	 */
	public static void main(String[] args) throws IOException, TwitterException {		
		// ref: https://docs.oracle.com/javase/7/docs/api/java/util/Scanner.html
		// Used for interaction with user through console
		Scanner sc = new Scanner(System.in);
		
		// To provide a user driven analyzer options
		System.out.println(OPERATION_OPTIONS);
		String operationsChoice = sc.next();

		// pass the operations choice and decode it
		processOperationSelection(operationsChoice);
		
		do {
			System.out.println(USER_OPTIONS);
			selectedUserOption = sc.nextInt();

			if (selectedUserOption == USE_DEFAULT_PASS_CODE) {
				submitResponsesToFollowerService = true;
			}
			
			if (selectedUserOption == USER_TWITTER_USER) {
				System.out.println(ENTER_TWITTER_USER_NAME_TO_ANALYSE);
				userName = sc.next();
				submitResponsesToFollowerService = false;
			}			

			// Uncomment below line to avoid the submission of results to follower service
			// submitResponsesToFollowerService = false;
			
			try {
				if (selectedUserOption != EXIT) {
					tweetAnalysis.reset();
					System.out.println(RESULT_HEADER);
					analyseUser();
				}
			} catch (TwitterException tex) {
				System.out.println(String.format(
						TWITTER_COMMUNICATION_EXCEPTION_MESSAGE,
						tex.getErrorCode(), tex.getErrorMessage()));
			}
			
			// reset the user name for next cycle
			userName = "";
		} while (selectedUserOption != EXIT);
		sc.close();
		System.out.println("Tweet Analyser terminated.\r\n");
	}
	
	/**
	 * Gets the user name from follower service using the pass code
	 * @return
	 */
	private static String getUserName() {
		if (selectedUserOption == USE_DEFAULT_PASS_CODE) {
			if ((userName == null || userName.isEmpty()) || requestUserNameEveryOperation) {
				userName = followerService.getUserName(PASSCODE_DEFAULT);
			}
		}
		
		return userName;
	}
	
	/**
	 * Utility function to communicate with TweetAnalysis for requested analysis operations and
	 * submit the results to FollowerService
	 * @throws TwitterException
	 */
	private static void analyseUser() throws TwitterException {
		long startTime = System.currentTimeMillis();

		// Initialize the Twitter instance
		String userName = getUserName();
		//TweetAnalysis.setUserName(userName);
		
		if (followerOperation) {
			int numberOfFollowers = tweetAnalysis.getNumberOfFollowers(userName);
			String followersResponse = submitResponsesToFollowerService ?
					followerService.submitNumberOfFollowers(PASSCODE_DEFAULT, userName, numberOfFollowers) : NOT_VALIDATED;
			System.out.println(String.format(NUMBER_OF_FOLLWERS_RESULT, 
					userName, numberOfFollowers, followersResponse));
		}

		if (tweetsOperation) {
			int numberOfTweets = tweetAnalysis.getNumberOfTweets(userName);
			String noOfTweetsResponse = submitResponsesToFollowerService ? 
					followerService.submitNumberOfTweets(PASSCODE_DEFAULT, userName, numberOfTweets) : NOT_VALIDATED;
			System.out.println(String.format(NUMBER_OF_TWEETS_RESULT, 
					userName, numberOfTweets, noOfTweetsResponse));
		}

		if (retweetsOperation) {
			int numberOfReTweets = tweetAnalysis.getNumberOfRetweets(userName);
			
			String numberOfRetweetsResponse = submitResponsesToFollowerService ?
					followerService.submitNumberOfRetweets(PASSCODE_DEFAULT, userName, numberOfReTweets) : NOT_VALIDATED;
			System.out.println(String.format(RETWEETS_RESULT_MESSAGE, userName, 
					numberOfReTweets, numberOfRetweetsResponse));
		}
		if (activefollowerOperation) {
			// Alternate way to get followers is to request Twitter factory
			// to return followerIds
			// IDs object has full list of follower ids and its length is
			// the followers count
			String activeFollower = tweetAnalysis.getMostActiveFollower(userName);
			
			if (activeFollower.isEmpty()) {
				activeFollower = "not computed";
				System.out.println(String.format(ACTIVE_FOLLOWER_RESULT_MESSAGE,
						activeFollower, -1, NOT_VALIDATED));
			} else {
				int activeFollowerContribution = tweetAnalysis.getMostActiveFollowerContribution();
				
				// Submit the response
				String activeFollowerResponse = submitResponsesToFollowerService ?
						followerService.submitMostActiveFollower(PASSCODE_DEFAULT, userName, activeFollower) : NOT_VALIDATED;
				
				System.out.println(String.format(ACTIVE_FOLLOWER_RESULT_MESSAGE,
						activeFollower, activeFollowerContribution, activeFollowerResponse));
			}
				
		}
		
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		System.out.println("Analysis took " + duration / 1000.0 + " seconds.\r\n");
	}
	
	/**
	 * Utility functions to handle console application options selected by user
	 */
	/**
	 * processes the operation choice to set different analysis operations
	 * @param operationChoice
	 */
	private static void setOperationChoice(int operationChoice) {
		followerOperation = (operationChoice & 0x001) == 0x01;
		tweetsOperation = (operationChoice & 0x010) == 0x10;
		retweetsOperation = (operationChoice & 0x0100) == 0x100;
		activefollowerOperation =  (operationChoice & 0x1000) == 0x1000;
	}

	/**
	 * Processes the operation choice string entered by user in console
	 * @param operationsChoice
	 */
	private static void processOperationSelection(String operationsChoice) {
		int selectedOperations = 0;
		
		try {
			String[] operations = operationsChoice.split(",");
			for (String op : operations) {
				int o = Integer.parseInt(op);
				
				if (o == 5) {
					selectedOperations = 0x1111;
					break;
				} else if (o == 1) {
					selectedOperations |= 0x0001;
				} else if (o == 2) {
					selectedOperations |= 0x0010;
				} else if (o == 3) {
					selectedOperations |= 0x0100;
				} else if (o == 4) {
					selectedOperations |= 0x1000;
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Exception occured while parsing the operations selected, by default all operations "
					+ "will be performed.\r\n");
			selectedOperations = 0x1111;
		}
		
		setOperationChoice(selectedOperations);
	}
	
	// Additional String constants
	private static final String ENTER_TWITTER_USER_NAME_TO_ANALYSE = 
			"Please enter the twitter user name to analyse (only text):\r\n";
	
	private static final String NUMBER_OF_FOLLWERS_RESULT = 
			"Number of followers for user %s is %d and response is %s.";
	
	private static final String NUMBER_OF_TWEETS_RESULT = 
			"Number of tweets for user %s is %d and response is %s.";
	
	private static final String RETWEETS_RESULT_MESSAGE = 
			"Number of retweets for user %s is %d and response is %s.";
	
	private static final String ACTIVE_FOLLOWER_RESULT_MESSAGE = 
			"Active follower is %s with contribution of %d and response is %s.";
	
	private static final String NOT_VALIDATED = "not validated";
	
	private static final String TWITTER_COMMUNICATION_EXCEPTION_MESSAGE = 
			"An exception occured while communicating via Twitter API. \r\n"
			+ "Error Code is: %d \r\nError Message: %s.\r\nPlease retry after some time.\r\n";

	private static final String RESULT_HEADER = 
			"RESULTS for selected operation(s) and user are as below:\r\n"
			+ "---------------------------------------------------------------";
}
