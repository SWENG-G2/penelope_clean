package sweng.penelope.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * <code>Bird</code> JPA Entity
 */
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class Bird {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bird information
    @NonNull
    private String name;
    @NonNull
    private String listImageURL;
    @NonNull
    private String heroImageURL;
    @NonNull
    private String soundURL;
    @NonNull
    @Column(columnDefinition = "TEXT")
    private String aboutMe;
    @NonNull
    private String aboutMeVideoURL;
    @NonNull
    @Column(columnDefinition = "TEXT")
    private String location;
    @NonNull
    private String locationImageURL;
    @NonNull
    @Column(columnDefinition = "TEXT")
    private String diet;
    @NonNull
    private String dietImageURL;
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private Campus campus;


    // Metadata
    @NonNull
    private String author;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @UpdateTimestamp
    private Date date;
}
