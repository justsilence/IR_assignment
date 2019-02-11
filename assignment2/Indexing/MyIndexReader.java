package Indexing;

import Classes.Path;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class MyIndexReader {
	//you are suggested to write very efficient code here, otherwise, your memory cannot hold our corpus...

    private String currentPath;

    private Map<String, Long> collFreq;
	private ArrayList<String> docNo2Id;

	private int[][] postingList;

	private int frequency = 0;



	
	public MyIndexReader( String type ) throws IOException {
		//read the index files you generated in task 1
		//remember to close them when you finish using them
		//use appropriate structure to store your index
        if (type.equals("trectext")) {
            currentPath = Path.IndexTextDir;
        } else if (type.equals("trecweb")) {
            currentPath = Path.IndexWebDir;
        }

        // read collection frequency
        FileReader fr = new FileReader(currentPath + Path.CollectionFrequency);
        BufferedReader br = new BufferedReader(fr);

        collFreq = new TreeMap<>();
        String line;
        while ((line = br.readLine()) != null) {
            String[] content = line.split(" ");
            collFreq.put(content[0], Long.parseLong(content[1]));
        }
        br.close();
        fr.close();

        // read index between docno and docid
        fr = new FileReader(currentPath + Path.Dictionary);
        br = new BufferedReader(fr);

        docNo2Id = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            docNo2Id.add(line.trim());
        }
        br.close();
        fr.close();
	}



	
	//get the non-negative integer dociId for the requested docNo
	//If the requested docno does not exist in the index, return -1
	public int GetDocid( String docno ) {
	    if (docNo2Id.contains(docno))
		    return docNo2Id.indexOf(docno);
	    return -1;
	}

	// Retrieve the docno for the integer docid
	public String GetDocno( int docid ) {
	    if (docid >= 0 && docid < docNo2Id.size())
		    return docNo2Id.get(docid);
	    return null;
	}
	
	/**
	 * Get the posting list for the requested token.
	 * 
	 * The posting list records the documents' docids the token appears and corresponding frequencies of the term, such as:
	 *  
	 *  [docid]		[freq]
	 *  1			3
	 *  5			7
	 *  9			1
	 *  13			9
	 * 
	 * ...
	 * 
	 * In the returned 2-dimension array, the first dimension is for each document, and the second dimension records the docid and frequency.
	 * 
	 * For example:
	 * array[0][0] records the docid of the first document the token appears.
	 * array[0][1] records the frequency of the token in the documents with docid = array[0][0]
	 * ...
	 * 
	 * NOTE that the returned posting list array should be ranked by docid from the smallest to the largest. 
	 * 
	 * @param token
	 * @return
	 */

	// In the file: postings_list, format looks like that: TERM|posting0,posting1,......
    // Also, know that I have already store mapping between docno and docid
	public int[][] GetPostingList( String token ) throws IOException {
	    if (!collFreq.containsKey(token))
		    return null;

//	    FileReader fr = new FileReader(file + Path.PostingList);
//	    BufferedReader br = new BufferedReader(fr);
//	    String line;
//	    String str = new String();
//	    while ((line = br.readLine()) != null) {
//	        String[] content = line.split("|");
//	        if (content[0].equals(token)) {
//	            str = content[1];
//	            break;
//            }
//        }
//	    br.close();
//	    fr.close();
//	    String[] contentOfPosting = str.split(",");
//	    frequency = contentOfPosting.length;
//	    int[][] postingList = new int[contentOfPosting.length][2];
//	    for (int i = 0; i < contentOfPosting.length; ++i) {
//	        String[] currPost = contentOfPosting[i].split(":");
//            postingList[i][0] = Integer.parseInt(currPost[0]);
//            postingList[i][1] = Integer.parseInt(currPost[1]);
//        }

	    return postingList;
	}

	// Return the number of documents that contains the token.
	public int GetDocFreq( String token ) throws IOException {
        if (!collFreq.containsKey(token))
            return 0;

        FileReader fr = new FileReader(currentPath + Path.PostingList);
        BufferedReader br = new BufferedReader(fr);
        String line;
        String str = "";
        while ((line = br.readLine()) != null) {
            String[] content = line.split("\\|");
            if (content[0].equals(token)) {
                str = content[1];
                break;
            }
        }
        br.close();
        fr.close();
        String[] contentOfPosting = str.split(",");
        frequency = contentOfPosting.length;
        postingList = new int[contentOfPosting.length][2];
        for (int i = 0; i < contentOfPosting.length; ++i) {
            String[] currPost = contentOfPosting[i].split(":");
            postingList[i][0] = Integer.parseInt(currPost[0]);
            postingList[i][1] = Integer.parseInt(currPost[1]);
        }
	    return frequency;
	}
	
	// Return the total number of times the token appears in the collection.
	public long GetCollectionFreq( String token ) throws IOException {
	    long freq = 0;
	    if (collFreq.containsKey(token)) {
	        freq = collFreq.get(token);
        }
	    return freq;
	}
	
	public void Close() throws IOException {
	}

	
}