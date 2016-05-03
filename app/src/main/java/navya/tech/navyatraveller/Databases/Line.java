package navya.tech.navyatraveller.Databases;

import java.io.Serializable;

/**
 * Created by gregoire.frezet on 25/03/2016.
 */
public class Line implements Serializable {

    private Long id;
    private String name;

    public Line() {}

    // Getter & Setter
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }


}
