package org.example.libraryservice.nlp;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NLPService {

    private TokenizerME tokenizer;
    private POSTaggerME posTagger;

    // 👇 1. Define words to ALWAYS ignore
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
            "he", "him", "his", "she", "her", "hers", "it", "its", "they", "them", "their",
            "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are",
            "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does",
            "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until",
            "while", "of", "at", "by", "for", "with", "about", "against", "between", "into",
            "through", "during", "before", "after", "above", "below", "to", "from", "up", "down",
            "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here",
            "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more",
            "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so",
            "than", "too", "very", "can", "will", "just", "don", "should", "now",
            // 👇 DOMAIN SPECIFIC STOP WORDS (The most important part!)
            "book", "books", "novel", "novels", "story", "stories", "find", "search",
            "looking", "want", "read", "reading", "show", "give", "list"
    ));

    @PostConstruct
    public void init() {
        try {
            InputStream tokenStream = new ClassPathResource("models/en-token.bin").getInputStream();
            TokenizerModel tokenModel = new TokenizerModel(tokenStream);
            this.tokenizer = new TokenizerME(tokenModel);

            InputStream posStream = new ClassPathResource("models/en-pos-maxent.bin").getInputStream();
            POSModel posModel = new POSModel(posStream);
            this.posTagger = new POSTaggerME(posModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> extractKeywords(String sentence) {
        List<String> keywords = new ArrayList<>();

        // Clean the input slightly before processing
        String cleanSentence = sentence.replaceAll("[^a-zA-Z\\s]", "").toLowerCase();

        String[] tokens = tokenizer.tokenize(cleanSentence);
        String[] tags = posTagger.tag(tokens);

        for (int i = 0; i < tokens.length; i++) {
            String word = tokens[i];
            String tag = tags[i];

            // 2. Filter: Must be Noun/Adj AND NOT in the Stop Words list
            if ((tag.startsWith("NN") || tag.startsWith("JJ")) && !STOP_WORDS.contains(word)) {
                keywords.add(word);
            }
        }
        return keywords;
    }
}