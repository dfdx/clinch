
package asi.simplelucene.gae;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.nutch.analysis.lang.LanguageIdentifier;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import asi.simplelucene.Doc;
import asi.simplelucene.QueryUtils;
import asi.simplelucene.Term;


public class Index {

	// Lucene constants
	private final static Field.Store STORE = Field.Store.YES;
	private final static Field.Index ANALYZE = Field.Index.ANALYZED;
	private final static int MAX_TOP_DOCS = 100;
	// fields (public to give other classes information what field names to use) 
	public final static String ID_FIELD = "id";
	public final static String LANGUAGE_FIELD = "language";
	public final static String CONTENTS_FIELD = "contents";
	// snippet constants
    private final static String FRAGMENTS_DELIMITER = " ... ";
    private final static int BEST_FRAGMENTS_COUNT = 2;   
    // utilities
    private final Object lockObj = new Object();
    private Directory indexDir;

	public Index() {
		this.indexDir = new RAMDirectory();
	}
	
	// *********** base functions ************
	
	public Doc retrieve(String id) throws Exception {
		List<Doc> queryResult = search("id:" + id);
		if (queryResult.isEmpty()) 
			return null;
		else if (queryResult.size() > 1) 
			throw new Exception("More than one document with such id");
		else return queryResult.get(0);
	}
	
	// NOTE: this method does NOT use id field, it retrieves document by number in Lucene 
	public Doc retrieveByLuceneNumber(Integer number) throws CorruptIndexException, IOException {
		IndexReader ir = IndexReader.open(this.indexDir);
		Doc res = luceneToNormalDoc(ir.document(number)); 
		ir.close();
		return res;
	}
	
	public List<Doc> search(String query) throws ParseException, CorruptIndexException, IOException {
		String langCode = QueryUtils.termValue(query, LANGUAGE_FIELD);					 
		Analyzer analyzer = selectAnalyzer(langCode);
        QueryParser parser = new QueryParser(CONTENTS_FIELD, analyzer);
        Query q = parser.parse(query);
        IndexReader ir = IndexReader.open(indexDir);
        List<Doc> res;
        try {
            q = q.rewrite(ir);
            TopDocCollector collector = new TopDocCollector(MAX_TOP_DOCS);
            Searcher searcher = new IndexSearcher(ir);
            searcher.search(q, collector);
            TopDocs results = collector.topDocs();
            ScoreDoc[] scoreDocs = results.scoreDocs;
            res = new ArrayList<Doc>();
            for (ScoreDoc scoreDoc : scoreDocs) {
                Doc doc = luceneToNormalDoc(ir.document(scoreDoc.doc));
                doc.setScore(scoreDoc.score);
                res.add(doc);
            }
        } finally {
            ir.close();
        }
        return res;
	}
	
	public String addDoc(Doc doc) throws IOException, InterruptedException {
		Analyzer analyzer = selectAnalyzer(doc);
		IndexWriter iw = null;
		String newId = generateUniqueId();
		doc.put(ID_FIELD, newId);  // rewrites old id, if any
		// synchronization for both different threads and different processes, accessing one index
		synchronized (lockObj) {
			try {
				while (IndexWriter.isLocked(indexDir)) {
					lockObj.wait();
				}
                iw = new IndexWriter(indexDir, analyzer, IndexWriter.MaxFieldLength.LIMITED);
                iw.addDocument(normalToLuceneDoc(doc));
                iw.commit();
			} finally {
				if (iw != null) {
					iw.close();
				}
				lockObj.notifyAll();
			}	
        }
		return newId;
	}
	
