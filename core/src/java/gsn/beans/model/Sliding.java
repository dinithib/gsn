package gsn.beans.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class Sliding implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private SlidingModel model;

    @OneToMany
    private List<Parameter> parameters;

    @OneToOne(optional = false)
    private DataNode dataNode;

    public Long getId() {
        return id;
    }

    public SlidingModel getModel() {
        return model;
    }

    public void setModel(SlidingModel model) {
        this.model = model;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public DataNode getNode() {
        return dataNode;
    }

    public void setNode(DataNode dataNode) {
        this.dataNode = dataNode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof Sliding)) return false;

        Sliding that = (Sliding) other;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getModel() != null ? !getModel().equals(that.getModel()) : that.getModel() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getModel() != null ? getModel().hashCode() : 0);
        return result;
    }
}