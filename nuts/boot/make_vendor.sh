create vendor document
curl -X POST localhost:1323/internal/vdr/v1/did -H "Content-Type: application/json" -d '{
    "selfControl": true,
    "keyAgreement": true,
    "assertionMethod": true,
    "capabilityInvocation": true
}'
--> id = vendor_id = did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE

set contact info, use vendor_id
curl -X PUT localhost:1323/internal/didman/v1/did/did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE/contactinfo -H "Content-Type: application/json" -d '{
    "name": "Michael Test",
    "phone": "06-12345678",
    "email": "michael@example.com",
    "website": "https://example.com"
}'

create endpoint, use vendor_id
curl -X POST localhost:1323/internal/didman/v1/did/did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE/endpoint -H "Content-Type: application/json" -d '{
    "type": "didcomm-messaging-kikv",
    "endpoint": "http://localhost:8080/test"
}'
curl -X POST localhost:1323/internal/didman/v1/did/did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE/endpoint -H "Content-Type: application/json" -d '{
    "type": "production-oauth",
    "endpoint": "http://localhost:8080/oauth"
}'

create organisation document, use vendor_id
curl -X POST localhost:1323/internal/vdr/v1/did -H "Content-Type: application/json" -d '{
    "selfControl": false,
    "controllers": ["did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE"],
    "assertionMethod": true,
    "capabilityInvocation": false
}'
--> id = org_id = did:nuts:HmTTxp5FxJLFtnfjxs1U9FUChrP5j7GpToivqdFt7Mnm

create organisation credential, use vendor_id + org_id
curl -X POST localhost:1323/internal/vcr/v2/issuer/vc -H "Content-Type: application/json" -d '{
    "type": "NutsOrganizationCredential",
    "issuer": "did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE",
    "credentialSubject": {
        "id": "did:nuts:HmTTxp5FxJLFtnfjxs1U9FUChrP5j7GpToivqdFt7Mnm",
        "organization": {
            "name": "Michael Test Org",
            "city": "Eindhoven"
        }
    },
    "visibility": "public"
}'

get trusted/untrusted vendors
curl -X GET localhost:1323/internal/vcr/v2/verifier/NutsOrganizationCredential/untrusted
curl -X GET localhost:1323/internal/vcr/v2/verifier/NutsOrganizationCredential/trusted

trust vencor, use vendor_id
curl -X POST localhost:1323/internal/vcr/v2/verifier/trust -H "Content-Type: application/json" -d '{
    "issuer": "did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE",
    "credentialType": "NutsOrganizationCredential"
}'

Add service, use org_id, vendor_id, vendor_id
curl -X POST localhost:1323/internal/didman/v1/did/did:nuts:HmTTxp5FxJLFtnfjxs1U9FUChrP5j7GpToivqdFt7Mnm/compoundservice -H "Content-Type: application/json" -d '{
    "type": "didcomm-service-kikv",
    "serviceEndpoint": {
	    "oauth": "did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE/serviceEndpoint?type=production-oauth",
	    "didcomm": "did:nuts:EM48vGgb4FQ4BkGeRWPK4u2m5hvLZKysxss57VuykjkE/serviceEndpoint?type=didcomm-messaging-kikv"
    }
}'



