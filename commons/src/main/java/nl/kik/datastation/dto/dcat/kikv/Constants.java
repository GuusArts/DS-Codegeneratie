package nl.kik.datastation.dto.dcat.kikv;

import java.net.MalformedURLException;
import java.net.URL;

public class Constants {
	public static final URL STANDARD_RDF = Constants.url("https://www.w3.org/TR/rdf-schema");
	public static final URL STANDARD_VERIFIED_SPARQL = Constants
			.url("https://verwijzing-naar-de-gepubliceerde-specificatie/");
	public static final URL STANDARD_SPARQL = Constants.url("https://www.w3.org/TR/sparql11-protocol/");
	public static final URL STANDARD_GRAPHSTORE = Constants
			.url("https://www.w3.org/TR/2013/REC-sparql11-http-rdf-update-20130321/");
	public static final URL STANDARD_SHACL = Constants
			.url("https://verwijzing-naar-de-gepubliceerde-specificatie/shacl");

	public static final URL FREQUENCY_CONTINUOUS = Constants.url("http://purl.org/cld/freq/continuous");
	public static final URL FREQUENCY_DAILY = Constants.url("http://purl.org/cld/freq/daily");
	public static final URL FREQUENCY_WEEKLY = Constants.url("http://purl.org/cld/freq/weekly");
	public static final URL FREQUENCY_MONTHLY = Constants.url("http://purl.org/cld/freq/monthly");
	public static final URL FREQUENCY_QUARTERLY = Constants.url("http://purl.org/cld/freq/quarterly");
	public static final URL FREQUENCY_ANNUAL = Constants.url("http://purl.org/cld/freq/annual");

	private static URL url(final String string) {
		try {
			return new URL(string);
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
