package Indexing;

import Classes.Path;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MyIndexWriter {
	// I suggest you to write very efficient code here, otherwise, your memory cannot hold our corpus...

    // block size
    private final int BLOCK_SIZE = 30000;

    // current path
	private String currentPath;

    // private Map<String, ArrayList<String>> index = new TreeMap<>();

    private Map<String, StringBuilder> index = new TreeMap<>();

	// collection frequency
	private Map<String, Long> collectionFreq = new HashMap<>();

	// dictionary between docno and docid
    private ArrayList<String> indexDocId = new ArrayList<>();

    // doc no.
	private int docId = 0;

	// block no.
	private int blockNo = 1;

	// using a linkedlist to store block no
	private LinkedList<Integer> blockNoList = new LinkedList<>();

	
	public MyIndexWriter(String type) throws IOException {
		// This constructor should initiate the FileWriter to output your index files
		// remember to close files if you finish writing the index
        if (type.equals("trectext")) {
            currentPath = Path.IndexTextDir;
        } else if (type.equals("trecweb")) {
            currentPath = Path.IndexWebDir;
        }
        File file = new File(currentPath);
        file.mkdir();
	}
	
	public void IndexADocument(String docno, String content) throws IOException {
		// you are strongly suggested to build the index by installments
		// you need to assign the new non-negative integer docId to each document, which will be used in MyIndexReader

        String[] contentArray = content.split(" ");
        List<String> list = Arrays.asList(contentArray);
        Set<String> set = new HashSet<>(list);
        indexDocId.add(docno);
		for (String s : set) {
            StringBuilder post = index.getOrDefault(s, new StringBuilder());
            if (post.length() == 0) {
                index.put(s, post);
            }
            post.append(docId + ":" + Collections.frequency(list, s) + ",");
		}

		for (String s: contentArray) {
            // save collection frequency into a file
            if (!collectionFreq.containsKey(s)) {
                collectionFreq.put(s, (long) 1);
            } else {
                collectionFreq.put(s, collectionFreq.get(s) + 1);
            }
        }
        docId++;

		/*
		    set the block size to 30000, it works on my computer.
		    If it does not work on your computer, just change the number.
		    The merge method should handle different block size.
		 */
		if (docId % BLOCK_SIZE == 0) {
		    writeBlock();
		    blockNo++;
        }
	}

    private void writeBlock() throws IOException {
	    blockNoList.add(blockNo);
	    FileWriter blockWriter = new FileWriter(currentPath + Path.PostingList + blockNo);

	    for (String key : index.keySet()) {
	        blockWriter.write(key + "|");
	        blockWriter.write(index.get(key).toString());
	        blockWriter.write("\n");
        }

	    index.clear();
	    blockWriter.close();
    }


    public void Close() throws IOException {
		// close the index writer, and you should output all the buffered content (if any).
		// if you write your index into several files, you need to fuse them here.

        // write collection frequency file. Format: TERM FREQUENCY
        FileWriter collectionWriter = new FileWriter(currentPath + Path.CollectionFrequency);

        for (String key: collectionFreq.keySet()) {
            collectionWriter.write(key + " " + collectionFreq.get(key));
            collectionWriter.write("\n");
        }
        collectionWriter.close();
        collectionFreq.clear();


        // write mapping between docno (String, like: XIE000.0000.0000, XIE000.0000.0001)
        //                      and docId (int, like 0, 1, 2 and so on)
        FileWriter dictWrtier = new FileWriter(currentPath + Path.Dictionary);

        for (String s : indexDocId) {
            dictWrtier.write(s);
            dictWrtier.write("\n");
        }
        dictWrtier.close();
        indexDocId.clear();

        // write rest of posting list to block
        writeBlock();

        // merge all of block files
        merge();

	}



    /* 
        in the merge method, the situation that same term which occurs in different blocks need to be handled.
        For example, if the block size is 20000, then the first block is document 1 to document 20000,
        the second block is document 20001 to 40000.
        If the first block contains term hello, and posting list:frequency is 1:2,3:1,2019:2.
            Format: hello|1:2,3:1,2019:2

        the second block also contains term hello, and posting:frequency is 20001:1,22019:2.
            Format: hello|20001:1,22019:2

        After merge, it should be hello|1:2,3:1,2019:2,20001:1,22019:2
    */
    private void merge() throws IOException {
        FileWriter postWriter = new FileWriter(currentPath + Path.PostingList);
        Iterator<Integer> iter = blockNoList.iterator();
        int curr = 0; // curr block id

        Map<String, StringBuilder> indexmap = new TreeMap<>();
        while (iter.hasNext()) {
            curr = iter.next();
            BufferedReader br = new BufferedReader(new FileReader(currentPath + Path.PostingList + curr));

            String line;
            while ((line = br.readLine()) != null) {
                String[] currList = line.split("\\|");
                String key = currList[0];
                String content = currList[1];

                StringBuilder sb = indexmap.getOrDefault(key, new StringBuilder());
                if (sb.length() == 0) {
                    indexmap.put(key, sb);
                }
                sb.append(content);
            }
            br.close();
        }

        for (String key : indexmap.keySet()) {
            postWriter.write(key + "|");
            postWriter.write(indexmap.get(key).toString());
            postWriter.write("\n");
        }

        postWriter.close();
    }
}
