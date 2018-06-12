/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.man.cs.owl.sealife;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.obolibrary.obo2owl.Obo2OWLConstants.Obo2OWLVocabulary;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;

/**
 * Maps OBO properties (as converted by OboInOwl) to SKOS.
 *
 * @author Simon Jupp
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class OBOAnnotationToSKOSMapper {

  private final Map<IRI, OWLDataProperty> annotationMap;

  public OBOAnnotationToSKOSMapper(final OWLOntologyManager manager) {
    final OWLDataFactory factory = manager.getOWLDataFactory();

    final Map<IRI, OWLDataProperty> temp = new HashMap<>();
    put(temp, OWLRDFVocabulary.RDFS_LABEL, SKOSVocabulary.PREFLABEL, factory);
    put(temp, Obo2OWLVocabulary.IRI_OIO_hasRelatedSynonym, SKOSVocabulary.ALTLABEL, factory);
    put(temp, Obo2OWLVocabulary.IRI_OIO_hasExactSynonym, SKOSVocabulary.ALTLABEL, factory);
    put(temp, Obo2OWLVocabulary.IRI_OIO_hasNarrowSynonym, SKOSVocabulary.ALTLABEL, factory);
    put(temp, Obo2OWLVocabulary.IRI_OIO_hasBroadSynonym, SKOSVocabulary.ALTLABEL, factory);
    put(temp, Obo2OWLVocabulary.IRI_IAO_0000115, SKOSVocabulary.DEFINITION, factory);
    put(temp, OWLRDFVocabulary.RDFS_COMMENT, SKOSVocabulary.NOTE, factory);

    this.annotationMap = Collections.unmodifiableMap(temp);
  }

  private static void put(final Map<IRI, OWLDataProperty> map,
      final HasIRI oProp, final HasIRI skosProp, final OWLDataFactory factory) {
    map.put(oProp.getIRI(), factory.getOWLDataProperty(skosProp.getIRI()));
  }

  public Map<IRI, OWLDataProperty> getAnnotationMap() {
    return annotationMap;
  }
}
