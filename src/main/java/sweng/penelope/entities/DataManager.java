package sweng.penelope.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>DataManager</code> JPA Entity
 */
@Entity
@Getter
@Setter
public class DataManager {
    @Id
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean sysadmin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_campus_rights", joinColumns = @JoinColumn(name = "username"), inverseJoinColumns = @JoinColumn(name = "campus_id"))
    private Set<Campus> campuses = new HashSet<>();
}
