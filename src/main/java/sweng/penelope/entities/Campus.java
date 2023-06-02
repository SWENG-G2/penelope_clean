package sweng.penelope.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;

import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>Campus</code> JPA Entity
 */
@Entity
@Getter
@Setter
public class Campus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Metadata
    private String author;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @UpdateTimestamp
    private Date date;

    @OneToMany(mappedBy = "campus", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<Bird> birds = new HashSet<>();

    @ManyToMany(mappedBy = "campuses")
    private Set<DataManager> dataManagers = new HashSet<>();

    @PreRemove
    private void removeAssociatedUsers() {
        for (DataManager dataManager : dataManagers) {
            Set<Campus> campuses = dataManager.getCampuses();
            campuses.remove(this);
            dataManager.setCampuses(campuses);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Campus))
            return false;

        Campus c = (Campus) obj;

        if (c.getId().equals(id) && c.getName().equals(name) && c.getAuthor().equals(author)
                && c.getDate().equals(date))
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        if (id != null)
            return id.hashCode();

        return super.hashCode();
    }
}
