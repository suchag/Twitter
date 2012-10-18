/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import weka.core.Stopwords;

/**
 *
 * @author juimanoj
 */
public class DataLoader {

    /**
     * Create HashMap of UserID and Time
     *
     * @param tweets
     */
    private static void createDictUserIDTime(String tweets) {

        String[] tokens = tweets.split("\t");
        // In the token list date time as 1 token
        // so splitting the DateTime token into date and time
        String[] dateTimeSplit = tokens[3].split(" ");
        //Insert the UserId and Time in hashMap DateTime
        FileParser.dateTime.put(Integer.parseInt(tokens[0]), dateTimeSplit[1]);
        Iterator iterateTime = FileParser.dateTime.entrySet().iterator();
        while (iterateTime.hasNext()) {
            Map.Entry pairs = (Map.Entry) iterateTime.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            iterateTime.remove(); // avoids a ConcurrentModificationException
        }
    }

    /**
     * Remove special characters and stop words from the tweets *
     *
     * @param Tweet
     * @return
     */
    private static String pruneTweets(String Tweet) {
        String cleanTweets = Tweet.replaceAll("[-+.^:?$;,!*'':()><}{]", "");
        return cleanTweets;
    }

    /**
     * Remove stopwords from the tweet
     *
     * @param words
     * @return
     */
    private static String[] removeStopWords(String[] words) {

        List<String> tweetList = new ArrayList<>();

        Stopwords stopWords = new Stopwords();
        stopWords.add("there");
        stopWords.add("Hey");
        stopWords.add("whats");
        stopWords.add("quick");
        stopWords.add("shit");
        boolean result = stopWords.remove("little");

        for (int i = 0; i < words.length; i++) {
            boolean value = stopWords.is(words[i]);
            if (value == false) {
                tweetList.add(words[i]);
            }
        }
        String[] arrTweet = tweetList.toArray(new String[tweetList.size()]);

        //Returns clean Tweets
        return arrTweet;
    }

    /**
     * create name value pair of UserID and TweetText
     *
     * @param line
     */
    public static void populateTweetInfo(String line) {
        // Every line is expected to have 4 items separated by tab
        String[] tokens = line.split("\t");

        // Get the userID which is in token0
        Integer UserID = Integer.parseInt(tokens[0]);

        // Get given UserID's word count map, if doesn't exist create a new one
        HashMap wordCount = null;
        if (FileParser.userWordFreq.containsKey(UserID)) {
            // Found exisiting map, use it
            wordCount = FileParser.userWordFreq.get(UserID);
        } else {
            // Generate a new map for a new userID
            wordCount = new HashMap();
        }

        //pruneTweet function will remove all the special characters and stop words from the Tweets
        String Tweets = pruneTweets(tokens[2]);

        //Split the tweet to get all words in the tweet
        String[] words = Tweets.split(" ");

        //Remove stopwords arguments as tweet word array
        String[] tweetWords = removeStopWords(words);

        // Populate inside the userid's word count map
        for (int i = 0; i < tweetWords.length; i++) {
            // Find the frequency of words in Perticular User
            int count = 1;
            if (wordCount.containsKey(tweetWords[i])) {
                count = (Integer) wordCount.get(tweetWords[i]) + 1;
            }
            wordCount.put(tweetWords[i], count);
            //System.out.println("Words " + tweetWords[i]);
        }
        // Add it to our all userId map
        FileParser.userWordFreq.put(UserID, wordCount);
    }

    /**
     * Create a list of similar user based on current topic
     *
     * @param currentTopics
     * @param UserWordFreq
     * @throws IOException
     */
    public static void populateCurrTopicSimilarUser() {

        int countWords = 0;
        Map.Entry userID, keyWord;
        String topic;
        Iterator it2;

        for (int i = 0; i < FileParser.currentTopics.size(); i++) {
            HashMap<String, Boolean> currTopicCount = new HashMap<>();
            // get each current topic
            topic = FileParser.currentTopics.get(i);

            //check if the current topic is single word or contains
            //white space
            boolean containsWhiteSpace = topic.contains(" ");
            if (containsWhiteSpace) {
                //split the word
                String[] substr = topic.split(" ");

                // add each word to the dictionary and keep track of no. of
                // times it occurs
                for (int j = 0; j < substr.length; j++) {
                    currTopicCount.put(substr[j], false);
                }
            } // if a single word add to dictionary
            else {
                currTopicCount.put(topic, false);
            }
            Iterator it1 = FileParser.userWordFreq.entrySet().iterator();
            while (it1.hasNext()) {
                userID = (Map.Entry) it1.next();
                Integer UserIDKey = (Integer) userID.getKey();
                HashMap words = (HashMap) userID.getValue();
                it2 = words.entrySet().iterator();

                while (it2.hasNext()) {
                    keyWord = (Map.Entry) it2.next();
                    String word = (String) keyWord.getKey();

                    // check if the dictionary contains that word or not
                    if (currTopicCount.containsKey(word)) {

                        // if that word is present than decrease the count
                        //   Boolean value = (currTopicCount.get(word.toString()));
                        Boolean value = true;
                        // add the updated value to the dictionary i.e if word
                        // is present make it true
                        currTopicCount.put(word, value);
                    } //if the word not present
                    else {
                        continue;
                    }
                }
                //currTopicCount is the hashtable which keep count of
                // the word in current topic list and frequency
                Iterator it = currTopicCount.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry wordCount = (Map.Entry) it.next();

                    if (wordCount.getValue() == true) {
                        countWords++;
                    }
                }
                if (countWords == currTopicCount.size()) {
                    if (FileParser.currTopicUserList.containsKey(topic)) {
                        String ids = (String) FileParser.currTopicUserList.get(topic);
                        String users = ids + " " + UserIDKey;
                        FileParser.currTopicUserList.put(topic, users);
                    } else {
                        FileParser.currTopicUserList.put(topic, Integer.toString(UserIDKey));
                    }
                }
                Iterator ct = currTopicCount.entrySet().iterator();
                while (ct.hasNext()) {
                    Map.Entry currTopic = (Map.Entry) ct.next();
                    String word = (String) currTopic.getKey();
                    boolean value = (boolean) currTopic.getValue();
                    if (value == true) {
                        value = false;
                        currTopicCount.put(word, value);
                    }
                }
                // reset word count
                countWords = 0;
            }
        }
        if (FileParser.debug == 1) {
            Iterator i = FileParser.currTopicUserList.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry pairs = (Map.Entry) i.next();
                System.out.println(pairs.getKey() + " = " + pairs.getValue());
                i.remove(); // avoids a ConcurrentModificationException
            }
        }
    }

    public static void populateUrlInfo(String previousTweet) {

        String[] token = previousTweet.split("\t");

        String userID = token[0];
        String tweet = token[2];
        String tinyURL, longURL;
        String title = null;

        String url = "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)";
        Pattern urlPattern = Pattern.compile(url, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = urlPattern.matcher(tweet);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();

            // Tweet only contains tiny URL
            tinyURL = tweet.substring(matchStart, matchEnd);
            try {
                // Tiny URL is converted to long URL
                longURL = URLExtractor.getLongUrl(tinyURL);
            } catch (MalformedURLException ex) {
                //Logger.getLogger(DataLoader.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            } catch ( IOException | NullPointerException ex) {
                //Logger.getLogger(DataLoader.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }


            try {
                title = URLTitleExtractor.getPageTitle(longURL);
            } catch (    IOException | NullPointerException ex) {
               continue;
            }

            System.out.println(userID + " "  + title );


        }
    }
}
