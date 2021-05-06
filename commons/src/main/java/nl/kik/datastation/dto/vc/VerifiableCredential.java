package nl.kik.datastation.dto.vc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import nl.kik.datastation.dto.Token;
import nl.kik.datastation.dto.ds.async.Message;

@SuperBuilder(toBuilder = true)
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VerifiableCredential extends VerifiableBase {
}
