package uk.ac.man.cs.owl.sealife;

import java.nio.file.Path;
import org.kohsuke.args4j.Option;

/**
 * Command line options.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Obo2SkosOptions {
  @Option(name = "-in", required = true, usage = "Path to input OBO file.")
  private Path input;
  @Option(name = "-out", required = true, usage = "Path to output SKOS file.")
  private Path output;
  @Option(name = "-includeObsolete", usage = "If true, obsolete stanzas will also be converted.")
  private boolean includeObsolete = false;
  @Option(name = "-baseURI", usage = "Base URI for converted SKOS file.")
  private String baseUri = "http://example.com/obo2skos";

  public Path getInput() {
    return input;
  }

  public void setInput(Path input) {
    this.input = input;
  }

  public Path getOutput() {
    return output;
  }

  public void setOutput(Path output) {
    this.output = output;
  }

  public boolean isIncludeObsolete() {
    return includeObsolete;
  }

  public void setIncludeObsolete(boolean includeObsolete) {
    this.includeObsolete = includeObsolete;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }

  @Override
  public String toString() {
    return "Obo2SkosOptions{" +
        "input=" + input +
        ", output=" + output +
        ", includeObsolete=" + includeObsolete +
        ", baseUri='" + baseUri + '\'' +
        '}';
  }
}
