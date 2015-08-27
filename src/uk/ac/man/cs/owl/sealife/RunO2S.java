package uk.ac.man.cs.owl.sealife;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyStorageException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
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
 * Date: Oct 19, 2009<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class RunO2S {

    public static void main(String[] args) {

        if (args.length == 0) {

            System.err.println("Please supply some arguments, try --help for details.");

        }
        else if (args[0].contains("--help")) {
            System.err.println("Arguments: --input=<Local file URI or URL> --output=<SKOS file> --baseURI=<base URI for converted SKOS file> --ignoreObsolete" );
        }
        else {

            OWLOntologyManager man = OWLManager.createOWLOntologyManager();

            URI inputURI = null;
            URI outURI = null;
            URI baseURI;
            boolean ignoreObs = false;

            Map<String, String> argsMap = new HashMap<String, String>();

            for (int x = 0; x <args.length ; x++) {
                String [] split = args[x].split("=");
                argsMap.put(split[0], split[1]);
            }

            System.out.println("arguments");
            for (String t : argsMap.keySet()) {
                System.out.println(t + " --> " + argsMap.get(t));
            }

            if (!argsMap.containsKey("--input")) {
                System.err.println("Please supply an input file: --input=<file URI>");
                System.exit(0);
            }
            else {
                inputURI = URI.create(argsMap.get("--input"));
            }

            if (!argsMap.containsKey("--output")) {
                System.err.println("Please supply an output file: --output=<file URI>");
                System.exit(0);
            }
            else {
                outURI = URI.create(argsMap.get("--output"));
            }

            try {
                System.err.println("Loading " + inputURI.toString() + "...");
                OWLOntology inputOntology = man.loadOntology(inputURI);
                OBO2SKOS obo2skos = new OBO2SKOS(man, inputOntology);

                if (argsMap.containsKey("--baseURI")) {
                    obo2skos.setBaseURI(argsMap.get("--baseURI"));
                }

                if (argsMap.containsKey("--ignoreObsolete")) {
                    obo2skos.setIgnoreObsolete(true);
                }
                else {
                    obo2skos.setIgnoreObsolete(ignoreObs);
                }

                System.err.println("Converting...");
                OWLOntology skos = obo2skos.convert();
                System.err.println("Saving...");

                man.saveOntology(skos, outURI);

                System.err.println("Done!");

            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }


    }

}
