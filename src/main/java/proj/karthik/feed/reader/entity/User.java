package proj.karthik.feed.reader.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.sql.Timestamp;
import java.util.Optional;

import static proj.karthik.feed.reader.Constants.CREATED_ON;
import static proj.karthik.feed.reader.Constants.ID;
import static proj.karthik.feed.reader.Constants.NAME;
import static proj.karthik.feed.reader.Constants.USER;

/**
 * Entity class representing User.
 */
@JsonRootName(USER)
public class User {

    private Optional<Long> id;
    private String name;
    private Optional<Timestamp> createdOn;

    public User() {
    }

    public User(final String name) {
        this.name = name;
    }

    public User(final long id, final String name) {
        this.id = Optional.of(id);
        this.name = name;
    }

    public User(final long id, final String name, final Timestamp createdOn) {
        this.id = Optional.of(id);
        this.name = name;
        this.createdOn = Optional.of(createdOn);
    }

    @JsonGetter(ID)
    public Long getId() {
        if (id != null) {
            return id.get();
        } else {
            return null;
        }
    }

    @JsonSetter(ID)
    public void setId(final Long id) {
        this.id = Optional.of(id);
    }

    @JsonGetter(NAME)
    public String getName() {
        return name;
    }

    @JsonSetter(NAME)
    public void setName(final String name) {
        this.name = name;
    }

    @JsonGetter(CREATED_ON)
    public Timestamp getCreatedOn() {
        if (createdOn != null) {
            return createdOn.get();
        } else {
            return null;
        }
    }

    @JsonSetter(CREATED_ON)
    public void setCreatedOn(final Timestamp createdOn) {
        this.createdOn = Optional.of(createdOn);
    }
}
