package nl.kik.commons.datastation.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.text.ParseException;
import java.util.Collection;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.nimbusds.jose.JOSEException;

import foundation.identity.jsonld.JsonLDUtils;
import nl.kik.commons.datastation.dto.kikv.ValidatedQueryCredential;
import nl.kik.commons.datastation.dto.nuts.credential.CreateVerifiableCredential;
import nl.kik.commons.datastation.dto.nuts.credential.Visibility;
import nl.kik.commons.datastation.dto.vc.VerifiablePresentation;

class VCTest {

	private ValidatedQueryCredential query;

	@BeforeEach
	void setUp() throws Exception {
		query = ValidatedQueryCredential.builder() //
				.id(URI.create("did:nuts:HmTTxp5FxJLFtnfjxs1U9FUChrP5j7GpToivqdFt7Mnm")) //
				.queryId(URI.create("urn:uuid:b7285a98-507c-49d0-9a96-3b76d197c99b")) //
				.issuer(URI.create("did:example:76e12ec712ebc6f1c221ebfeb1f")) //
				.issuanceDate(JsonLDUtils.stringToDate("2019-06-16T18:56:59Z"))//
				.expirationDate(JsonLDUtils.stringToDate("2019-06-17T18:56:59Z")) //
				.name("2.3.1 Ziekteverzuimpercentage")//
				.description(
						"CBS/Vernet: Het ziekteverzuimpercentage is het totaal aantal ziektedagen van de personeelsleden, in procenten van het totaal aantal beschikbare (werk-/kalender) dagen van de werknemers in de verslagperiode. Het ziekteverzuimpercentage is inclusief het verzuim langer dan een jaar en exclusief zwangerschaps- en bevallingsverlof.")//
				.profile("https://kik-v.gitlab.io/uitwisselprofielen/uitwisselprofiel-odb/") //
				.ontology("https://raw.githubusercontent.com/kik-v/onto-kik/master/kik-v.owl") //
				.sparql("...") //
				.build();
	}

	@Test
	void testQuery() throws DecoderException, JOSEException, ParseException {
		System.out.println("Original JSON" + query.toJson(true));
		String json = query.toJson();
		VerifiableCredential vc = VerifiableCredential.fromJson(json);
		System.out.println("VC JSON" + vc.toJson(true));

		ValidatedQueryCredential q = ValidatedQueryCredential.fromJsonLDObject(vc);
		System.out.println("Parsed JSON" + q.toJson(true));
	}

	@Test
	void testCreateQuery() {
		CreateVerifiableCredential create = CreateVerifiableCredential.builder(query) //
				.visibility(Visibility.Private) //
				.publishToNetwork(true) //
				.build();

		System.out.println("Original JSON" + create.toJson(true));
		String json = create.toJson();
		VerifiableCredential vc = VerifiableCredential.fromJson(json);
		System.out.println("VC JSON" + vc.toJson(true));

		create = CreateVerifiableCredential.fromJsonLDObject(vc);
		System.out.println("Parsed JSON" + create.toJson(true));
	}

	@Test
	void testVP() {
		VerifiablePresentation vp = VerifiablePresentation.builder() //
				.verifiableCredential(query) //
				.build();

		System.out.println("Original JSON" + vp.toJson(true));
		String json = vp.toJson();
		vp = VerifiablePresentation.fromJson(json);
		System.out.println("VP JSON" + vp.toJson(true));

		Collection<VerifiableCredential> vc = vp.getVerifiableCredentials();
		assertEquals(1, vc.size());

		ValidatedQueryCredential q = ValidatedQueryCredential.fromJsonLDObject(vc.iterator().next());
		System.out.println("Parsed JSON" + q.toJson(true));

		vp = VerifiablePresentation.builder() //
				.verifiableCredential(query) //
				.verifiableCredential(query) //
				.build();

		System.out.println("Original JSON" + vp.toJson(true));
		json = vp.toJson();
		vp = VerifiablePresentation.fromJson(json);
		System.out.println("VP JSON" + vp.toJson(true));

		vc = vp.getVerifiableCredentials();
		assertEquals(2, vc.size());

		q = ValidatedQueryCredential.fromJsonLDObject(vc.iterator().next());
		System.out.println("Parsed JSON" + q.toJson(true));

	}

}
