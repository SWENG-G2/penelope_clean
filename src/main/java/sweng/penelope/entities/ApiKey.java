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

@Entity
@Getter
@Setter
public class ApiKey {
    @Id
    private String identity;

    private String ownerName;

    @Column(columnDefinition = "boolean default false")
    private Boolean admin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "apikeys_campus_rights", joinColumns = @JoinColumn(name = "public_key"), inverseJoinColumns = @JoinColumn(name = "campus_id"))
    private Set<Campus> campuses = new HashSet<>();
}
