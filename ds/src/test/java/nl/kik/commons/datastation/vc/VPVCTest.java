package nl.kik.commons.datastation.vc;

import java.net.URI;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.jsonld.VerifiableCredentialContexts;
import com.danubetech.verifiablecredentials.jwt.FromJwtConverter;
import com.danubetech.verifiablecredentials.jwt.JwtVerifiableCredential;
import com.danubetech.verifiablecredentials.jwt.JwtVerifiablePresentation;
import com.danubetech.verifiablecredentials.jwt.ToJwtConverter;
import com.nimbusds.jose.JOSEException;

import foundation.identity.jsonld.JsonLDUtils;

class VPVCTest {
	@Test
	void vc() throws ParseException, DecoderException, JOSEException {
		Map<String, Object> claims = new LinkedHashMap<>();
		Map<String, Object> degree = new LinkedHashMap<String, Object>();
		degree.put("name", "Bachelor of Science and Arts");
		degree.put("type", "BachelorDegree");
		claims.put("college", "Test University");
		claims.put("degree", degree);

		CredentialSubject credentialSubject = CredentialSubject.builder()
				.id(URI.create("did:example:ebfeb1f712ebc6f1c276e12ec21")).claims(claims).build();

		VerifiableCredential verifiableCredential = VerifiableCredential.builder()
				.context(VerifiableCredentialContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_EXAMPLES_V1)
				.type("UniversityDegreeCredential").id(URI.create("http://example.edu/credentials/3732"))
				.issuer(URI.create("did:example:76e12ec712ebc6f1c221ebfeb1f"))
				.issuanceDate(JsonLDUtils.stringToDate("2019-06-16T18:56:59Z"))
				.expirationDate(JsonLDUtils.stringToDate("2019-06-17T18:56:59Z")).credentialSubject(credentialSubject)
				.build();

		byte[] testEd25519PrivateKey = Hex.decodeHex(
				"984b589e121040156838303f107e13150be4a80fc5088ccba0b0bdc9b1d89090de8777a28f8da1a74e7a13090ed974d879bf692d001cddee16e4cc9f84b60580"
						.toCharArray());

		JwtVerifiableCredential jwtVerifiableCredential = ToJwtConverter
				.toJwtVerifiableCredential(verifiableCredential);

		String jwtPayload = jwtVerifiableCredential.getPayload().toString();
		System.out.println("VC Payload" + jwtPayload);

		String jwtString = jwtVerifiableCredential.sign_Ed25519_EdDSA(testEd25519PrivateKey);
		System.out.println("VC" + jwtString);

		byte[] testEd25519PublicKey = Hex
				.decodeHex("de8777a28f8da1a74e7a13090ed974d879bf692d001cddee16e4cc9f84b60580".toCharArray());

		jwtVerifiableCredential = JwtVerifiableCredential.fromCompactSerialization(jwtString);
//		System.out.println(jwtVerifiableCredential.verify_Ed25519_EdDSA(testEd25519PublicKey));

		jwtPayload = jwtVerifiableCredential.getPayload().toString();
		String jwtPayloadVerifiableCredential = jwtVerifiableCredential.getPayloadObject().toJson(true);
		System.out.println("VC Payload" + jwtPayload);
		System.out.println("VC Payload JSON" + jwtPayloadVerifiableCredential);

		verifiableCredential = FromJwtConverter.fromJwtVerifiableCredential(jwtVerifiableCredential);
		System.out.println("VC JSON" + verifiableCredential.toJson(true));
	}

	@Test
	void vp() throws ParseException, JOSEException, DecoderException {
		Map<String, Object> claims = new LinkedHashMap<>();
		Map<String, Object> degree = new LinkedHashMap<String, Object>();
		degree.put("name", "Bachelor of Science and Arts");
		degree.put("type", "BachelorDegree");
		claims.put("college", "Test University");
		claims.put("degree", degree);

		CredentialSubject credentialSubject = CredentialSubject.builder()
				.id(URI.create("did:example:ebfeb1f712ebc6f1c276e12ec21")).claims(claims).build();

		VerifiableCredential verifiableCredential = VerifiableCredential.builder()
				.context(VerifiableCredentialContexts.JSONLD_CONTEXT_W3C_2018_CREDENTIALS_EXAMPLES_V1)
				.type("UniversityDegreeCredential").id(URI.create("http://example.edu/credentials/3732"))
				.issuer(URI.create("did:example:76e12ec712ebc6f1c221ebfeb1f"))
				.issuanceDate(JsonLDUtils.stringToDate("2019-06-16T18:56:59Z"))
				.expirationDate(JsonLDUtils.stringToDate("2019-06-17T18:56:59Z")).credentialSubject(credentialSubject)
				.build();

		byte[] testEd25519PrivateKey = Hex.decodeHex(
				"984b589e121040156838303f107e13150be4a80fc5088ccba0b0bdc9b1d89090de8777a28f8da1a74e7a13090ed974d879bf692d001cddee16e4cc9f84b60580"
						.toCharArray());

		JwtVerifiableCredential jwtVerifiableCredential = ToJwtConverter
				.toJwtVerifiableCredential(verifiableCredential);

		String jwtPayload = jwtVerifiableCredential.getPayload().toString();
		System.out.println("VP Payload" + jwtPayload);

		String jwtString = jwtVerifiableCredential.sign_Ed25519_EdDSA(testEd25519PrivateKey);
		System.out.println("VP" + jwtString);

		JwtVerifiablePresentation jwtVerifiablePresentation = JwtVerifiablePresentation
				.fromCompactSerialization(jwtString);
		jwtPayload = jwtVerifiablePresentation.getPayload().toString();
		System.out.println("VP" + jwtPayload);

		jwtString = jwtVerifiablePresentation.sign_Ed25519_EdDSA(testEd25519PrivateKey);
		System.out.println("VP" + jwtString);

	}
}
