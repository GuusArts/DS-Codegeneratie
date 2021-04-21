package nl.kik.datastation.dto.dcat.kikv;

import java.net.MalformedURLException;
import java.net.URL;

public class Constants {
	public static final URL STANDARD_RDF = url("https://www.w3.org/TR/rdf-schema");
	public static final URL STANDARD_VERIFIED_SPARQL = url("https://verwijzing-naar-de-gepubliceerde-specificatie/");
	public static final URL STANDARD_SPARQL = url("https://www.w3.org/TR/sparql11-protocol/");
	public static final URL STANDARD_GRAPHSTORE = url("https://www.w3.org/TR/2013/REC-sparql11-http-rdf-update-20130321/");
	public static final URL STANDARD_SHACL = url("https://verwijzing-naar-de-gepubliceerde-specificatie/shacl");

	public static final URL FREQUENCY_CONTINUOUS = url("http://purl.org/cld/freq/continuous");
	public static final URL FREQUENCY_DAILY = url("http://purl.org/cld/freq/daily");
	public static final URL FREQUENCY_WEEKLY = url("http://purl.org/cld/freq/weekly");
	public static final URL FREQUENCY_MONTHLY = url("http://purl.org/cld/freq/monthly");
	public static final URL FREQUENCY_QUARTERLY = url("http://purl.org/cld/freq/quarterly");
	public static final URL FREQUENCY_ANNUAL = url("http://purl.org/cld/freq/annual");

	private static URL url(String string) {
		try {
			return new URL(string);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
