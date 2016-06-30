package uk.ac.man.cs.owl.sealife;

import org.coode.owlapi.obo12.parser.OBOVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.net.URI;
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
 *
 * This program reads in a OBO ontology using the OWL-API and creates a new representation using the SKOS model
 *
 */
public class OBO2SKOS {

    public OWLOntologyManager man;
    public OWLOntology inputOntology;
    public OWLOntology skosOntology;
    public OWLDataFactory factory;

    public OWLIndividual skosConceptScheme;

    public static String oboURI = "http://geneontology.org/formats/oboInSKOS";
    public static String OBSOLETEANNOTATIONURI = OBOVocabulary.IS_OBSOLETE.getIRI().toString();

    public static String frag = "#";

    Set<OWLAxiom> axList;

    public boolean ignoreObsolete = true;

    private OWLOntologyFormat format;

    public OBO2SKOS(OWLOntologyManager man, OWLOntology inp) {

        this.man = man;
        this.inputOntology = inp;
        this.factory = man.getOWLDataFactory();

        // store all the axioms here, we add them to the new ontology at the end
        axList = new HashSet<OWLAxiom>();


    }


    public OWLOntology convert () throws OWLOntologyCreationException {
        // create the new skos ontology
        skosOntology = man.createOntology(IRI.create(OBO2SKOS.oboURI));
        createConceptScheme();
        parseOntology();
        return skosOntology;
    }

    private void parseOntology () {

        // copy the original ontology annotation axiom
        for (OWLAnnotation ax : inputOntology.getAnnotations()) {
            axList.add(factory.getOWLAnnotationAssertionAxiom(skosOntology.getOntologyID().getOntologyIRI(), ax));
        }

        // add the structure of the vocabulary using the object properties
        for (OWLClass cls : inputOntology.getClassesInSignature()) {

            // ignore obsolete classes
            if (!(!cls.getAnnotations(inputOntology, factory.getOWLAnnotationProperty(IRI.create(OBO2SKOS.OBSOLETEANNOTATIONURI))).isEmpty() && ignoreObsolete)) {
                // get the sub and super classes, convert to SKOS Concepts, include broader/narrower relationships
                addSKOSRelationships(cls, cls.getSuperClasses(inputOntology));

                // add the annotation properties as skos data type properties
                addSKOSDataProperties(cls, cls.getAnnotations(inputOntology));
            }

        }

        // gather all the axiom and commit them to the new ontology
        for (OWLAxiom ax : axList) {

            AddAxiom add = new AddAxiom (skosOntology, ax);
            try {
                man.applyChange(add);
            } catch (OWLOntologyChangeException e) {
                e.printStackTrace();
            }
        }
    }

    private void addSKOSDataProperties (OWLClass cls, Set<OWLAnnotation> annos) {

        // Need a method in the OWL API to get Annotation by URI...
        OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(oboURI + frag + cls.getIRI().getFragment()));
        axList.add(getSKOSConceptAxiom(ind));


        OBOAnnotationToSKOSMapper map = new OBOAnnotationToSKOSMapper(man, inputOntology);
        Map annoMap = map.getAnnotationMap();
        for (OWLAnnotation anno : annos) {


            if (annoMap.containsKey(anno.getProperty().getIRI())) {
                String str = ( (OWLLiteral) anno.getValue()).getLiteral();

                OWLDataProperty prop = (OWLDataProperty) annoMap.get(anno.getProperty().getIRI());
                OWLAxiom ax  = factory.getOWLDataPropertyAssertionAxiom(prop, ind, factory.getOWLLiteral(str, "en"));
                axList.add(ax);
            }
            else {
                // for all other annotation axioms, just add them as they are
                axList.add(factory.getOWLAnnotationAssertionAxiom(ind.getIRI(), anno));
            }
        }

        // This is an after thought, whole script needs re-writing if i were to do this properly anyway
        // want to use SKOS note to keep the orginal OBO identifier.

