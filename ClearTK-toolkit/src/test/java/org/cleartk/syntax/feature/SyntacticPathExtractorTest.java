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
package org.cleartk.syntax.feature;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.cleartk.CleartkException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.simple.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.simple.TypePathExtractor;
import org.cleartk.syntax.TreebankTestsUtil;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.JCasFactory;
import org.uutuc.factory.TokenFactory;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */

public class SyntacticPathExtractorTest {

	@Test
	public void test() throws UIMAException, CleartkException {
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		TokenFactory.createTokens(jCas, "I ran home", Token.class, Sentence.class, null, "PRP VBD NN", null, "org.cleartk.type.Token:pos", null);
		TreebankNode iNode = TreebankTestsUtil.newNode(jCas, 0, 1, "PRP");
		TreebankNode inpNode = TreebankTestsUtil.newNode(jCas, "NP", iNode);
		TreebankNode ranNode = TreebankTestsUtil.newNode(jCas, 2, 5, "VBD");
		TreebankNode homeNode = TreebankTestsUtil.newNode(jCas, 6, 10, "NN");
		TreebankNode homenpNode = TreebankTestsUtil.newNode(jCas, "NP", homeNode);
		TreebankNode ranvpNode = TreebankTestsUtil.newNode(jCas, "VP", ranNode, homenpNode);
		TreebankNode topNode = TreebankTestsUtil.newNode(jCas, "S", inpNode, ranvpNode);
		
		SpannedTextExtractor textExtractor = new SpannedTextExtractor();
		TypePathExtractor tagExtractor = new TypePathExtractor(TreebankNode.class, "nodeType");
		SyntacticPathExtractor extractor;
		List<Feature> features;
		
		extractor = new SyntacticPathExtractor(tagExtractor);
		features = extractor.extract(jCas, iNode, ranNode);
		Assert.assertEquals(2, features.size());
		Assert.assertEquals("SyntacticPath(TypePath(NodeType))", features.get(0).getName());
		Assert.assertEquals("PRP::NP::S;;VP;;VBD", features.get(0).getValue());
		Assert.assertEquals("SyntacticPath_Length", features.get(1).getName());
		Assert.assertEquals(5, ((Long)features.get(1).getValue()).intValue());

		extractor = new SyntacticPathExtractor(tagExtractor);
		features = extractor.extract(jCas, topNode, homenpNode);
		Assert.assertEquals(2, features.size());
		Assert.assertEquals("SyntacticPath(TypePath(NodeType))", features.get(0).getName());
		Assert.assertEquals("S;;VP;;NP", features.get(0).getValue());
		Assert.assertEquals("SyntacticPath_Length", features.get(1).getName());
		Assert.assertEquals(3, ((Long)features.get(1).getValue()).intValue());

		extractor = new SyntacticPathExtractor(textExtractor, true);
		features = extractor.extract(jCas, homeNode, ranNode);
		Assert.assertEquals(2, features.size());
		Assert.assertEquals("PartialSyntacticPath(null)", features.get(0).getName());
		Assert.assertEquals("home::home::ran home", features.get(0).getValue());
		Assert.assertEquals("PartialSyntacticPath_Length", features.get(1).getName());
		Assert.assertEquals(3, ((Long)features.get(1).getValue()).intValue());
		
	}
}
