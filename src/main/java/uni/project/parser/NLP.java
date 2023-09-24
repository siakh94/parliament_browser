package uni.project.parser;

import org.apache.uima.fit.factory.AggregateBuilder;
import org.hucompute.textimager.fasttext.labelannotator.LabelAnnotatorDocker;
import org.hucompute.textimager.uima.gervader.GerVaderSentiment;
import org.hucompute.textimager.uima.spacy.SpaCyMultiTagger3;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

/**
 * this method was taken from the sample solution
 * it helps applying NLP to our program
 */
public class NLP {
    private AggregateBuilder aggregateBuilder = new AggregateBuilder();

    /**
     * constructor
     *
     * @return
     */
    public AggregateBuilder getAggregateBuilder() {
        return this.aggregateBuilder;
    }

    /**
     * @return a boolean which indicates if the engine has been started !!!
     */
    public boolean initPipeline() {

        try {
            aggregateBuilder.add(createEngineDescription(SpaCyMultiTagger3.class,
                    SpaCyMultiTagger3.PARAM_REST_ENDPOINT, "http://spacy.lehre.texttechnologylab.org"
            ));

            aggregateBuilder.add(createEngineDescription(GerVaderSentiment.class,
                    GerVaderSentiment.PARAM_REST_ENDPOINT, "http://gervader.lehre.texttechnologylab.org",
                    GerVaderSentiment.PARAM_SELECTION, "text,de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"
            ));

            aggregateBuilder.add(createEngineDescription(LabelAnnotatorDocker.class,
                    LabelAnnotatorDocker.PARAM_FASTTEXT_K, 100,
                    LabelAnnotatorDocker.PARAM_CUTOFF, false,
                    LabelAnnotatorDocker.PARAM_SELECTION, "text",
                    LabelAnnotatorDocker.PARAM_TAGS, "ddc3",
                    LabelAnnotatorDocker.PARAM_USE_LEMMA, true,
                    LabelAnnotatorDocker.PARAM_ADD_POS, true,
                    LabelAnnotatorDocker.PARAM_POSMAP_LOCATION, "src/main/resources/am_posmap.txt",
                    LabelAnnotatorDocker.PARAM_REMOVE_FUNCTIONWORDS, true,
                    LabelAnnotatorDocker.PARAM_REMOVE_PUNCT, true,
                    LabelAnnotatorDocker.PARAM_REST_ENDPOINT, "http://ddc.lehre.texttechnologylab.org"
            ));
            return true;
        } catch (Exception ex) {
            System.out.println("Couldn't create the NlpPipeline, error: " + ex.getMessage());
            return false;
        }
    }
}
