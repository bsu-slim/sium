# Simple Incremental Update Model (Java)


### Overview

You can use `gradle build` to get all of the required libraries. 

Run the sium.app.PentoDemo and it will run a 10-fold evaluation 
on Pento data (as in Kennington et al., 2013) for the "object" slot only. 

In PentoDemo, you can change the NaiveBayesMapping to either MaxEntMapping or LanguageModelMapping and note the different
accuracies. You can also run test.sium.AllUnitTests as a junit test, and if all tests pass, then it is working properly. 

### How it works

The two main data structures are Context and LingEvidence. The context represents the "world" as a set of entities 
and properties belonging to those entities. See test.sium.context.ContextTest for an example where two entities with
two properties each are created. The name of the entities is arbitrary, as are the properties, but the properties should
be consistent during training and evaluation. 

LingEvidence contains, as expected, linguistic evidence. This, coupled with a context, is what is given to the model
to learn from. The model learns to map between linguistic evidence and the properties in a context. Hence, the next
important object is the Mapping object. At the moment, there are three available:

- NaiveBayesMapping
- MaxEntMapping
- LangaugeModelMapping

A Mapping object takes a LingEvidence/Context row pair and sets the linguistic evidence as features and the properties
as the class labels. 

During runtime, the Mapping object is given only a LingEvidence object, which then returns a Distribution, which is
then passed to a Grounder which is what functions incrementally to produce an ongoing Distribution over the entities. 
	
### References

```
@article{KENNINGTON201743,
title = "A simple generative model of incremental reference resolution for situated dialogue",
journal = "Computer Speech & Language",
volume = "41",
pages = "43 - 67",
year = "2017",
issn = "0885-2308",
doi = "https://doi.org/10.1016/j.csl.2016.04.002",
url = "http://www.sciencedirect.com/science/article/pii/S0885230815300127",
author = "Casey Kennington and David Schlangen",
keywords = "Dialogue, Situated, Incremental, Stochastic, Reference resolution"
}
```

```
@inproceedings{Kennington2013a,
author = {Kennington, Casey and Kousidis, Spyros and Schlangen, David},
booktitle = {SIGdial 2013},
title = {{Interpreting Situated Dialogue Utterances: an Update Model that Uses Speech, Gaze, and Gesture Information}},
year = {2013}
}
```


Mapping Classifiers:

MaxEnt: https://opennlp.apache.org/

NaiveBayes:  https://github.com/ptnplanet/Java-Naive-Bayes-Classifier

LanguageModel: Suffix Tree Language Model in caseyreddkennington.com
