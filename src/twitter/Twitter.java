/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
public class Twitter {

    private static HashMap<Integer, String> DateTime = new HashMap<>();
    //Create a hashmap based on UserID and Tweet Word Count
    private static HashMap<Integer, HashMap<String, Integer>> UserWordFreq = new HashMap<>();
    //Create a list of current topics
    private static List<String> currentTopics = new ArrayList<>();
    //Create a hashmap of word and UserID
    private static HashMap<String, String> currTopicUserList = new HashMap<>();
    //Create a list of User having same location
    private static HashMap<String, String> CommonLocationUser = new HashMap<>();


    /*
     * public static void readTextFile1() throws IOException { BufferedReader br
     * = null; char[] buffer = null; ArrayList<Character> key = null;
     * ArrayList<Character> value = null; StringBuilder sb = null; StringBuilder
     * stringBuilder = null; try { br = new BufferedReader(new
     * FileReader("TweetDetails.txt")); } catch (FileNotFoundException ex) {
     * Logger.getLogger(Twitter.class.getName()).log(Level.SEVERE, null, ex); }
     * String line = br.readLine(); System.out.println(line); while (line !=
     * null) { char[] charArray = line.toCharArray(); // reading each character
     * int i = 0; while (charArray[i] != '\n') { key = new
     * ArrayList<Character>(); if (charArray[i] == '"') { i++;
     * //System.out.println(character); while (charArray[i] != '"') {
     * key.add(charArray[i]); i++; } sb = new StringBuilder(key.size()); for
     * (Character ch : key) { sb.append(ch); } String fieldName = sb.toString();
     * System.out.println(fieldName + " "); } if (charArray[i] == ':') { value =
     * new ArrayList<Character>(); i++; int next = i; while (charArray[i] !=
     * ',') { if (charArray[++next] != '"') { value.add(charArray[i]); } i++; }
     * stringBuilder = new StringBuilder(value.size()); for (Character ch :
     * value) { stringBuilder.append(ch); } String data =
     * stringBuilder.toString(); System.out.println(data + "\n"); } i++; } }
     *
     * br.close(); }
     */
    /**
     * Create HashMap of UserID and Time
     *
     * @param tweets
     */
    private static void CreateDictUserIDTime(String tweets) {

        String[] tokens = tweets.split("\t");
        // In the token list date time as 1 token
        // so splitting the DateTime token into date and time
        String[] dateTimeSplit = tokens[3].split(" ");
        //Insert the UserId and Time in hashMap DateTime
        DateTime.put(Integer.parseInt(tokens[0]), dateTimeSplit[1]);
        Iterator iterateTime = DateTime.entrySet().iterator();
        while (iterateTime.hasNext()) {
            Map.Entry pairs = (Map.Entry) iterateTime.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            iterateTime.remove(); // avoids a ConcurrentModificationException
        }
    }

