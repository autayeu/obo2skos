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

import static org.obolibrary.obo2owl.Obo2OWLConstants.OIOVOCAB_IRI_PREFIX;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.obolibrary.obo2owl.Obo2OWLConstants.Obo2OWLVocabulary;
import org.obolibrary.oboformat.parser.OBOFormatConstants.OboFormatTag;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Reads in a OBO ontology using the OWL-API and creates a new representation using the SKOS model.
 *
 * @author Simon Jupp
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Obo2Skos {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final Set<String> HELP_KEYS =
      Collections.unmodifiableSet(
          new HashSet<>(Arrays.asList("--help", "-help", "/help", "--?", "-?", "?", "/?")));
  private static final String frag = "#";
  private static final Pattern UNDERSCORE = Pattern.compile("_");

  private final OWLOntologyManager manager;
  private final OWLOntology inputOntology;
  private final OWLDataFactory factory;
  private final OWLClass skosConceptClass;
  private final OWLClass skosConceptSchemeClass;
  private final Set<OWLAxiom> axioms;
  private final OBOAnnotationToSKOSMapper mapper;

  private final OWLAnnotationProperty obsoleteProperty;
  private final OWLAnnotationProperty defaultNamespace;
  private final OWLObjectProperty inSchemeProperty;
  private final OWLObjectProperty broaderProperty;
  private final OWLObjectProperty narrowerProperty;

  private OWLOntology skosOntology;
  private OWLIndividual skosConceptScheme;
  private Map<String, OWLIndividual> skosConceptSchemes;
  private String baseURI = "http://example.com/obo2skos";
  private Set<String> includeNamespaces = Collections.emptySet();
  private Set<String> excludeNamespaces = Collections.emptySet();
  private boolean includeObsolete = false;
  private boolean includeUnmappedProperties = false;


  public Obo2Skos(final OWLOntologyManager manager, final OWLOntology inputOntology) {
    this.manager = manager;
    this.inputOntology = inputOntology;
    this.factory = manager.getOWLDataFactory();
    this.obsoleteProperty = factory.getOWLAnnotationProperty(OWLRDFVocabulary.OWL_DEPRECATED);
    this.defaultNamespace = factory.getOWLAnnotationProperty(
        OIOVOCAB_IRI_PREFIX + OboFormatTag.TAG_DEFAULT_NAMESPACE.getTag());
    this.inSchemeProperty = factory.getOWLObjectProperty(SKOSVocabulary.INSCHEME);
    this.broaderProperty = factory.getOWLObjectProperty(SKOSVocabulary.BROADER);
    this.narrowerProperty = factory.getOWLObjectProperty(SKOSVocabulary.NARROWER);
    this.skosConceptClass = factory.getOWLClass(SKOSVocabulary.CONCEPT);
    this.skosConceptSchemeClass = factory.getOWLClass(SKOSVocabulary.CONCEPTSCHEME);

    // store all the axioms here, we add them to the new ontology at the end
    this.axioms = new HashSet<>();
    this.mapper = new OBOAnnotationToSKOSMapper(manager);
    this.skosConceptSchemes = new HashMap<>();
  }


  public OWLOntology convert() throws OWLOntologyCreationException {
    // create the new skos ontology
    skosOntology = manager.createOntology(IRI.create(baseURI));
    createConceptScheme();
    parseOntology();
    return skosOntology;
  }

  private void parseOntology() {
    // copy the original ontology annotation axioms
    if (skosOntology.getOntologyID().getOntologyIRI().isPresent()) {
      final IRI subject = skosOntology.getOntologyID().getOntologyIRI().get();
      inputOntology.annotations()
          .map(a -> factory.getOWLAnnotationAssertionAxiom(subject, a))
          .forEach(axioms::add);
    }

    // add the structure of the vocabulary using the object properties
    inputOntology.classesInSignature().forEach(cls -> {
      if (!isObsolete(cls) || includeObsolete) {
        final String fragment = cls.getIRI().getFragment();
        final String namespace = EntitySearcher.getAnnotations(cls, inputOntology)
            .filter(a -> Obo2OWLVocabulary.IRI_OIO_hasOboNamespace.sameIRI(a.getProperty()))
            .findFirst()
            .map(OWLAnnotation::getValue)
            .flatMap(OWLAnnotationValue::asLiteral)
            .map(OWLLiteral::getLiteral)
            .orElse(null);

        if ((includeNamespaces.isEmpty() || includeNamespaces.contains(namespace)) &&
            (excludeNamespaces.isEmpty() || !excludeNamespaces.contains(namespace))
            ) {
          final OWLNamedIndividual concept =
              factory.getOWLNamedIndividual(baseURI + frag + fragment);
          axioms.add(getSKOSConceptAxiom(concept));

          // get the sub and super classes, convert to SKOS Concepts, include broader/narrower relationships
          addSKOSRelationships(concept, EntitySearcher.getSuperClasses(cls, inputOntology));

          // add the annotation properties as skos data type properties
          addSKOSDataProperties(concept, EntitySearcher.getAnnotations(cls, inputOntology));
        }
      }
    });

    // gather all the axioms and commit them to the new ontology
    for (final OWLAxiom ax : axioms) {
      final AddAxiom add = new AddAxiom(skosOntology, ax);
      try {
        manager.applyChange(add);
      } catch (OWLOntologyChangeException e) {
        log.error("Error adding axiom {}", ax, e);
      }
    }
  }

  private boolean isObsolete(final OWLClass cls) {
    return EntitySearcher
        .getAnnotations(cls, inputOntology, obsoleteProperty).findFirst().isPresent();
  }

  private void addSKOSDataProperties(final OWLNamedIndividual concept,
      final Stream<OWLAnnotation> annos) {
    // Need a method in the OWL API to get Annotation by URI...
    final Map<IRI, OWLDataProperty> annoMap = mapper.getAnnotationMap();
    final AtomicBoolean inSchemeAdded = new AtomicBoolean(false);
    annos.forEach(anno -> {
      final OWLDataProperty prop = annoMap.get(anno.getProperty().getIRI());
      if (prop != null && anno.getValue().asLiteral().isPresent()) {
        final String literal = anno.getValue().asLiteral().get().getLiteral();
        final OWLAxiom ax = factory
            .getOWLDataPropertyAssertionAxiom(prop, concept, factory.getOWLLiteral(literal, "en"));
        axioms.add(ax);
      } else {
        if (Obo2OWLVocabulary.IRI_OIO_hasOboNamespace.sameIRI(anno.getProperty())) {
          final OWLIndividual scheme = anno.getValue().asLiteral().map(OWLLiteral::getLiteral)
              .map(this::getSkosScheme).orElse(skosConceptScheme);
          axioms.add(factory.
              getOWLObjectPropertyAssertionAxiom(inSchemeProperty, concept, scheme));
          inSchemeAdded.set(true);
        } else if (includeUnmappedProperties) {
          // for all other annotation axioms, just add them as they are
          axioms.add(factory.getOWLAnnotationAssertionAxiom(concept.getIRI(), anno));
        }
      }
    });

    // ensure inScheme is set. we need it, so set to default if missing
    if (!inSchemeAdded.get()) {
      axioms.add(factory.
          getOWLObjectPropertyAssertionAxiom(inSchemeProperty, concept, skosConceptScheme));
    }

    // This is an after thought, whole script needs re-writing if i were to do this properly anyway
    // want to use SKOS note to keep the original OBO identifier.
    final String frag = concept.getIRI().getFragment();
    final String oboId = UNDERSCORE.matcher(frag).replaceFirst(":");
    axioms.add(factory.getOWLDataPropertyAssertionAxiom(
        factory.getOWLDataProperty(SKOSVocabulary.NOTE), concept, oboId));
  }

  private OWLIndividual getSkosScheme(final String schemeName) {
    return skosConceptSchemes.compute(schemeName, (name, scheme) -> {
      if (scheme == null) {
        final OWLIndividual skosConceptScheme =
            factory.getOWLNamedIndividual(baseURI + frag + name);

        axioms.add(factory.getOWLClassAssertionAxiom(skosConceptSchemeClass, skosConceptScheme));

        scheme = skosConceptScheme;
      }

      return scheme;
    });
  }

  private void createConceptScheme() {
    // get the default namespace from the OBO ontology, set this as the new skos:conceptScheme
    final String schemeName = inputOntology.annotations(defaultNamespace)
        .findFirst().flatMap(a -> a.getValue().asLiteral())
        .map(OWLLiteral::getLiteral).orElse("scheme");

    skosConceptScheme = getSkosScheme(schemeName);
  }

  private void addSKOSRelationships(final OWLNamedIndividual concept,
      final Stream<OWLClassExpression> superClasses) {
    superClasses.forEach(superClass -> {
      if (superClass.equals(factory.getOWLThing())) {
        // make the concept top concept
        final OWLObjectProperty hasTopConcept =
            factory.getOWLObjectProperty(SKOSVocabulary.HASTOPCONCEPT);
        axioms.add(factory
            .getOWLObjectPropertyAssertionAxiom(hasTopConcept, skosConceptScheme, concept));
      }

      if (superClass instanceof OWLClass) {
        // this class is broader than the current class
        final OWLNamedIndividual broaderConcept = factory.getOWLNamedIndividual(
            baseURI + frag + superClass.asOWLClass().getIRI().getFragment());
        axioms.add(getSKOSConceptAxiom(broaderConcept));

        // add the property assertion (and the inverse)
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(broaderProperty, concept, broaderConcept));
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(narrowerProperty, broaderConcept, concept));
      } else if (superClass instanceof OWLObjectSomeValuesFrom) {
        /*
         * create the skos:related relationships
         * It is here that you would need to do any manual mapping for certain ObjectProperties to Broader or Narrower
         * (e.g. part-of could possibly map to skos:broader instead of skos:related) this is up to the person doing the conversion
         * for now all other obo properties just map to related
         */

        // get the filler of the existential restriction
        // it should be a class for most OBO ontologies....
        OWLObjectSomeValuesFrom rest = (OWLObjectSomeValuesFrom) superClass;
        if (!rest.getFiller().isAnonymous()) {
          OWLClass relatedClass = (OWLClass) rest.getFiller();

          // create the new SKOS concept
          OWLNamedIndividual skosConceptInd = factory.getOWLNamedIndividual(
              IRI.create(baseURI + frag + relatedClass.getIRI().getFragment()));
          axioms.add(getSKOSConceptAxiom(skosConceptInd));

          // get the property name and make it a sub property of skos:related
          OWLObjectPropertyExpression prop = rest.getProperty();
          OWLObjectProperty related = factory.getOWLObjectProperty(
              IRI.create(baseURI + frag + prop.asOWLObjectProperty().getIRI().getFragment()));

          OWLAxiom ax = factory.getOWLSubObjectPropertyOfAxiom(related,
              factory.getOWLObjectProperty(SKOSVocabulary.RELATED));
          axioms.add(ax);

          // finally create the object property assertion.
          axioms.add(factory.getOWLObjectPropertyAssertionAxiom(related, concept, skosConceptInd));
          // add the inverse as well
          axioms.add(factory.getOWLObjectPropertyAssertionAxiom(related, skosConceptInd, concept));
        }
      }
    });
  }

  private OWLAxiom getSKOSConceptAxiom(final OWLIndividual ind) {
    return factory.getOWLClassAssertionAxiom(skosConceptClass, ind);
  }

  public void setIncludeObsolete(boolean val) {
    includeObsolete = val;
  }

  public void setBaseURI(String uri) {
    baseURI = uri;
  }

  public void setIncludeNamespaces(final Collection<String> includeNamespaces) {
    this.includeNamespaces = includeNamespaces == null ? Collections.emptySet() :
        Collections.unmodifiableSet(new HashSet<>(includeNamespaces));
  }

  public void setExcludeNamespaces(final Collection<String> excludeNamespaces) {
    this.excludeNamespaces = excludeNamespaces == null ? Collections.emptySet() :
        Collections.unmodifiableSet(new HashSet<>(excludeNamespaces));
  }

  public void setIncludeUnmappedProperties(boolean includeUnmappedProperties) {
    this.includeUnmappedProperties = includeUnmappedProperties;
  }

  private static <T> T parseCLIOptions(final String[] args,
      final Class<T> optionsClass,
      final Class<?> callerClass) {
    try {
      final T options = optionsClass.newInstance();
      final CmdLineParser parser = new CmdLineParser(options);
      try {
        if (args.length == 0 || HELP_KEYS.contains(args[0])) {
          System.out.println("Usage: ");
          parser.printUsage(System.out);
          System.exit(0);
        }

        parser.parseArgument(args);
      } catch (CmdLineException e) {
        System.err.println(e.getMessage());
        System.err.print("Usage: " + callerClass.getSimpleName());
        System.err.println(parser.printExample(OptionHandlerFilter.ALL));
        System.exit(1);
      }
      return options;
    } catch (IllegalAccessException | InstantiationException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void main(String[] args) throws OWLOntologyStorageException,
      OWLOntologyCreationException {
    final Obo2SkosOptions options = parseCLIOptions(args, Obo2SkosOptions.class, Obo2Skos.class);

    final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    log.info(options.toString());

    log.info("Loading {}...", options.getInput());
    final OWLOntology inputOntology = manager.loadOntology(IRI.create(options.getInput().toFile()));
    final Obo2Skos obo2skos = new Obo2Skos(manager, inputOntology);
    obo2skos.setBaseURI(options.getBaseUri());
    obo2skos.setIncludeObsolete(options.isIncludeObsolete());
    obo2skos.setIncludeNamespaces(options.getIncludeNamespaces());
    obo2skos.setExcludeNamespaces(options.getExcludeNamespaces());
    obo2skos.setIncludeUnmappedProperties(options.isIncludeUnmappedProperties());

    log.info("Converting...");
    final OWLOntology skos = obo2skos.convert();
    log.info("Saving...");
    manager.saveOntology(skos, IRI.create(options.getOutput().toFile()));
    log.info("Done!");
  }
}
