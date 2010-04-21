 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
*/
package org.cleartk.classifier.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarFile;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.jar.JarDataWriterFactory;
import org.cleartk.classifier.jar.Train;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uimafit.factory.UimaContextFactory;
import org.uimafit.util.HideOutput;
import org.uimafit.util.TearDownUtil;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.
 * 
 * @author Steven Bethard
*/
public class RunLIBSVMTest {

	protected Random random;
	protected String outputDirectory = "test/data/libsvm";
	
	@Before
	public void setUp() {
		random = new Random(System.currentTimeMillis());
	}
	
	@After
	public void tearDown() throws Exception {
		File outputDirectory = new File(this.outputDirectory);
		TearDownUtil.removeDirectory(outputDirectory);
		Assert.assertFalse(outputDirectory.exists());
	}
	
	@Test
	public void testBinaryLIBSVM() throws Exception {
		
		// create the data writer
		BinaryAnnotator annotator = new BinaryAnnotator();		
		annotator.initialize(UimaContextFactory.createUimaContext(
				JarDataWriterFactory.PARAM_OUTPUT_DIRECTORY, this.outputDirectory,
				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, DefaultBinaryLIBSVMDataWriterFactory.class.getName()));
		
		// run process to produce a bunch of instances
		annotator.process(null);
		
		annotator.collectionProcessComplete();
		
		// check that the output file was written and is not empty
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				this.outputDirectory, "training-data.libsvm")));
		Assert.assertTrue(reader.readLine().length() > 0);
		reader.close();
		
		// run the training command
		HideOutput hider = new HideOutput();
		Train.main(this.outputDirectory, "-c", "1.0", "-s", "0", "-t", "0");
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		JarFile modelFile = new JarFile(new File(this.outputDirectory, "model.jar")); 
		BinaryLIBSVMClassifier classifier = new BinaryLIBSVMClassifier(modelFile);
		modelFile.close();
		for (Instance<Boolean> instance: generateBooleanInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			Boolean outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}

	@Test
	public void testLIBLINEAR() throws Exception {
		
		// create the data writer
		BinaryAnnotator annotator = new BinaryAnnotator();		
		annotator.initialize(UimaContextFactory.createUimaContext(
				JarDataWriterFactory.PARAM_OUTPUT_DIRECTORY, this.outputDirectory,
				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, DefaultLIBLINEARDataWriterFactory.class.getName()));
		
		// run process to produce a bunch of instances
		annotator.process(null);
		
		annotator.collectionProcessComplete();
		
		// check that the output file was written and is not empty
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				this.outputDirectory, "training-data.libsvm")));
		Assert.assertTrue(reader.readLine().length() > 0);
		reader.close();
		
		// run the training command
		HideOutput hider = new HideOutput();
		Train.main(this.outputDirectory, "-c", "1.0", "-s", "1");
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		JarFile modelFile = new JarFile(new File(this.outputDirectory, "model.jar")); 
		LIBLINEARClassifier classifier = new LIBLINEARClassifier(modelFile);
		modelFile.close();
		for (Instance<Boolean> instance: generateBooleanInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			Boolean outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}

	@Test
	public void testMultiClassLIBSVM() throws Exception {
		
		// create the data writer
		StringAnnotator annotator = new StringAnnotator();		
		annotator.initialize(UimaContextFactory.createUimaContext(
				JarDataWriterFactory.PARAM_OUTPUT_DIRECTORY, this.outputDirectory,
				CleartkAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, DefaultMultiClassLIBSVMDataWriterFactory.class.getName()));
		
		// run process to produce a bunch of instances
		annotator.process(null);
		
		annotator.collectionProcessComplete();

		// check that the output files were written for each class
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				this.outputDirectory, "training-data.libsvm")));
		Assert.assertTrue(reader.readLine().length() > 0);
		reader.close();
		
		// run the training command
		HideOutput hider = new HideOutput();
		Train.main(this.outputDirectory, "-c", "1.0", "-t", "2");
		hider.restoreOutput();
		
		// read in the classifier and test it on new instances
		JarFile modelFile = new JarFile(new File(this.outputDirectory, "model.jar")); 
		MultiClassLIBSVMClassifier classifier = new MultiClassLIBSVMClassifier(modelFile);
		modelFile.close();

		for (Instance<String> instance: generateStringInstances(1000)) {
			List<Feature> features = instance.getFeatures();
			String outcome = instance.getOutcome();
			Assert.assertEquals(outcome, classifier.classify(features));
		}
	}
	

	private static List<Instance<Boolean>> generateBooleanInstances(int n) {
		Random random = new Random(42);
		List<Instance<Boolean>> instances = new ArrayList<Instance<Boolean>>();
		for (int i = 0; i < n; i++) {
			Instance<Boolean> instance = new Instance<Boolean>();
			if (random.nextInt(2) == 0) {
				instance.setOutcome(true);
				instance.add(new Feature("hello", random.nextInt(100) + 1000));
				instance.add(new Feature("goodbye", 500));
			}
			else {
				instance.setOutcome(false);
				instance.add(new Feature("hello", random.nextInt(100)));
				instance.add(new Feature("goodbye", 500));
			}
			instances.add(instance);
		}
		return instances;
	}
	
	private static List<Instance<String>> generateStringInstances(int n) {
		Random random = new Random(42);
		List<Instance<String>> instances = new ArrayList<Instance<String>>();
		for (int i = 0; i < n; i++) {
			Instance<String> instance = new Instance<String>();
			int c = random.nextInt(3);
			if ( c == 0 ) {
				instance.setOutcome("A");
				instance.add(new Feature("hello", random.nextInt(100) + 950));
				instance.add(new Feature("goodbye", random.nextInt(100)));
				instance.add(new Feature("farewell", random.nextInt(100)));
			}
			else if( c == 1 ) {
				instance.setOutcome("B");
				instance.add(new Feature("hello", random.nextInt(100)));
				instance.add(new Feature("goodbye", random.nextInt(100) + 950));
				instance.add(new Feature("farewell", random.nextInt(100)));
			} else {
				instance.setOutcome("C");
				instance.add(new Feature("hello", random.nextInt(100)));
				instance.add(new Feature("goodbye", random.nextInt(100)));
				instance.add(new Feature("farewell", random.nextInt(100) + 950));
			}
			instances.add(instance);
		}
		return instances;
	}
	
