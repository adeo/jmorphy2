package net.uaprom.jmorphy2.solr;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import net.uaprom.jmorphy2.nlp.Ruleset;
import net.uaprom.jmorphy2.nlp.Tagger;
import net.uaprom.jmorphy2.nlp.SimpleTagger;
import net.uaprom.jmorphy2.nlp.Parser;
import net.uaprom.jmorphy2.nlp.SimpleParser;
import net.uaprom.jmorphy2.nlp.SubjectExtractor;
import net.uaprom.jmorphy2.test._BaseTestCase;


@RunWith(JUnit4.class)
public class Jmorphy2SubjectFilterTest extends _BaseTestCase {
    private static final Version LUCENE_VERSION = Version.LUCENE_47;
    private static final String TAGGER_RULES_RESOURCE = "/tagger_rules.txt";
    private static final String PARSER_RULES_RESOURCE = "/parser_rules.txt";

    @Before
    public void setUp() throws IOException {
        initMorphAnalyzer();
    }

    @Test
    public void test() throws IOException {
        // TODO: use lucene-test-framework
        Tagger tagger =
            new SimpleTagger(morph,
                             new Ruleset(getClass().getResourceAsStream(TAGGER_RULES_RESOURCE)));
        Parser parser =
            new SimpleParser(morph,
                             tagger,
                             new Ruleset(getClass().getResourceAsStream(PARSER_RULES_RESOURCE)));
        final SubjectExtractor subjExtractor =
            new SubjectExtractor(parser,
                                 "+NP,nomn +NP,accs -PP NOUN,nomn NOUN,accs LATN NUMB",
                                 true);

        Analyzer analyzer =
            new Analyzer() {
                @Override
                protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
                    Tokenizer source = new WhitespaceTokenizer(LUCENE_VERSION, reader);
                    Jmorphy2SubjectFilter filter = new Jmorphy2SubjectFilter(source, subjExtractor);
                    return new TokenStreamComponents(source, filter);
                }
            };

        assertAnalyzesTo(analyzer,
                         "",
                         Arrays.asList(new String[0]));
        assertAnalyzesTo(analyzer,
                         "теплые перчатки",
                         Arrays.asList(new String[]{"перчатка"}));
        assertAnalyzesTo(analyzer,
                         "магнит на холодильник",
                         Arrays.asList(new String[]{"магнит"}));
        assertAnalyzesTo(analyzer,
                         "чехол for iphone",
                         Arrays.asList(new String[]{"чехол"}));
    }

    private void assertAnalyzesTo(Analyzer analyzer, String sent, List<String> expected) throws IOException {
        TokenStream ts = analyzer.tokenStream("dummy", sent);
        CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
        List<String> tokens = new ArrayList<String>();
        ts.reset();
        for (int i = 0; ts.incrementToken(); i++) {
            tokens.add(new String(termAtt.buffer(), 0, termAtt.length()));
        }
        ts.close();
        assertEquals(expected, tokens);
    }
}