    /**
     * Remove special characters and stop words from the tweets
     *
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
    private static String[] RemoveStopWords(String[] words) {

        List<String> tweetList = new ArrayList<>();

        Stopwords stopWords = new Stopwords();
        stopWords.add("there");
        stopWords.add("Hey");
        stopWords.add("whats");
        stopWords.add("quick");
        stopWords.add("shit");
        boolean result = stopWords.remove("little");
        System.out.println(result);

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
     * populate current topic list
     *
     * @param fileName
     * @throws IOException
     */
    private static void CreateCurrentTopicList(String fileName) throws IOException {
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Twitter.class.getName()).log(Level.SEVERE, null, ex);
        }
        String newLine;
        newLine = fileReader.readLine();
        while (newLine != null) {
            currentTopics.add(newLine);
            newLine = fileReader.readLine();
        }
        System.out.println("No. of Current Topics = " + currentTopics.size());
    }

    /**
     * create name value pair of UserID and TweetText
     *
     * @param line
     */
    private static void populateTweetInfo(String line) {
        // Every line is expected to have 4 items separated by tab
        String[] tokens = line.split("\t");

        // Get the userID which is in token0
        Integer UserID = Integer.parseInt(tokens[0]);

        // Get given UserID's word count map, if doesn't exist create a new one
        HashMap wordCount = null;
        if (UserWordFreq.containsKey(UserID)) {
            // Found exisiting map, use it
            wordCount = UserWordFreq.get(UserID);
        } else {
            // Generate a new map for a new userID
            wordCount = new HashMap();
        }

        //pruneTweet function will remove all the special characters and stop words from the Tweets
        String Tweets = pruneTweets(tokens[2]);

        //Split the tweet to get all words in the tweet
        String[] words = Tweets.split(" ");

        //Remove stopwords arguments as tweet word array
        String[] tweetWords = RemoveStopWords(words);

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
        UserWordFreq.put(UserID, wordCount);
    }

    /**
     * Read the text file and parse the text file Call the populateTweetInfo()
     *
     * @param filePath
     * @throws IOException
     */
    public static void readTextFile(String filePath) throws IOException {
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Twitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        String newTweetStartpattern = "^\\d{2,8}\\t\\d{10}\\t";
        // Create a Pattern object
        Pattern newTweetPattern = Pattern.compile(newTweetStartpattern);
        // Now create matcher object.
        Matcher m = null;
        String previousTweet = "";
        String newTweet = "";
        String newLine;
        int count = 0;
        while ((newLine = fileReader.readLine()) != null) {
            // Get the next complete tweet string which may be multiline
            m = newTweetPattern.matcher(newLine);
            if (m.find()) {
                newTweet = newLine;
                if (!previousTweet.isEmpty()) {
                    // This marks the end of previous tweet. Extract the info from previous tweet
                    //System.out.println("************************************");
                    //System.out.println(previousTweet);
                    populateTweetInfo(previousTweet);
                    //     CreateDictUserIDTime(previousTweet);
                }
                // Start building the new tweet
                previousTweet = newTweet;
            } else {
                previousTweet = previousTweet + " " + newLine;
            }
            count++;
            if (count % 100000 == 0) {
                System.gc();
            }
            System.out.println("Done with " + count + " lines.");
        }
        // last line handling
        if (!previousTweet.isEmpty()) {
            // This marks the end of previous tweet. Extract the info from previous tweet
            //System.out.println("************************************");
            //System.out.println(previousTweet);
            populateTweetInfo(previousTweet);
            //  CreateDictUserIDTime(previousTweet);
        }
    }

    /**
     * Create a list of similar user based on current topic
     *
     * @param currentTopics
     * @param UserWordFreq
     * @throws IOException
     */
    private static void populateCurrTopicSimilarUser() {

        int countWords = 0;
        Map.Entry userID, keyWord;
        String topic;
        Iterator it2;

        for (int i = 0; i < currentTopics.size(); i++) {
            HashMap<String, Boolean> currTopicCount = new HashMap<>();
            // get each current topic
            topic = currentTopics.get(i);

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
            }
            // if a single word add to dictionary
            else {
                currTopicCount.put(topic, false);
            }
            Iterator it1 = UserWordFreq.entrySet().iterator();
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
                    if (currTopicUserList.containsKey(topic)) {
                        String ids = (String) currTopicUserList.get(topic);
                        String users = ids + " " + UserIDKey;
                        currTopicUserList.put(topic, users);
                    } else {
                        currTopicUserList.put(topic, Integer.toString(UserIDKey));
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
        Iterator i = currTopicUserList.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry pairs = (Map.Entry) i.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            i.remove(); // avoids a ConcurrentModificationException
        }
    }

    /**
     * create a list of users and there location
     *
     * @param filename
     * @throws IOException
     */
    public static void readLocTextFile(String filename) throws IOException {

        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Twitter.class.getName()).log(Level.SEVERE, null, ex);
        }

        String newLine;

        while ((newLine = fileReader.readLine()) != null) {
            String[] tokens = newLine.split("\t");
            if (CommonLocationUser.containsKey(tokens[1])) {
                String ids = (String) CommonLocationUser.get(tokens[1]);
                String usersID = ids + " " + tokens[0];
                CommonLocationUser.put(tokens[1], usersID);
            } else {
                CommonLocationUser.put(tokens[1], tokens[0]);
            }
        }
        Iterator i = CommonLocationUser.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry pairs = (Map.Entry) i.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            i.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println("User Location List Count" + currTopicUserList.size());
    }

    /**
     * @param args the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        String filePath = "Src\\Data\\test.txt";//test.txt"; //training_set_tweets.txt" training_set_users;
        String userLocFilePath = "Src\\Data\\training_set_users.txt";
        readTextFile(filePath);
        String CurrentTopicfileName = "Src\\Data\\hottopics.txt";
        CreateCurrentTopicList(CurrentTopicfileName);
        populateCurrTopicSimilarUser();
        readLocTextFile(userLocFilePath);
        System.out.println("#Users=" + UserWordFreq.size());
    }
}