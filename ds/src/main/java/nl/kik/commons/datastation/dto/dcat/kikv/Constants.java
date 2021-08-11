package nl.kik.commons.datastation.dto.dcat.kikv;

import java.net.URI;
import java.net.URISyntaxException;

public class Constants {
	public static final URI STANDARD_RDF = Constants.url("https://www.w3.org/TR/rdf-schema");
	public static final URI STANDARD_VERIFIED_SPARQL = Constants
			.url("https://verwijzing-naar-de-gepubliceerde-specificatie/");
	public static final URI STANDARD_SPARQL = Constants.url("https://www.w3.org/TR/sparql11-protocol/");
	public static final URI STANDARD_GRAPHSTORE = Constants
			.url("https://www.w3.org/TR/2013/REC-sparql11-http-rdf-update-20130321/");
	public static final URI STANDARD_SHACL = Constants.url("https://verwijzing-naar-de-gepubliceerde-specificatie/shacl");

	public static final URI FREQUENCY_CONTINUOUS = Constants.url("http://purl.org/cld/freq/continuous");
	public static final URI FREQUENCY_DAILY = Constants.url("http://purl.org/cld/freq/daily");
	public static final URI FREQUENCY_WEEKLY = Constants.url("http://purl.org/cld/freq/weekly");
	public static final URI FREQUENCY_MONTHLY = Constants.url("http://purl.org/cld/freq/monthly");
	public static final URI FREQUENCY_QUARTERLY = Constants.url("http://purl.org/cld/freq/quarterly");
	public static final URI FREQUENCY_ANNUAL = Constants.url("http://purl.org/cld/freq/annual");

	private static URI url(final String string) {
		try {
			return new URI(string);
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