	public void deleteDoc(String id) throws CorruptIndexException, IOException, ParseException, InterruptedException {
		Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser(ID_FIELD, analyzer);
        Query q = parser.parse(ID_FIELD + ":" + id);
		synchronized (lockObj) {
            while (IndexWriter.isLocked(indexDir)) {
                lockObj.wait();
            }
            IndexWriter iw = null;
            try {
                iw = new IndexWriter(indexDir, null, IndexWriter.MaxFieldLength.LIMITED);
                iw.deleteDocuments(q);                
                iw.commit();
                iw.optimize();
            } finally {
                if (iw != null) {
                    iw.close();
                }
                lockObj.notifyAll();
            }
        }
	}
	

    public void updateDoc(String id, Doc newDocument) throws IOException, InterruptedException {
    	newDocument.put(ID_FIELD, id);
    	Analyzer analyzer = selectAnalyzer(newDocument);
        org.apache.lucene.index.Term term = new org.apache.lucene.index.Term(ID_FIELD, id);
        synchronized (lockObj) {
            while (IndexWriter.isLocked(indexDir)) {
                lockObj.wait();
            }
            IndexWriter iw = null;
            try {
                iw = new IndexWriter(indexDir, analyzer, IndexWriter.MaxFieldLength.LIMITED);
                iw.updateDocument(term, normalToLuceneDoc(newDocument));
                iw.commit();
            } finally {
                if (iw != null) {
                    iw.close();
                }
                lockObj.notifyAll();
            }
        }
    }
	
    /* traversing documents */
    
    
    public List<String> allDocIds() throws CorruptIndexException, IOException {
    	List<String> result = new ArrayList<String>();
    	IndexReader reader = IndexReader.open(indexDir);
    	int num = reader.numDocs();
    	for (int i = 0; i < num; i++) {
    		if (reader.isDeleted(i) == false) {
    			Document d = reader.document(i);
    			Field id = d.getField(ID_FIELD);
    			if (id != null)
    		    result.add(id.stringValue());
    		}
    	}
    	reader.close();
    	return result;
    }
    
    public List<Integer> allDocNums() throws CorruptIndexException, IOException {
    	List<Integer> ret = new ArrayList<Integer>();
    	IndexReader reader = IndexReader.open(indexDir);
    	int numDocs = reader.numDocs();
    	for (int i = 0; i < numDocs; i++) {
    		if (reader.isDeleted(i) == false) {
    		    ret.add(i);
    		}
    	}
    	reader.close();
    	return ret;
    }
    
	
	public List<String> allWords() throws CorruptIndexException, IOException {
		IndexReader reader = IndexReader.open(this.indexDir);
		TermEnum termEnum = reader.terms();
		List<String> ret = new ArrayList<String>();
		while (termEnum.next()) {
			if (CONTENTS_FIELD.equals(termEnum.term().field())) {
				ret.add(termEnum.term().text());
			}		
		}
		reader.close();
		return ret;
	}
    
	public List<Doc> termDocs(String field, String val) throws CorruptIndexException, IOException, ParseException {
		return search(field + ":(" + val + ")");
	}
	
//	public List<Doc> termDocs(Term term) throws CorruptIndexException, IOException {
//		IndexReader ir = IndexReader.open(this.index);
//		TermDocs termDocs = ir.termDocs();
//		List<Doc> res = new ArrayList<Doc>();
//		while (termDocs.next()) {
//			res.add(this.retrieveByLuceneNumber(termDocs.doc()));
//		}
//		ir.close();
//		return res;
//	}
	
	public List<Term> allTerms() throws CorruptIndexException, IOException {
		IndexReader ir = IndexReader.open(this.indexDir);
		TermEnum terms = ir.terms();
		List<Term> res = new ArrayList<Term>();
		while (terms.next()) {
            res.add(new Term(terms.term().field(), terms.term().text()));
        }
		return res;
	}
	
	/* frequency */
	
