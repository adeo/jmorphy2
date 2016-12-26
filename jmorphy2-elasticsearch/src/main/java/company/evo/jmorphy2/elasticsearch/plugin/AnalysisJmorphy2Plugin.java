package company.evo.jmorphy2.elasticsearch.plugin;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.env.Environment;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenFilterFactory;
// import org.elasticsearch.index.analysis.compound.DictionaryCompoundWordTokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import company.evo.jmorphy2.elasticsearch.index.Jmorphy2AnalyzerProvider;
import company.evo.jmorphy2.elasticsearch.index.Jmorphy2StemTokenFilterFactory;
import company.evo.jmorphy2.elasticsearch.index.Jmorphy2SubjectTokenFilterFactory;
import company.evo.jmorphy2.elasticsearch.indices.Jmorphy2Service;


public class AnalysisJmorphy2Plugin extends Plugin implements AnalysisPlugin {
    private final Jmorphy2Service jmorphy2Service;

    public AnalysisJmorphy2Plugin(Settings settings) throws IOException {
        super();
        System.out.println(settings.get(Environment.PATH_CONF_SETTING.getKey()));
        jmorphy2Service = new Jmorphy2Service(settings);
    }

    @Override
    public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {

        Map<String, AnalysisProvider<TokenFilterFactory>> tokenFilters = new HashMap<>();
        tokenFilters.put("jmorphy2_stemmer", new Jmorphy2AnalysisProvider() {
                @Override
                public TokenFilterFactory get(IndexSettings indexSettings,
                                              Environment environment,
                                              String name,
                                              Settings settings) {
                    return new Jmorphy2StemTokenFilterFactory
                        (indexSettings, environment, name, settings, jmorphy2Service);
                }
            });
        // tokenFilters.put("jmorphy2_stemmer", Jmorphy2StemTokenFilterFactory::new);
        // tokenFilters.put("jmorphy2_subject", Jmorphy2SubjectTokenFilterFactory::new);
        return tokenFilters;
    }

    // @Override
    // public Map<String, AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
    //     return Collections.singletonMap("jmorphy2_analyzer", Jmorphy2AnalyzerProvider::new);
    // }

    public interface Jmorphy2AnalysisProvider extends AnalysisProvider<TokenFilterFactory> {
        @Override
        default boolean requiresAnalysisSettings() {
            return true;
        }
    }
}
