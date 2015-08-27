package uk.ac.man.cs.owl.sealife;

import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.vocab.OWLRDFVocabulary;
import org.semanticweb.owl.vocab.SKOSVocabulary;
import org.coode.obo.parser.OBOVocabulary;

import java.net.URI;
import java.util.Map;
import java.util.HashMap;
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

/**
 * Author: Simon Jupp<br>
 * Date: Jan 28, 2008<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class OBOAnnotationToSKOSMapper {

    Map<URI, OWLDataProperty> annoMap;
    OWLOntologyManager man;
    OWLOntology ontology;
    OWLDataFactory factory;


    public OBOAnnotationToSKOSMapper(OWLOntologyManager man, OWLOntology ontology) {

        this.man = man;
        this.ontology = ontology;
        factory = man.getOWLDataFactory();

        annoMap = new HashMap<URI, OWLDataProperty>();
        annoMap.put(OWLRDFVocabulary.RDFS_LABEL.getURI(), factory.getOWLDataProperty(SKOSVocabulary.PREFLABEL.getURI()));
        annoMap.put(OBOVocabulary.RELATED_SYNONYM.getURI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getURI()));
        annoMap.put(OBOVocabulary.EXACT_SYNONYM.getURI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getURI()));
        annoMap.put(OBOVocabulary.SYNONYM.getURI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getURI()));
        annoMap.put(OBOVocabulary.NARROW_SYNONYM.getURI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getURI()));
        annoMap.put(OBOVocabulary.BROAD_SYNONYM.getURI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getURI()));
        annoMap.put(OBOVocabulary.ALT_ID.getURI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getURI()));
        annoMap.put(OBOVocabulary.DEF.getURI(), factory.getOWLDataProperty(SKOSVocabulary.DEFINITION.getURI()));
        annoMap.put(OBOVocabulary.COMMENT.getURI(), factory.getOWLDataProperty(SKOSVocabulary.COMMENT.getURI()));

    }

    public Map<URI, OWLDataProperty> getAnnotationMap() {
        return annoMap;
    }


}
