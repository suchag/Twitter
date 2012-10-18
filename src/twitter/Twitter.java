/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitter;

import java.io.IOException;

/**
 *
 * @author juimanoj
 */
public class Twitter {

    /**
     * @param args the command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        String filePath = "Src\\Data\\test.txt";//test.txt"; //training_set_tweets.txt" training_set_users;
        String userLocFilePath = "Src\\Data\\training_set_users.txt";
        FileParser.readTextFile(filePath);
        String currentTopicfileName = "Src\\Data\\hottopics.txt";
        FileParser.createCurrentTopicList(currentTopicfileName);
        DataLoader.populateCurrTopicSimilarUser();
        FileParser.readLocTextFile(userLocFilePath);
//        System.out.println("#Users=" + UserWordFreq.size());
    }
}