	public int frequency(int docNum, String field, String val) throws CorruptIndexException, IOException {
		//org.apache.lucene.index.Term term = new org.apache.lucene.index.Term(field, val);
		IndexReader ir = IndexReader.open(indexDir);
		TermFreqVector freqVector = ir.getTermFreqVector(docNum, field);
		String[] words = freqVector.getTerms();
		int[] frequencies = freqVector.getTermFrequencies();
		int ret = 0;
		for (int i = 0; i < words.length; i++) {
			if (words[i].equals(val)) {
				ret = frequencies[i];
				break;
			}
		}
		ir.close();
		return ret;
	}
	
	
	/* language manipulations */ 
	
	private Analyzer selectAnalyzer(String lang) {
		Analyzer langAnalyzer;
		if (lang == null || "".equals(lang)) {
			langAnalyzer = new StandardAnalyzer();
		} else if (lang.indexOf("en") == 0) {
            langAnalyzer = new StandardAnalyzer();
        } else if (lang.indexOf("ru") == 0) {
            langAnalyzer = new RussianAnalyzer();
        } else if (lang.indexOf("fr") == 0) {
            langAnalyzer = new FrenchAnalyzer();
        } else if (lang.indexOf("de") == 0) {
            langAnalyzer = new GermanAnalyzer();
        } else {
            langAnalyzer = new StandardAnalyzer();
        }
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(langAnalyzer);
        analyzer.addAnalyzer(ID_FIELD, new KeywordAnalyzer());
        //analyzer.addAnalyzer(ID, new KeywordAnalyzer());
        return analyzer;
	}
	
	private Analyzer selectAnalyzer(Doc doc) {
//		String analyzerField = doc.get(LANGUAGE_FIELD);
//		if (analyzerField != null) {
//			return selectAnalyzer(analyzerField);
//		} else {
//			String contents = doc.get(CONTENTS_FIELD);
//			if (contents != null) {
//				return selectAnalyzer(detectLang(contents));
//			} else {
//				return selectAnalyzer(""); // selectAnalyzer will forward that too
//			}
//			
//		}
		return new StandardAnalyzer();
	}
	
//	private String detectLang(String text) {
//        if (text == null || "".equalsIgnoreCase(text.trim())) {
//            return "en";
//        }
//		String lang = "";//Lang.getLanguage(text); // ибо OutOfMemory вылетает
//		if (lang.indexOf("English") != -1) return "en";
//		else if (lang.indexOf("Russian") != -1) return "ru";
//		else if (lang.indexOf("French") != -1) return "fr";
//		else if (lang.indexOf("German") != -1) return "de";
//		else return "en";
//	}
	
	public static String detectLang(String text) {
		LanguageIdentifier identifier = new LanguageIdentifier(new Configuration());
		return identifier.identify(text);
	}
	
	/* snippets */
	
	public String snippetForHTML(Doc doc, String query) {
		String contents = doc.get(CONTENTS_FIELD);
		if (contents != null) {
			try {
				Analyzer analyzer = selectAnalyzer(doc);
				QueryParser parser = new QueryParser(CONTENTS_FIELD, analyzer);
		        Query q = parser.parse(query);
				Highlighter hl = new Highlighter(new QueryScorer(q));
				TokenStream tokens = analyzer.tokenStream(null, new StringReader(contents));
				String fragments = hl.getBestFragments(tokens, contents, BEST_FRAGMENTS_COUNT, FRAGMENTS_DELIMITER);
				return fragments;
			} catch (Exception e) {
				return "<i>no snippet</i>";
			}
		} else {
			return "<i>no snippet</i>";
		}
		
	}
	
	/* utilities */
	
	private Doc luceneToNormalDoc(Document d) {
		Doc doc = new Doc();
		for (Object fObj : d.getFields()) {
			Field f = (Field)fObj;
			doc.put(f.name(), f.stringValue());
		}			
		return doc;
	}
	
	private Document normalToLuceneDoc(Doc doc) {
		Document d = new Document();
		for (String field : doc.keySet()) 
			d.add(new Field(field, doc.get(field), STORE, ANALYZE, Field.TermVector.YES));
		return d;
	}
	
	public static synchronized String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
	
}
