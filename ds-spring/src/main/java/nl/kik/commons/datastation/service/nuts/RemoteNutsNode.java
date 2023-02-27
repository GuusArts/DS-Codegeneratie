package nl.kik.commons.datastation.service.nuts;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import nl.kik.commons.datastation.dto.nuts.oauth.AccessToken;
import nl.kik.commons.datastation.dto.nuts.oauth.CreateAccessToken;

public interface RemoteNutsNode {
    @PostMapping("/n2n/auth/v1/accesstoken")
    AccessToken createAccessToken(@RequestBody CreateAccessToken body);
}
