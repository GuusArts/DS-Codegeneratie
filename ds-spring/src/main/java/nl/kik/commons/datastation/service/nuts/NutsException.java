package nl.kik.commons.datastation.service.nuts;

import lombok.Getter;

@Getter
public class NutsException extends RuntimeException {

    private int statusCode;

    public NutsException(int statusCode, String reasonPhrase) {
        super(reasonPhrase);
        this.statusCode = statusCode;
    }

    private static final long serialVersionUID = -3115095171746097029L;

}