        String frag = ind.getIRI().getFragment();
        String OBO_ID = frag.replaceFirst("_", ":");
        axList.add(factory.getOWLDataPropertyAssertionAxiom(factory.getOWLDataProperty(IRI.create("http://www.w3.org/2004/02/skos/core#notation")), ind, OBO_ID));



    }


    private void createConceptScheme() {

        // get the default namespace from the OBO ontology, set this as the new skos:conceptScheme

        for (OWLAnnotation anno : inputOntology.getAnnotations()) {

            if (anno.getProperty().getIRI().equals(IRI.create("http://www.geneontology.org/go#default-namespace"))){
                skosConceptScheme = factory.getOWLNamedIndividual(IRI.create(oboURI + frag + anno.getValue().toString()));

                OWLClass skosConceptSchemeClass = factory.getOWLClass(SKOSVocabulary.CONCEPTSCHEME.getIRI());

                axList.add(factory.getOWLClassAssertionAxiom(skosConceptSchemeClass, skosConceptScheme));
            }
        }

        if (skosConceptScheme == null) {
            skosConceptScheme = factory.getOWLNamedIndividual(IRI.create(oboURI + frag + "scheme_" + System.nanoTime()));
            OWLClass skosConceptSchemeClass = factory.getOWLClass(SKOSVocabulary.CONCEPTSCHEME.getIRI());
            axList.add(factory.getOWLClassAssertionAxiom(skosConceptSchemeClass, skosConceptScheme));
        }

    }

    private void addSKOSRelationships (OWLClass cls, Set<OWLClassExpression> classes) {

        OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(oboURI + frag + cls.getIRI().getFragment()));
        axList.add(getSKOSConceptAxiom(ind));

        for (OWLClassExpression desc : classes) {

            if (desc.equals(factory.getOWLThing())) {

                // make the concept top concept
                OWLObjectProperty hasTopConcept = factory.getOWLObjectProperty(SKOSVocabulary.HASTOPCONCEPT.getIRI());
                System.err.println("top concept " + hasTopConcept.toString());
                axList.add(factory.getOWLObjectPropertyAssertionAxiom(hasTopConcept, skosConceptScheme, ind));


            }

            if (desc instanceof OWLClass) {
                // this class is broader than the current class

                // create the obo is-a (and the inverse super-class-of) sub property of skos:broader/skos:narrower respectively.
                OWLObjectProperty isAprop = factory.getOWLObjectProperty(IRI.create(oboURI + frag + "is-a"));
                OWLAxiom ax = factory.getOWLSubObjectPropertyOfAxiom(isAprop,
                        factory.getOWLObjectProperty(SKOSVocabulary.BROADER.getIRI()));
                axList.add(ax);

                OWLObjectProperty superClassProp = factory.getOWLObjectProperty(IRI.create(oboURI + frag + "super-class-of"));
                OWLAxiom ax1 = factory.getOWLSubObjectPropertyOfAxiom(superClassProp,
                        factory.getOWLObjectProperty(SKOSVocabulary.NARROWER.getIRI()));
                axList.add(ax1);

                OWLNamedIndividual ind1 = factory.getOWLNamedIndividual(IRI.create(oboURI + frag + desc.asOWLClass().getIRI().getFragment()));
                axList.add(getSKOSConceptAxiom(ind1));

                // add the property assertion (and the inverse)
                axList.add(factory.getOWLObjectPropertyAssertionAxiom(isAprop, ind, ind1));
                axList.add(factory.getOWLObjectPropertyAssertionAxiom(superClassProp, ind1, ind));


            }
            else if (desc instanceof OWLObjectSomeValuesFrom) {

                /*
                 * create the skos:related relationships
                 * It is here that you would need to do any manual mapping for certain ObjectProperties to Broader or Narrower
                 * (e.g. part-of could possibly map to skos:broader instead of skos:realted) this is up to the person doing the conversion
                 * for now all other obo properties just map to related
                 *
                 */

                // get the filler of the existential restrion
                // it should be a class for most OBO ontologies....
                OWLObjectSomeValuesFrom rest = (OWLObjectSomeValuesFrom) desc;
                if (!rest.getFiller().isAnonymous())  {
                    OWLClass relatedClass = (OWLClass) rest.getFiller();

                    // create the new SKOS concept
                    OWLNamedIndividual skosConceptInd = factory.getOWLNamedIndividual(IRI.create(oboURI + frag + relatedClass.getIRI().getFragment()));
                    axList.add(getSKOSConceptAxiom(skosConceptInd));

                    // get the property name and make it a sub property of skos:related
                    OWLObjectPropertyExpression prop = rest.getProperty();
                    OWLObjectProperty related = factory.getOWLObjectProperty(IRI.create(oboURI + frag + prop.asOWLObjectProperty().getIRI().getFragment()));

                    OWLAxiom ax = factory.getOWLSubObjectPropertyOfAxiom(related, factory.getOWLObjectProperty(SKOSVocabulary.RELATED.getIRI()));
                    axList.add(ax);

                    // finally create the object property assertion.
                    axList.add(factory.getOWLObjectPropertyAssertionAxiom(related, ind, skosConceptInd));
                    // add the inverse as well
                    axList.add(factory.getOWLObjectPropertyAssertionAxiom(related, skosConceptInd, ind));
                }

            }
        }
    }

    private OWLAxiom getSKOSConceptAxiom (OWLIndividual ind) {
        OWLClass skosConcept = factory.getOWLClass(SKOSVocabulary.CONCEPT.getIRI());
        return factory.getOWLClassAssertionAxiom(skosConcept, ind );
    }

    public void setIgnoreObsolete (boolean val) {
        ignoreObsolete = val;
    }

    public void setBaseURI (String uri) {

        oboURI = uri;


    }

    public static void main(String[] args) {

        // input is a obo file

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        try {
            OWLOntology ont = man.loadOntology(IRI.create("file:/Users/jupp/Dropbox/dev/obo2skos/input/cell.obo"));
//            OWLOntology ont = man.loadOntologyFromPhysicalURI(URI.create("file:/Users/simon/ontologies/owl/BreakTheSKOSConverter.owl"));

            OBO2SKOS obo2skos = new OBO2SKOS(man, ont);
            obo2skos.setIgnoreObsolete(true);
//            obo2skos.setBaseURI("http://juppid.com");
            OWLOntology skosOntology = obo2skos.convert();

            // save the skos ontology to a file in rdf/xml
            man.saveOntology(skosOntology, new RDFXMLOntologyFormat(), IRI.create("file:/Users/jupp/Dropbox/dev/obo2skos/output/cell.skos.rdf"));

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }
}
