/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package twitter;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author juimanoj
 */
public class TwitterTest {

    public TwitterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of readTextFile method, of class Twitter.
     */
    @Test
    public void testReadTextFile() throws Exception {
        System.out.println("readTextFile");
        String filePath = "";
        FileParser.readTextFile(filePath);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class Twitter.
     */
    @Test
    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = null;
        Twitter.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
