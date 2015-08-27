# obo2skos
Script to convert OBO ontologies to SKOS

To run the converter:
---------------------

	./run.sh --input=<Local file URI or URL> --output=<SKOS file> --baseURI=<base URI for converted SKOS file> --ignoreObsolete=<true|false>

EXAMPLE:
---------

	./run.sh --input='file:./input/cell.obo' --output=file:./output/cell.skos.rdf --baseURI=http://example.com/obo2skos --ignoreObsolete=false

ARGS:
-----

	--help
	--input The location of the OBO file you want to convert.
        	This can either be a URL to a file on the web
        	(e.g. http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/cell_type/cell.obo)
        	or it can be a local file (e.g. file:/$CWD/input/cell.obo).

	--output The location of the new SKOS file. FUll file URI required
        	  (e.g. file:/$CWD/output/cell.rdf).

	-- baseURI The base URI for the new SKOS vocabulary.
           The default is http://geneontology.org/formats/oboInSKOS


	--ignoreObsolete set this to ture or false depening on if you want to include obsolete terms

