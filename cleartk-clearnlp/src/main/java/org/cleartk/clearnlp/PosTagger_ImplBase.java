/*
 * Copyright (c) 2012, Regents of the University of Colorado 
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
package org.cleartk.clearnlp;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;

import com.google.common.annotations.Beta;
import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPLib;
import com.clearnlp.reader.AbstractReader;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * This class provides a UIMA/ClearTK wrapper for the ClearNLP part of speech (POS) tagger. This
 * engine requires tokenize input and produces POS tags on the tokens.
 * 
 * Subclasses should override the abstract methods to produce the annotations relevant for the
 * target type system.
 * 
 * This tagger is available here:
 * <p>
 * http://clearnlp.googlecode.com
 * <p>
 * 
 * @author Lee Becker
 * 
 */
@Beta
public abstract class PosTagger_ImplBase<TOKEN_TYPE extends Annotation> extends
    JCasAnnotator_ImplBase {

  public static final String DEFAULT_MODEL_FILE_NAME = "ontonotes-en-pos-1.3.0.tgz";
  
  public static final String PARAM_MODEL_PATH = "modelPath";
  
  @ConfigurationParameter(
      name = PARAM_MODEL_PATH,
      mandatory = false,
      description = "This parameter provides the path to the pos tagger model.",
      defaultValue="general-en")
  private String modelPath;

  
  /*
  public static final String PARAM_MODEL_URI = "modelUri";

  @ConfigurationParameter(
      name = PARAM_MODEL_URI,
      mandatory = false,
      description = "This parameter provides the URI to the pos tagger model.")
  private URI modelUri;
  */

  public static final String PARAM_LANGUAGE_CODE = "languageCode";

  @ConfigurationParameter(
      name = PARAM_LANGUAGE_CODE,
      mandatory = false,
      description = "Language code for the pos tagger (default value=en).",
      defaultValue = AbstractReader.LANG_EN)
  private String languageCode;

  public static final String PARAM_WINDOW_CLASS = "windowClass";

  private static final String WINDOW_TYPE_DESCRIPTION = "specifies the class type of annotations that will be tokenized. "
      + "By default, the tokenizer will tokenize a document sentence by sentence.  If you do not want to precede tokenization with"
      + "sentence segmentation, then a reasonable value for this parameter is 'org.apache.uima.jcas.tcas.DocumentAnnotation'";

  @ConfigurationParameter(
      name = PARAM_WINDOW_CLASS,
      mandatory = false,
      description = WINDOW_TYPE_DESCRIPTION,
      defaultValue = "org.cleartk.token.type.Sentence")
  private Class<? extends Annotation> windowClass;

  private TokenOps<TOKEN_TYPE> tokenOps;

  public PosTagger_ImplBase(TokenOps<TOKEN_TYPE> tokenOps) {
    this.tokenOps = tokenOps;
  }

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      // Load POS tagger model
      this.tagger = NLPGetter.getComponent(modelPath, languageCode, NLPLib.MODE_POS);

    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    for (Annotation window : JCasUtil.select(jCas, this.windowClass)) {
      List<TOKEN_TYPE> tokens = this.tokenOps.selectTokens(jCas, window);
      if (tokens.size() <= 0) {
        return;
      }

      List<String> tokenStrings = JCasUtil.toText(tokens);

      // As of version 1.3.0, ClearNLP does all processing to go through its own dependency tree
      // structure
      DEPTree clearNlpDepTree = NLPGetter.toDEPTree(tokenStrings);
      this.tagger.process(clearNlpDepTree);

      // Note the ClearNLP counts index 0 as the sentence dependency node, so the POS tag indices
      // are shifted by one from the token indices
      for (int i = 0; i < tokens.size(); i++) {
        TOKEN_TYPE token = tokens.get(i);
        DEPNode node = clearNlpDepTree.get(i+1);
        this.tokenOps.setPos(jCas, token, node.pos);
        this.tokenOps.setLemma(jCas, token, node.lemma);
      }
    }
  }

  private AbstractComponent tagger;

}
