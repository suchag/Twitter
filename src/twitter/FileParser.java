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
public class FileParser {

    public static final int debug = 1;
    public static HashMap<Integer, String> dateTime = new HashMap<>();
    //Create a hashmap based on UserID and Tweet Word Count
    public static HashMap<Integer, HashMap<String, Integer>> userWordFreq = new HashMap<>();
    //Create a list of current topics
    public static List<String> currentTopics = new ArrayList<>();
    //Create a hashmap of word and UserID
    public static HashMap<String, String> currTopicUserList = new HashMap<>();
    //Create a list of User having same location
    public static HashMap<String, String> commonLocationUser = new HashMap<>();


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
     * populate current topic list
     *
     * @param fileName
     * @throws IOException
     */
    public static void createCurrentTopicList(String fileName) throws IOException {
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
        if (debug == 1) {
            System.out.println("No. of Current Topics = " + currentTopics.size());
        }
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
                    DataLoader.populateTweetInfo(previousTweet);
                    //  CreateDictUserIDTime(previousTweet);
                   // DataLoader.populateUrlInfo(previousTweet);
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
            if (debug == 1) {
                System.out.println("Done with " + count + " lines.");
            }
        }
        // last line handling
        if (!previousTweet.isEmpty()) {
            // This marks the end of previous tweet. Extract the info from previous tweet
            //System.out.println("************************************");
            //System.out.println(previousTweet);
            DataLoader.populateTweetInfo(previousTweet);
          //  DataLoader.populateUrlInfo(previousTweet);
            //   CreateDictUserIDTime(previousTweet);
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
            if (commonLocationUser.containsKey(tokens[1])) {
                String ids = (String) commonLocationUser.get(tokens[1]);
                String usersID = ids + " " + tokens[0];
                commonLocationUser.put(tokens[1], usersID);
            } else {
                commonLocationUser.put(tokens[1], tokens[0]);
            }
        }
        Iterator i = commonLocationUser.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry pairs = (Map.Entry) i.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            i.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println("User Location List Count" + currTopicUserList.size());
    }
}
