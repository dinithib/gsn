package gsn.beans.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("wrapper")
public class WrapperModel extends BasicModel {

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof WrapperModel)) return false;

        return super.equals(other);
    }
}
