package proj.karthik.feed.reader.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.sql.Timestamp;

import static proj.karthik.feed.reader.Constants.CREATED_ON;
import static proj.karthik.feed.reader.Constants.FEED;
import static proj.karthik.feed.reader.Constants.NAME;

/**
 * Entity class representing feed.
 */
@JsonRootName(FEED)
public class Feed {

    private String name;
    private Timestamp createdOn;

    public Feed() {
    }

    public Feed(final String name, final Timestamp createdOn) {
        this.name = name;
        this.createdOn = createdOn;
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
        return createdOn;
    }

    @JsonSetter(CREATED_ON)
    public void setCreatedOn(final Timestamp createdOn) {
        this.createdOn = createdOn;
    }
}
