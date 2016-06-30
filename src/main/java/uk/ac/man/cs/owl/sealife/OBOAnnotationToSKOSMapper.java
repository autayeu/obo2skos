package uk.ac.man.cs.owl.sealife;



import org.coode.owlapi.obo12.parser.OBOVocabulary;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;

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

    Map<IRI, OWLDataProperty> annoMap;
    OWLOntologyManager man;
    OWLOntology ontology;
    OWLDataFactory factory;


    public OBOAnnotationToSKOSMapper(OWLOntologyManager man, OWLOntology ontology) {

        this.man = man;
        this.ontology = ontology;
        factory = man.getOWLDataFactory();

        annoMap = new HashMap<IRI, OWLDataProperty>();
        annoMap.put(OWLRDFVocabulary.RDFS_LABEL.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.PREFLABEL.getIRI()));
        annoMap.put(OBOVocabulary.RELATED_SYNONYM.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getIRI()));
        annoMap.put(OBOVocabulary.EXACT_SYNONYM.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getIRI()));
        annoMap.put(OBOVocabulary.SYNONYM.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getIRI()));
        annoMap.put(OBOVocabulary.NARROW_SYNONYM.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getIRI()));
        annoMap.put(OBOVocabulary.BROAD_SYNONYM.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getIRI()));
        annoMap.put(OBOVocabulary.ALT_ID.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.ALTLABEL.getIRI()));
        annoMap.put(OBOVocabulary.DEF.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.DEFINITION.getIRI()));
        annoMap.put(OBOVocabulary.COMMENT.getIRI(), factory.getOWLDataProperty(SKOSVocabulary.COMMENT.getIRI()));

    }

    public Map<IRI, OWLDataProperty> getAnnotationMap() {
        return annoMap;
    }


}
