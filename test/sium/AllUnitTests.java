package sium;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	sium.system.SetupTests.class,
	sium.nlu.context.ContextTest.class,
	sium.nlu.stat.DistributionTest.class,
	sium.nlu.language.LingEvidenceTest.class,
	sium.nlu.grounding.GroundingTest.class,
	// the MaxEntMappingTrainTest should come before MaxEntMappingEvalTest
	sium.nlu.language.MaxEntMappingTrainTest.class, 
	sium.nlu.language.MaxEntMappingEvalTest.class,
	sium.nlu.language.NaiveBayesMappingTest.class,
	sium.nlu.multi.DistanceDurationClassifierTest.class
//	sium.nlu.language.DeepLearningMappingTest.class
})


public class AllUnitTests {


}
