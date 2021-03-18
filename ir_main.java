package irassign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class ir_main {
	
	public static String lemmatization(String word) {
		
		
		
		String sword=word;
		int iWordLength=0;
		
		iWordLength=sword.length();
		int wordEnd=0;
		
		String [] end= {"sses","ies","y","s"};
		String [] choped= {"ss","i","i",""};
		
		int scanLength=end.length;
		
		for(int z=0;z<scanLength;z++) {
			
			
			wordEnd=end[z].length();
			
			if(iWordLength>wordEnd) {
			if(sword.substring(iWordLength-wordEnd).equals(end[z])) {
				sword=sword.substring(0,iWordLength-wordEnd)+choped[z];
				break;
				
			}
			}
		}
		
		
		
		return sword;
		
	}

	public static void main(String[] args) {
		
		
		
		//BEGIN - Find the current directory
		String cwd="";
		File fFile=new File("");
		cwd=fFile.getAbsolutePath();
		System.out.println("Current Directory detected as: "+cwd);
		//END - Find the current directory
		

		// BEGIN - Read stop list
		File stop_list = null;
		BufferedReader br = null;
		String[] stop_words_list = null;
		int stop_word_count = 0;
		try {
			System.out.println("Reading stop list...");
			stop_list = new File(cwd+"//stop_words//stop.txt");
			br = new BufferedReader(new FileReader(stop_list));
			String stop_word_line = br.readLine();
			if (stop_word_line != null) {
				stop_words_list = stop_word_line.split(",");
				stop_word_count = stop_words_list.length;
			}

			System.out.println("Total stop words detected:"+stop_word_count);
			if (stop_word_count > 0) {
				for (int m = 0; m < stop_word_count; m++) {
					stop_words_list[m] = stop_words_list[m].toLowerCase();
				}
			}

		} catch (Exception e4) {

			e4.printStackTrace();

		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// System.out.println(stop_words_list[0]+" "+stop_words_list[1]);

		// END - Read stop list

		String[] sTokens = null;
		int iDocCount = 0;
		int iTotalTokens = 0;

		// START -- Get dictionary size

		System.out.println("Constructing the dictionary...");
		File folder = new File(cwd+"//irdataset");

		HashSet<String> dictionary = new HashSet<String>();

		iDocCount = folder.listFiles().length;
		int iTotalTokensProcessed=0;

		for (final File fileEntry : folder.listFiles()) {

			// System.out.println(fileEntry.getName());

			// Read docx files

			try {

				System.out.println("Processing " + fileEntry.getName() + "...");
				File file = new File(cwd+"//irdataset//" + fileEntry.getName());
				FileInputStream fis = new FileInputStream(file.getAbsolutePath());

				XWPFDocument document = new XWPFDocument(fis);

				List<XWPFParagraph> paragraphs = document.getParagraphs();

				// System.out.println(paragraphs.size());

				int iTokenLen = 0;
				int iDontAddFlag = 0;

				for (XWPFParagraph para : paragraphs) {
					if (para.getText().length() > 1) {
						sTokens = para.getText().split(" ");
						iTokenLen = sTokens.length;
						iTotalTokens += iTokenLen;
						System.out.println("Total tokens in "+fileEntry.getName()+":" + sTokens.length);
						//System.out.println(sTokens[0]);

						for (int i = 0; i < iTokenLen; i++) {

							sTokens[i] = sTokens[i].toLowerCase();

							for (int c = 0; c < stop_word_count; c++) {
								if (sTokens[i].equals(stop_words_list[c])) {
									iDontAddFlag = 1;
									break;
								}

							}
							if (iDontAddFlag != 1) {
								//System.out.println(lemmatization(sTokens[i]));
								dictionary.add(lemmatization(sTokens[i]));
								iTotalTokensProcessed++;
							} else {
								iDontAddFlag = 0;
							}

						}

					}

				}
				
				fis.close();
				document.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		List<String> al = new ArrayList<String>(dictionary);
		Collections.sort(al);

		//System.out.println(al.size());
		System.out.println("\n\n");
		System.out.println("Total tokens in the dataset:" + iTotalTokens);
		System.out.println("Total tokens processed after removing the stop words (if specified):"+iTotalTokensProcessed);
		System.out.println("Dictionary size:" + dictionary.size());
		System.out.println("\n\n");

		//System.out.println("Printing dictionary...");

		// END -- Get dictionary size

		// BEGIN - Index construction

		int iDocCounter = 0;

		ArrayList<String> index_term = new ArrayList<String>(al.size());
		Integer[] index_term_freq = new Integer[al.size()];
		Integer[][] index_term_doc_freq = new Integer[al.size()][iDocCount];
		List<Integer>[][] index_term_doc_position = new List[al.size()][iDocCount];

		HashMap<String, Integer> hmap = new HashMap<String, Integer>();

		for (int j = 0; j < al.size(); j++) {

			hmap.put(al.get(j), j);
			index_term.add(al.get(j));
			index_term_freq[j] = 0;

			for (int k = 0; k < iDocCount; k++) {
				index_term_doc_freq[j][k] = 0;
				index_term_doc_position[j][k] = null;
			}

			//System.out.println(al.get(j));

		}

		File folder1 = new File(cwd+"//irdataset");

		for (final File fileEntry : folder1.listFiles()) {

			// System.out.println(fileEntry.getName());

			// Read docx files

			int iIndexPos = 0;

			try {

				System.out.println("Processing " + fileEntry.getName() + " for building an index...");
				File file = new File(cwd+"//irdataset//" + fileEntry.getName());
				FileInputStream fis = new FileInputStream(file.getAbsolutePath());

				XWPFDocument document = new XWPFDocument(fis);

				List<XWPFParagraph> paragraphs = document.getParagraphs();

				// System.out.println(paragraphs.size());

				int iTokenLen = 0;

				List<Integer> li = null;
				for (XWPFParagraph para : paragraphs) {
					if (para.getText().length() > 1) {

						sTokens = para.getText().split(" ");
						iTokenLen = sTokens.length;

						for (int i = 0; i < iTokenLen; i++) {
							sTokens[i] = sTokens[i].toLowerCase();

							try {
								iIndexPos = hmap.get(lemmatization(sTokens[i]));

								index_term_freq[iIndexPos]++;
								index_term_doc_freq[iIndexPos][iDocCounter] += 1;
								li = index_term_doc_position[iIndexPos][iDocCounter];
								// System.out.println(li);
								if (li == null) {
									//System.out.println("creating a list");
									index_term_doc_position[iIndexPos][iDocCounter] = new LinkedList<Integer>();
									index_term_doc_position[iIndexPos][iDocCounter].add((i + 1));
								} else {

									li.add((i + 1));

								}

								// System.out.println(iIndexPos);
							} catch (Exception e1) {

							}

						}

						iDocCounter++;

					}

				}
				fis.close();
				document.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// END - Index construction

		}
		
		System.out.println(" ");

		// BEGIN - Print Index
		FileWriter oIndex = null;
		BufferedWriter bw = null;
		try {
			oIndex = new FileWriter(cwd+"//index_output//index.txt");
			bw = new BufferedWriter(oIndex);

			int iDictionarySize = al.size();
			String outline = "";
			List<Integer> postingList = null;
			int postingLength = 0;

			for (int h = 0; h < iDictionarySize; h++) {

				outline = al.get(h) + "," + index_term_freq[h] + ":{";

				for (int a = 0; a < iDocCount; a++) {

					if (index_term_doc_freq[h][a] != 0) {
						outline += " Doc" + (a + 1) + ".docx," + index_term_doc_freq[h][a] + ": {";

						postingList = index_term_doc_position[h][a];

						if (postingList == null) {
							outline += "}; ";
						} else {
							postingLength = postingList.size();
							for (int b = 0; b < postingLength; b++) {
								outline += postingList.get(b);
								if (b != postingLength - 1) {
									outline += ", ";
								} else {
									outline += "}; ";
								}
							}
						}
					}

				}

				outline += "}\n";

				// System.out.println(outline);
				bw.write(outline);

			}
		} catch (Exception e2) {

		} finally {
			try {
				bw.close();
				oIndex.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		// END - Print Index

		// BEGIN - Search part using given input

		String[] queryTokens = null;
		int iQueryTokensCount = 0;
		int iOperatorPos = 0;
		int iPerformSearch = 1;
		int iIndexPosSearch = -1;
		String result = "";
		int iDidFindAnything = 0;
		int iIndexPosFirstTerm = -1;
		int iIndexPosSecondTerm = -1;

		Scanner scan = new Scanner(System.in);
		String query = "";
		do {
			iOperatorPos = 0;
			iPerformSearch = 1;
			iIndexPosFirstTerm = -1;
			iIndexPosSecondTerm = -1;

			System.out.println("\n\n");
			System.out.print("Enter a search query, to exit type '#exit'> ");
			query = scan.nextLine();
			System.out.println(" ");
			//System.out.println(query);
			queryTokens = query.split(" ");
			iQueryTokensCount = queryTokens.length;
			if (iQueryTokensCount > 0) {

				if (!queryTokens[0].equals("#exit")) {
					for (int e = 0; e < iQueryTokensCount; e++) {
						queryTokens[e] = queryTokens[e].toLowerCase();

						if (queryTokens[e].equals("and") || queryTokens[e].equals("or")) {
							iOperatorPos = e;
							if (iOperatorPos != iQueryTokensCount / 2 ) {
								
								if(iQueryTokensCount>=3) {
									
									if(iQueryTokensCount==3)
										iOperatorPos=1;
									
									if(iQueryTokensCount==4) {
									
										if(queryTokens[2].equals("not")) {
											iOperatorPos=1;
										}else {
										iOperatorPos=2;
										}
									}
									
									if(iQueryTokensCount==5)
										iOperatorPos=2;
								}else {
									System.out.println("Error: It seems that the query is not properly formed. Please review your query: "+query+"\n");
								}
								iPerformSearch = 0;
								break;
							}
						}

					}

					try {
						if (iQueryTokensCount <= 2 && iPerformSearch==1) {
							//System.out.println("Performing search...");
							if (queryTokens[0].equals("not")) {
								iIndexPosSearch = hmap.get(lemmatization(queryTokens[1]));
								for (int f = 0; f < iDocCount; f++) {
									if (index_term_doc_freq[iIndexPosSearch][f] == 0) {
										iDidFindAnything = 1;
										result += "Doc" + (f + 1) + ".docx ";
									}
								}

								if (iDidFindAnything == 1) {
									
									if(result.equals("")) {
										result="No macth found";
									}
									System.out.println("The following documents don't contain the term '"
											+ queryTokens[1] + "'-> " + result);
									result = "";
									iDidFindAnything = 0;
								} else {
									System.out.println("No documents match the query: " + query);
								}
							} else {

								iIndexPosSearch = hmap.get(lemmatization(queryTokens[0]));
								for (int f = 0; f < iDocCount; f++) {
									if (index_term_doc_freq[iIndexPosSearch][f] > 0) {
										iDidFindAnything = 1;
										result += "Doc" + (f + 1) + ".docx ";
									}
								}

								if (iDidFindAnything == 1) {
									if(result.equals("")) {
										result="No match found";
									}
									System.out.println("The following documents contain the term '" + queryTokens[0]
											+ "'-> " + result);
									result = "";
									iDidFindAnything = 0;
								} else {
									System.out.println("No documents match the query: " + query);
								}

							}

						}

						if (iQueryTokensCount >= 3 && iQueryTokensCount <= 5) {

							String sFirstTerm = "";
							String sSecondTerm =lemmatization(queryTokens[iQueryTokensCount - 1]);

							int sFirstTermNotFlag = 0;
							int sSecondTermNotFlag = 0;

							Integer[] sFirstTermFreq = new Integer[iDocCount];
							Integer[] sSecondTermFreq = new Integer[iDocCount];

							for (int s = 0; s < iDocCount; s++) {
								sFirstTermFreq[s] = 0;
								sSecondTermFreq[s] = 0;
							}

							if (iOperatorPos == 1) {
								sFirstTerm = lemmatization(queryTokens[0]);

							}

							if (iOperatorPos == 2) {
								sFirstTerm = lemmatization(queryTokens[1]);
								sFirstTermNotFlag = 1;

							}

							if ((iQueryTokensCount - 1) - iOperatorPos > 1) {
								sSecondTermNotFlag = 1;
							}

							try {
								iIndexPosFirstTerm = hmap.get(sFirstTerm);
								for (int n = 0; n < iDocCount; n++) {
									sFirstTermFreq[n] = index_term_doc_freq[iIndexPosFirstTerm][n];
									if (sFirstTermNotFlag == 1) {
										if (sFirstTermFreq[n] > 0) {
											sFirstTermFreq[n] = 0;
										} else {
											sFirstTermFreq[n] = 1;
										}
									} else {
										if (sFirstTermFreq[n] > 0) {
											sFirstTermFreq[n] = 1;
										}
									}
								}
							} catch (Exception e9) {
								iIndexPosFirstTerm = -1;
							}

							try {
								iIndexPosSecondTerm = hmap.get(sSecondTerm);

								for (int n = 0; n < iDocCount; n++) {
									sSecondTermFreq[n] = index_term_doc_freq[iIndexPosSecondTerm][n];
									if (sSecondTermNotFlag == 1) {
										if (sSecondTermFreq[n] > 0) {
											sSecondTermFreq[n] = 0;
										} else {
											sSecondTermFreq[n] = 1;
										}
									} else {
										if (sSecondTermFreq[n] > 0) {
											sSecondTermFreq[n] = 1;
										}
									}
								}

							} catch (Exception e9) {
								iIndexPosSecondTerm = -1;
							}

							String finalResult = "";

							if (queryTokens[iOperatorPos].equals("and")) {

								for (int q = 0; q < iDocCount; q++) {

									//System.out.println(sFirstTermFreq[q] + sSecondTermFreq[q]);
									if ((sFirstTermFreq[q] + sSecondTermFreq[q]) > 1) {
										finalResult += "Doc" + (q + 1) + ".docx ";
									}
								}

								if(finalResult.equals("")) {
									finalResult="No match found";
								}
								System.out
										.println("The following docs match the query: '" + query + "' -> " + finalResult);

							}

							if (queryTokens[iOperatorPos].equals("or")) {

								for (int q = 0; q < iDocCount; q++) {

									// System.out.println(sFirstTermFreq[q]+sSecondTermFreq[q]);
									if ((sFirstTermFreq[q] + sSecondTermFreq[q]) > 0) {
										finalResult += "Doc" + (q + 1) + ".docx ";
									}
								}
								
								if(finalResult.equals("")) {
									finalResult="No match found";
								}

								System.out
										.println("The following docs match the query: '" + query + "' -> " + finalResult);

							}

							System.out.println(" ");

						}
					} catch (Exception e8) {
						System.out.println("The given query is not part of any document"+"\n");
					}

				}
			}

		} while (!query.equals("#exit"));

		// END - Search part using given input

	}

}