//	private static List<Instance<String>> generateStringInstances2(int n) {
//		Random random = new Random(42);
//		List<Instance<String>> instances = new ArrayList<Instance<String>>();
//		for (int i = 0; i < n; i++) {
//			Instance<String> instance = new Instance<String>();
//			int c = random.nextInt(3);
//			if ( c == 0 ) {
//				instance.setOutcome("A");
//				instance.add(new Feature("aardvark", 1));
//				instance.add(new Feature("apple", 1));
//				instance.add(new Feature("algorithm", 1));
//			}
//			else if( c == 1 ) {
//				instance.setOutcome("B");
//				instance.add(new Feature("bat", 1));
//				instance.add(new Feature("banana", 1));
//				instance.add(new Feature("bayes", 1));
//			} else {
//				instance.setOutcome("C");
//				instance.add(new Feature("cat", 1));
//				instance.add(new Feature("coconut", 1));
//				instance.add(new Feature("calculus", 1));
//			}
//			instances.add(instance);
//		}
//		return instances;
//	}
	
	private static class BinaryAnnotator extends CleartkAnnotator<Boolean> {
		@Override
		public void process(JCas aJCas) throws AnalysisEngineProcessException {			
			for( Instance<Boolean> instance : generateBooleanInstances(1000) ) {
				try {
					this.dataWriter.write(instance);
				} catch (CleartkException e) {
					throw new AnalysisEngineProcessException(e);
				}
			}
		}
	}

	private static class StringAnnotator extends CleartkAnnotator<String> {
		@Override
		public void process(JCas aJCas) throws AnalysisEngineProcessException {			
			for( Instance<String> instance : generateStringInstances(1000) ) {
				try {
					this.dataWriter.write(instance);
				} catch (CleartkException e) {
					throw new AnalysisEngineProcessException(e);
				}
			}
		}
	}

}
